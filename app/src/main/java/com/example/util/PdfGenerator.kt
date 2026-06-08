package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.model.CvData
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object PdfGenerator {

    /**
     * Generates a PDF file from CV data using a native PdfDocument.
     * Supports multiple templates: "modern_prof", "classic_elegance", and "creative_portfolio".
     */
    fun generateCvPdf(context: Context, cv: CvData): File? {
        val pdfDocument = PdfDocument()
        
        // A4 Paper Size in points (595 x 842)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            when (cv.templateId) {
                "classic_elegance" -> renderClassicTemplate(canvas, cv)
                "creative_portfolio" -> renderCreativeTemplate(canvas, cv)
                else -> renderModernTemplate(canvas, cv) // Default "modern_prof"
            }

            pdfDocument.finishPage(page)

            // Dynamic filename
            val cleanName = cv.name.replace("\\s+".toRegex(), "_")
            val fileName = "CV_${if (cleanName.isEmpty()) "Local_CV" else cleanName}_${System.currentTimeMillis()}.pdf"
            
            // Output file
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Save to Public Downloads via MediaStore if Android Q+
            saveToPublicDownloads(context, file, fileName)

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }

    private fun saveToPublicDownloads(context: Context, file: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    if (outputStream != null) {
                        outputStream.write(file.readBytes())
                        outputStream.close()
                        Toast.makeText(context, "Saved to Downloads folder!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Wrap paragraph text into multiple lines
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }

    // ----------------------------------------------------
    // MODERN PROFESSIONAL TEMPLATE (Elegant Blue Accents)
    // ----------------------------------------------------
    private fun renderModernTemplate(canvas: Canvas, cv: CvData) {
        val primaryColor = Color.parseColor("#1A365D") // Deep Navy
        val accentColor = Color.parseColor("#3182CE") // Premium blue
        val bodyColor = Color.parseColor("#2D3748") // Charcoal
        val dividerColor = Color.parseColor("#E2E8F0") // Soft Grey

        val paintText = Paint().apply { isAntiAlias = true }
        var currentY = 50f
        val marginX = 40f
        val contentWidth = 595f - (marginX * 2)

        // Title and Name
        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paintText.textSize = 24f
        paintText.color = primaryColor
        canvas.drawText(cv.name.ifEmpty { "Full Name" }, marginX, currentY, paintText)
        currentY += 22f

        paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paintText.textSize = 14f
        paintText.color = accentColor
        canvas.drawText(cv.title.ifEmpty { "Professional Title" }, marginX, currentY, paintText)
        currentY += 25f

        // Contact details row
        paintText.textSize = 9f
        paintText.color = Color.parseColor("#718096")
        val contactString = buildString {
            if (cv.email.isNotEmpty()) append("${cv.email}  |  ")
            if (cv.phone.isNotEmpty()) append("${cv.phone}  |  ")
            if (cv.location.isNotEmpty()) append("${cv.location}")
        }.removeSuffix("  |  ")
        canvas.drawText(contactString, marginX, currentY, paintText)
        currentY += 10f

        if (cv.linkedin.isNotEmpty()) {
            canvas.drawText("LinkedIn: ${cv.linkedin}", marginX, currentY, paintText)
            currentY += 15f
        } else {
            currentY += 10f
        }

        // Section Divider
        drawSectionDivider(canvas, marginX, currentY, contentWidth, dividerColor)
        currentY += 25f

        // Summary
        if (cv.summary.isNotEmpty()) {
            drawSectionTitle(canvas, "PROFESSIONAL SUMMARY", marginX, currentY, primaryColor, paintText)
            currentY += 16f
            
            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paintText.textSize = 10f
            paintText.color = bodyColor
            val wrappedSummary = wrapText(cv.summary, paintText, contentWidth)
            for (line in wrappedSummary) {
                canvas.drawText(line, marginX, currentY, paintText)
                currentY += 14f
            }
            currentY += 15f
        }

        // Work Experience
        if (cv.experience.isNotEmpty()) {
            drawSectionTitle(canvas, "WORK EXPERIENCE", marginX, currentY, primaryColor, paintText)
            currentY += 20f

            for (exp in cv.experience) {
                // Company & Dates on same line
                paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paintText.textSize = 11f
                paintText.color = bodyColor
                canvas.drawText("${exp.company} - ${exp.title}", marginX, currentY, paintText)
                
                paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paintText.textSize = 10f
                paintText.color = Color.parseColor("#4A5568")
                val dateStr = "${exp.startDate} - ${exp.endDate}"
                val dateWidth = paintText.measureText(dateStr)
                canvas.drawText(dateStr, 595f - marginX - dateWidth, currentY, paintText)
                
                currentY += 16f
                
                // Job Duties description
                if (exp.description.isNotEmpty()) {
                    paintText.color = bodyColor
                    val wrappedDesc = wrapText(exp.description, paintText, contentWidth - 10f)
                    for (line in wrappedDesc) {
                        canvas.drawText("• $line", marginX + 10f, currentY, paintText)
                        currentY += 13f
                    }
                }
                currentY += 15f
            }
        }

        // Education
        if (cv.education.isNotEmpty()) {
            drawSectionTitle(canvas, "EDUCATION", marginX, currentY, primaryColor, paintText)
            currentY += 20f

            for (edu in cv.education) {
                paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paintText.textSize = 11f
                paintText.color = bodyColor
                canvas.drawText(edu.institution, marginX, currentY, paintText)

                paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paintText.textSize = 10f
                paintText.color = Color.parseColor("#4A5568")
                val yearWidth = paintText.measureText(edu.year)
                canvas.drawText(edu.year, 595f - marginX - yearWidth, currentY, paintText)
                
                currentY += 15f

                paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paintText.color = bodyColor
                canvas.drawText(edu.degree, marginX, currentY, paintText)
                currentY += 20f
            }
        }

        // Skills
        if (cv.skills.isNotEmpty()) {
            drawSectionTitle(canvas, "SKILLS", marginX, currentY, primaryColor, paintText)
            currentY += 20f

            paintText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paintText.textSize = 10f
            paintText.color = bodyColor
            
            val skillsText = cv.skills.joinToString(", ")
            val wrappedSkills = wrapText(skillsText, paintText, contentWidth)
            for (line in wrappedSkills) {
                canvas.drawText(line, marginX, currentY, paintText)
                currentY += 14f
            }
        }

        // Watermark if FREE selection
        drawWatermark(canvas, cv, paintText)
    }

    // ----------------------------------------------------
    // CLASSIC ELEGANCE TEMPLATE (Serif Centered Headings)
    // ----------------------------------------------------
    private fun renderClassicTemplate(canvas: Canvas, cv: CvData) {
        val primaryColor = Color.parseColor("#111111")
        val bodyColor = Color.parseColor("#333333")
        val dividerColor = Color.parseColor("#111111")

        val paintText = Paint().apply { isAntiAlias = true }
        var currentY = 60f
        val marginX = 50f
        val contentWidth = 595f - (marginX * 2)

        // Centered Name
        paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paintText.textSize = 26f
        paintText.color = primaryColor
        val nameWidth = paintText.measureText(cv.name.ifEmpty { "Full Name" })
        canvas.drawText(cv.name.ifEmpty { "Full Name" }, (595f - nameWidth)/2, currentY, paintText)
        currentY += 24f

        // Centered Title
        paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paintText.textSize = 13f
        val titleWidth = paintText.measureText(cv.title.ifEmpty { "Professional Title" })
        canvas.drawText(cv.title.ifEmpty { "Professional Title" }, (595f - titleWidth)/2, currentY, paintText)
        currentY += 25f

        // Centered Contact Box
        paintText.textSize = 9f
        paintText.color = Color.parseColor("#555555")
        val contactString = buildString {
            if (cv.email.isNotEmpty()) append("${cv.email}   •   ")
            if (cv.phone.isNotEmpty()) append("${cv.phone}   •   ")
            if (cv.location.isNotEmpty()) append("${cv.location}")
        }.removeSuffix("   •   ")
        val contactWidth = paintText.measureText(contactString)
        canvas.drawText(contactString, (595f - contactWidth)/2, currentY, paintText)
        currentY += 12f

        if (cv.linkedin.isNotEmpty()) {
            val linkedinStr = "LinkedIn: ${cv.linkedin}"
            val lWidth = paintText.measureText(linkedinStr)
            canvas.drawText(linkedinStr, (595f - lWidth)/2, currentY, paintText)
            currentY += 20f
        } else {
            currentY += 15f
        }

        // Strong Centered Divider block
        val pLine = Paint().apply {
            color = dividerColor
            strokeWidth = 1.5f
        }
        canvas.drawLine(marginX, currentY, 595f - marginX, currentY, pLine)
        currentY += 25f

        // Summary
        if (cv.summary.isNotEmpty()) {
            drawCenteredClassicTitle(canvas, "Summary", currentY, paintText)
            currentY += 18f
            
            paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            paintText.textSize = 10f
            paintText.color = bodyColor
            val wrappedSummary = wrapText(cv.summary, paintText, contentWidth)
            for (line in wrappedSummary) {
                canvas.drawText(line, marginX, currentY, paintText)
                currentY += 14f
            }
            currentY += 20f
        }

        // Work Experience
        if (cv.experience.isNotEmpty()) {
            drawCenteredClassicTitle(canvas, "Professional Experience", currentY, paintText)
            currentY += 22f

            for (exp in cv.experience) {
                paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                paintText.textSize = 11f
                paintText.color = primaryColor
                canvas.drawText("${exp.company}  —  ${exp.title}", marginX, currentY, paintText)
                
                paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                paintText.textSize = 10f
                paintText.color = Color.parseColor("#444444")
                val dateStr = "${exp.startDate} - ${exp.endDate}"
                val dateWidth = paintText.measureText(dateStr)
                canvas.drawText(dateStr, 595f - marginX - dateWidth, currentY, paintText)
                
                currentY += 16f
                
                if (exp.description.isNotEmpty()) {
                    paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                    paintText.color = bodyColor
                    val wrappedDesc = wrapText(exp.description, paintText, contentWidth - 15f)
                    for (line in wrappedDesc) {
                        canvas.drawText("  $line", marginX + 10f, currentY, paintText)
                        currentY += 13f
                    }
                }
                currentY += 18f
            }
        }

        // Education
        if (cv.education.isNotEmpty()) {
            drawCenteredClassicTitle(canvas, "Education", currentY, paintText)
            currentY += 22f

            for (edu in cv.education) {
                paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                paintText.textSize = 11f
                paintText.color = primaryColor
                canvas.drawText(edu.institution, marginX, currentY, paintText)

                paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                paintText.textSize = 10f
                val yearWidth = paintText.measureText(edu.year)
                canvas.drawText(edu.year, 595f - marginX - yearWidth, currentY, paintText)
                
                currentY += 15f

                paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                canvas.drawText(edu.degree, marginX, currentY, paintText)
                currentY += 22f
            }
        }

        // Skills
        if (cv.skills.isNotEmpty()) {
            drawCenteredClassicTitle(canvas, "Expertise & Skills", currentY, paintText)
            currentY += 22f

            paintText.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            paintText.textSize = 10f
            paintText.color = bodyColor
            
            val skillsText = cv.skills.joinToString("   •   ")
            val wrappedSkills = wrapText(skillsText, paintText, contentWidth)
            for (line in wrappedSkills) {
                val lineW = paintText.measureText(line)
                canvas.drawText(line, (595f - lineW)/2, currentY, paintText)
                currentY += 14f
            }
        }

        // Watermark if FREE selection
        drawWatermark(canvas, cv, paintText)
    }

    // ----------------------------------------------------
    // CREATIVE PORTFOLIO TEMPLATE (Asymmetric Left-Side Accent Panel)
    // ----------------------------------------------------
    private fun renderCreativeTemplate(canvas: Canvas, cv: CvData) {
        val lateralPanelColor = Color.parseColor("#ECEFF1") // Cool clean silver-grey
        val headerColor = Color.parseColor("#37474F") // Slate Grey
        val accentColor = Color.parseColor("#D81B60") // Creative Magenta Pink
        val bodyColor = Color.parseColor("#37474F")

        val paintText = Paint().apply { isAntiAlias = true }
        
        // Render 1/3 sidebar panel
        val pPanel = Paint().apply {
            color = lateralPanelColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 180f, 842f, pPanel)

        // Draw name in creative box header
        val pBox = Paint().apply {
            color = headerColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 130f, pBox)

        // Draw Name & Title in Header Box
        paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paintText.textSize = 24f
        paintText.color = Color.WHITE
        canvas.drawText(cv.name.ifEmpty { "Full Name" }, 30f, 60f, paintText)

        paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paintText.textSize = 13f
        paintText.color = Color.parseColor("#FF80AB") // Bright pink accent
        canvas.drawText(cv.title.ifEmpty { "Professional Title" }, 30f, 85f, paintText)

        // Lateral Panel: Contacts & Skills
        var sideY = 170f
        val sideX = 15f
        val sideWidth = 150f

        paintText.color = headerColor
        paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paintText.textSize = 11f
        canvas.drawText("CONTACT INFO", sideX, sideY, paintText)
        sideY += 16f

        paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paintText.textSize = 8.5f
        paintText.color = Color.parseColor("#455A64")

        if (cv.email.isNotEmpty()) {
            val wrapEmail = wrapText(cv.email, paintText, sideWidth)
            for (line in wrapEmail) {
                canvas.drawText(line, sideX, sideY, paintText)
                sideY += 12f
            }
        }
        if (cv.phone.isNotEmpty()) {
            canvas.drawText(cv.phone, sideX, sideY, paintText)
            sideY += 14f
        }
        if (cv.location.isNotEmpty()) {
            val wrapLoc = wrapText(cv.location, paintText, sideWidth)
            for (line in wrapLoc) {
                canvas.drawText(line, sideX, sideY, paintText)
                sideY += 12f
            }
        }
        if (cv.linkedin.isNotEmpty()) {
            sideY += 5f
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText("LINKEDIN", sideX, sideY, paintText)
            sideY += 12f
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val wrapLi = wrapText(cv.linkedin, paintText, sideWidth)
            for (line in wrapLi) {
                canvas.drawText(line, sideX, sideY, paintText)
                sideY += 12f
            }
        }

        // Skills in lateral
        if (cv.skills.isNotEmpty()) {
            sideY += 25f
            paintText.color = headerColor
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paintText.textSize = 11f
            canvas.drawText("SKILLS", sideX, sideY, paintText)
            sideY += 16f

            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paintText.textSize = 9f
            paintText.color = Color.parseColor("#37474F")
            for (skill in cv.skills) {
                val wrapS = wrapText(skill, paintText, sideWidth - 10f)
                for (line in wrapS) {
                    canvas.drawText("• $line", sideX, sideY, paintText)
                    sideY += 13f
                }
            }
        }

        // Main Panel (Y: 170f, X: 200f)
        var mainY = 170f
        val mainX = 205f
        val mainWidth = 350f

        // Professional Summary
        if (cv.summary.isNotEmpty()) {
            paintText.color = accentColor
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paintText.textSize = 12f
            canvas.drawText("ABOUT ME", mainX, mainY, paintText)
            mainY += 16f

            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paintText.textSize = 10f
            paintText.color = bodyColor
            val wrappedSummary = wrapText(cv.summary, paintText, mainWidth)
            for (line in wrappedSummary) {
                canvas.drawText(line, mainX, mainY, paintText)
                mainY += 14f
            }
            mainY += 20f
        }

        // Work Experience
        if (cv.experience.isNotEmpty()) {
            paintText.color = accentColor
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paintText.textSize = 12f
            canvas.drawText("WORK EXPERIENCE", mainX, mainY, paintText)
            mainY += 18f

            for (exp in cv.experience) {
                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paintText.textSize = 10.5f
                paintText.color = headerColor
                canvas.drawText(exp.company, mainX, mainY, paintText)
                
                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paintText.textSize = 9.5f
                paintText.color = Color.parseColor("#78909C")
                val dateStr = "${exp.startDate} - ${exp.endDate}"
                val dateWidth = paintText.measureText(dateStr)
                canvas.drawText(dateStr, 595f - 30f - dateWidth, mainY, paintText)
                mainY += 13f

                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                paintText.color = bodyColor
                canvas.drawText(exp.title, mainX, mainY, paintText)
                mainY += 15f

                if (exp.description.isNotEmpty()) {
                    paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    paintText.color = bodyColor
                    paintText.textSize = 9.5f
                    val wrappedDesc = wrapText(exp.description, paintText, mainWidth - 10f)
                    for (line in wrappedDesc) {
                        canvas.drawText("- $line", mainX + 5f, mainY, paintText)
                        mainY += 12f
                    }
                }
                mainY += 15f
            }
        }

        // Education
        if (cv.education.isNotEmpty()) {
            paintText.color = accentColor
            paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paintText.textSize = 12f
            canvas.drawText("EDUCATION", mainX, mainY, paintText)
            mainY += 18f

            for (edu in cv.education) {
                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                paintText.textSize = 10.5f
                paintText.color = headerColor
                canvas.drawText(edu.institution, mainX, mainY, paintText)

                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                paintText.textSize = 9.5f
                val yearWidth = paintText.measureText(edu.year)
                canvas.drawText(edu.year, 595f - 30f - yearWidth, mainY, paintText)
                mainY += 13f

                paintText.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
                paintText.color = bodyColor
                canvas.drawText(edu.degree, mainX, mainY, paintText)
                mainY += 18f
            }
        }

        // Watermark if FREE selection
        drawWatermark(canvas, cv, paintText)
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, x: Float, y: Float, color: Int, paint: Paint) {
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        paint.color = color
        canvas.drawText(title, x, y, paint)
    }

    private fun drawSectionDivider(canvas: Canvas, x: Float, y: Float, width: Float, color: Int) {
        val pLine = Paint().apply {
            this.color = color
            strokeWidth = 1f
        }
        canvas.drawLine(x, y, x + width, y, pLine)
    }

    private fun drawCenteredClassicTitle(canvas: Canvas, title: String, y: Float, paint: Paint) {
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 13f
        paint.color = Color.parseColor("#111111")
        val width = paint.measureText(title)
        canvas.drawText(title, (595f - width)/2, y, paint)

        // Draw double tiny underline lines under classic header
        val pLine = Paint().apply {
            color = Color.parseColor("#BBBBBB")
            strokeWidth = 0.5f
        }
        canvas.drawLine((595f - width)/2 - 10f, y + 4f, (595f + width)/2 + 10f, y + 4f, pLine)
    }

    private fun drawWatermark(canvas: Canvas, cv: CvData, paintText: Paint) {
        // Only draw watermark if NOT premium, to align with requirements
        // We can check if isPremium state is false dynamically.
        // For standard compilation safety, we check if templateId contains a flag, OR we write the watermark on all generated
        // drafts of Free templates, unless premium template is purchased!
        // To be compliant with the premium rules, we render the watermark ONLY if the user is a free user.
        // Wait, how does this method know if user is Free? The CvViewModel updates the CvData when saving, or we can write a watermark parameter!
        // Let's check templateId or write a generic rule: since free users might bypass if they want, let's write watermark
        // "Created with Local CV Cafe" at the very bottom of the PDF on all PDFs unless the user explicitly owns premium membership!
        // Let's pass a parameter `isPremium: Boolean` to allow disabling watermark based on user status. Let's make it easy.
    }
    
    // Overloaded with isPremium flag
    fun generateCvPdf(context: Context, cv: CvData, isPremium: Boolean): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        try {
            when (cv.templateId) {
                "classic_elegance" -> renderClassicTemplate(canvas, cv)
                "creative_portfolio" -> renderCreativeTemplate(canvas, cv)
                else -> renderModernTemplate(canvas, cv)
            }

            // Draw watermark ONLY for free users
            if (!isPremium) {
                val paintWatermark = Paint().apply {
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    textSize = 8f
                    color = Color.parseColor("#A0AEC0")
                }
                val watermarkText = "Made with Local CV Cafe ❤️ Save unlimited CVs with Premium Plan"
                val textW = paintWatermark.measureText(watermarkText)
                canvas.drawText(watermarkText, (595f - textW)/2, 825f, paintWatermark)
            }

            pdfDocument.finishPage(page)

            val cleanName = cv.name.replace("\\s+".toRegex(), "_")
            val fileName = "CV_${if (cleanName.isEmpty()) "Local_CV" else cleanName}_${System.currentTimeMillis()}.pdf"
            
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            saveToPublicDownloads(context, file, fileName)
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
    }
}
