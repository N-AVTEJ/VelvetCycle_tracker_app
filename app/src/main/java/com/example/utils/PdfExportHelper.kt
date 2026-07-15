package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object PdfExportHelper {
    fun exportReport(context: Context, storageHelper: StorageHelper) {
        val pdfDocument = PdfDocument()
        
        // Letter/A4 size page dimensions: 595 x 842
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        
        val margin = 40f
        var currentY = 50f
        
        // Title Banner (velvet pink Accent)
        paint.color = Color.parseColor("#D4537E")
        paint.style = Paint.Style.FILL
        canvas.drawRect(margin, currentY, 595f - margin, currentY + 40f, paint)
        
        textPaint.color = Color.WHITE
        textPaint.textSize = 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VelvetCycle Health Report", margin + 15f, currentY + 26f, textPaint)
        
        currentY += 60f
        
        // Header info
        textPaint.color = Color.BLACK
        textPaint.textSize = 12f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Patient Name: ${storageHelper.userName.ifEmpty { "VelvetCycle User" }}", margin, currentY, textPaint)
        currentY += 18f
        canvas.drawText("Generated on: ${LocalDate.now()}", margin, currentY, textPaint)
        currentY += 30f
        
        // Divider
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(margin, currentY, 595f - margin, currentY, paint)
        currentY += 25f
        
        // SECTION 1: Summary
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#D4537E")
        canvas.drawText("SECTION 1: Cycle Summary", margin, currentY, textPaint)
        currentY += 22f
        
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = Color.BLACK
        
        val cycleHistory = storageHelper.getCycleHistory()
        val avgCycle = if (cycleHistory.isNotEmpty()) cycleHistory.map { it.duration }.average().toInt() else storageHelper.cycleLength
        val avgPeriod = storageHelper.periodDuration
        val totalCycles = cycleHistory.size
        
        canvas.drawText("• Average cycle length: $avgCycle days", margin + 15f, currentY, textPaint)
        currentY += 18f
        canvas.drawText("• Average period duration: $avgPeriod days", margin + 15f, currentY, textPaint)
        currentY += 18f
        canvas.drawText("• Total cycles tracked: $totalCycles", margin + 15f, currentY, textPaint)
        currentY += 18f
        canvas.drawText("• Last period start date: ${storageHelper.lastPeriodStart}", margin + 15f, currentY, textPaint)
        currentY += 30f
        
        // SECTION 2: Last 6 Cycles Table
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#D4537E")
        canvas.drawText("SECTION 2: Last 6 Cycles History", margin, currentY, textPaint)
        currentY += 22f
        
        // Table Header Banner
        paint.color = Color.parseColor("#FFF0F5")
        canvas.drawRect(margin, currentY, 595f - margin, currentY + 20f, paint)
        
        textPaint.textSize = 10f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Cycle No.", margin + 10f, currentY + 14f, textPaint)
        canvas.drawText("Start Date", margin + 100f, currentY + 14f, textPaint)
        canvas.drawText("Duration", margin + 250f, currentY + 14f, textPaint)
        canvas.drawText("Notes", margin + 350f, currentY + 14f, textPaint)
        
        currentY += 20f
        
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val displayCycles = cycleHistory.take(6)
        for (cycle in displayCycles) {
            canvas.drawText("#${cycle.cycleNo}", margin + 10f, currentY + 14f, textPaint)
            canvas.drawText(cycle.startDate.toString(), margin + 100f, currentY + 14f, textPaint)
            canvas.drawText("${cycle.duration} days", margin + 250f, currentY + 14f, textPaint)
            
            val noteText = if (cycle.notes.length > 25) cycle.notes.take(22) + "..." else cycle.notes
            canvas.drawText(noteText, margin + 350f, currentY + 14f, textPaint)
            
            paint.color = Color.LTGRAY
            canvas.drawLine(margin, currentY + 20f, 595f - margin, currentY + 20f, paint)
            currentY += 20f
        }
        currentY += 15f
        
        // SECTION 3: Symptom Frequency
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#D4537E")
        canvas.drawText("SECTION 3: Symptom Frequency Summary", margin, currentY, textPaint)
        currentY += 22f
        
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = Color.BLACK
        
        val logs = storageHelper.getAllLogs()
        val symptomsCount = mutableMapOf<String, Int>()
        logs.values.forEach { log ->
            log.symptoms.forEach { sym ->
                symptomsCount[sym] = (symptomsCount[sym] ?: 0) + 1
            }
        }
        val sortedSymptoms = symptomsCount.entries.sortedByDescending { it.value }.take(5)
        if (sortedSymptoms.isNotEmpty()) {
            for (entry in sortedSymptoms) {
                canvas.drawText("• ${entry.key}: logged ${entry.value} times", margin + 15f, currentY, textPaint)
                currentY += 18f
            }
        } else {
            canvas.drawText("No symptoms logged yet.", margin + 15f, currentY, textPaint)
            currentY += 18f
        }
        currentY += 15f
        
        // SECTION 4: Mood patterns
        textPaint.textSize = 14f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = Color.parseColor("#D4537E")
        canvas.drawText("SECTION 4: Mood & Wellness Patterns", margin, currentY, textPaint)
        currentY += 22f
        
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = Color.BLACK
        
        val moodCount = mutableMapOf<String, Int>()
        logs.values.forEach { log ->
            if (log.mood.isNotEmpty()) {
                moodCount[log.mood] = (moodCount[log.mood] ?: 0) + 1
            }
        }
        val topMood = moodCount.maxByOrNull { it.value }?.key ?: "Happy"
        canvas.drawText("• Most common mood logged overall: $topMood", margin + 15f, currentY, textPaint)
        
        // Footer (A4 total height is 842f)
        currentY = 780f
        paint.color = Color.LTGRAY
        canvas.drawLine(margin, currentY, 595f - margin, currentY, paint)
        
        textPaint.textSize = 8.5f
        textPaint.color = Color.GRAY
        canvas.drawText("This report was generated by VelvetCycle app. Please consult a doctor for medical advice.", margin, currentY + 16f, textPaint)
        
        pdfDocument.finishPage(page)
        
        // Save PDF to cache or external files
        val pdfFile = File(context.cacheDir, "VelvetCycle_Health_Report.pdf")
        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.close()
            pdfDocument.close()
            
            // Share PDF
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Report via"))
            
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
