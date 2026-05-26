# ShooterMind

> A personal training companion for competitive air rifle and air pistol athletes.

ShooterMind is an Android application built for precision shooters to log training sessions, track performance over time, manage competition schedules, and analyse progress — all from their phone.

---

## Screenshots<img width="1080" height="2400" alt="calendar" src="https://github.com/user-attachments/assets/50b7ae13-1475-4ba2-a394-2bec60702992" />
<img width="1080" height="2400" alt="stats" src="https://github.com/user-attachments/assets/f2f6b5dc-7def-46d7-be92-51123911a4ef" />
<img width="1080" height="2400" alt="sessions" src="https://github.com/user-attachments/assets/bded8d0f-65e0-49e2-9853-4f7d72e949cd" />
<img width="1080" height="2400" alt="journal" src="https://github.com/user-attachments/assets/2008d323-b762-4826-aac1-b9675ed2eaa2" />
<img width="1080" height="2400" alt="start training" src="https://github.com/user-attachments/assets/23795c2f-801c-4207-a081-a0b7a3ad4cc5" />

<img width="1080" height="2400" alt="Home page" src="https://github.com/user-attachments/assets/e25890f6-c024-49d8-a6c7-ae8f75dba2bf" />


---

## Features

### Training Sessions
- Log sessions for **10m Air Rifle** (decimal scoring) and **10m Air Pistol** (integer scoring)
- Series breakdown with individual shot entry
- Duration timer or manual input
- Competition / control session flags
- Session photo (camera or gallery)
- GPS location tagging with reverse geocoding
- **Export session as PDF** and share with a coach

### Statistics & Progress
- Overview: total sessions, best score, average score
- Weekly stats: sessions this week, current streak, total training hours, competition count
- **Score progress line chart** (last 20 sessions)
- Per-discipline breakdown (Air Rifle vs Air Pistol)

### Training Journal
- Post-session reflection (notes, batch/ammunition, air pressure)
- Physical & mental state ratings (muscle recovery, fatigue, concentration, endurance, heart rate)

### Calendar & Reminders
- Schedule training sessions, competitions, recovery days
- Set reminders: 30 min / 1 hr / 2 hrs / 1 day before
- Smart notification text: "Competition starts in 30 min" vs "Training starts tomorrow"

### Profile
- ISSF category (Youth / Junior / Senior)
- Training goal tracking with milestone progress
- Achievements system (First Session, Consistent Athlete, Sharpshooter, Elite Shooter, …)
- Profile photo (camera or gallery)

### Account
- Email/password, Google, and Facebook sign-in
- **Forgot password** with automatic reset email
- **Reset password** from Settings
- **Delete account** with full data cleanup (Firestore + Firebase Auth)
- Multi-language: English 🇬🇧 and Macedonian 🇲🇰
- Light / Dark / System theme

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| Local DB | Room (SQLite) |
| Remote DB | Firebase Firestore |
| Auth | Firebase Authentication |
| Push | Firebase Cloud Messaging |
| Navigation | Jetpack Navigation Compose |
| Image loading | Coil |
| Location | Google Play Services – FusedLocationProviderClient |
| PDF export | `android.graphics.pdf.PdfDocument` (built-in) |
| Charts | Compose Canvas (custom line chart) |
| Build | Gradle KTS + KSP |

---

## Project Structure
