package com.example.data.repository

import com.example.data.local.CartDao
import com.example.data.local.MedicineDao
import com.example.data.local.MedicineInitialData
import com.example.data.local.OrderDao
import com.example.data.model.CartItem
import com.example.data.model.Medicine
import com.example.data.model.Order
import com.example.data.model.OrderDeliveryStage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class MedicineRepository(
    private val medicineDao: MedicineDao,
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    suspend fun checkAndSeedInitialData() {
        if (medicineDao.getCount() == 0) {
            medicineDao.insertAll(MedicineInitialData.getInitialMedicines())
        }
    }

    fun getAllMedicines(): Flow<List<Medicine>> =
        medicineDao.getAllMedicines().onStart { checkAndSeedInitialData() }

    fun getMedicinesByCategory(category: String): Flow<List<Medicine>> =
        medicineDao.getMedicinesByCategory(category).onStart { checkAndSeedInitialData() }

    fun searchMedicines(query: String): Flow<List<Medicine>> =
        medicineDao.searchMedicines(query).onStart { checkAndSeedInitialData() }

    suspend fun getMedicineById(id: Long): Medicine? = medicineDao.getMedicineById(id)

    // Cart operations
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getAllCartItems()

    suspend fun addToCart(medicine: Medicine) {
        val existing = cartDao.getCartItem(medicine.id)
        if (existing != null) {
            cartDao.update(existing.copy(quantity = existing.quantity + 1))
        } else {
            cartDao.insertOrUpdate(
                CartItem(
                    medicineId = medicine.id,
                    name = medicine.name,
                    dosageForm = medicine.dosageForm,
                    price = medicine.price,
                    quantity = 1,
                    requiresPrescription = medicine.requiresPrescription,
                    category = medicine.category
                )
            )
        }
    }

    suspend fun updateCartQuantity(medicineId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteByMedicineId(medicineId)
        } else {
            val existing = cartDao.getCartItem(medicineId)
            if (existing != null) {
                cartDao.update(existing.copy(quantity = newQuantity))
            }
        }
    }

    suspend fun removeFromCart(medicineId: Long) {
        cartDao.deleteByMedicineId(medicineId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Orders & Tracking
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getActiveUrgentOrder(): Flow<Order?> = orderDao.getActiveUrgentOrder()

    suspend fun placeOrder(
        itemsSummary: String,
        totalAmount: Double,
        isUrgent30Min: Boolean,
        deliveryAddress: String,
        prescriptionUploaded: Boolean = false,
        prescriptionNotes: String = ""
    ): Order {
        val orderId = "TS-" + (10000..99999).random().toString()
        val order = Order(
            orderId = orderId,
            itemsSummary = itemsSummary,
            totalAmount = totalAmount,
            isUrgent30Min = isUrgent30Min,
            deliveryAddress = deliveryAddress,
            stage = OrderDeliveryStage.PHARMACIST_REVIEW,
            createdAtTimestamp = System.currentTimeMillis(),
            initialMinutes = if (isUrgent30Min) 30 else 120,
            remainingSeconds = if (isUrgent30Min) 1800 else 7200,
            prescriptionUploaded = prescriptionUploaded,
            prescriptionNotes = prescriptionNotes
        )
        orderDao.insertOrder(order)
        cartDao.clearCart()
        return order
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }
}
