package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val routesCount: Int = 0,
    val storesCount: Int = 0,
    val productsCount: Int = 0,
    val transactionsCount: Int = 0
)

object BackupRestoreUtil {

    private const val BACKUP_VERSION = 1
    private const val BACKUP_FILE_PREFIX = "salestrack_backup_"

    /**
     * Serializes entire local Room database into a structured JSON string.
     */
    suspend fun createBackupJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val rootObj = JSONObject()
        rootObj.put("appName", "Stock Sales")
        rootObj.put("backupVersion", BACKUP_VERSION)
        rootObj.put("timestamp", System.currentTimeMillis())
        rootObj.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // 1. Products
        val products = database.productDao().getAllProductsSnapshot()
        val productsArray = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("unitName", p.unitName)
            obj.put("packSize", p.packSize)
            obj.put("costPrice", p.costPrice)
            obj.put("sellPrice", p.sellPrice)
            obj.put("retailPrice", p.retailPrice)
            obj.put("sku", p.sku)
            obj.put("category", p.category)
            obj.put("isActive", p.isActive)
            obj.put("createdAt", p.createdAt)
            productsArray.put(obj)
        }
        rootObj.put("products", productsArray)

        // 2. Routes
        val routes = database.routeDao().getAllRoutesSnapshot()
        val routesArray = JSONArray()
        routes.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("dayOfWeek", r.dayOfWeek)
            obj.put("areaDescription", r.areaDescription)
            obj.put("colorHex", r.colorHex)
            obj.put("sortOrder", r.sortOrder)
            obj.put("createdAt", r.createdAt)
            routesArray.put(obj)
        }
        rootObj.put("routes", routesArray)

        // 3. Stores
        val stores = database.storeDao().getAllStoresSnapshot()
        val storesArray = JSONArray()
        stores.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("routeId", s.routeId)
            obj.put("name", s.name)
            obj.put("ownerName", s.ownerName)
            obj.put("phone", s.phone)
            obj.put("address", s.address)
            obj.put("notes", s.notes)
            obj.put("orderIndex", s.orderIndex)
            obj.put("outstandingDebt", s.outstandingDebt)
            obj.put("lastVisitedDate", s.lastVisitedDate ?: -1L)
            obj.put("isVisitedToday", s.isVisitedToday)
            if (s.latitude != null) obj.put("latitude", s.latitude)
            if (s.longitude != null) obj.put("longitude", s.longitude)
            storesArray.put(obj)
        }
        rootObj.put("stores", storesArray)

        // 4. Store Consignments
        val consignments = database.consignmentDao().getAllConsignmentsSnapshot()
        val consignmentsArray = JSONArray()
        consignments.forEach { c ->
            val obj = JSONObject()
            obj.put("storeId", c.storeId)
            obj.put("productId", c.productId)
            obj.put("currentDroppedQuantity", c.currentDroppedQuantity)
            obj.put("lastUpdated", c.lastUpdated)
            consignmentsArray.put(obj)
        }
        rootObj.put("consignments", consignmentsArray)

        // 5. Van Loads
        val vanLoads = database.vanLoadDao().getAllVanLoadsSnapshot()
        val vanLoadsArray = JSONArray()
        vanLoads.forEach { vl ->
            val obj = JSONObject()
            obj.put("id", vl.id)
            obj.put("dateString", vl.dateString)
            obj.put("productId", vl.productId)
            obj.put("initialLoadedQty", vl.initialLoadedQty)
            obj.put("returnedQty", vl.returnedQty)
            obj.put("damagedQty", vl.damagedQty)
            obj.put("notes", vl.notes)
            obj.put("updatedAt", vl.updatedAt)
            vanLoadsArray.put(obj)
        }
        rootObj.put("vanLoads", vanLoadsArray)

        // 6. Visit Transactions
        val transactions = database.transactionDao().getAllTransactionsSnapshot()
        val transactionsArray = JSONArray()
        transactions.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("receiptNumber", t.receiptNumber)
            obj.put("storeId", t.storeId)
            obj.put("storeName", t.storeName)
            obj.put("routeId", t.routeId)
            obj.put("routeName", t.routeName)
            obj.put("visitTimestamp", t.visitTimestamp)
            obj.put("totalAmountDue", t.totalAmountDue)
            obj.put("amountPaid", t.amountPaid)
            obj.put("previousDebtPaid", t.previousDebtPaid)
            obj.put("newDebtAdded", t.newDebtAdded)
            obj.put("totalProfit", t.totalProfit)
            obj.put("totalItemsSold", t.totalItemsSold)
            obj.put("paymentStatus", t.paymentStatus)
            obj.put("notes", t.notes)
            transactionsArray.put(obj)
        }
        rootObj.put("transactions", transactionsArray)

        // 7. Transaction Items
        val items = database.transactionDao().getAllTransactionItemsSnapshot()
        val itemsArray = JSONArray()
        items.forEach { ti ->
            val obj = JSONObject()
            obj.put("id", ti.id)
            obj.put("transactionId", ti.transactionId)
            obj.put("productId", ti.productId)
            obj.put("productName", ti.productName)
            obj.put("unitName", ti.unitName)
            obj.put("packSize", ti.packSize)
            obj.put("previousStock", ti.previousStock)
            obj.put("remainingStock", ti.remainingStock)
            obj.put("soldQuantity", ti.soldQuantity)
            obj.put("newDroppedQuantity", ti.newDroppedQuantity)
            obj.put("costPrice", ti.costPrice)
            obj.put("sellPrice", ti.sellPrice)
            obj.put("subtotalDue", ti.subtotalDue)
            obj.put("subtotalProfit", ti.subtotalProfit)
            itemsArray.put(obj)
        }
        rootObj.put("transactionItems", itemsArray)

        rootObj.toString(2)
    }

    /**
     * Saves backup to a temporary cache file and returns a shareable File.
     */
    suspend fun saveBackupToCacheFile(context: Context, database: AppDatabase): File = withContext(Dispatchers.IO) {
        val json = createBackupJson(database)
        val backupDir = File(context.cacheDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileName = "${BACKUP_FILE_PREFIX}${sdf.format(Date())}.json"
        val file = File(backupDir, fileName)
        file.writeText(json, Charsets.UTF_8)
        file
    }

    /**
     * Writes backup directly to an OutputStream (for SAF CreateDocument picker).
     */
    suspend fun writeBackupToUri(context: Context, uri: Uri, database: AppDatabase) = withContext(Dispatchers.IO) {
        val json = createBackupJson(database)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(json.toByteArray(Charsets.UTF_8))
            outputStream.flush()
        }
    }

    /**
     * Shares backup JSON file via Intent (WhatsApp, Drive, Email, Bluetooth, etc.).
     */
    fun shareBackupFile(context: Context, backupFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SalesTrack Database Backup")
            putExtra(
                Intent.EXTRA_TEXT,
                "File cadangan database SalesTrack Konsinyasi. Simpan file ini untuk memindahkan data ke HP baru atau arsip cadangan."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan / Simpan File Cadangan via:")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Reads entire JSON string from a content Uri (picked by user via SAF).
     */
    suspend fun readJsonFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        } ?: throw IllegalArgumentException("Tidak dapat membuka file yang dipilih")
    }

    /**
     * Restores database from a JSON backup string.
     * Uses database.withTransaction to guarantee atomicity.
     */
    suspend fun restoreDatabaseFromJson(
        jsonString: String,
        database: AppDatabase
    ): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val rootObj = JSONObject(jsonString)

            // Validate root structure
            val appName = rootObj.optString("appName", "")
            if (appName != "Stock Sales" && appName != "SalesTrack") {
                return@withContext RestoreResult(
                    success = false,
                    message = "Format file tidak valid. File ini bukan file cadangan Stock Sales."
                )
            }

            // Parse Products
            val productsList = mutableListOf<ProductEntity>()
            val productsArr = rootObj.optJSONArray("products") ?: JSONArray()
            for (i in 0 until productsArr.length()) {
                val obj = productsArr.getJSONObject(i)
                productsList.add(
                    ProductEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "Produk"),
                        unitName = obj.optString("unitName", "Pack"),
                        packSize = obj.optInt("packSize", 10),
                        costPrice = obj.optDouble("costPrice", 0.0),
                        sellPrice = obj.optDouble("sellPrice", 0.0),
                        retailPrice = obj.optDouble("retailPrice", 0.0),
                        sku = obj.optString("sku", ""),
                        category = obj.optString("category", "Makanan Ringan"),
                        isActive = obj.optBoolean("isActive", true),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Routes
            val routesList = mutableListOf<RouteEntity>()
            val routesArr = rootObj.optJSONArray("routes") ?: JSONArray()
            for (i in 0 until routesArr.length()) {
                val obj = routesArr.getJSONObject(i)
                routesList.add(
                    RouteEntity(
                        id = obj.optLong("id", 0L),
                        name = obj.optString("name", "Rute"),
                        dayOfWeek = obj.optString("dayOfWeek", "Senin"),
                        areaDescription = obj.optString("areaDescription", ""),
                        colorHex = obj.optString("colorHex", "#0D9488"),
                        sortOrder = obj.optInt("sortOrder", i),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Stores
            val storesList = mutableListOf<StoreEntity>()
            val storesArr = rootObj.optJSONArray("stores") ?: JSONArray()
            for (i in 0 until storesArr.length()) {
                val obj = storesArr.getJSONObject(i)
                val lastVisited = obj.optLong("lastVisitedDate", -1L)
                storesList.add(
                    StoreEntity(
                        id = obj.optLong("id", 0L),
                        routeId = obj.optLong("routeId", 1L),
                        name = obj.optString("name", "Warung"),
                        ownerName = obj.optString("ownerName", ""),
                        phone = obj.optString("phone", ""),
                        address = obj.optString("address", ""),
                        notes = obj.optString("notes", ""),
                        orderIndex = obj.optInt("orderIndex", i),
                        outstandingDebt = obj.optDouble("outstandingDebt", 0.0),
                        lastVisitedDate = if (lastVisited > 0) lastVisited else null,
                        isVisitedToday = obj.optBoolean("isVisitedToday", false),
                        latitude = if (obj.has("latitude")) obj.optDouble("latitude") else null,
                        longitude = if (obj.has("longitude")) obj.optDouble("longitude") else null
                    )
                )
            }

            // Parse Consignments
            val consignmentsList = mutableListOf<StoreConsignmentEntity>()
            val consignmentsArr = rootObj.optJSONArray("consignments") ?: JSONArray()
            for (i in 0 until consignmentsArr.length()) {
                val obj = consignmentsArr.getJSONObject(i)
                consignmentsList.add(
                    StoreConsignmentEntity(
                        storeId = obj.optLong("storeId", 0L),
                        productId = obj.optLong("productId", 0L),
                        currentDroppedQuantity = obj.optInt("currentDroppedQuantity", 0),
                        lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                    )
                )
            }

            // Parse Van Loads
            val vanLoadsList = mutableListOf<VanLoadEntity>()
            val vanLoadsArr = rootObj.optJSONArray("vanLoads") ?: JSONArray()
            for (i in 0 until vanLoadsArr.length()) {
                val obj = vanLoadsArr.getJSONObject(i)
                vanLoadsList.add(
                    VanLoadEntity(
                        id = obj.optLong("id", 0L),
                        dateString = obj.optString("dateString", ""),
                        productId = obj.optLong("productId", 0L),
                        initialLoadedQty = obj.optInt("initialLoadedQty", 0),
                        returnedQty = obj.optInt("returnedQty", 0),
                        damagedQty = obj.optInt("damagedQty", 0),
                        notes = obj.optString("notes", ""),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Transactions
            val txList = mutableListOf<VisitTransactionEntity>()
            val txArr = rootObj.optJSONArray("transactions") ?: JSONArray()
            for (i in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(i)
                txList.add(
                    VisitTransactionEntity(
                        id = obj.optLong("id", 0L),
                        receiptNumber = obj.optString("receiptNumber", "STR-$i"),
                        storeId = obj.optLong("storeId", 0L),
                        storeName = obj.optString("storeName", ""),
                        routeId = obj.optLong("routeId", 0L),
                        routeName = obj.optString("routeName", ""),
                        visitTimestamp = obj.optLong("visitTimestamp", System.currentTimeMillis()),
                        totalAmountDue = obj.optDouble("totalAmountDue", 0.0),
                        amountPaid = obj.optDouble("amountPaid", 0.0),
                        previousDebtPaid = obj.optDouble("previousDebtPaid", 0.0),
                        newDebtAdded = obj.optDouble("newDebtAdded", 0.0),
                        totalProfit = obj.optDouble("totalProfit", 0.0),
                        totalItemsSold = obj.optInt("totalItemsSold", 0),
                        paymentStatus = obj.optString("paymentStatus", "LUNAS"),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            // Parse Transaction Items
            val itemsList = mutableListOf<TransactionItemEntity>()
            val itemsArr = rootObj.optJSONArray("transactionItems") ?: JSONArray()
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                itemsList.add(
                    TransactionItemEntity(
                        id = obj.optLong("id", 0L),
                        transactionId = obj.optLong("transactionId", 0L),
                        productId = obj.optLong("productId", 0L),
                        productName = obj.optString("productName", ""),
                        unitName = obj.optString("unitName", "Pack"),
                        packSize = obj.optInt("packSize", 10),
                        previousStock = obj.optInt("previousStock", 0),
                        remainingStock = obj.optInt("remainingStock", 0),
                        soldQuantity = obj.optInt("soldQuantity", 0),
                        newDroppedQuantity = obj.optInt("newDroppedQuantity", 0),
                        costPrice = obj.optDouble("costPrice", 0.0),
                        sellPrice = obj.optDouble("sellPrice", 0.0),
                        subtotalDue = obj.optDouble("subtotalDue", 0.0),
                        subtotalProfit = obj.optDouble("subtotalProfit", 0.0)
                    )
                )
            }

            // Atomically replace all database contents
            database.withTransaction {
                // Delete existing records
                database.transactionDao().deleteAllTransactionItems()
                database.transactionDao().deleteAllTransactions()
                database.vanLoadDao().deleteAllVanLoads()
                database.consignmentDao().deleteAllConsignments()
                database.storeDao().deleteAllStores()
                database.routeDao().deleteAllRoutes()
                database.productDao().deleteAllProducts()

                // Re-insert imported records
                if (productsList.isNotEmpty()) database.productDao().insertProducts(productsList)
                if (routesList.isNotEmpty()) database.routeDao().insertRoutes(routesList)
                if (storesList.isNotEmpty()) database.storeDao().insertStores(storesList)
                if (consignmentsList.isNotEmpty()) database.consignmentDao().insertConsignments(consignmentsList)
                if (vanLoadsList.isNotEmpty()) database.vanLoadDao().insertLoads(vanLoadsList)
                if (txList.isNotEmpty()) database.transactionDao().insertTransactions(txList)
                if (itemsList.isNotEmpty()) database.transactionDao().insertTransactionItems(itemsList)
            }

            RestoreResult(
                success = true,
                message = "Data berhasil dipulihkan secara penuh!",
                routesCount = routesList.size,
                storesCount = storesList.size,
                productsCount = productsList.size,
                transactionsCount = txList.size
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                message = "Gagal memulihkan database: ${e.localizedMessage}"
            )
        }
    }
}
