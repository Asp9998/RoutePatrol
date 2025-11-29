package com.aryanspatel.routepatrol.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.aryanspatel.routepatrol.MainActivity
import com.aryanspatel.routepatrol.R
import com.aryanspatel.routepatrol.domain.repository.TripRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class RoutePatrolLocationService : LifecycleService(){

    @Inject
    lateinit var tripRepository: TripRepository

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager

    private var currentFleetCode: String? = null
    private var currentTripId: String? = null
    private var isTracking = false

    private var lastRecordedLocation: Location? = null
    private val MIN_DISTANCE_METERS = 20f    // tweak this (10–30m is typical)

    companion object {
        private const val CHANNEL_ID = "routepatrol_trip_tracking"
        private const val NOTIFICATION_ID = 1001
        private const val LOCATION_INTERVAL_MS = 5_000L
        private const val LOCATION_FASTEST_INTERVAL_MS = 2_000L
        private const val ACTION_START = "RoutePatrolLocationService.ACTION_START"
        private const val ACTION_STOP = "RoutePatrolLocationService.ACTION_STOP"
        private const val EXTRA_FLEET_CODE = "extra_fleet_code"
        private const val EXTRA_TRIP_ID = "extra_trip_id"

        fun createStartIntent(
            context: Context,
            fleetCode: String,
            tripId: String
        ): Intent {
            return Intent(context, RoutePatrolLocationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_FLEET_CODE, fleetCode)
                putExtra(EXTRA_TRIP_ID, tripId)
            }
        }

        fun createStopIntent(context: Context): Intent{
            return Intent(context, RoutePatrolLocationService::class.java).apply {
                action = ACTION_STOP
            }
        }

    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
            .build()
    }

    private val locationCallBack = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            val last = lastRecordedLocation

            if (last != null) {
                val distance = last.distanceTo(loc)  // meters
                if (distance < MIN_DISTANCE_METERS) {
                    // Too small = likely jitter → ignore
                    return
                }
            }
            lastRecordedLocation = loc

            val fleetCode = currentFleetCode ?: return
            val tripId = currentTripId ?: return

            val lat = loc.latitude
            val lng = loc.longitude
            val timestamp = System.currentTimeMillis()

            // write to firestore and room
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    tripRepository.updateLastLocation(
                        fleetCode = fleetCode,
                        tripId = tripId,
                        lat = lat,
                        lng = lng,
                        timestamp = timestamp
                    )
                    tripRepository.addTripLocation(
                        fleetCode = fleetCode,
                        tripId = tripId,
                        lat = lat,
                        lng = lng,
                        timestamp = timestamp
                    )
                } catch (e: Exception){
                    // log / send to Crashlytics
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when(intent?.action){
            ACTION_START -> {
                val fleetCode = intent.getStringExtra(EXTRA_FLEET_CODE)
                val tripId = intent.getStringExtra(EXTRA_TRIP_ID)

                if(fleetCode.isNullOrBlank() || tripId.isNullOrBlank()) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                currentFleetCode = fleetCode
                currentTripId = tripId

                startForeground(NOTIFICATION_ID, buildNotification())
                startLocationUpdates()
            }

            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }



    private fun startLocationUpdates() {
        if(isTracking) return

        // Safety: check permission
        val fineGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if(!fineGranted && !coarseGranted){
            stopSelf()
            return
        }

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
        isTracking = true
    }

    private fun stopLocationUpdates() {
        if(!isTracking) return
        fusedClient.removeLocationUpdates(locationCallBack)
        isTracking = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RoutePatrol Trip Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        // tap on notification -> open app
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RoutePatrol")
            .setContentText("Tracking your trip in real time")
            .setSmallIcon(R.drawable.app_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }


}