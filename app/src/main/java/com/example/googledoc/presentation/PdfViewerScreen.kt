package com.example.googledoc.presentation

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.googledoc.pdfView.ZoomableImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PdfViewerScreen(
    modifier: Modifier = Modifier,
    pdfUri: String?,
    navController: NavController = rememberNavController()
) {


    var isLoading by remember { mutableStateOf(true) }
    var currentLoadingPage by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var pdfBitmapsState by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    pdfUri?.let { uriString ->
        val context = LocalContext.current

        // Load the PDF using PdfRenderer in a coroutine
        LaunchedEffect(uriString) {
            val contentResolver = context.contentResolver
            val uri = Uri.parse(uriString)
            val fileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor?.let {
                val pdfRenderer = PdfRenderer(it)
                val bitmaps = mutableListOf<Bitmap>()

                pageCount = pdfRenderer.pageCount
                for (i in 0 until pageCount) {
                    isLoading = true
                    val page = pdfRenderer.openPage(i)
                    val bitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    currentLoadingPage = i + 1 // Page count starts at 1
                    page.close()
                }
                pdfBitmapsState = bitmaps
                isLoading = false
                pdfRenderer.close()
            }
        }

        Scaffold { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (isLoading) {
                    LoadingIndicator(currentLoadingPage, pageCount)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(pdfBitmapsState) { bitmap ->
                            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                            ZoomableImage(
                                painter = BitmapPainter(bitmap.asImageBitmap()), // Convert ImageBitmap to BitmapPainter                            modifier = Modifier
                                modifier = modifier
                                    .fillMaxWidth()
                                    .aspectRatio(aspectRatio)
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(currentPage: Int, totalPageCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            progress = currentPage.toFloat() / totalPageCount.toFloat()
        )
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = "${currentPage} pages loaded / ${totalPageCount} total pages",
            textAlign = TextAlign.Center
        )
    }
}