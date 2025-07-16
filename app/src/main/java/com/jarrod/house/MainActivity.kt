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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarrod.house.ui.screens.AdminDashboard
import com.jarrod.house.ui.screens.LoginScreen
import com.jarrod.house.ui.screens.UserDashboard
import com.jarrod.house.ui.screens.UsersManagementScreen
import com.jarrod.house.ui.screens.ApartmentsManagementScreen
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
                    // TODO: Navigate to create debt screen
                },
                onViewMetrics = {
                    // TODO: Navigate to metrics screen
                },
                onViewPayments = {
                    // TODO: Navigate to payments screen
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
                    // TODO: Navigate to payment screen
                },
                onViewPaymentHistory = {
                    // TODO: Navigate to payment history screen
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
    }
}