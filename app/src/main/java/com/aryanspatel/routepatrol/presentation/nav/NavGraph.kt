package com.aryanspatel.routepatrol.presentation.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aryanspatel.routepatrol.data.datastore.Preferences
import com.aryanspatel.routepatrol.domain.model.UserRole
import com.aryanspatel.routepatrol.presentation.screens.CreateFleetScreen
import com.aryanspatel.routepatrol.presentation.screens.DriverHomeScreen
import com.aryanspatel.routepatrol.presentation.screens.JoinFleetScreen
import com.aryanspatel.routepatrol.presentation.screens.OnboardingScreen
import com.aryanspatel.routepatrol.presentation.screens.ViewerHomeScreen

@Composable
fun NavGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current
    var isSessionLoaded by rememberSaveable { mutableStateOf(false) }
    var startDestination by rememberSaveable { mutableStateOf(Route.Onboarding.route) }

    LaunchedEffect(Unit) {
        val session = Preferences.getSession(context)

        startDestination = when (session?.role) {
            UserRole.DRIVER -> Route.DriverHome.route
            UserRole.VIEWER -> Route.ViewerHome.route
            null -> Route.Onboarding.route
        }

        isSessionLoaded = true
    }

    if (!isSessionLoaded) {
        // simple splash / loading state
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {fadeIn(animationSpec = tween(0))},
        exitTransition = {fadeOut(animationSpec = tween(0))},
        popEnterTransition = {fadeIn(animationSpec = tween(0))},
        popExitTransition = {fadeOut(animationSpec = tween(0))}
    ) {

        // Onboarding flow
        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onCreateFleet = { navController.navigate(Route.CreateFleet.route) },
                onJoinFleet = { navController.navigate(Route.JoinFleet.route) }
            )
        }

        composable(Route.CreateFleet.route) {
            CreateFleetScreen(
                onBack = { navController.popBackStack() },
                onFleetCreated = { fleetCode, role ->
                    val homeRoute = when (role) {
                        UserRole.VIEWER -> Route.ViewerHome.route
                        UserRole.DRIVER -> Route.DriverHome.route
                    }
                    // Later: navigate based on role
                    navController.navigate(homeRoute) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.JoinFleet.route) {
            JoinFleetScreen(
                onBack = { navController.popBackStack() },
                onFleetJoined = { fleetCode, role ->
                    val homeRoute = when (role) {
                        UserRole.VIEWER -> Route.ViewerHome.route
                        UserRole.DRIVER -> Route.DriverHome.route
                    }
                    navController.navigate(homeRoute) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Driver
        composable(Route.DriverHome.route) {
            DriverHomeScreen()
        }

        // Viewer
        composable(Route.ViewerHome.route) {
            ViewerHomeScreen()
        }
    }
}