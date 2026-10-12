package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey val id: Long,
    val name: String,
    val genericName: String,
    val brand: String,
    val category: String,
    val dosageForm: String, // e.g., "Strip of 10 Tablets", "200 Dose Inhaler"
    val strength: String, // e.g. "650mg", "100mcg"
    val price: Double,
    val originalPrice: Double,
    val requiresPrescription: Boolean,
    val isUrgent30MinAvailable: Boolean,
    val inStock: Boolean = true,
    val rating: Float = 4.8f,
    val reviewCount: Int = 120,
    val description: String,
    val dosageInstructions: String,
    val sideEffects: String,
    val manufacturer: String
)
