package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderDeliveryStage {
    PHARMACIST_REVIEW,
    DISPENSING_AND_PACKING,
    RIDER_PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val orderId: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val isUrgent30Min: Boolean,
    val deliveryAddress: String,
    val stage: OrderDeliveryStage,
    val createdAtTimestamp: Long,
    val initialMinutes: Int = 30,
    val remainingSeconds: Int = 1800,
    val riderName: String = "Marcus Reed",
    val riderPhone: String = "+1 (800) 555-0199",
    val riderVehicle: String = "Speed Delivery Bike #TS-09",
    val pharmacistVerification: String = "Verified by Dr. J. Sharma (TS Pharmacy Lead)",
    val prescriptionUploaded: Boolean = false,
    val prescriptionNotes: String = ""
)
