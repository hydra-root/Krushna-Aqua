package com.waterplant.distribution.admin.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.waterplant.distribution.admin.ui.backup.BackupScreen
import com.waterplant.distribution.admin.ui.customer.AddEditCustomerScreen
import com.waterplant.distribution.admin.ui.customer.CustomerListScreen
import com.waterplant.distribution.admin.ui.dashboard.DashboardScreen
import com.waterplant.distribution.admin.ui.employee.AddEditEmployeeScreen
import com.waterplant.distribution.admin.ui.employee.EmployeeListScreen
import com.waterplant.distribution.admin.ui.login.AdminLoginScreen
import com.waterplant.distribution.admin.ui.reports.ReportsScreen
import com.waterplant.distribution.admin.ui.customer.PdfPreviewScreen
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding

object AdminRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val EMPLOYEES = "employees"
    const val ADD_EMPLOYEE = "add_employee"
    const val EDIT_EMPLOYEE = "edit_employee/{employeeId}"
    const val CUSTOMERS = "customers"
    const val ADD_CUSTOMER = "add_customer"
    const val EDIT_CUSTOMER = "edit_customer/{customerId}"
    const val REPORTS = "reports"
    const val BACKUP = "backup"
    const val PDF_PREVIEW = "pdf_preview/{filePath}"
}

@Composable
fun AdminNavHost() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        AdminRoutes.DASHBOARD, 
        AdminRoutes.CUSTOMERS, 
        AdminRoutes.REPORTS, 
        AdminRoutes.BACKUP // Using Backup as Settings for now
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == AdminRoutes.DASHBOARD,
                        onClick = {
                            navController.navigate(AdminRoutes.DASHBOARD) {
                                popUpTo(AdminRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, contentDescription = "Customers") },
                        label = { Text("Customers") },
                        selected = currentRoute == AdminRoutes.CUSTOMERS,
                        onClick = {
                            navController.navigate(AdminRoutes.CUSTOMERS) {
                                popUpTo(AdminRoutes.DASHBOARD)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Sync, contentDescription = "Sync") },
                        label = { Text("Sync") },
                        selected = false,
                        onClick = { /* Server sync is background, but could show a modal */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                        label = { Text("Reports") },
                        selected = currentRoute == AdminRoutes.REPORTS,
                        onClick = {
                            navController.navigate(AdminRoutes.REPORTS) {
                                popUpTo(AdminRoutes.DASHBOARD)
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == AdminRoutes.BACKUP,
                        onClick = {
                            navController.navigate(AdminRoutes.BACKUP) {
                                popUpTo(AdminRoutes.DASHBOARD)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = AdminRoutes.LOGIN,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable(AdminRoutes.LOGIN) {
                AdminLoginScreen(onLoginSuccess = {
                    navController.navigate(AdminRoutes.DASHBOARD) {
                        popUpTo(AdminRoutes.LOGIN) { inclusive = true }
                    }
                })
            }
        composable(AdminRoutes.DASHBOARD) {
            DashboardScreen(
                onNavigateToEmployees = { navController.navigate(AdminRoutes.EMPLOYEES) },
                onNavigateToCustomers = { navController.navigate(AdminRoutes.CUSTOMERS) },
                onNavigateToReports = { navController.navigate(AdminRoutes.REPORTS) },
                onNavigateToBackup = { navController.navigate(AdminRoutes.BACKUP) },
                onNavigateToCustomerHistory = { id -> navController.navigate("customer_history/$id") },
                onLogout = {
                    navController.navigate(AdminRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(AdminRoutes.EMPLOYEES) {
            EmployeeListScreen(
                onBack = { navController.popBackStack() },
                onAddEmployee = { navController.navigate(AdminRoutes.ADD_EMPLOYEE) },
                onEditEmployee = { id -> navController.navigate("edit_employee/$id") }
            )
        }
        composable(AdminRoutes.ADD_EMPLOYEE) {
            AddEditEmployeeScreen(
                employeeId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            AdminRoutes.EDIT_EMPLOYEE,
            arguments = listOf(navArgument("employeeId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddEditEmployeeScreen(
                employeeId = backStackEntry.arguments?.getString("employeeId"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(AdminRoutes.CUSTOMERS) {
            CustomerListScreen(
                onBack = { navController.popBackStack() },
                onAddCustomer = { navController.navigate(AdminRoutes.ADD_CUSTOMER) },
                onEditCustomer = { id -> navController.navigate("edit_customer/$id") },
                onNavigateToHistory = { id -> navController.navigate("customer_history/$id") }
            )
        }
        composable(AdminRoutes.ADD_CUSTOMER) {
            AddEditCustomerScreen(
                customerId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            AdminRoutes.EDIT_CUSTOMER,
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddEditCustomerScreen(
                customerId = backStackEntry.arguments?.getString("customerId"),
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "customer_history/{customerId}",
            arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            com.waterplant.distribution.admin.ui.customer.CustomerHistoryScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() },
                onNavigateToPreview = { path ->
                    val encodedPath = Uri.encode(path)
                    navController.navigate("pdf_preview/$encodedPath")
                }
            )
        }
        composable(
            AdminRoutes.PDF_PREVIEW,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath")?.let { Uri.decode(it) } ?: return@composable
            PdfPreviewScreen(
                filePath = filePath,
                onBack = { navController.popBackStack() }
            )
        }
        composable(AdminRoutes.REPORTS) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }
        composable(AdminRoutes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
    }
}
