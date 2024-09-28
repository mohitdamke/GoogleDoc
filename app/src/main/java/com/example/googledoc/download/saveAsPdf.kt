package com.example.googledoc.download

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.googledoc.R
import com.example.googledoc.data.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// Function to save the document as a PDF and send a notification
fun saveAsPdf(context: Context, document: Document) {
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
            Toast.makeText(context, "PDF saved at Downloads", Toast.LENGTH_SHORT).show()
            Log.d("PDFSave", "PDF saved successfully at Downloads")

            // Trigger notification after PDF is saved
            sendDownloadNotification(context, fileName, file.absolutePath)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Log.e("PDFSaveError", "Error saving PDF: ${e.message}")
        Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

// Function to trigger a notification after saving the PDF
private fun sendDownloadNotification(context: Context, fileName: String, filePath: String) {
    val channelId = "pdf_download_channel"
    val channelName = "PDF Download Notification"
    val notificationId = 1

    // Create notification channel for Android 8.0 and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for PDF downloads"
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Build the notification
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.googledoc) // Ensure this icon exists in your drawable folder
        .setContentTitle("PDF Downloaded")
        .setContentText("$fileName saved to Downloads")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notificationId, notification)
    }
}
