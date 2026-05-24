package com.shootermind.app.service

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shootermind.app.core.notification.NotificationHelper

class ShooterMindMessagingService : FirebaseMessagingService() {

    // ── Incoming message ───────────────────────────────────────────────────

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "ShooterMind"
        val body  = message.notification?.body
            ?: message.data["body"]
            ?: ""

        NotificationHelper.showNotification(applicationContext, title, body)
    }

    // ── Token refresh ──────────────────────────────────────────────────────

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("fcmTokens")
            .document("current")
            .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
    }
}
