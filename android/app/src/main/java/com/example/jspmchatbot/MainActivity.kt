package com.example.jspmchatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.jspmchatbot.theme.JSPMChatbotTheme
import com.example.jspmchatbot.ui.*

enum class AppNavState {
    SPLASH,
    LOGIN,
    PORTAL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JSPMChatbotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var navState by remember { mutableStateOf(AppNavState.SPLASH) }
                    var userRole by remember { mutableStateOf("Student") }
                    var userName by remember { mutableStateOf("Anuja") }
                    var currentTab by remember { mutableStateOf(PortalTab.HOME) }

                    when (navState) {
                        AppNavState.SPLASH -> {
                            SplashScreen(
                                onGetStarted = { navState = AppNavState.LOGIN }
                            )
                        }
                        AppNavState.LOGIN -> {
                            LoginScreen(
                                onLoginSuccess = { role, name ->
                                    userRole = role
                                    userName = name
                                    navState = AppNavState.PORTAL
                                    currentTab = PortalTab.HOME
                                }
                            )
                        }
                        AppNavState.PORTAL -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                bottomBar = {
                                    PortalBottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { currentTab = it }
                                    )
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentTab) {
                                        PortalTab.HOME -> {
                                            StudentHomeScreen(
                                                studentName = userName,
                                                onNavigateToChat = { currentTab = PortalTab.CHATBOT },
                                                onNavigateToTimetable = { currentTab = PortalTab.TIMETABLE },
                                                onNavigateToAttendance = { currentTab = PortalTab.ATTENDANCE },
                                                onNavigateToNotices = { currentTab = PortalTab.NOTICES }
                                            )
                                        }
                                        PortalTab.TIMETABLE -> {
                                            TimetableScreen(
                                                onNavigateBack = { currentTab = PortalTab.HOME }
                                            )
                                        }
                                        PortalTab.CHATBOT -> {
                                            ChatScreen()
                                        }
                                        PortalTab.ATTENDANCE -> {
                                            AttendanceScreen(
                                                onNavigateBack = { currentTab = PortalTab.HOME }
                                            )
                                        }
                                        PortalTab.NOTICES -> {
                                            NoticesScreen(
                                                onNavigateBack = { currentTab = PortalTab.HOME }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
