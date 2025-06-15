package com.example.ap

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsScreen(context: Context) {
    val db = remember { ParkingLocationDatabase.getDatabase(context) }
    val dao = remember { db.parkingLocationDao() }
    val coroutineScope = rememberCoroutineScope()
    var locations by remember { mutableStateOf<List<ParkingLocation>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val result = dao.getAllLocations()
            locations = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Saved Locations") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (locations.isEmpty()) {
                Text("No saved locations.")
            } else {
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                locations.forEach { location ->
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Text("Lat: ${location.latitude}, Lng: ${location.longitude}", fontWeight = FontWeight.Bold)
                        if (location.note.isNotEmpty()) {
                            Text("Note: ${location.note}")
                        }
                        Text("Saved at: ${formatter.format(Date(location.timestamp))}", fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(onClick = {
                            val gmmIntentUri =
                                "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Zapisana+lokalizacja)".toUri()
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
}
