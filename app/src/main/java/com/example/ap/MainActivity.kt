package com.example.ap

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ap.ui.theme.APTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.pm.PackageManager

class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var currentLocation by remember {
                mutableStateOf(LatLng(0.0, 0.0))
            }

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentLocation, 10f)
            }

            val db = remember { ParkingLocationDatabase.getDatabase(this) }
            val dao = db.parkingLocationDao()

            APTheme {
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        LocationScreen(
                            currentLocation = currentLocation,
                            permissions = permissions,
                            onLocationFetched = { latLng ->
                                currentLocation = latLng
                            },
                            camerapositionState = cameraPositionState,
                            onShowSavedLocations = {
                                navController.navigate("savedLocations")
                            },
                            onShowLastLocation = {
                                navController.navigate("lastLocation")
                            },
                            onSaveLocation = { lat, lng, note ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    dao.insert(
                                        ParkingLocation(
                                            latitude = lat,
                                            longitude = lng,
                                            note = note,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        )
                    }

                    composable("savedLocations") {
                        val context = LocalContext.current
                        SavedLocationsScreen(context = context)
                    }
                    composable("lastLocation") {
                        LastLocationScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationScreen(
    currentLocation: LatLng?,
    permissions: Array<String>,
    onLocationFetched: (LatLng) -> Unit,
    camerapositionState: CameraPositionState,
    onShowSavedLocations: () -> Unit,
    onShowLastLocation: () -> Unit,
    onSaveLocation: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResults ->
        val allGranted = permissionResults.values.all { it }
        if (allGranted) {
            getCurrentLocation(context, fusedLocationClient, onLocationFetched)
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camerapositionState
            ) {
                currentLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "You",
                        snippet = "You are here"
                    )

                    LaunchedEffect(location) {
                        camerapositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(location)
                                    .zoom(15f)
                                    .build()
                            ),
                            durationMs = 1000
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentLocation?.let {
                    "Your location: ${it.latitude}/${it.longitude}"
                } ?: "Location not fetched"
            )

            var note by remember { mutableStateOf("") }

            Button(onClick = {
                val allGranted = permissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                if (allGranted) {
                    getCurrentLocation(context, fusedLocationClient, onLocationFetched)
                } else {
                    launcherMultiplePermissions.launch(permissions)
                }
            }) {
                Text("Get your location")
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note about parking") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                if (currentLocation != null) {
                    onSaveLocation(currentLocation.latitude, currentLocation.longitude, note)
                    Toast.makeText(context, "Location saved", Toast.LENGTH_SHORT).show()
                    note = ""
                } else {
                    Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save location")
            }

            Button(onClick = {
                onShowLastLocation()
            }) {
                Text("Last saved location")
            }


            Button(onClick = onShowSavedLocations) {
                Text("Show saved locations")
            }
        }
    }
}

@Suppress("MissingPermission")
private fun getCurrentLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocationFetched: (LatLng) -> Unit
) {
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationFetched(LatLng(location.latitude, location.longitude))
                Toast.makeText(context, "Location fetched", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
}
