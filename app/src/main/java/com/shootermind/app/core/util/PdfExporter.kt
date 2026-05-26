package com.shootermind.app.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.shootermind.app.domain.model.Discipline
import com.shootermind.app.domain.model.TrainingSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates an A4 PDF report for a [TrainingSession] and returns a shareable [Uri].
 * Must be called from a background thread (I/O).
 */
object PdfExporter {

    fun exportSession(context: Context, session: TrainingSession): Uri {
        val pdfDoc = PdfDocument()
        val page   = pdfDoc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        drawPage(page.canvas, session)
        pdfDoc.finishPage(page)

        val dir  = File(context.cacheDir, "pdf_export").also { it.mkdirs() }
        val file = File(dir, "session_${session.id}.pdf")
        file.outputStream().use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // ── Page rendering ────────────────────────────────────────────────────────

    private fun drawPage(cv: Canvas, s: TrainingSession) {
        val purple       = Color.rgb(109, 40, 217)
        val textDark     = Color.rgb(17,  24,  39)
        val textGray     = Color.rgb(107, 114, 128)
        val dividerColor = Color.rgb(229, 231, 235)

        // ── Paints ────────────────────────────────────────────────────────────
        val pHeaderBg  = Paint().apply { color = purple }
        val pWhite12B  = paint(Color.WHITE, 12f, bold = true)
        val pWhite22B  = paint(Color.WHITE, 22f, bold = true)
        val pWhite11   = paint(Color.argb(204, 255, 255, 255), 11f)
        val pPurple48B = paint(purple, 48f, bold = true)
        val pDark12    = paint(textDark, 12f)
        val pGray10    = paint(textGray, 10f)
        val pGray9     = paint(textGray, 9f)
        val pPurple13B = paint(purple, 13f, bold = true)
        val pDark11    = paint(textDark, 11f)
        val pPurple11B = paint(purple, 11f, bold = true)
        val pDivider   = Paint().apply { color = dividerColor; strokeWidth = 1f }

        // ── Header ────────────────────────────────────────────────────────────
        cv.drawRect(0f, 0f, 595f, 130f, pHeaderBg)
        cv.drawText("ShooterMind", 30f, 30f, pWhite12B)
        cv.drawText("Training Session Report", 30f, 58f, pWhite22B)
        val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            .format(Date(s.dateMs))
        cv.drawText(dateStr, 30f, 80f, pWhite11)
        val disciplineLabel = if (s.discipline == Discipline.AIR_RIFLE) "10m Air Rifle"
                              else "10m Air Pistol"
        val tags = buildString {
            append(disciplineLabel)
            if (s.isCompetition)     append("  ·  Competition")
            if (s.isControlSession)  append("  ·  Control Session")
        }
        cv.drawText(tags, 30f, 100f, pWhite11)

        // ── Score block ───────────────────────────────────────────────────────
        var y = 160f
        val scoreStr = if (s.useDecimalScore) "%.1f".format(s.totalScore)
                       else s.totalScore.toInt().toString()
        cv.drawText(scoreStr, 30f, y, pPurple48B)
        cv.drawText("TOTAL SCORE", 30f, y + 14f, pGray10)

        cv.drawText("${s.shotCount}", 220f, y - 6f, pDark12)
        cv.drawText("SHOTS", 220f, y + 8f, pGray10)
        if (s.durationMinutes > 0) {
            cv.drawText("${s.durationMinutes} min", 310f, y - 6f, pDark12)
            cv.drawText("DURATION", 310f, y + 8f, pGray10)
        }
        if (!s.locationName.isNullOrBlank()) {
            cv.drawText(s.locationName, 220f, y + 22f, pGray10)
        }

        // ── Divider ───────────────────────────────────────────────────────────
        y += 50f
        cv.drawLine(30f, y, 565f, y, pDivider)
        y += 18f

        // ── Series breakdown ──────────────────────────────────────────────────
        cv.drawText("Series Breakdown", 30f, y, pPurple13B)
        y += 16f
        val seriesList = parseSeriesData(s.seriesData)
        if (seriesList.isEmpty()) {
            cv.drawText("No series data recorded.", 30f, y, pGray10)
            y += 14f
        } else {
            seriesList.forEachIndexed { idx, (total, shots) ->
                cv.drawText("Series ${idx + 1}", 30f, y, pGray10)
                cv.drawText("%.2f".format(total), 120f, y, pPurple11B)
                if (shots.isNotEmpty()) {
                    val shotsText = shots.joinToString(" · ") {
                        if (s.useDecimalScore) "%.1f".format(it) else it.toInt().toString()
                    }
                    cv.drawText(shotsText, 185f, y, pDark11)
                }
                y += 15f
            }
        }

        // ── Journal ───────────────────────────────────────────────────────────
        val hasJournal = s.notes.isNotBlank() || s.batch.isNotBlank() || s.airPressure.isNotBlank()
        if (hasJournal) {
            y += 8f
            cv.drawLine(30f, y, 565f, y, pDivider)
            y += 18f
            cv.drawText("Journal", 30f, y, pPurple13B)
            y += 16f
            if (s.notes.isNotBlank()) {
                cv.drawText("Notes:", 30f, y, pGray10)
                y += 13f
                wrapText(s.notes, 530f, pDark11).forEach { line ->
                    cv.drawText(line, 30f, y, pDark11)
                    y += 13f
                }
            }
            if (s.batch.isNotBlank()) {
                cv.drawText("Batch / Ammunition: ${s.batch}", 30f, y, pDark11)
                y += 13f
            }
            if (s.airPressure.isNotBlank()) {
                cv.drawText("Air Pressure: ${s.airPressure} bar", 30f, y, pDark11)
                y += 13f
            }
        }

        // ── Physical & mental state ───────────────────────────────────────────
        val hasRatings = s.muscleRecovery > 0 || s.fatigue > 0 ||
            s.concentration > 0 || s.endurance > 0 || s.heartRate > 0
        if (hasRatings) {
            y += 8f
            cv.drawLine(30f, y, 565f, y, pDivider)
            y += 18f
            cv.drawText("Physical & Mental State", 30f, y, pPurple13B)
            y += 16f
            if (s.muscleRecovery > 0) { cv.drawText("Muscle Recovery:", 30f, y, pGray10); cv.drawText("${s.muscleRecovery} / 5", 170f, y, pDark11); y += 13f }
            if (s.fatigue > 0)        { cv.drawText("Fatigue:", 30f, y, pGray10);          cv.drawText("${s.fatigue} / 5", 170f, y, pDark11);          y += 13f }
            if (s.concentration > 0)  { cv.drawText("Concentration:", 30f, y, pGray10);    cv.drawText("${s.concentration} / 5", 170f, y, pDark11);    y += 13f }
            if (s.endurance > 0)      { cv.drawText("Endurance:", 30f, y, pGray10);        cv.drawText("${s.endurance} / 5", 170f, y, pDark11);        y += 13f }
            if (s.heartRate > 0)      { cv.drawText("Heart Rate:", 30f, y, pGray10);       cv.drawText("${s.heartRate} / 5", 170f, y, pDark11);        y += 13f }
        }

        // ── Footer ────────────────────────────────────────────────────────────
        val generatedOn = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        cv.drawText("Generated by ShooterMind · $generatedOn", 30f, 825f, pGray9)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun paint(color: Int, textSize: Float, bold: Boolean = false): Paint =
        Paint().apply {
            this.color    = color
            this.textSize = textSize
            this.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            isAntiAlias   = true
        }

    private fun parseSeriesData(seriesData: String): List<Pair<Double, List<Double>>> {
        if (seriesData.isBlank()) return emptyList()
        return seriesData.split("|").mapNotNull { seriesStr ->
            val parts = seriesStr.split(",").mapNotNull { it.trim().toDoubleOrNull() }
            when {
                parts.isEmpty() -> null
                parts.size == 1 -> parts[0] to emptyList()
                else            -> parts.sum() to parts
            }
        }
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var line  = StringBuilder()
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(test) <= maxWidth) {
                line = StringBuilder(test)
            } else {
                if (line.isNotEmpty()) lines.add(line.toString())
                line = StringBuilder(word)
            }
        }
        if (line.isNotEmpty()) lines.add(line.toString())
        return lines
    }
}
