package com.example.celltowertrackingforbus.NewScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celltowertrackingforbus.BusTracking.BusLocation
import com.example.celltowertrackingforbus.Screens.OfflineBusTracker
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Splash : Routes("splash")
    object Login : Routes("login")
    object TrackBus : Routes("track_bus")
    object DestinationSearch : Routes("search_destination")
    object OfflineTracking : Routes("offline_tracking")
    object OnlineTracking : Routes("online_tracking")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    busLocationFlow: StateFlow<BusLocation>? = null,
    onStartTracking: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val destinationANdHomeViewModel: HomeViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    userName = "User Name",
                    userEmail = "user@email.com",
                    onHomeClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    },
                    onLanguageClick = {
                        scope.launch { drawerState.close() }
                        // Handle language selection
                    },
                    onFeedbackClick = {
                        scope.launch { drawerState.close() }
                        // Handle feedback
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        // Handle settings
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.Splash.route
        ) {
            composable(Routes.Splash.route) {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.Login.route) {
                LoginScreen(
                    onSignIn = { email, password ->
                        // Handle sign in logic
                        navController.navigate(Routes.TrackBus.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    },
                    onCreateAccount = {
                        // Handle create account
                    },
                    onForgotPassword = {
                        // Handle forgot password
                    },
                    onGuestLogin = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.TrackBus.route) {
                TrackBusScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onBusClick = { busNumber ->
                        // Handle bus click - navigate to bus details/map
                    },
                    onSearch = { busNumber ->
                        // Handle search
                    }
                )
            }

            composable(Routes.DestinationSearch.route) {
                DestinationSearchResult(
                    onBackButtonClick = {
                        navController.navigate(Routes.Home.route)
                    },
                    onInsideBus = {
                        // Start the tracking service when user selects "I am inside bus"
                        onStartTracking()
                        navController.navigate(Routes.OfflineTracking.route)
                    },
                    onOutsideBus = {

                    },
                    viewModel = destinationANdHomeViewModel
                )
            }
            composable(Routes.Home.route) {
                HomeScreen(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onFindBuses = { from, to ->
                        navController.navigate(Routes.DestinationSearch.route)
                    },
                    onTrackBusClick = {
                        navController.navigate(Routes.TrackBus.route)
                    },
                    destinationANdHomeViewModel
                )
            }

            composable(Routes.OfflineTracking.route) {
                if (busLocationFlow != null) {
                    OfflineBusTracker(
                        busLocationFlow = busLocationFlow
                    )
                } else {
                    // Show loading or error state when service is not bound yet
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Loading state - you can add a proper loading UI here
                    }
                }
            }
        }
    }
}

