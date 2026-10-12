package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ElectricBike
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CartScreen
import com.example.ui.screens.EmergencyHelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MedicineDetailSheet
import com.example.ui.screens.PrescriptionUploadScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.UrgentEmergencyRed
import com.example.ui.theme.UrgentTimerNeon
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineMinutesApp(
    viewModel: MedicineMinutesViewModel
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val activeOrder by viewModel.activeOrder.collectAsStateWithLifecycle()
    val selectedMedicine by viewModel.selectedMedicine.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle ViewModel snackbar alerts
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Handle back button: return to Shop tab if on another screen
    BackHandler(enabled = currentTab != AppNavTab.SHOP) {
        viewModel.navigateTo(AppNavTab.SHOP)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF004D40)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPharmacy,
                                contentDescription = null,
                                tint = UrgentTimerNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "T.S Medicine",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = UrgentEmergencyRed
                                ) {
                                    Text(
                                        text = "30 MIN",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Express Urgent Prescriptions",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.EMERGENCY) },
                        modifier = Modifier.testTag("app_bar_sos_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "Emergency SOS",
                                tint = UrgentEmergencyRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppNavTab.CART) },
                        modifier = Modifier.testTag("app_bar_cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = UrgentEmergencyRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("${cartItems.sumOf { it.quantity }}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // 1. SHOP
                NavigationBarItem(
                    selected = currentTab == AppNavTab.SHOP,
                    onClick = { viewModel.navigateTo(AppNavTab.SHOP) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.SHOP) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                            contentDescription = "Shop"
                        )
                    },
                    label = { Text("Shop", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_shop")
                )

                // 2. UPLOAD RX
                NavigationBarItem(
                    selected = currentTab == AppNavTab.PRESCRIPTION,
                    onClick = { viewModel.navigateTo(AppNavTab.PRESCRIPTION) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.PRESCRIPTION) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Upload Rx"
                        )
                    },
                    label = { Text("Upload Rx", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_prescription")
                )

                // 3. TRACKER (LIVE 30M)
                NavigationBarItem(
                    selected = currentTab == AppNavTab.TRACKER,
                    onClick = { viewModel.navigateTo(AppNavTab.TRACKER) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeOrder != null) {
                                    Badge(
                                        containerColor = UrgentTimerNeon,
                                        contentColor = Color(0xFF003830)
                                    ) {
                                        Text("30m")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.TRACKER) Icons.Filled.ElectricBike else Icons.Outlined.ElectricBike,
                                contentDescription = "Live 30m"
                            )
                        }
                    },
                    label = { Text("Live 30m", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_tracker")
                )

                // 4. CART
                NavigationBarItem(
                    selected = currentTab == AppNavTab.CART,
                    onClick = { viewModel.navigateTo(AppNavTab.CART) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text("${cartItems.sumOf { it.quantity }}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppNavTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart"
                            )
                        }
                    },
                    label = { Text("Cart", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_cart")
                )

                // 5. EMERGENCY HELP
                NavigationBarItem(
                    selected = currentTab == AppNavTab.EMERGENCY,
                    onClick = { viewModel.navigateTo(AppNavTab.EMERGENCY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppNavTab.EMERGENCY) Icons.Filled.LocalHospital else Icons.Outlined.LocalHospital,
                            contentDescription = "SOS"
                        )
                    },
                    label = { Text("SOS", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = UrgentEmergencyRed,
                        selectedTextColor = UrgentEmergencyRed
                    ),
                    modifier = Modifier.testTag("nav_tab_sos")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.SHOP -> HomeScreen(viewModel = viewModel)
                AppNavTab.PRESCRIPTION -> PrescriptionUploadScreen(viewModel = viewModel)
                AppNavTab.TRACKER -> TrackerScreen(viewModel = viewModel)
                AppNavTab.CART -> CartScreen(viewModel = viewModel)
                AppNavTab.EMERGENCY -> EmergencyHelpScreen(viewModel = viewModel)
            }

            // Medicine Detail Modal Sheet
            if (selectedMedicine != null) {
                MedicineDetailSheet(
                    medicine = selectedMedicine,
                    onDismiss = { viewModel.selectMedicine(null) },
                    onAddToCart = { med -> viewModel.addToCart(med) }
                )
            }
        }
    }
}
