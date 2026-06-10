package com.nugst.launchland.util.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun createPdf(context: Context, fileName: String, content: String): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.textSize = 12f
        var y = 40f
        val x = 40f
        
        // Split content by new lines and draw
        content.split("\n").forEach { line ->
            canvas.drawText(line, x, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(null), "\$fileName.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
