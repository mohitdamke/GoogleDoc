package com.example.googledoc.pdfView

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@ExperimentalFoundationApi
@Composable
fun PdfViewerFromUri(
    pdfUri: Uri,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF909090),
    pageColor: Color = White,
    listDirection: PdfListDirection = PdfListDirection.VERTICAL,
    arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(16.dp),
    loadingListener: (
        isLoading: Boolean,
        currentPage: Int?,
        maxPage: Int?,
    ) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val pdfStream = remember {
        contentResolver.openInputStream(pdfUri)
    }

    pdfStream?.let {
        PdfViewer(
            pdfStream = it,
            modifier = modifier,
            backgroundColor = backgroundColor,
            pageColor = pageColor,
            listDirection = listDirection,
            arrangement = arrangement,
            loadingListener = loadingListener
        )
    }
}
