package com.waterplant.distribution.employee.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.waterplant.distribution.employee.ui.customers.CustomerListScreen
import com.waterplant.distribution.employee.ui.history.HistoryScreen
import com.waterplant.distribution.employee.ui.login.EmployeeLoginScreen
import com.waterplant.distribution.employee.ui.sync.SyncScreen

object EmpRoutes {
    const val LOGIN = "login"
    const val CUSTOMERS = "customers/{employeeId}"
    const val HISTORY = "history/{employeeId}"
    const val SYNC = "sync/{employeeId}"
}

@Composable
fun EmployeeNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Use string parsing to get the employeeId if it exists in the route
    val currentEmpId = navBackStackEntry?.arguments?.getString("employeeId") ?: ""

    val showBottomBar = currentRoute?.startsWith("customers/") == true ||
            currentRoute?.startsWith("history/") == true ||
            currentRoute?.startsWith("sync/") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentEmpId.isNotEmpty()) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute?.startsWith("customers/") == true,
                        onClick = {
                            navController.navigate("customers/$currentEmpId") {
                                popUpTo("customers/$currentEmpId") { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { Text("Customers") },
                        selected = false,
                        onClick = { /* Just keeping it for UI compliance as per instructions, it could open a search modal */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") },
                        selected = currentRoute?.startsWith("history/") == true,
                        onClick = {
                            navController.navigate("history/$currentEmpId") {
                                popUpTo("customers/$currentEmpId")
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Sync, contentDescription = "Sync") },
                        label = { Text("Sync") },
                        selected = currentRoute?.startsWith("sync/") == true,
                        onClick = {
                            navController.navigate("sync/$currentEmpId") {
                                popUpTo("customers/$currentEmpId")
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = false,
                        onClick = {
                            navController.navigate(EmpRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = EmpRoutes.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
        composable(EmpRoutes.LOGIN) {
            EmployeeLoginScreen(onLoginSuccess = { employeeId ->
                navController.navigate("customers/$employeeId") {
                    popUpTo(EmpRoutes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(
            EmpRoutes.CUSTOMERS,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val empId = backStackEntry.arguments?.getString("employeeId") ?: ""
            CustomerListScreen(
                employeeId = empId,
                onNavigateToHistory = { navController.navigate("history/$empId") },
                onNavigateToSync = { navController.navigate("sync/$empId") },
                onLogout = {
                    navController.navigate(EmpRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            EmpRoutes.HISTORY,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            HistoryScreen(
                employeeId = backStackEntry.arguments?.getString("employeeId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            EmpRoutes.SYNC,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            SyncScreen(
                employeeId = backStackEntry.arguments?.getString("employeeId") ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
    }
}
