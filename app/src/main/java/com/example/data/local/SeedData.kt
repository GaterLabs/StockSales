package com.example.data.local

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*

object SeedData {
    suspend fun populateInitialData(database: AppDatabase) {
        val productDao = database.productDao()
        val routeDao = database.routeDao()
        val storeDao = database.storeDao()
        val consignmentDao = database.consignmentDao()
        val vanLoadDao = database.vanLoadDao()
        val transactionDao = database.transactionDao()

        // 1. Initial Products
        val products = listOf(
            ProductEntity(
                name = "Kerupuk Ikan Renyah",
                unitName = "Pack",
                packSize = 10,
                costPrice = 11000.0,
                sellPrice = 15000.0,
                retailPrice = 2000.0,
                sku = "KRP-IKAN-10",
                category = "Kerupuk & Keripik"
            ),
            ProductEntity(
                name = "Makaroni Pedas Balado",
                unitName = "Pack",
                packSize = 12,
                costPrice = 14000.0,
                sellPrice = 18000.0,
                retailPrice = 2000.0,
                sku = "MKN-PEDAS-12",
                category = "Snack Pedas"
            ),
            ProductEntity(
                name = "Keripik Singkong Balado",
                unitName = "Pack",
                packSize = 10,
                costPrice = 12000.0,
                sellPrice = 16000.0,
                retailPrice = 2000.0,
                sku = "KRP-SINGKONG-10",
                category = "Kerupuk & Keripik"
            ),
            ProductEntity(
                name = "Basreng Daun Jeruk Pedas",
                unitName = "Pack",
                packSize = 10,
                costPrice = 13000.0,
                sellPrice = 17000.0,
                retailPrice = 2000.0,
                sku = "BSR-JERUK-10",
                category = "Snack Pedas"
            ),
            ProductEntity(
                name = "Kacang Bawang Super Gurih",
                unitName = "Pack",
                packSize = 15,
                costPrice = 18000.0,
                sellPrice = 23000.0,
                retailPrice = 2000.0,
                sku = "KCG-BWG-15",
                category = "Kacang-kacangan"
            )
        )
        productDao.insertProducts(products)

        // 2. Initial Routes (5-7 Routes weekly)
        val routes = listOf(
            RouteEntity(
                name = "Rute 1 - Minggu (Kemang & Cipete)",
                dayOfWeek = "Minggu",
                areaDescription = "Area Jl. Kemang Raya, Cipete Utara & Selatan",
                colorHex = "#0D9488",
                sortOrder = 0
            ),
            RouteEntity(
                name = "Rute 2 - Senin (Pasar Minggu & Pejaten)",
                dayOfWeek = "Senin",
                areaDescription = "Area Pasar Minggu, Ragunan, Pejaten Barat",
                colorHex = "#2563EB",
                sortOrder = 1
            ),
            RouteEntity(
                name = "Rute 3 - Selasa (Tebet & Manggarai)",
                dayOfWeek = "Selasa",
                areaDescription = "Area Tebet Barat, Tebet Timur, Bukit Duri",
                colorHex = "#7C3AED",
                sortOrder = 2
            ),
            RouteEntity(
                name = "Rute 4 - Rabu (Kuningan & Mampang)",
                dayOfWeek = "Rabu",
                areaDescription = "Area Mampang Prapatan, Tegal Parang, Duren Tiga",
                colorHex = "#D97706",
                sortOrder = 3
            ),
            RouteEntity(
                name = "Rute 5 - Kamis (Fatmawati & Pondok Indah)",
                dayOfWeek = "Kamis",
                areaDescription = "Area RS Fatmawati, Cilandak Barat, Pondok Pinang",
                colorHex = "#059669",
                sortOrder = 4
            ),
            RouteEntity(
                name = "Rute 6 - Jumat (Gandaria & Kebayoran)",
                dayOfWeek = "Jumat",
                areaDescription = "Area Gandaria Selatan, Kramat Pela, Radio Dalam",
                colorHex = "#DC2626",
                sortOrder = 5
            ),
            RouteEntity(
                name = "Rute 7 - Sabtu (Jagakarsa & Lenteng)",
                dayOfWeek = "Sabtu",
                areaDescription = "Area Ciganjur, Srengseng Sawah, Lenteng Agung",
                colorHex = "#4F46E5",
                sortOrder = 6
            )
        )
        routeDao.insertRoutes(routes)

        // 3. Initial Stores for Route 1 (Minggu) & Route 2 (Senin)
        val stores = listOf(
            // Route 1 Stores (id = 1)
            StoreEntity(
                routeId = 1,
                name = "Warung Berkah Ibu Siti",
                ownerName = "Ibu Siti",
                phone = "0812-3456-7890",
                address = "Jl. Kemang Timur No. 14 RT 03/02",
                notes = "Titip di rak depan dekat kasir. Bayar selalu tunai tepat waktu.",
                orderIndex = 0,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            ),
            StoreEntity(
                routeId = 1,
                name = "Toko Kelontong Bu Hj. Maryam",
                ownerName = "Hj. Maryam",
                phone = "0813-8877-6655",
                address = "Jl. Cipete Raya No. 45B",
                notes = "Suka minta drop kerupuk banyak saat akhir pekan.",
                orderIndex = 1,
                outstandingDebt = 25000.0,
                lastVisitedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            ),
            StoreEntity(
                routeId = 1,
                name = "Kios Madura Berkah 24 Jam",
                ownerName = "Cak Salim",
                phone = "0857-1122-3344",
                address = "Simpang Tiga Jl. Kemang Selatan",
                notes = "Buka 24 jam, perputaran snack basreng & makaroni sangat cepat.",
                orderIndex = 2,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            ),
            StoreEntity(
                routeId = 1,
                name = "Warung Sembako Barokah Pak Joko",
                ownerName = "Pak Joko",
                phone = "0819-4455-6677",
                address = "Jl. Antasari Bawah Gg. Kancil",
                notes = "Akses motor mudah, langganan keripik singkong.",
                orderIndex = 3,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            ),

            // Route 2 Stores (id = 2)
            StoreEntity(
                routeId = 2,
                name = "Toko Sumber Rejeki Bu Endang",
                ownerName = "Bu Endang",
                phone = "0815-9988-7711",
                address = "Jl. Pasar Minggu Raya No. 88",
                notes = "Dekat stasiun, ramai pembeli komuter.",
                orderIndex = 0,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000L
            ),
            StoreEntity(
                routeId = 2,
                name = "Warung Bu Nur Pejaten",
                ownerName = "Bu Nur",
                phone = "0821-3344-5566",
                address = "Jl. Pejaten Barat II No. 12",
                notes = "Titip kerupuk kaleng dan basreng.",
                orderIndex = 1,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000L
            ),

            // Route 3 Stores (id = 3)
            StoreEntity(
                routeId = 3,
                name = "Toko Manunggal Tebet",
                ownerName = "Pak Heru",
                phone = "0818-7766-5544",
                address = "Jl. Tebet Barat Dalam Raya No. 20",
                notes = "Samping fotokopi, anak sekolah sering jajan.",
                orderIndex = 0,
                outstandingDebt = 0.0,
                lastVisitedDate = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L
            )
        )
        storeDao.insertStores(stores)

        // 4. Initial Consignments (Titipan barang yang sedang ada di warung)
        // For Warung Bu Siti (Store ID 1): Exactly 10 packs of Kerupuk Ikan & 5 packs Makaroni
        val consignments = listOf(
            StoreConsignmentEntity(storeId = 1, productId = 1, currentDroppedQuantity = 10), // Kerupuk 10 pack
            StoreConsignmentEntity(storeId = 1, productId = 2, currentDroppedQuantity = 6),  // Makaroni 6 pack
            StoreConsignmentEntity(storeId = 1, productId = 4, currentDroppedQuantity = 5),  // Basreng 5 pack

            StoreConsignmentEntity(storeId = 2, productId = 1, currentDroppedQuantity = 15), // Bu Maryam
            StoreConsignmentEntity(storeId = 2, productId = 3, currentDroppedQuantity = 8),

            StoreConsignmentEntity(storeId = 3, productId = 2, currentDroppedQuantity = 12), // Kios Madura
            StoreConsignmentEntity(storeId = 3, productId = 4, currentDroppedQuantity = 10),

            StoreConsignmentEntity(storeId = 4, productId = 1, currentDroppedQuantity = 8),
            StoreConsignmentEntity(storeId = 4, productId = 3, currentDroppedQuantity = 6),

            StoreConsignmentEntity(storeId = 5, productId = 1, currentDroppedQuantity = 12),
            StoreConsignmentEntity(storeId = 5, productId = 5, currentDroppedQuantity = 8),

            StoreConsignmentEntity(storeId = 6, productId = 2, currentDroppedQuantity = 10),
            StoreConsignmentEntity(storeId = 7, productId = 1, currentDroppedQuantity = 10)
        )
        consignmentDao.insertConsignments(consignments)

        // 5. Initial Van / Motorcycle Cargo Load for Today (Muatan Bawaan Salesman)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val vanLoads = listOf(
            VanLoadEntity(dateString = todayStr, productId = 1, initialLoadedQty = 50, notes = "Muatan pagi kerupuk"),
            VanLoadEntity(dateString = todayStr, productId = 2, initialLoadedQty = 30, notes = "Muatan pagi makaroni"),
            VanLoadEntity(dateString = todayStr, productId = 3, initialLoadedQty = 25, notes = "Muatan pagi singkong"),
            VanLoadEntity(dateString = todayStr, productId = 4, initialLoadedQty = 30, notes = "Muatan pagi basreng"),
            VanLoadEntity(dateString = todayStr, productId = 5, initialLoadedQty = 20, notes = "Muatan pagi kacang")
        )
        vanLoadDao.insertLoads(vanLoads)

        // 6. Sample past transactions (to immediately provide analytics & financial metrics)
        val sampleTxId1 = transactionDao.insertTransaction(
            VisitTransactionEntity(
                receiptNumber = "STR-20260810-001",
                storeId = 1,
                storeName = "Warung Berkah Ibu Siti",
                routeId = 1,
                routeName = "Rute 1 - Minggu (Kemang & Cipete)",
                visitTimestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                totalAmountDue = 145000.0,
                amountPaid = 145000.0,
                totalProfit = 38000.0,
                totalItemsSold = 10,
                paymentStatus = "LUNAS",
                notes = "Kunjungan mingguan pertama bulan ini"
            )
        )
        transactionDao.insertTransactionItems(
            listOf(
                TransactionItemEntity(
                    transactionId = sampleTxId1,
                    productId = 1,
                    productName = "Kerupuk Ikan Renyah",
                    unitName = "Pack",
                    packSize = 10,
                    previousStock = 10,
                    remainingStock = 3,
                    soldQuantity = 7,
                    newDroppedQuantity = 10,
                    costPrice = 11000.0,
                    sellPrice = 15000.0,
                    subtotalDue = 105000.0,
                    subtotalProfit = 28000.0
                ),
                TransactionItemEntity(
                    transactionId = sampleTxId1,
                    productId = 2,
                    productName = "Makaroni Pedas Balado",
                    unitName = "Pack",
                    packSize = 12,
                    previousStock = 5,
                    remainingStock = 2,
                    soldQuantity = 3,
                    newDroppedQuantity = 6,
                    costPrice = 14000.0,
                    sellPrice = 18000.0,
                    subtotalDue = 54000.0,
                    subtotalProfit = 12000.0
                )
            )
        )

        val sampleTxId2 = transactionDao.insertTransaction(
            VisitTransactionEntity(
                receiptNumber = "STR-20260811-002",
                storeId = 5,
                storeName = "Toko Sumber Rejeki Bu Endang",
                routeId = 2,
                routeName = "Rute 2 - Senin (Pasar Minggu & Pejaten)",
                visitTimestamp = System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000L,
                totalAmountDue = 180000.0,
                amountPaid = 180000.0,
                totalProfit = 48000.0,
                totalItemsSold = 12,
                paymentStatus = "LUNAS",
                notes = "Laris manis"
            )
        )
        transactionDao.insertTransactionItems(
            listOf(
                TransactionItemEntity(
                    transactionId = sampleTxId2,
                    productId = 1,
                    productName = "Kerupuk Ikan Renyah",
                    unitName = "Pack",
                    packSize = 10,
                    previousStock = 12,
                    remainingStock = 4,
                    soldQuantity = 8,
                    newDroppedQuantity = 12,
                    costPrice = 11000.0,
                    sellPrice = 15000.0,
                    subtotalDue = 120000.0,
                    subtotalProfit = 32000.0
                ),
                TransactionItemEntity(
                    transactionId = sampleTxId2,
                    productId = 5,
                    productName = "Kacang Bawang Super Gurih",
                    unitName = "Pack",
                    packSize = 15,
                    previousStock = 8,
                    remainingStock = 4,
                    soldQuantity = 4,
                    newDroppedQuantity = 8,
                    costPrice = 18000.0,
                    sellPrice = 23000.0,
                    subtotalDue = 92000.0,
                    subtotalProfit = 20000.0
                )
            )
        )
    }
}
