package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val medicineId: Long,
    val name: String,
    val dosageForm: String,
    val price: Double,
    val quantity: Int,
    val requiresPrescription: Boolean,
    val category: String
)
