import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db        = admin.firestore();
const messaging = admin.messaging();

// ── Weekly Training Summary ────────────────────────────────────────────────
// Fires every Monday at 08:00 (Europe/Skopje = UTC+2/UTC+1).
// For each user that trained this week, sends an FCM push with a summary.
// For users who did NOT train, sends a motivational nudge.
//
// Firestore paths used:
//   users/{uid}/sessions/{id}          — TrainingSession (dateMs, totalScore, durationMinutes)
//   users/{uid}/fcmTokens/current      — { token, updatedAt }
// ──────────────────────────────────────────────────────────────────────────

export const weeklySummary = functions.pubsub
    .schedule("every monday 08:00")
    .timeZone("Europe/Skopje")
    .onRun(async (_context) => {
        const weekAgo = Date.now() - 7 * 24 * 60 * 60 * 1000;

        // All registered users
        const usersSnap = await db.collection("users").get();

        const jobs = usersSnap.docs.map(async (userDoc) => {
            const uid = userDoc.id;
            try {
                await sendSummaryToUser(uid, weekAgo);
            } catch (err) {
                // Never let one user's failure block the rest
                functions.logger.error(`weeklySummary failed for uid=${uid}`, err);
            }
        });

        await Promise.all(jobs);
        functions.logger.info(`weeklySummary: processed ${usersSnap.size} users`);
        return null;
    });


async function sendSummaryToUser(uid: string, weekAgo: number): Promise<void> {
    // 1 — Get FCM token; skip user if missing
    const tokenDoc = await db
        .collection("users").doc(uid)
        .collection("fcmTokens").doc("current")
        .get();

    if (!tokenDoc.exists) return;
    const token = (tokenDoc.data() as any)?.token as string | undefined;
    if (!token) return;

    // 2 — Sessions from the past 7 days
    const sessionsSnap = await db
        .collection("users").doc(uid)
        .collection("sessions")
        .where("dateMs", ">=", weekAgo)
        .get();

    const sessions = sessionsSnap.docs.map((d) => d.data());

    // 3 — Build notification text
    let title: string;
    let body: string;

    if (sessions.length === 0) {
        // No training this week — motivational nudge
        title = "Back to the range! 🎯";
        body  = "No training logged this week. Every session counts!";
    } else {
        const scores = sessions
            .map((s) => s["totalScore"] as number)
            .filter((s) => s > 0);

        const bestScore      = scores.length > 0 ? Math.max(...scores) : null;
        const totalMinutes   = sessions.reduce(
            (sum, s) => sum + ((s["durationMinutes"] as number) || 0), 0
        );
        const competitionCount = sessions.filter((s) => s["isCompetition"]).length;

        // Time label
        const hours = Math.floor(totalMinutes / 60);
        const mins  = totalMinutes % 60;
        const timeStr = totalMinutes > 0
            ? (hours > 0 ? `${hours}h ${mins > 0 ? mins + "m" : ""}`.trim() : `${mins}m`)
            : "";

        // Session count label
        const sessionLabel = sessions.length === 1 ? "1 session" : `${sessions.length} sessions`;

        title = `Weekly Summary 🎯`;
        body  = sessionLabel;
        if (bestScore !== null) body += ` · best ${formatScore(bestScore, sessions)}`;
        if (timeStr)            body += ` · ${timeStr} training`;
        if (competitionCount > 0) body += ` · ${competitionCount} competition${competitionCount > 1 ? "s" : ""}`;
    }

    // 4 — Send push
    await messaging.send({
        token,
        notification: { title, body },
        android: {
            priority: "normal",
            notification: {
                channelId: "session_reminders", // matches NotificationHelper channel
                sound    : "default",
            },
        },
    });

    functions.logger.info(`weeklySummary: sent to uid=${uid} — ${body}`);
}

/** Format score as integer (pistol) or 1-decimal (rifle), same as app logic. */
function formatScore(score: number, sessions: any[]): string {
    const allDecimal = sessions.every((s) => s["useDecimalScore"] === true);
    return allDecimal ? score.toFixed(1) : Math.round(score).toString();
}
