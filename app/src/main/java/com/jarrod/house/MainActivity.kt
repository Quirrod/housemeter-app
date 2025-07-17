package com.jarrod.house

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.jarrod.house.ui.theme.HousemeterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    var userRole by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    userRole = role
                    when (role) {
                        "admin" -> navController.navigate("admin_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                        "user" -> navController.navigate("user_dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
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
                    userRole = null
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
                    userRole = null
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