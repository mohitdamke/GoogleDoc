package com.example.googledoc.common

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.googledoc.data.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Function to save the document as a PDF
fun SaveAsPdf(context: Context, document: Document) {
    // Create a PdfDocument instance
    val pdfDocument = PdfDocument()

    // Create a new page with desired size (A4 size 595 x 842 points)
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)

    // Draw content on the page's canvas
    val canvas = page.canvas
    val paint = Paint().apply {
        textSize = 18f
        color = Color.BLACK
    }
    canvas.drawText(document.title, 50f, 50f, paint)
    paint.textSize = 14f
    canvas.drawText(document.content, 50f, 100f, paint)

    // Finish the page
    pdfDocument.finishPage(page)

    // Define the file name and path
    val fileName = "${document.title}.pdf"
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    // Ensure directory exists
    if (directory != null && !directory.exists()) {
        directory.mkdirs()
    }

    // File path
    val file = File(directory, fileName)

    try {
        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
            Toast.makeText(context, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            Log.d("PDFSave", "PDF saved successfully at: ${file.absolutePath}")
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("PDFSaveError", "Error saving PDF: ${e.message}")
        Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
