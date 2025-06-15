package com.example.ap

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastLocationScreen() {
    val context = LocalContext.current
    val db = remember { ParkingLocationDatabase.getDatabase(context) }
    val dao = db.parkingLocationDao()

    var lastLocation by remember { mutableStateOf<ParkingLocation?>(null) }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(true) {
        val latest = withContext(Dispatchers.IO) {
            dao.getLastLocation()
        }
        lastLocation = latest
        latest?.let {
            cameraPositionState.move(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude),
                    16f
                )
            )

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Last Saved Location") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (lastLocation == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No location saved yet.")
                }
            } else {
                val latLng = com.google.android.gms.maps.model.LatLng(
                    lastLocation!!.latitude,
                    lastLocation!!.longitude
                )

                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Saved Location"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(lastLocation!!.timestamp))

                    Text("Note: ${lastLocation!!.note}", style = MaterialTheme.typography.bodyLarge)
                    Text("Saved on: $date", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = {
                        val gmmIntentUri =
                            "geo:${lastLocation!!.latitude},${lastLocation!!.longitude}?q=${lastLocation!!.latitude},${lastLocation!!.longitude}(Zapisana+lokalizacja)".toUri()
                        val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            Toast.makeText(context, "Google Maps not found", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Open in Google Maps")
                    }
                }

            }
        }
    }
}
