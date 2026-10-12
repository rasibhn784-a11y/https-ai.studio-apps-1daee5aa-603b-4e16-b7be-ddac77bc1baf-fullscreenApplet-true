package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CartItem
import com.example.data.model.Medicine
import com.example.data.model.Order
import com.example.data.model.OrderDeliveryStage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromDeliveryStage(stage: OrderDeliveryStage): String = stage.name

    @TypeConverter
    fun toDeliveryStage(value: String): OrderDeliveryStage = try {
        OrderDeliveryStage.valueOf(value)
    } catch (e: Exception) {
        OrderDeliveryStage.PHARMACIST_REVIEW
    }
}

@Database(
    entities = [Medicine::class, CartItem::class, Order::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ts_medicine_minutes.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default medicines
                        CoroutineScope(Dispatchers.IO).launch {
                            getDatabase(context).medicineDao().insertAll(MedicineInitialData.getInitialMedicines())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
