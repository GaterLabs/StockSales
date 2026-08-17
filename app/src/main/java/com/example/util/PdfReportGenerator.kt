package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.TransactionWithItems
import com.example.ui.viewmodel.SalesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595  // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points
    private const val MARGIN = 36f

    /**
     * Generates a multi-page PDF report file saved in the app's cache directory.
     */
    suspend fun generateSalesPdfReport(
        context: Context,
        transactions: List<TransactionWithItems>,
        periodTitle: String
    ): File = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()

        val sdfDate = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        val printDateStr = sdfDate.format(Date())

        // Calculate KPI Summaries
        val totalRevenue = transactions.sumOf { it.transaction.totalAmountDue }
        val totalCollected = transactions.sumOf { it.transaction.amountPaid }
        val totalProfit = transactions.sumOf { it.transaction.totalProfit }
        val totalItemsSold = transactions.sumOf { it.transaction.totalItemsSold }
        val totalNewDebt = transactions.sumOf { it.transaction.newDebtAdded }
        val totalTransactions = transactions.size
        val uniqueStores = transactions.map { it.transaction.storeId }.distinct().size
        val totalCapitalDeployed = transactions.flatMap { it.items }.sumOf {
            (it.soldQuantity + it.remainingStock) * it.costPrice
        }

        // Product aggregate
        val productSalesMap = mutableMapOf<String, ProductSalesStat>()
        transactions.forEach { twi ->
            twi.items.forEach { item ->
                val curr = productSalesMap.getOrPut(item.productName) {
                    ProductSalesStat(
                        productName = item.productName,
                        unitName = item.unitName,
                        totalSold = 0,
                        totalRevenue = 0.0,
                        totalProfit = 0.0
                    )
                }
                curr.totalSold += item.soldQuantity
                curr.totalRevenue += item.subtotalDue
                curr.totalProfit += item.subtotalProfit
            }
        }
        val topProducts = productSalesMap.values.sortedByDescending { it.totalRevenue }

        // Paints
        val titlePaint = Paint().apply {
            color = Color.rgb(24, 43, 73) // Deep Navy
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(100, 116, 139) // Slate Gray
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bgCardPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val borderCardPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val tableHeaderBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tableRowAltBgPaint = Paint().apply {
            color = Color.rgb(250, 250, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.75f
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(22, 101, 52)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val redPaint = Paint().apply {
            color = Color.rgb(185, 28, 28)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
            // Top Accent Bar
            val accentPaint = Paint().apply {
                color = Color.rgb(37, 99, 235) // Royal Blue
                style = Paint.Style.FILL
            }
            c.drawRect(MARGIN, MARGIN, PAGE_WIDTH - MARGIN, MARGIN + 4f, accentPaint)

            // Header Title
            c.drawText("STOCK SALES • LAPORAN REKAPITULASI PENJUALAN KONSINYASI", MARGIN, MARGIN + 20f, titlePaint)
            c.drawText("Periode: $periodTitle  |  Dicetak: $printDateStr", MARGIN, MARGIN + 32f, subtitlePaint)
            c.drawLine(MARGIN, MARGIN + 38f, PAGE_WIDTH - MARGIN, MARGIN + 38f, dividerPaint)

            // Footer
            val footerPaint = Paint().apply {
                color = Color.rgb(148, 163, 184)
                textSize = 7.5f
                isAntiAlias = true
            }
            c.drawLine(MARGIN, PAGE_HEIGHT - MARGIN - 12f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - 12f, dividerPaint)
            c.drawText("Dokumen ini digenerate secara otomatis oleh Aplikasi Stock Sales Konsinyasi", MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
            val pStr = "Hal $pNum"
            val pWidth = footerPaint.measureText(pStr)
            c.drawText(pStr, PAGE_WIDTH - MARGIN - pWidth, PAGE_HEIGHT - MARGIN, footerPaint)
        }

        drawHeaderAndFooter(canvas, pageNumber)

        var y = MARGIN + 52f

        // 1. Financial Summary Card on Page 1
        val cardHeight = 72f
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight, 6f, 6f, bgCardPaint)
        canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight, 6f, 6f, borderCardPaint)

        val colWidth = (PAGE_WIDTH - 2 * MARGIN) / 4f

        // Box 1: Total Omset
        canvas.drawText("TOTAL OMSET", MARGIN + 10f, y + 18f, subtitlePaint)
        canvas.drawText(SalesViewModel.formatRupiah(totalRevenue), MARGIN + 10f, y + 36f, titlePaint.apply { textSize = 11.5f })
        canvas.drawText("$totalTransactions Transaksi ($totalItemsSold item)", MARGIN + 10f, y + 54f, subtitlePaint)

        // Box 2: Total Laba Bersih
        val marginPercent = if (totalCapitalDeployed > 0) (totalProfit / totalCapitalDeployed) * 100 else 0.0
        canvas.drawText("LABA BERSIH", MARGIN + colWidth + 10f, y + 18f, subtitlePaint)
        canvas.drawText("+${SalesViewModel.formatRupiah(totalProfit)}", MARGIN + colWidth + 10f, y + 36f, greenPaint.apply { textSize = 11.5f })
        canvas.drawText("Margin: ${String.format(Locale.US, "%.1f", marginPercent)}%", MARGIN + colWidth + 10f, y + 54f, subtitlePaint)

        // Box 3: Total Kas Masuk
        canvas.drawText("UANG DITERIMA", MARGIN + colWidth * 2 + 10f, y + 18f, subtitlePaint)
        canvas.drawText(SalesViewModel.formatRupiah(totalCollected), MARGIN + colWidth * 2 + 10f, y + 36f, boldTextPaint.apply { textSize = 11f })
        canvas.drawText("Dari $uniqueStores Warung", MARGIN + colWidth * 2 + 10f, y + 54f, subtitlePaint)

        // Box 4: Sisa Bon Baru
        canvas.drawText("BON / PIUTANG BARU", MARGIN + colWidth * 3 + 10f, y + 18f, subtitlePaint)
        canvas.drawText(SalesViewModel.formatRupiah(totalNewDebt), MARGIN + colWidth * 3 + 10f, y + 36f, redPaint.apply { textSize = 11f })
        canvas.drawText("Belum Lunas", MARGIN + colWidth * 3 + 10f, y + 54f, subtitlePaint)

        y += cardHeight + 18f

        // 2. Section: Product Sales Breakdown
        if (topProducts.isNotEmpty()) {
            canvas.drawText("RINGKASAN PENJUALAN PRODUK TERLARIS", MARGIN, y, sectionPaint)
            y += 10f

            val pRowHeight = 16f
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + pRowHeight, tableHeaderBgPaint)
            canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + pRowHeight, borderCardPaint)

            canvas.drawText("NAMA PRODUK", MARGIN + 6f, y + 11.5f, headerPaint)
            canvas.drawText("SATUAN", MARGIN + 210f, y + 11.5f, headerPaint)
            canvas.drawText("TERJUAL", MARGIN + 280f, y + 11.5f, headerPaint)
            canvas.drawText("TOTAL OMSET", MARGIN + 355f, y + 11.5f, headerPaint)
            canvas.drawText("LABA BERSIH", MARGIN + 450f, y + 11.5f, headerPaint)
            y += pRowHeight

            topProducts.take(5).forEachIndexed { idx, p ->
                if (idx % 2 == 1) {
                    canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + pRowHeight, tableRowAltBgPaint)
                }
                canvas.drawText(p.productName.take(30), MARGIN + 6f, y + 11f, textPaint)
                canvas.drawText(p.unitName, MARGIN + 210f, y + 11f, textPaint)
                canvas.drawText("${p.totalSold}", MARGIN + 280f, y + 11f, boldTextPaint)
                canvas.drawText(SalesViewModel.formatRupiah(p.totalRevenue), MARGIN + 355f, y + 11f, textPaint)
                canvas.drawText("+${SalesViewModel.formatRupiah(p.totalProfit)}", MARGIN + 450f, y + 11f, greenPaint)
                canvas.drawLine(MARGIN, y + pRowHeight, PAGE_WIDTH - MARGIN, y + pRowHeight, dividerPaint)
                y += pRowHeight
            }

            y += 16f
        }

        // 3. Section: Detailed Transactions List
        canvas.drawText("RINCIAN TRANSAKSI KUNJUNGAN & NOTA", MARGIN, y, sectionPaint)
        y += 10f

        val txRowHeight = 17f
        fun drawTxTableHeader(c: Canvas, currentY: Float) {
            c.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + txRowHeight, tableHeaderBgPaint)
            c.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + txRowHeight, borderCardPaint)
            c.drawText("NO NOTA", MARGIN + 4f, currentY + 11.5f, headerPaint)
            c.drawText("WARUNG & RUTE", MARGIN + 85f, currentY + 11.5f, headerPaint)
            c.drawText("WAKTU", MARGIN + 235f, currentY + 11.5f, headerPaint)
            c.drawText("LAKU", MARGIN + 310f, currentY + 11.5f, headerPaint)
            c.drawText("TAGIHAN", MARGIN + 355f, currentY + 11.5f, headerPaint)
            c.drawText("DIBAYAR", MARGIN + 425f, currentY + 11.5f, headerPaint)
            c.drawText("STATUS", MARGIN + 485f, currentY + 11.5f, headerPaint)
        }

        drawTxTableHeader(canvas, y)
        y += txRowHeight

        val txTimeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

        transactions.forEachIndexed { index, twi ->
            // Check if we need a new page
            if (y + txRowHeight > PAGE_HEIGHT - MARGIN - 30f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawHeaderAndFooter(canvas, pageNumber)
                y = MARGIN + 48f
                drawTxTableHeader(canvas, y)
                y += txRowHeight
            }

            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + txRowHeight, tableRowAltBgPaint)
            }

            val t = twi.transaction
            canvas.drawText(t.receiptNumber, MARGIN + 4f, y + 11.5f, boldTextPaint)
            val storeAndRoute = "${t.storeName} (${t.routeName})"
            canvas.drawText(storeAndRoute.take(26), MARGIN + 85f, y + 11.5f, textPaint)
            canvas.drawText(txTimeFormat.format(Date(t.visitTimestamp)), MARGIN + 235f, y + 11.5f, subtitlePaint)
            canvas.drawText("${t.totalItemsSold}", MARGIN + 310f, y + 11.5f, boldTextPaint)
            canvas.drawText(SalesViewModel.formatRupiah(t.totalAmountDue), MARGIN + 355f, y + 11.5f, textPaint)
            canvas.drawText(SalesViewModel.formatRupiah(t.amountPaid), MARGIN + 425f, y + 11.5f, textPaint)

            if (t.paymentStatus == "LUNAS") {
                canvas.drawText("LUNAS", MARGIN + 485f, y + 11.5f, greenPaint)
            } else {
                canvas.drawText("BON: ${SalesViewModel.formatRupiah(t.newDebtAdded)}", MARGIN + 485f, y + 11.5f, redPaint)
            }

            canvas.drawLine(MARGIN, y + txRowHeight, PAGE_WIDTH - MARGIN, y + txRowHeight, dividerPaint)
            y += txRowHeight
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "Laporan_SalesTrack_${fileDateFormat.format(Date())}.pdf"
        val pdfFile = File(reportsDir, fileName)

        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        pdfFile
    }

    /**
     * Writes generated PDF directly to a Uri (Storage Access Framework).
     */
    suspend fun writePdfToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionWithItems>,
        periodTitle: String
    ) = withContext(Dispatchers.IO) {
        val tempFile = generateSalesPdfReport(context, transactions, periodTitle)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
            outputStream.flush()
        }
    }

    /**
     * Opens native Android Share sheet for the generated PDF.
     */
    fun sharePdfReport(context: Context, pdfFile: File, subject: String = "Laporan Penjualan Konsinyasi SalesTrack") {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(
                Intent.EXTRA_TEXT,
                "Berikut kami lampirkan dokumen PDF resmi Rekapitulasi Laporan Penjualan & Konsinyasi SalesTrack."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan Laporan PDF via:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private data class ProductSalesStat(
        val productName: String,
        val unitName: String,
        var totalSold: Int,
        var totalRevenue: Double,
        var totalProfit: Double
    )
}
