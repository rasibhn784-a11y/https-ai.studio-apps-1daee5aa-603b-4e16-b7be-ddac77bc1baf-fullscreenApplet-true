package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Medicine
import com.example.data.model.Order
import com.example.data.model.OrderDeliveryStage
import com.example.data.repository.MedicineRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    SHOP("Shop"),
    PRESCRIPTION("Upload Rx"),
    TRACKER("Live 30m"),
    CART("Cart"),
    EMERGENCY("SOS / Help")
}

data class PrescriptionUploadForm(
    val patientName: String = "",
    val patientAge: String = "",
    val doctorName: String = "",
    val symptomsOrNotes: String = "",
    val urgencyLevel: String = "CRITICAL 30-MIN",
    val hasPrescriptionImage: Boolean = false,
    val isVerifiedByDoctor: Boolean = true
)

class MedicineMinutesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MedicineRepository(
        database.medicineDao(),
        database.cartDao(),
        database.orderDao()
    )

    // Navigation State
    private val _currentTab = MutableStateFlow(AppNavTab.SHOP)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Filters and Search
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterOnlyUrgent = MutableStateFlow(false)
    val filterOnlyUrgent: StateFlow<Boolean> = _filterOnlyUrgent.asStateFlow()

    private val _filterOnlyRx = MutableStateFlow(false)
    val filterOnlyRx: StateFlow<Boolean> = _filterOnlyRx.asStateFlow()

    // Selected medicine for detail modal
    private val _selectedMedicine = MutableStateFlow<Medicine?>(null)
    val selectedMedicine: StateFlow<Medicine?> = _selectedMedicine.asStateFlow()

    // Delivery Address
    private val _deliveryAddress = MutableStateFlow("Flat 402, Green Park Avenue, Sector 14")
    val deliveryAddress: StateFlow<String> = _deliveryAddress.asStateFlow()

    // Prescription form
    private val _prescriptionForm = MutableStateFlow(PrescriptionUploadForm())
    val prescriptionForm: StateFlow<PrescriptionUploadForm> = _prescriptionForm.asStateFlow()

    // UI Events / Toasts
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    // Active order tracking timer
    private var trackingTimerJob: Job? = null

    // Room Flows
    val allMedicines: StateFlow<List<Medicine>> = repository.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMedicines: StateFlow<List<Medicine>> = combine(
        allMedicines,
        _selectedCategory,
        _searchQuery,
        _filterOnlyUrgent,
        _filterOnlyRx
    ) { list, cat, query, onlyUrgent, onlyRx ->
        list.filter { item ->
            val matchCategory = cat == "All" || item.category.equals(cat, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.genericName.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchUrgent = !onlyUrgent || item.isUrgent30MinAvailable
            val matchRx = !onlyRx || item.requiresPrescription
            matchCategory && matchQuery && matchUrgent && matchRx
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeOrder: StateFlow<Order?> = repository.getActiveUrgentOrder()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val orderHistory: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
        startTrackingTimer()
    }

    private fun startTrackingTimer() {
        trackingTimerJob?.cancel()
        trackingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = activeOrder.value
                if (current != null && current.stage != OrderDeliveryStage.DELIVERED) {
                    val remaining = (current.remainingSeconds - 1).coerceAtLeast(0)
                    val newStage = when {
                        remaining > 1560 -> OrderDeliveryStage.PHARMACIST_REVIEW
                        remaining > 1200 -> OrderDeliveryStage.DISPENSING_AND_PACKING
                        remaining > 720 -> OrderDeliveryStage.RIDER_PICKED_UP
                        remaining > 0 -> OrderDeliveryStage.OUT_FOR_DELIVERY
                        else -> OrderDeliveryStage.DELIVERED
                    }

                    if (remaining != current.remainingSeconds || newStage != current.stage) {
                        val updated = current.copy(
                            remainingSeconds = remaining,
                            stage = newStage
                        )
                        repository.updateOrder(updated)
                    }
                }
            }
        }
    }

    fun navigateTo(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOnlyUrgent() {
        _filterOnlyUrgent.value = !_filterOnlyUrgent.value
    }

    fun toggleOnlyRx() {
        _filterOnlyRx.value = !_filterOnlyRx.value
    }

    fun selectMedicine(medicine: Medicine?) {
        _selectedMedicine.value = medicine
    }

    fun updateDeliveryAddress(newAddress: String) {
        _deliveryAddress.value = newAddress
    }

    fun addToCart(medicine: Medicine) {
        viewModelScope.launch {
            repository.addToCart(medicine)
            _userMessage.emit("Added ${medicine.name} to express cart")
        }
    }

    fun updateCartQuantity(medicineId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(medicineId, newQuantity)
        }
    }

    fun removeFromCart(medicineId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(medicineId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Prescription Upload Actions
    fun updatePrescriptionForm(
        patientName: String = _prescriptionForm.value.patientName,
        patientAge: String = _prescriptionForm.value.patientAge,
        doctorName: String = _prescriptionForm.value.doctorName,
        symptoms: String = _prescriptionForm.value.symptomsOrNotes,
        urgency: String = _prescriptionForm.value.urgencyLevel,
        hasImage: Boolean = _prescriptionForm.value.hasPrescriptionImage
    ) {
        _prescriptionForm.value = _prescriptionForm.value.copy(
            patientName = patientName,
            patientAge = patientAge,
            doctorName = doctorName,
            symptomsOrNotes = symptoms,
            urgencyLevel = urgency,
            hasPrescriptionImage = hasImage
        )
    }

    fun submitUrgentPrescription(notes: String) {
        viewModelScope.launch {
            val patient = _prescriptionForm.value.patientName.ifBlank { "Emergency Patient" }
            val doc = _prescriptionForm.value.doctorName.ifBlank { "Dr. R. Kapoor, MD" }
            val summary = "Urgent Prescription ($patient, Rx by $doc) - Salbutamol Inhaler + Amoxicillin 625mg + Paracetamol"
            val order = repository.placeOrder(
                itemsSummary = summary,
                totalAmount = 28.30,
                isUrgent30Min = true,
                deliveryAddress = _deliveryAddress.value,
                prescriptionUploaded = true,
                prescriptionNotes = notes
            )
            _prescriptionForm.value = PrescriptionUploadForm()
            _userMessage.emit("Urgent 30-Min Prescription dispatched! Rider assigned.")
            _currentTab.value = AppNavTab.TRACKER
        }
    }

    fun checkoutCurrentCart(isUrgent30Min: Boolean) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) {
                _userMessage.emit("Cart is empty!")
                return@launch
            }

            val summary = items.joinToString(", ") { "${it.name} x${it.quantity}" }
            val subtotal = items.sumOf { it.price * it.quantity }
            val expressFee = if (isUrgent30Min && subtotal < 15.0) 1.99 else 0.0
            val tax = subtotal * 0.05
            val total = subtotal + expressFee + tax

            repository.placeOrder(
                itemsSummary = summary,
                totalAmount = total,
                isUrgent30Min = isUrgent30Min,
                deliveryAddress = _deliveryAddress.value,
                prescriptionUploaded = items.any { it.requiresPrescription }
            )
            _userMessage.emit("Urgent Order placed! Guaranteed delivery in 30 minutes.")
            _currentTab.value = AppNavTab.TRACKER
        }
    }

    fun fastReorder(order: Order) {
        viewModelScope.launch {
            repository.placeOrder(
                itemsSummary = "Reorder: ${order.itemsSummary}",
                totalAmount = order.totalAmount,
                isUrgent30Min = true,
                deliveryAddress = order.deliveryAddress,
                prescriptionUploaded = order.prescriptionUploaded
            )
            _userMessage.emit("Reordered! Express rider dispatching now.")
            _currentTab.value = AppNavTab.TRACKER
        }
    }
}
