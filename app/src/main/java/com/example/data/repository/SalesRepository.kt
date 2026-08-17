package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SalesRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val routeDao = database.routeDao()
    private val storeDao = database.storeDao()
    private val consignmentDao = database.consignmentDao()
    private val vanLoadDao = database.vanLoadDao()
    private val transactionDao = database.transactionDao()

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getAllActiveProducts()

    suspend fun saveProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
            product.id
        }
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    // Routes
    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutes()

    suspend fun saveRoute(route: RouteEntity): Long = withContext(Dispatchers.IO) {
        if (route.id == 0L) {
            routeDao.insertRoute(route)
        } else {
            routeDao.updateRoute(route)
            route.id
        }
    }

    suspend fun deleteRoute(route: RouteEntity) = withContext(Dispatchers.IO) {
        routeDao.deleteRoute(route)
    }

    // Stores
    val allStores: Flow<List<StoreEntity>> = storeDao.getAllStores()

    fun getStoresByRoute(routeId: Long): Flow<List<StoreEntity>> =
        storeDao.getStoresByRoute(routeId)

    suspend fun saveStore(store: StoreEntity): Long = withContext(Dispatchers.IO) {
        if (store.id == 0L) {
            storeDao.insertStore(store)
        } else {
            storeDao.updateStore(store)
            store.id
        }
    }

    suspend fun deleteStore(store: StoreEntity) = withContext(Dispatchers.IO) {
        storeDao.deleteStore(store)
    }

    suspend fun resetDailyVisitStatus() = withContext(Dispatchers.IO) {
        storeDao.resetDailyVisitStatus()
    }

    // Consignments
    val allConsignmentDetails: Flow<List<ConsignmentProductDetail>> = consignmentDao.getAllConsignmentDetails()

    fun getConsignmentsForStore(storeId: Long): Flow<List<ConsignmentProductDetail>> =
        consignmentDao.getConsignmentDetailsForStore(storeId)

    val totalFieldConsignmentQty: Flow<Int?> = consignmentDao.getTotalFieldConsignmentQty()
    val fieldStockSummary: Flow<List<ProductFieldStockSummary>> = consignmentDao.getFieldStockSummaryByProduct()

    suspend fun addOrUpdateConsignment(storeId: Long, productId: Long, qty: Int) = withContext(Dispatchers.IO) {
        consignmentDao.insertOrUpdateConsignment(
            StoreConsignmentEntity(
                storeId = storeId,
                productId = productId,
                currentDroppedQuantity = qty
            )
        )
    }

    suspend fun removeConsignment(storeId: Long, productId: Long) = withContext(Dispatchers.IO) {
        consignmentDao.deleteConsignment(storeId, productId)
    }

    // Van Cargo Loads
    fun getVanLoadsForDate(dateStr: String): Flow<List<VanLoadEntity>> =
        vanLoadDao.getLoadsForDate(dateStr)

    suspend fun saveVanLoad(load: VanLoadEntity): Long = withContext(Dispatchers.IO) {
        vanLoadDao.insertOrUpdateLoad(load)
    }

    suspend fun updateVanLoadReturn(id: Long, returned: Int, damaged: Int) = withContext(Dispatchers.IO) {
        vanLoadDao.updateVanLoadReturn(id, returned, damaged, System.currentTimeMillis())
    }

    suspend fun deleteVanLoad(load: VanLoadEntity) = withContext(Dispatchers.IO) {
        vanLoadDao.deleteVanLoad(load)
    }

    // Transactions & Analytics
    val allTransactions: Flow<List<TransactionWithItems>> = transactionDao.getAllTransactionsWithItems()
    val totalRevenue: Flow<Double?> = transactionDao.getTotalRevenue()
    val totalProfit: Flow<Double?> = transactionDao.getTotalProfit()
    val totalItemsSold: Flow<Int?> = transactionDao.getTotalItemsSold()

    fun getTransactionsByRoute(routeId: Long): Flow<List<TransactionWithItems>> =
        transactionDao.getTransactionsByRoute(routeId)

    fun getTransactionsByStore(storeId: Long): Flow<List<TransactionWithItems>> =
        transactionDao.getTransactionsByStore(storeId)

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionWithItems>> =
        transactionDao.getTransactionsByDateRange(start, end)

    /**
     * Executes the automatic consignment reconciliation transaction:
     * 1. Records the visit transaction & individual item breakdowns (Previous, Remaining, Sold, Subtotal, Profit, New Dropped)
     * 2. Updates active store consignment quantities for the next visit
     * 3. Updates store outstanding debt and marked visit status
     */
    suspend fun executeVisitReconciliation(
        store: StoreEntity,
        route: RouteEntity,
        reconciledItems: List<ReconciliationItemInput>,
        amountPaid: Double,
        previousDebtPaid: Double,
        paymentStatus: String,
        notes: String
    ): VisitTransactionEntity = withContext(Dispatchers.IO) {
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomDigits = (100..999).random()
        val receiptNumber = "STR-$dateCode-$randomDigits"

        val totalAmountDue = reconciledItems.sumOf { it.soldQty * it.sellPrice }
        val totalProfit = reconciledItems.sumOf { it.soldQty * (it.sellPrice - it.costPrice) }
        val totalItemsSold = reconciledItems.sumOf { it.soldQty }

        val totalOwedBeforePayment = store.outstandingDebt + totalAmountDue
        val totalPaid = amountPaid.coerceAtLeast(0.0)
        val updatedDebt = (totalOwedBeforePayment - totalPaid).coerceAtLeast(0.0)

        // Break down payment between past debt & current sales for receipt reporting
        val actualOldDebtPaid = if (previousDebtPaid > 0) minOf(store.outstandingDebt, previousDebtPaid) else minOf(store.outstandingDebt, totalPaid)
        val paymentForNewSales = (totalPaid - actualOldDebtPaid).coerceAtLeast(0.0)
        val netNewDebt = (totalAmountDue - paymentForNewSales).coerceAtLeast(0.0)

        val transaction = VisitTransactionEntity(
            receiptNumber = receiptNumber,
            storeId = store.id,
            storeName = store.name,
            routeId = route.id,
            routeName = route.name,
            visitTimestamp = System.currentTimeMillis(),
            totalAmountDue = totalAmountDue,
            amountPaid = totalPaid,
            previousDebtPaid = actualOldDebtPaid,
            newDebtAdded = netNewDebt,
            totalProfit = totalProfit,
            totalItemsSold = totalItemsSold,
            paymentStatus = paymentStatus,
            notes = notes
        )

        val transactionId = transactionDao.insertTransaction(transaction)

        val itemEntities = reconciledItems.map { item ->
            TransactionItemEntity(
                transactionId = transactionId,
                productId = item.productId,
                productName = item.productName,
                unitName = item.unitName,
                packSize = item.packSize,
                previousStock = item.previousStock,
                remainingStock = item.remainingStock,
                soldQuantity = item.soldQty,
                newDroppedQuantity = item.newDroppedQty,
                costPrice = item.costPrice,
                sellPrice = item.sellPrice,
                subtotalDue = item.soldQty * item.sellPrice,
                subtotalProfit = item.soldQty * (item.sellPrice - item.costPrice)
            )
        }
        transactionDao.insertTransactionItems(itemEntities)

        // Update Store Consignments for next visit:
        // The total stock at the warung now becomes: (remaining stock + newly dropped stock)
        for (item in reconciledItems) {
            val nextTotalStock = item.remainingStock + item.newDroppedQty
            if (nextTotalStock > 0) {
                consignmentDao.insertOrUpdateConsignment(
                    StoreConsignmentEntity(
                        storeId = store.id,
                        productId = item.productId,
                        currentDroppedQuantity = nextTotalStock,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            } else {
                // If completely cleared out
                consignmentDao.deleteConsignment(store.id, item.productId)
            }
        }

        // Update Store Debt & Status
        storeDao.updateStoreDebt(store.id, updatedDebt)
        storeDao.updateStoreVisitStatus(store.id, System.currentTimeMillis(), true)

        transaction.copy(id = transactionId)
    }
}

data class ReconciliationItemInput(
    val productId: Long,
    val productName: String,
    val unitName: String,
    val packSize: Int,
    val previousStock: Int,
    val remainingStock: Int,
    val soldQty: Int,
    val newDroppedQty: Int,
    val costPrice: Double,
    val sellPrice: Double
)
