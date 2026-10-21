package com.waterplant.distribution.admin.ui.customer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.waterplant.distribution.admin.utils.PdfGenerator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewScreen(
    filePath: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableStateOf(0) }
    
    // Density to scale the PDF correctly
    val density = LocalDensity.current.density
    
    DisposableEffect(filePath) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                fileDescriptor = fd
                val renderer = PdfRenderer(fd)
                pdfRenderer = renderer
                pageCount = renderer.pageCount
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        onDispose {
            try {
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Preview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val uri = PdfGenerator.savePdfToDownloads(context, filePath)
                        if (uri != null) {
                            Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                    IconButton(onClick = {
                        PdfGenerator.sharePdf(context, filePath)
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        if (pdfRenderer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Failed to load PDF.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.LightGray),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(pageCount) { index ->
                    val renderer = pdfRenderer
                    if (renderer != null) {
                        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                        
                        LaunchedEffect(index) {
                            val page = renderer.openPage(index)
                            // Render at double resolution for crisp text
                            val newBitmap = Bitmap.createBitmap(
                                (page.width * density * 2).toInt(),
                                (page.height * density * 2).toInt(),
                                Bitmap.Config.ARGB_8888
                            )
                            // White background
                            newBitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            bitmap = newBitmap
                        }
                        
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "PDF Page ${index + 1}",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            )
                        } ?: Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
