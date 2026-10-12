package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MedicineInitialData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Medicine Minutes", appName)
    }

    @Test
    fun `initial medicine list contains urgent 30-min items`() {
        val medicines = MedicineInitialData.getInitialMedicines()
        assertTrue(medicines.isNotEmpty())
        assertTrue(medicines.any { it.isUrgent30MinAvailable })
        assertTrue(medicines.any { it.requiresPrescription })
    }
}
