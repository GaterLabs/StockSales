package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionWithItems
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AppThemeColors
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryDateFilter {
    ALL,
    TODAY,
    LAST_7_DAYS,
    THIS_MONTH,
    CUSTOM
}

data class DailyGroup(
    val dateKey: String,
    val displayDate: String,
    val isToday: Boolean,
    val isYesterday: Boolean,
    val totalRevenue: Double,
    val totalProfit: Double,
    val totalDebt: Double,
    val paidCount: Int,
    val unpaidCount: Int,
    val transactions: List<TransactionWithItems>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SalesViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryDateFilter.ALL) }
    var customSelectedCalendar by remember { mutableStateOf<Calendar?>(null) }
    var selectedTransaction by remember { mutableStateOf<TransactionWithItems?>(null) }

    val locale = if (language.code == "id") Locale("in", "ID") else Locale.ENGLISH

    // Date Formatters
    val dayKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val fullDateFormatter = remember(language) { SimpleDateFormat("EEEE, d MMMM yyyy", locale) }
    val timeFormatter = remember(language) { SimpleDateFormat("HH:mm", locale) }

    val todayCalendar = remember { Calendar.getInstance() }
    val todayKey = remember { dayKeyFormatter.format(Date()) }

    val yesterdayCalendar = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    }
    val yesterdayKey = remember { dayKeyFormatter.format(yesterdayCalendar.time) }

    // Filter by Search Query & Date Filter
    val filteredTransactions = remember(allTransactions, searchQuery, selectedFilter, customSelectedCalendar) {
        val now = Calendar.getInstance()

        allTransactions.filter { item ->
            val tx = item.transaction
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.visitTimestamp }

            // Match Search
            val matchSearch = searchQuery.isBlank() ||
                    tx.storeName.contains(searchQuery, ignoreCase = true) ||
                    tx.routeName.contains(searchQuery, ignoreCase = true) ||
                    tx.receiptNumber.contains(searchQuery, ignoreCase = true) ||
                    item.items.any { it.productName.contains(searchQuery, ignoreCase = true) }

            if (!matchSearch) return@filter false

            // Match Date Filter
            when (selectedFilter) {
                HistoryDateFilter.ALL -> true
                HistoryDateFilter.TODAY -> {
                    dayKeyFormatter.format(txCal.time) == todayKey
                }
                HistoryDateFilter.LAST_7_DAYS -> {
                    val sevenDaysAgo = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -7)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    tx.visitTimestamp >= sevenDaysAgo.timeInMillis
                }
                HistoryDateFilter.THIS_MONTH -> {
                    txCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            txCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }
                HistoryDateFilter.CUSTOM -> {
                    if (customSelectedCalendar != null) {
                        dayKeyFormatter.format(txCal.time) == dayKeyFormatter.format(customSelectedCalendar!!.time)
                    } else {
                        true
                    }
                }
            }
        }
    }

    // Group filtered transactions by date
    val groupedByDate = remember(filteredTransactions, language) {
        filteredTransactions
            .groupBy { dayKeyFormatter.format(Date(it.transaction.visitTimestamp)) }
            .map { (dateKey, items) ->
                val firstTimestamp = items.first().transaction.visitTimestamp
                val displayDate = fullDateFormatter.format(Date(firstTimestamp))
                val isToday = (dateKey == todayKey)
                val isYesterday = (dateKey == yesterdayKey)

                val totalRevenue = items.sumOf { it.transaction.totalAmountDue }
                val totalProfit = items.sumOf { it.transaction.totalProfit }
                val totalDebt = items.filter { it.transaction.paymentStatus == "UNPAID" }
                    .sumOf { it.transaction.totalAmountDue }
                val paidCount = items.count { it.transaction.paymentStatus == "PAID" }
                val unpaidCount = items.count { it.transaction.paymentStatus == "UNPAID" }

                DailyGroup(
                    dateKey = dateKey,
                    displayDate = displayDate,
                    isToday = isToday,
                    isYesterday = isYesterday,
                    totalRevenue = totalRevenue,
                    totalProfit = totalProfit,
                    totalDebt = totalDebt,
                    paidCount = paidCount,
                    unpaidCount = unpaidCount,
                    transactions = items.sortedByDescending { it.transaction.visitTimestamp }
                )
            }
            .sortedByDescending { it.dateKey }
    }

    // Overall summary of filtered results
    val totalFilteredRevenue = remember(filteredTransactions) {
        filteredTransactions.sumOf { it.transaction.totalAmountDue }
    }
    val totalFilteredProfit = remember(filteredTransactions) {
        filteredTransactions.sumOf { it.transaction.totalProfit }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = strings.historyTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredTransactions.size} ${strings.txPrefix} • ${groupedByDate.size} Days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val activePeriodName = when (selectedFilter) {
                                HistoryDateFilter.ALL -> "Semua Riwayat Nota"
                                HistoryDateFilter.TODAY -> "Nota Hari Ini"
                                HistoryDateFilter.LAST_7_DAYS -> "Nota 7 Hari Terakhir"
                                HistoryDateFilter.THIS_MONTH -> "Nota Bulan Ini"
                                HistoryDateFilter.CUSTOM -> "Nota Tanggal Terpilih"
                            }
                            viewModel.generatePdfReport(context, activePeriodName) { pdfFile ->
                                if (pdfFile != null) {
                                    com.example.util.PdfReportGenerator.sharePdfReport(
                                        context,
                                        pdfFile,
                                        "Rekapitulasi Nota Penjualan SalesTrack ($activePeriodName)"
                                    )
                                } else {
                                    android.widget.Toast.makeText(context, "Gagal membuat dokumen PDF", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF Report",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchHistoryPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp)
            )

            // Date Filter Horizontal Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryDateFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    val filterLabel = when (filter) {
                        HistoryDateFilter.ALL -> strings.filterAllTime
                        HistoryDateFilter.TODAY -> strings.filterToday
                        HistoryDateFilter.LAST_7_DAYS -> strings.filterThisWeek
                        HistoryDateFilter.THIS_MONTH -> strings.filterThisMonth
                        HistoryDateFilter.CUSTOM -> strings.filterPickDate
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (filter == HistoryDateFilter.CUSTOM) {
                                val cal = customSelectedCalendar ?: Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(year, month, dayOfMonth)
                                        }
                                        customSelectedCalendar = newCal
                                        selectedFilter = HistoryDateFilter.CUSTOM
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } else {
                                selectedFilter = filter
                            }
                        },
                        label = {
                            if (filter == HistoryDateFilter.CUSTOM && customSelectedCalendar != null && isSelected) {
                                val customDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(customSelectedCalendar!!.time)
                                Text("📅 $customDateStr")
                            } else {
                                Text(filterLabel)
                            }
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Summary Card
            if (filteredTransactions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.totalRevenueStat.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SalesViewModel.formatRupiah(totalFilteredRevenue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.totalProfitStat.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${SalesViewModel.formatRupiah(totalFilteredProfit)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppThemeColors.profitColor
                            )
                        }
                    }
                }
            }

            // Grouped Transaction List
            if (groupedByDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.noTransactionsFound,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.noTransactionsFoundDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    groupedByDate.forEach { group ->
                        // Date Header Sticky / Section Title
                        item(key = "header_${group.dateKey}") {
                            DailyHeaderCard(group = group)
                        }

                        // Transaction Cards for this Date
                        items(group.transactions, key = { it.transaction.id }) { item ->
                            val tx = item.transaction
                            val timeStr = timeFormatter.format(Date(tx.visitTimestamp))

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTransaction = item }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = tx.storeName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.surfaceVariant
                                                ) {
                                                    Text(
                                                        text = tx.routeName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "No. ${tx.receiptNumber} • $timeStr",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        StatusBadge(status = tx.paymentStatus)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Item summaries (products sold & returned)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            item.items.forEach { lineItem ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "• ${lineItem.productName}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        text = "${strings.soldLabel} ${lineItem.soldQuantity} pcs",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${strings.profitPrefix}:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppThemeColors.profitColor
                                            )
                                            Text(
                                                text = "+${SalesViewModel.formatRupiah(tx.totalProfit)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = AppThemeColors.profitColor
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${strings.totalAmountDueLabel}:",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = SalesViewModel.formatRupiah(tx.totalAmountDue),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedTransaction != null) {
        ReceiptDialog(
            transaction = selectedTransaction!!.transaction,
            items = selectedTransaction!!.items,
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun DailyHeaderCard(group: DailyGroup) {
    val strings = LocalAppStrings.current
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = group.displayDate,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (group.isToday) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = strings.todayBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (group.isYesterday) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = strings.yesterdayBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${group.transactions.size} Stores",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-summary row for this day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rev: ${SalesViewModel.formatRupiah(group.totalRevenue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${strings.profitPrefix}: +${SalesViewModel.formatRupiah(group.totalProfit)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppThemeColors.profitColor
                    )
                }

                if (group.totalDebt > 0) {
                    Text(
                        text = "Debt: ${SalesViewModel.formatRupiah(group.totalDebt)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppThemeColors.debtColor
                    )
                }
            }
        }
    }
}
