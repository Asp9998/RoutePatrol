package com.aryanspatel.routepatrol.presentation.nav

sealed class Route(val route: String) {
    object Onboarding : Route("onboarding")
    object CreateFleet : Route("create_fleet")
    object JoinFleet : Route("join_fleet")

    // Driver
    object DriverHome : Route("driver/home")
    object DriverTrips : Route("driver/trips")
    object DriverTripDetail : Route("driver/trip_detail/{tripId}") {
        fun create(tripId: String) = "driver/trip_detail/$tripId"
    }

    // Viewer
    object ViewerHome : Route("viewer/home")
    object ViewerHubs : Route("viewer/hubs")
    object ViewerAddHub : Route("viewer/add_hub")
    object ViewerTripDetail : Route("viewer/trip_detail/{tripId}") {
        fun create(tripId: String) = "viewer/trip_detail/$tripId"
    }

    object Settings : Route("settings")
}
