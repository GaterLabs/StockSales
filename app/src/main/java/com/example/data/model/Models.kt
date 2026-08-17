package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unitName: String = "Pack", // e.g. Pack, Kaleng, Bungkus, Bal
    val packSize: Int = 10,        // e.g. 10 pcs per pack
    val costPrice: Double,         // Harga Modal / Beli Pabrik (e.g. 11,000)
    val sellPrice: Double,         // Harga Jual Sales ke Warung (e.g. 15,000)
    val retailPrice: Double = 0.0, // Harga Eceran Warung ke Konsumen (e.g. 2,000 / pc)
    val sku: String = "",
    val category: String = "Makanan Ringan",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,             // e.g. "Rute 1 - Minggu (Kemang & Cilandak)"
    val dayOfWeek: String,        // e.g. "Minggu", "Senin", "Selasa", dll.
    val areaDescription: String = "",
    val colorHex: String = "#0D9488",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stores",
    foreignKeys = [
        ForeignKey(
            entity = RouteEntity::class,
            parentColumns = ["id"],
            childColumns = ["routeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routeId")]
)
data class StoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: Long,
    val name: String,             // e.g. "Toko Berkah Bu Siti"
    val ownerName: String = "",   // e.g. "Ibu Siti"
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val orderIndex: Int = 0,
    val outstandingDebt: Double = 0.0, // Piutang / Bon yang belum dibayar
    val lastVisitedDate: Long? = null,
    val isVisitedToday: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Entity(
    tableName = "store_consignments",
    primaryKeys = ["storeId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId"), Index("productId")]
)
data class StoreConsignmentEntity(
    val storeId: Long,
    val productId: Long,
    val currentDroppedQuantity: Int, // Jumlah barang yang sedang dititip di warung
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "van_loads",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId"), Index("dateString")]
)
data class VanLoadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,        // format: "YYYY-MM-DD"
    val productId: Long,
    val initialLoadedQty: Int,    // Muatan pagi yang dibawa di motor/mobil
    val returnedQty: Int = 0,     // Sisa bawaan yang dibawa pulang sore
    val damagedQty: Int = 0,      // Barang rusak/pecah
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "visit_transactions",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId"), Index("routeId")]
)
data class VisitTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,     // e.g. "STR-20260817-001"
    val storeId: Long,
    val storeName: String,
    val routeId: Long,
    val routeName: String,
    val visitTimestamp: Long = System.currentTimeMillis(),
    val totalAmountDue: Double,    // Total nilai barang laku
    val amountPaid: Double,        // Uang yang dibayar ibu warung
    val previousDebtPaid: Double = 0.0, // Pembayaran hutang lama
    val newDebtAdded: Double = 0.0,     // Hutang baru yang dicatat
    val totalProfit: Double,       // Keuntungan salesman (Laba bersih)
    val totalItemsSold: Int,       // Total pack/pcs laku
    val paymentStatus: String = "LUNAS", // "LUNAS", "BON/TEMPO", "SEBAGIAN"
    val notes: String = ""
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = VisitTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("productId")]
)
data class TransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val unitName: String,
    val packSize: Int,
    val previousStock: Int,        // Stok dititip periode lalu (e.g. 10)
    val remainingStock: Int,       // Sisa di warung saat dicek (e.g. 3)
    val soldQuantity: Int,         // Terjual / Laku (e.g. 7)
    val newDroppedQuantity: Int,   // Tambahan stok baru yang dititipkan (e.g. 10)
    val costPrice: Double,         // Harga modal saat transaksi (e.g. 11,000)
    val sellPrice: Double,         // Harga jual ke warung saat transaksi (e.g. 15,000)
    val subtotalDue: Double,       // soldQuantity * sellPrice (e.g. 105,000)
    val subtotalProfit: Double     // soldQuantity * (sellPrice - costPrice) (e.g. 28,000)
)

@Entity(
    tableName = "van_returns",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId"), Index("productId"), Index("dateString")]
)
data class VanReturnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,        // format: "YYYY-MM-DD"
    val storeId: Long,
    val storeName: String,
    val productId: Long,
    val returnedQty: Int,          // Barang sisa yang dikembalikan dari warung
    val timestamp: Long = System.currentTimeMillis()
)
