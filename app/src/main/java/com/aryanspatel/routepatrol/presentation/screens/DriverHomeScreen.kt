package com.aryanspatel.routepatrol.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.launch
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aryanspatel.routepatrol.R
import com.aryanspatel.routepatrol.presentation.ActionCardIcon
import com.aryanspatel.routepatrol.presentation.SnackBarMessage
import com.aryanspatel.routepatrol.presentation.models.CreateFleetEvent
import com.aryanspatel.routepatrol.presentation.models.DriverHomeEvent
import com.aryanspatel.routepatrol.presentation.viewmodels.DriverHomeViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// --- Model -----------------------------------------------------------

data class Hub(
    val id: Int,
    val name: String,
    val address: String,
    val visited: Boolean = false
)

// --- Preview entry ---------------------------------------------------

@Preview(showBackground = true)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var currentTime by remember { mutableStateOf("") }
    var visitedHubIds by remember { mutableStateOf(listOf(1, 2)) }
    val baseHubs = remember {
        listOf(
            Hub(1, "Winnipeg Main DC", "Industrial Park – Gate 3"),
            Hub(2, "Downtown Depot", "123 Main Street"),
            Hub(3, "North Hub", "456 North Ave"),
            Hub(4, "East Distribution", "789 East Road"),
            Hub(5, "South Terminal", "321 South Blvd")
        )
    }

    // derive hubs with visited flag
    val hubs = baseHubs.map { hub ->
        hub.copy(visited = visitedHubIds.contains(hub.id))
    }

    // current hub = first not visited hub (simple heuristic)
    val currentHub = hubs.firstOrNull { !it.visited }

    val driverLocation = state.lastLat?.let { lat ->
        state.lastLng?.let { lng -> LatLng(lat, lng) }
    }

    // Map pathPoints -> LatLng for MapSection
    val pathLatLng = state.pathPoints.map { LatLng(it.lat, it.lng) }

    // For now: fake name/vehicle/fleet; later from session
    val driverName = state.driverName
    val vehicleLabel = state.vehicle
    val fleetCode = state.fleetCode
    val context = LocalContext.current

    // Update current time every second (for preview feel)
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            currentTime = sdf.format(Date())
            delay(1000)
        }
    }

    DriverHomeScreenContent(
        events = viewModel.events,
        tripRunning = state.activeTrip != null,
        driverName = driverName,
        vehicleLabel = vehicleLabel,
        fleetCode = fleetCode,
        currentTime = currentTime,
        hubs = hubs,
        currentHub = currentHub,
        driverLocation = driverLocation,
        pathPoints = pathLatLng,
        onStartTrip = {
            viewModel.onStartTripClicked()
        },
        onEndTrip = {
            viewModel.onEndTripClicked(
                context = context
            )
        },
        onMarkHubVisited = { hubId ->
            if (!visitedHubIds.contains(hubId)) {
                visitedHubIds = visitedHubIds + hubId
            }
        },
        onHubClicked = { /* later: center map on hub */ },
        onLocationPermissionChanged = { viewModel.onLocationPermissionChanged(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreenContent(
    events: SharedFlow<DriverHomeEvent>,
    tripRunning: Boolean,
    driverName: String,
    vehicleLabel: String?,
    fleetCode: String,
    currentTime: String,
    hubs: List<Hub>,
    currentHub: Hub?,
    driverLocation: LatLng?,
    pathPoints: List<LatLng>,
    onStartTrip: () -> Unit,
    onEndTrip: () -> Unit,
    onMarkHubVisited: (Int) -> Unit,
    onHubClicked: (Int) -> Unit,
    onLocationPermissionChanged: (Boolean) -> Unit
) {
    val visitedCount = hubs.count { it.visited }
    val totalHubs = hubs.size

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // Check if any of the requested permissions was granted
        val foregroundGranted =
            (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

        // Tell ViewModel what happened
        onLocationPermissionChanged(foregroundGranted)

        // check for background location permission
    }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is DriverHomeEvent.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
                is DriverHomeEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp, // how much of the sheet is visible when collapsed
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {

            DriverBottomSheetContent(
                tripRunning = tripRunning,
                currentTime = currentTime,
                visitedCount = visitedCount,
                totalHubs = totalHubs,
                hubs = hubs,
                currentHub = currentHub,
                onStartTrip = onStartTrip,
                onEndTrip = onEndTrip,
                onMarkVisited = onMarkHubVisited,
                onHubClicked = onHubClicked
            )
        },
        snackbarHost = {
            SnackBarMessage(
                snackbarHostState = snackbarHostState)
        }
    ) { innerPadding ->
        // MAIN CONTENT (Top bar + summary + map)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            DriverSummaryCard(
                tripRunning = tripRunning,
                driverName = driverName,
                vehicleLabel = vehicleLabel,
                fleetCode = fleetCode
            )

            // Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MapSection(
                    tripRunning = tripRunning,
                    hubs = hubs,
                    driverLocation = driverLocation,
                    pathPoints = pathPoints
                )
            }
        }
    }
}

@Composable
fun DriverBottomSheetContent(
    tripRunning: Boolean,
    currentTime: String,
    visitedCount: Int,
    totalHubs: Int,
    hubs: List<Hub>,
    currentHub: Hub?,
    onStartTrip: () -> Unit,
    onEndTrip: () -> Unit,
    onMarkVisited: (Int) -> Unit,
    onHubClicked: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Trip action button
            if (!tripRunning) {
                Button(
                    onClick = onStartTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Trip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Location tracking will start once you begin a trip.",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )

            } else {
                Button(
                    onClick = onEndTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End Trip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Hubs visited", "$visitedCount / $totalHubs")
                    StatItem("Last Update", currentTime)
                    StatItem("Status", if (tripRunning) "Live" else "Offline")
                }
            }

            // Hubs list
            Text(
                text = "Route Hubs",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                hubs.forEach { hub ->
                    HubListItem(
                        hub = hub,
                        onClick = { onHubClicked(hub.id) }
                    )
                }
            }

            // Debug info (later: real tripId / fleetCode)
            Divider(color = Color(0xFFE5E7EB))
            Text(
                text = "Trip ID: abc123 | Fleet Code: XYZ123",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun DriverSummaryCard(
    tripRunning: Boolean,
    driverName: String,
    vehicleLabel: String?,
    fleetCode: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RoutePatrol – Driver",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {

                    ActionCardIcon(
                        icon = Icons.Default.LocalShipping
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {


                        Text(
                            text = driverName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        if(vehicleLabel != null) {
                            Text(
                                text = vehicleLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }
                        Text(
                            text = "Fleet Code: $fleetCode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (tripRunning) Color(0xFF10B981) else Color(0xFF9CA3AF)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tripRunning) {
                            val infiniteTransition =
                                rememberInfiniteTransition(label = "badge-pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 0.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "badge-alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .alpha(alpha)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (tripRunning) "Online" else "Offline",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MapSection(
    tripRunning: Boolean,
    driverLocation: LatLng?,
    pathPoints: List<LatLng>,
    hubs: List<Hub>
) {
    val defaultCenter = LatLng(49.8951, -97.1384)
    val center = driverLocation ?: defaultCenter

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 12f)
    }
    val scope = rememberCoroutineScope()

    // Basic map config – tilt/rotation enabled, my-location turned off (we show our own marker)
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false
        )
    }

    val bearing = remember(pathPoints) {
        if (pathPoints.size >= 2) computeBearing(pathPoints) else 0f
    }

//    // When trip starts or driver moves (later), recenter a bit
//    LaunchedEffect(tripRunning,defaultCenter) {
//        defaultCenter?.let {
//            cameraPositionState.animate(
//                update = CameraUpdateFactory.newLatLngZoom(it, 14f),
//                durationMs = 800
//            )
//        }
//    }
    // 2) When trip starts or driver moves, jump camera (no animation = no crash)
    LaunchedEffect(tripRunning, driverLocation) {
        if (tripRunning) {
            val target = driverLocation ?: pathPoints.lastOrNull() ?: center
            val currentZoom = cameraPositionState.position.zoom.takeIf { it != 0f } ?: 15f
            cameraPositionState.position = CameraPosition.fromLatLngZoom(target, currentZoom)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {

            // 1) Path polyline
            if(pathPoints.size >= 2) {
                Polyline(
                    points = pathPoints,
                    width = 12f,
                    color = Color(0xFF2563EB).copy(0.9f),
                    jointType = JointType.ROUND
                )
            }

            // 2) Driver marker (at last point if available)
            val driverPos = driverLocation ?: pathPoints.lastOrNull()
            driverPos?.let { pos ->
                Marker(
                    state = MarkerState(position = driverPos),
                    title = "You",
                    snippet = if (tripRunning) "Trip running" else "Offline",
                    icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.ic_driver_puck),
                    rotation = bearing,       // 👈 direction
                    flat = true,              // marker lays "on the map"
                    anchor = Offset(0.5f, 0.5f) // center of the icon is the rotation pivot
                )
            }

            // Fake positions for hubs (spread around the city a bit)
            val hubPositions = listOf(
                LatLng(49.9051, -97.1184),
                LatLng(49.8851, -97.1584),
                LatLng(49.8951, -97.1884),
                LatLng(49.8751, -97.1384),
                LatLng(49.9151, -97.1484)
            )

            hubs.take(hubPositions.size).forEachIndexed { index, hub ->
                val pos = hubPositions[index]

                Marker(
                    state = MarkerState(position = pos),
                    title = hub.name,
                    snippet = hub.address,
                    icon = null // later: custom bitmap for visited vs pending
                )
            }
        }

        // Overlays: recenter / zoom FABs
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val target = driverLocation ?: center
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(target, 13f),
                            durationMs = 600
                        )
                    }

                },
                modifier = Modifier.size(48.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF374151)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    // Zoom out to show more area – later: compute bounds of hubs + driver
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(center, 11f),
                            durationMs = 600
                        )
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF374151)
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Zoom to route",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int
): BitmapDescriptor {
    val drawable = ContextCompat.getDrawable(context, vectorResId)
        ?: return BitmapDescriptorFactory.defaultMarker()

    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

    val bm = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
    val canvas = Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}

fun computeBearing(points: List<LatLng>): Float {
    if (points.size < 2) return 0f

    val start = points[points.size - 2]
    val end = points[points.size - 1]

    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)

    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    var brng = Math.toDegrees(atan2(y, x))

    brng = (brng + 360.0) % 360.0
    return brng.toFloat() // 0 = North, clockwise
}

// --- Misc components --------------------------------------------------

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun HubListItem(
    hub: Hub,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (hub.visited) Color(0xFFDCFCE7) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (hub.visited) Color(0xFFBBF7D0) else Color(0xFFE5E7EB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (hub.visited) Color(0xFF10B981) else Color(0xFFDBEAFE)
                    )
                    .border(
                        width = if (hub.visited) 0.dp else 2.dp,
                        color = Color(0xFF3B82F6),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hub.visited) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Visited",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = hub.id.toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hub.name,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827),
                    fontSize = 14.sp
                )
                Text(
                    text = hub.address,
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (hub.visited) {
                Text(
                    text = "Visited",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF059669),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
