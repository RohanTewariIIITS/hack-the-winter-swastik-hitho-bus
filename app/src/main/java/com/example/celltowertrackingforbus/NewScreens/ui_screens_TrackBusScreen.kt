package com.example.celltowertrackingforbus.NewScreens


import androidx.compose.foundation. background
import androidx. compose.foundation.clickable
import androidx. compose.foundation.layout.*
import androidx. compose.foundation.lazy.LazyColumn
import androidx.compose. foundation.lazy.items
import androidx.compose.foundation.shape. RoundedCornerShape
import androidx. compose.material.icons.Icons
import androidx.compose.material. icons.filled.ArrowBack
import androidx.compose.material.icons. filled.DirectionsBus
import androidx.compose.material.icons.filled. Menu
import androidx.compose.material3.*
import androidx.compose. runtime.*
import androidx.compose.ui. Alignment
import androidx.compose.ui. Modifier
import androidx.compose.ui. draw.clip
import androidx.compose.ui. graphics.Color
import androidx.compose.ui. text.font.FontWeight
import androidx.compose.ui.unit. dp
import androidx. compose.ui.unit.sp

data class RecentBus(
    val busNumber: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackBusScreen(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onBusClick: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var busNumber by remember { mutableStateOf("") }
    val recentBuses = remember {
        listOf(
            RecentBus("UP78KT5170"),
            RecentBus("HR58B0941"),
            RecentBus("UP14DT8196")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Track Your Bus",
                        color = Color.White,
                        fontWeight = FontWeight. Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                . fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = busNumber,
                onValueChange = { busNumber = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("BusNo") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = "Bus",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Button
            Button(
                onClick = { onSearch(busNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    . height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Search",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Tracking Section
            Text(
                text = "Recent tracking",
                fontSize = 16.sp,
                fontWeight = FontWeight. Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentBuses) { bus ->
                    RecentBusCard(
                        bus = bus,
                        onClick = { onBusClick(bus.busNumber) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentBusCard(
    bus: RecentBus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons. Default.DirectionsBus,
                    contentDescription = "Bus",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier. width(12.dp))

            Column {
                Text(
                    text = "Bus ${bus.busNumber}",
                    fontWeight = FontWeight. Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to view on map",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}