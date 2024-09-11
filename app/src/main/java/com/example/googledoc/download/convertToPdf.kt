package com.example.googledoc.download

import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream

fun convertToPdf(content: String, filePath: String) {
    val pdfWriter = PdfWriter(filePath)
    val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
    val document = Document(pdfDocument)

    // Add content to PDF
    document.add(Paragraph(content))

    document.close()
}



fun convertToWord(content: String, filePath: String) {
    val document = XWPFDocument()
    val paragraph = document.createParagraph()
    paragraph.createRun().setText(content)

    FileOutputStream(filePath).use {
        document.write(it)
    }
}

