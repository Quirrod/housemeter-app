package com.jarrod.house

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.jarrod.house.data.datastore.DataStoreManager
import com.jarrod.house.utils.NotificationManager
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarrod.house.ui.viewmodel.DebtViewModel
import com.jarrod.house.ui.screens.AdminDashboard
import com.jarrod.house.ui.screens.LoginScreen
import com.jarrod.house.ui.screens.UserDashboard
import com.jarrod.house.ui.screens.UsersManagementScreen
import com.jarrod.house.ui.screens.ApartmentsManagementScreen
import com.jarrod.house.ui.screens.DebtsManagementScreen
import com.jarrod.house.ui.screens.MetricsScreen
import com.jarrod.house.ui.screens.PaymentsManagementScreen
import com.jarrod.house.ui.screens.PaymentScreen
import com.jarrod.house.ui.screens.PaymentHistoryScreen
import com.jarrod.house.ui.screens.RegisterScreen
import com.jarrod.house.ui.theme.HousemeterTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var notificationManager: NotificationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize managers
        dataStoreManager = DataStoreManager(this)
        notificationManager = NotificationManager(this, dataStoreManager)
        
        // Request notification permission and initialize Firebase
        notificationManager.requestNotificationPermission(this)
        notificationManager.initializeFirebaseMessaging()
        
        setContent {
            HousemeterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HouseMeterApp()
                }
            }
        }
    }
}

@Composable
fun HouseMeterApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var userRole by remember { mutableStateOf<String?>(null) }
    var isCheckingSession by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val dataStoreManager = DataStoreManager(context)
        val savedRole = dataStoreManager.getUserRole()
        val savedToken = dataStoreManager.getAuthToken()
        
        if (savedRole != null && savedToken != null) {
            userRole = savedRole
        }
        isCheckingSession = false
    }

    val logout = remember {
        {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                val dataStoreManager = DataStoreManager(context)
                dataStoreManager.clearAuthToken()
                dataStoreManager.clearUserData()
                userRole = null
            }
        }
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (userRole != null) {
            when (userRole) {
                "admin", "house_admin" -> "admin_dashboard"
                "user" -> "user_dashboard"
                else -> "login"
            }
        } else {
            "login"
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    userRole = role
                    when (role) {
                        "admin", "house_admin" -> navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                        "user" -> navController.navigate("user_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboard(
                onCreateDebt = {
                    navController.navigate("debts_management")
                },
                onViewMetrics = {
                    navController.navigate("metrics")
                },
                onViewPayments = {
                    navController.navigate("payments_management")
                },
                onManageUsers = {
                    navController.navigate("users_management")
                },
                onManageApartments = {
                    navController.navigate("apartments_management")
                },
                onLogout = {
                    logout()
                    navController.navigate("login") {
                        popUpTo("admin_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("user_dashboard") {
            UserDashboard(
                onPayDebt = { debt ->
                    navController.navigate("payment/${debt.id}")
                },
                onViewPaymentHistory = {
                    navController.navigate("payment_history")
                },
                onLogout = {
                    logout()
                    navController.navigate("login") {
                        popUpTo("user_dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("users_management") {
            UsersManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("apartments_management") {
            ApartmentsManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("debts_management") {
            DebtsManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("metrics") {
            MetricsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("payments_management") {
            PaymentsManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("payment_history") {
            PaymentHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "payment/{debtId}",
            arguments = listOf(navArgument("debtId") { type = NavType.IntType })
        ) { backStackEntry ->
            val debtId = backStackEntry.arguments?.getInt("debtId") ?: 0
            val debtViewModel: DebtViewModel = viewModel()
            val debts by debtViewModel.debts.collectAsState()
            val context = LocalContext.current
            
            // Find the debt by ID
            val debt = debts.find { it.id == debtId }
            
            LaunchedEffect(Unit) {
                debtViewModel.loadDebts(context)
            }
            
            debt?.let {
                PaymentScreen(
                    debt = it,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPaymentSuccess = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}