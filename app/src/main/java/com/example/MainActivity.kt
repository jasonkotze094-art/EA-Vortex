package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.AnalysisRepository
import com.example.data.repository.LicenseRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VortexBg
import com.example.viewmodel.AnalysisViewModel
import com.example.viewmodel.AnalysisViewModelFactory
import com.example.viewmodel.LicenseViewModel
import com.example.viewmodel.LicenseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val licenseRepo = LicenseRepository(database.licenseDao())
        val analysisRepo = AnalysisRepository(database.analysisDao())

        setContent {
            MyApplicationTheme {
                val licenseViewModel: LicenseViewModel = viewModel(
                    factory = LicenseViewModelFactory(licenseRepo)
                )
                val analysisViewModel: AnalysisViewModel = viewModel(
                    factory = AnalysisViewModelFactory(analysisRepo)
                )

                EaVortexApp(
                    licenseViewModel = licenseViewModel,
                    analysisViewModel = analysisViewModel
                )
            }
        }
    }
}

@Composable
fun EaVortexApp(
    licenseViewModel: LicenseViewModel,
    analysisViewModel: AnalysisViewModel
) {
    var currentScreen by remember { mutableStateOf("LANDING") } // "LANDING", "AUTH", "DASHBOARD"
    var loggedInUser by remember { mutableStateOf("Alex Trader") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VortexBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (currentScreen) {
            "LANDING" -> {
                LandingScreen(
                    onStartAnalyzing = { currentScreen = "DASHBOARD" },
                    onLoginClick = { currentScreen = "AUTH" },
                    onOpenLicenseManager = { currentScreen = "DASHBOARD" }
                )
            }
            "AUTH" -> {
                AuthScreen(
                    onAuthSuccess = { name, licenseCode ->
                        loggedInUser = name
                        currentScreen = "DASHBOARD"
                    },
                    onBack = { currentScreen = "LANDING" }
                )
            }
            "DASHBOARD" -> {
                MainDashboardScreen(
                    userName = loggedInUser,
                    analysisViewModel = analysisViewModel,
                    licenseViewModel = licenseViewModel,
                    onSignOut = { currentScreen = "LANDING" }
                )
            }
        }
    }
}
