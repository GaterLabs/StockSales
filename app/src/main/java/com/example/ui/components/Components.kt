package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TransactionItemEntity
import com.example.data.model.VisitTransactionEntity
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.DebtBadge
import com.example.ui.theme.ProfitBadge
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 9999,
    label: String = "",
    modifier: Modifier = Modifier
) {
    EditableNumberStepper(
        value = value,
        onValueChange = onValueChange,
        minValue = minValue,
        maxValue = maxValue,
        label = label,
        modifier = modifier
    )
}

@Composable
fun EditableNumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 9999,
    label: String = "",
    unit: String = "",
    modifier: Modifier = Modifier
) {
    var textInput by remember(value) { mutableStateOf(value.toString()) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilledTonalIconButton(
                onClick = {
                    if (value > minValue) {
                        val next = value - 1
                        textInput = next.toString()
                        onValueChange(next)
                    }
                },
                enabled = value > minValue,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .widthIn(min = 52.dp)
                    .height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    BasicTextField(
                        value = textInput,
                        onValueChange = { newStr ->
                            val digitsOnly = newStr.filter { it.isDigit() }
                            textInput = digitsOnly
                            val num = digitsOnly.toIntOrNull() ?: 0
                            val clamped = num.coerceIn(minValue, maxValue)
                            onValueChange(clamped)
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (textInput.isBlank()) {
                                    textInput = minValue.toString()
                                    onValueChange(minValue)
                                }
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.widthIn(min = 40.dp)
                    )
                }
            }

            FilledTonalIconButton(
                onClick = {
                    if (value < maxValue) {
                        val next = value + 1
                        textInput = next.toString()
                        onValueChange(next)
                    }
                },
                enabled = value < maxValue,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
            }

            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val successClr = AppThemeColors.successColor
    val debtClr = AppThemeColors.debtColor
    val warningClr = AppThemeColors.warningColor

    val (bg, fg, label) = when (status.uppercase()) {
        "PAID", "LUNAS", "SELESAI", "VISITED" -> Triple(successClr.copy(alpha = 0.15f), successClr, strings.paymentStatusPaid)
        "UNPAID", "BON/TEMPO", "HUTANG", "BELUM" -> Triple(debtClr.copy(alpha = 0.15f), debtClr, strings.paymentStatusUnpaid)
        "PARTIAL", "SEBAGIAN", "PENDING" -> Triple(warningClr.copy(alpha = 0.15f), warningClr, strings.paymentStatusPartial)
        else -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, status)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

enum class AgingLevel {
    NEW_STORE,      // Belum pernah dikunjungi (Titip Dasar)
    FRESH,          // 0-5 hari (Aman / Baru dititip)
    DUE_SOON,       // 6-7 hari (Waktunya Kunjungan / Ganti)
    OVERDUE         // > 7 hari (Lewat Jadwal)
}

data class StoreVisitAgingInfo(
    val daysAgo: Int?,
    val label: String,
    val level: AgingLevel
)

fun calculateStoreAging(lastVisitedDate: Long?, strings: AppStrings): StoreVisitAgingInfo {
    if (lastVisitedDate == null) {
        return StoreVisitAgingInfo(
            daysAgo = null,
            label = strings.ageNewStore,
            level = AgingLevel.NEW_STORE
        )
    }
    val diffMillis = System.currentTimeMillis() - lastVisitedDate
    val days = (diffMillis / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)

    return when {
        days == 0 -> StoreVisitAgingInfo(days, strings.ageToday, AgingLevel.FRESH)
        days == 1 -> StoreVisitAgingInfo(days, strings.ageYesterday, AgingLevel.FRESH)
        days in 2..5 -> StoreVisitAgingInfo(days, strings.ageDaysAgo(days), AgingLevel.FRESH)
        days in 6..7 -> StoreVisitAgingInfo(days, strings.ageDueToday, AgingLevel.DUE_SOON)
        else -> StoreVisitAgingInfo(days, strings.ageOverdue(days), AgingLevel.OVERDUE)
    }
}

@Composable
fun StoreVisitAgingBadge(
    lastVisitedDate: Long?,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val aging = remember(lastVisitedDate, strings) { calculateStoreAging(lastVisitedDate, strings) }

    val (bg, fg, icon) = when (aging.level) {
        AgingLevel.NEW_STORE -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.FiberNew
        )
        AgingLevel.FRESH -> Triple(
            AppThemeColors.successColor.copy(alpha = 0.15f),
            AppThemeColors.successColor,
            Icons.Default.AccessTime
        )
        AgingLevel.DUE_SOON -> Triple(
            AppThemeColors.warningColor.copy(alpha = 0.2f),
            AppThemeColors.warningColor,
            Icons.Default.Schedule
        )
        AgingLevel.OVERDUE -> Triple(
            AppThemeColors.debtColor.copy(alpha = 0.18f),
            AppThemeColors.debtColor,
            Icons.Default.Warning
        )
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = aging.label,
                color = fg,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StoreDistanceBadge(
    distanceMeters: Double?,
    isNextClosest: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (distanceMeters == null) return

    val formatted = if (distanceMeters < 1000.0) {
        "${distanceMeters.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
    }

    val (bg, fg) = when {
        isNextClosest -> Pair(
            AppThemeColors.successColor.copy(alpha = 0.2f),
            AppThemeColors.successColor
        )
        distanceMeters < 300.0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        border = if (isNextClosest) BorderStroke(1.dp, AppThemeColors.successColor.copy(alpha = 0.5f)) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isNextClosest) "📍 $formatted (Terdekat)" else "📍 $formatted",
                color = fg,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isNextClosest) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ReceiptDialog(
    transaction: VisitTransactionEntity,
    items: List<TransactionItemEntity>,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val locale = if (language.code == "id") Locale("in", "ID") else Locale.ENGLISH
    val dateStr = remember(language) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(transaction.visitTimestamp))
    }
    val profitClr = AppThemeColors.profitColor

    val whatsappText = buildString {
        appendLine("================================")
        appendLine("🧾 *${strings.receiptDialogTitle.uppercase()}*")
        appendLine("*SALESTRACK CONSIGNMENT*")
        appendLine("================================")
        appendLine("${strings.receiptNumberLabel} : ${transaction.receiptNumber}")
        appendLine("${strings.storeLabel}      : ${transaction.storeName}")
        appendLine("${strings.routeLabel}      : ${transaction.routeName}")
        appendLine("${strings.timeLabel}     : $dateStr")
        appendLine("--------------------------------")
        appendLine("*${strings.consignReconcileTitle.uppercase()}:*")
        items.forEachIndexed { idx, it ->
            val pSize = if (it.packSize > 0) it.packSize else 10
            val prevPackStr = if (pSize > 1) " (${it.previousStock / pSize} ${it.unitName})" else ""
            val endingStock = it.remainingStock + it.newDroppedQuantity
            val endPack = endingStock / pSize
            val endPcs = endingStock % pSize
            val endStr = if (endPack > 0 && endPcs > 0) "$endPack ${it.unitName} + $endPcs pcs" else if (endPack > 0) "$endPack ${it.unitName}" else "$endPcs pcs"

            appendLine("${idx + 1}. *${it.productName}*")
            appendLine("   ${strings.previousStock}: ${it.previousStock} pcs$prevPackStr | *${strings.remainingStock}: ${it.remainingStock} pcs*")
            appendLine("   *${strings.soldLabel.uppercase()}: ${it.soldQuantity} pcs* x ${SalesViewModel.formatRupiah(it.sellPrice)} = *${SalesViewModel.formatRupiah(it.subtotalDue)}*")
            appendLine("   Drop Baru: +${it.newDroppedQuantity} pcs | *Stok Ditinggal: $endingStock pcs ($endStr)*")
        }
        appendLine("--------------------------------")
        appendLine("${strings.totalAmountDueLabel} : *${SalesViewModel.formatRupiah(transaction.totalAmountDue)}*")
        if (transaction.previousDebtPaid > 0) {
            appendLine("Debt Paid          : *+${SalesViewModel.formatRupiah(transaction.previousDebtPaid)}*")
        }
        appendLine("${strings.amountPaidLabel}      : *${SalesViewModel.formatRupiah(transaction.amountPaid)}*")
        appendLine("${strings.paymentStatusLabel}  : *${transaction.paymentStatus}*")
        if (transaction.newDebtAdded > 0) {
            appendLine("${strings.remainingDebtLabel}    : *${SalesViewModel.formatRupiah(transaction.newDebtAdded)}*")
        }
        if (transaction.notes.isNotEmpty()) {
            appendLine("${strings.visitNotesLabel}: ${transaction.notes}")
        }
        appendLine("================================")
        appendLine("Thank you for your business! 🙏")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header Receipt Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = strings.receiptDialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    StatusBadge(status = transaction.paymentStatus)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Content in Mono Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.storeName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${transaction.receiptNumber} • $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(4.dp))

                        items.forEach { item ->
                            val pSize = if (item.packSize > 0) item.packSize else 10
                            val endingStock = item.remainingStock + item.newDroppedQuantity
                            val endPack = endingStock / pSize
                            val endPcs = endingStock % pSize
                            val endStr = if (endPack > 0 && endPcs > 0) "$endPack ${item.unitName} + $endPcs pcs" else if (endPack > 0) "$endPack ${item.unitName}" else "$endPcs pcs"

                            Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = SalesViewModel.formatRupiah(item.subtotalDue),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Titip Lalu: ${item.previousStock} pcs | Sisa: ${item.remainingStock} pcs -> Laku: ${item.soldQuantity} pcs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Drop Baru: +${item.newDroppedQuantity} pcs | Total Stok Ditinggal: $endingStock pcs ($endStr)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.totalAmountDueLabel}:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = SalesViewModel.formatRupiah(transaction.totalAmountDue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.totalProfitStat}:",
                                style = MaterialTheme.typography.bodySmall,
                                color = profitClr
                            )
                            Text(
                                text = "+${SalesViewModel.formatRupiah(transaction.totalProfit)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = profitClr
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${strings.amountPaidLabel}:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = SalesViewModel.formatRupiah(transaction.amountPaid),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(whatsappText))
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, whatsappText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, strings.btnSendWhatsapp))
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnSendWhatsapp, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Text(strings.btnClose, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
