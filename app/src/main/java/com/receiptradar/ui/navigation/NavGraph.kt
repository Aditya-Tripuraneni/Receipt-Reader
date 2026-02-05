package com.receiptradar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.receiptradar.ui.screens.capture.CaptureScreen
import com.receiptradar.ui.screens.detail.ReceiptDetailScreen
import com.receiptradar.ui.screens.insights.InsightsScreen
import com.receiptradar.ui.screens.receipts.ReceiptListScreen
import com.receiptradar.ui.screens.settings.SettingsScreen

/**
 * Navigation routes for the app.
 */
object Routes {
    const val RECEIPTS = "receipts"
    const val CAPTURE = "capture"
    const val RECEIPT_DETAIL = "receipt/{receiptId}"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
    
    fun receiptDetail(receiptId: Long) = "receipt/$receiptId"
}

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Routes.RECEIPTS
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Receipt list (home)
        composable(Routes.RECEIPTS) {
            ReceiptListScreen(
                onReceiptClick = { receiptId ->
                    navController.navigate(Routes.receiptDetail(receiptId))
                },
                onAddClick = {
                    navController.navigate(Routes.CAPTURE)
                }
            )
        }
        
        // Capture screen
        composable(Routes.CAPTURE) {
            CaptureScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReceiptProcessed = { receiptId ->
                    navController.navigate(Routes.receiptDetail(receiptId)) {
                        popUpTo(Routes.RECEIPTS)
                    }
                }
            )
        }
        
        // Receipt detail
        composable(
            route = Routes.RECEIPT_DETAIL,
            arguments = listOf(
                navArgument("receiptId") { type = NavType.LongType }
            )
        ) {
            ReceiptDetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Insights
        composable(Routes.INSIGHTS) {
            InsightsScreen()
        }
        
        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
