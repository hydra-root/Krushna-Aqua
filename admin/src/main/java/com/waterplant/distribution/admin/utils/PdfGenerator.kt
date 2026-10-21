package com.waterplant.distribution.admin.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.waterplant.distribution.core.data.model.Customer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility object for generating, saving, and sharing PDF invoices.
 * 
 * Uses Android's built-in `PdfDocument` to generate the PDF natively without
 * any third-party libraries. Handles drawing text and tables directly onto a Canvas.
 */
object PdfGenerator {

    /**
     * Generates a PDF invoice for a customer and saves it to a temporary cache file.
     * 
     * @return The absolute path to the cached PDF file, which can be used by the `PdfRenderer` for previewing.
     */
    fun generateCustomerBill(
        context: Context,
        customer: Customer,
        totalDelivered: Int,
        totalReturned: Int,
        totalAmount: Double,
        totalPaid: Double
    ): String? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawBill(context, canvas, customer, totalDelivered, totalReturned, totalAmount, totalPaid)
        pdfDocument.finishPage(page)

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Bill_${customer.name.replace(" ", "_")}_$timestamp.pdf"
        var cacheFilePath: String? = null

        try {
            val cacheDir = File(context.cacheDir, "pdf_preview")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            cacheFilePath = file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return cacheFilePath
    }

    /**
     * Copies the generated PDF from the temporary cache directory into the public `Downloads` directory.
     * Uses `MediaStore` on Android Q and above, and direct file I/O on older versions.
     */
    fun savePdfToDownloads(context: Context, cacheFilePath: String): Uri? {
        val file = File(cacheFilePath)
        if (!file.exists()) return null
        
        val fileName = file.name
        var pdfUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/KrushnaAqua")
                }
                pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                pdfUri?.let { uri ->
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "KrushnaAqua")
                if (!appDir.exists()) appDir.mkdirs()
                val destFile = File(appDir, fileName)
                file.copyTo(destFile, overwrite = true)
                pdfUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", destFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return pdfUri
    }

    /**
     * Internal helper function that handles drawing the invoice layout onto the `PdfDocument` canvas.
     */
    private fun drawBill(context: Context, canvas: Canvas, customer: Customer, delivered: Int, returned: Int, totalAmount: Double, totalPaid: Double) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        // Header
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.waterplant.distribution.admin.R.mipmap.ic_launcher)
        if (bitmap != null) {
            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 80, 80, false)
            canvas.drawBitmap(scaledBitmap, 50f, 40f, paint)
        }
        
        canvas.drawText("KRUSHNA AQUA", 150f, 80f, paint)
        
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Water Delivery Invoice", 180f, 110f, paint)
        
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        canvas.drawText("Date: $dateString", 400f, 150f, paint)

        // Customer Details
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Billed To:", 50f, 180f, paint)
        
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer.name, 50f, 210f, paint)
        if (customer.address.isNotBlank()) {
            canvas.drawText(customer.address, 50f, 235f, paint)
        }
        if (customer.phone.isNotBlank()) {
            canvas.drawText("Phone: ${customer.phone}", 50f, 260f, paint)
        }

        // Table Header
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 300f, 545f, 300f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Description", 60f, 330f, paint)
        canvas.drawText("Quantity / Amount", 350f, 330f, paint)
        
        canvas.drawLine(50f, 350f, 545f, 350f, paint)
        
        // Table Content
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Jars Delivered", 60f, 390f, paint)
        canvas.drawText("$delivered", 420f, 390f, paint)

        canvas.drawText("Total Jars Returned", 60f, 430f, paint)
        canvas.drawText("$returned", 420f, 430f, paint)

        canvas.drawLine(50f, 480f, 545f, 480f, paint)

        canvas.drawText("Total Bill Amount", 60f, 510f, paint)
        canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%.2f", totalAmount)}", 420f, 510f, paint)

        canvas.drawText("Cash Paid", 60f, 550f, paint)
        canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%.2f", totalPaid)}", 420f, 550f, paint)

        canvas.drawLine(50f, 580f, 545f, 580f, paint)

        // Total
        val pending = totalAmount - totalPaid
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Pending Balance:", 200f, 620f, paint)
        
        paint.color = Color.parseColor("#1565C0") // Brand Blue
        canvas.drawText("Rs. ${String.format(Locale.getDefault(), "%.2f", pending)}", 420f, 620f, paint)
        
        // Footer
        paint.color = Color.GRAY
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Thank you for your business!", 200f, 750f, paint)
    }

    /**
     * Opens the standard Android Share Sheet to share the generated PDF via external apps (like WhatsApp).
     * Automatically grants read URI permissions via the application's `FileProvider`.
     */
    fun sharePdf(context: Context, cacheFilePath: String) {
        val file = File(cacheFilePath)
        if (!file.exists()) return
        
        val pdfUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        try {
            context.startActivity(Intent.createChooser(intent, "Share Bill"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
