package com.example.celltowertrackingforbus.features.inBusTracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.celltowertrackingforbus.BusTracking.BusLocation
import com.example.celltowertrackingforbus.BusTracking.Stop
import com.example.celltowertrackingforbus.BusTracking.CellInfo
import com.example.celltowertrackingforbus.BusTracking.StopStatus
import com.example.celltowertrackingforbus.features.onlineRemoteTracking.OnlineTrackingViewModel
import com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens.OnlineTrackingScreen
import com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens.TripOverviewHeader
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineBusTracker(
    busLocationFlow: StateFlow<BusLocation>,
    onBackClick: () -> Unit = {},
    viewModel: OnlineTrackingViewModel
) {
    // Collect state from the service's StateFlow
    val actualBusLocation by busLocationFlow.collectAsState()

    // Local override for simulation - when set, this takes priority
    var simulatedLocation by remember { mutableStateOf<BusLocation?>(null) }

    // Use simulated location if set, otherwise use actual
    val busLocation = simulatedLocation ?: actualBusLocation

    // State for contribution dialog
    var showContributionDialog by remember { mutableStateOf(false) }

    // State for tracking mode toggle (0 = Offline, 1 = GPS)
    var selectedTrackingMode by remember { mutableStateOf(0) }

    // List of simulated towers along the route
    val simulatedTowers = remember {
        listOf(
            Triple(
                Stop(
                    id = "stop_1",
                    name = "Haldwani Bus Stand",
                    lat = 29.2183,
                    long = 79.5130,
                    sequence = 1
                ),
                "Haldwani Central Tower",
                LatLng(29.2183, 79.5130)
            ),
            Triple(
                Stop(
                    id = "stop_2",
                    name = "Kathgodam",
                    lat = 29.2747,
                    long = 79.5234,
                    sequence = 2
                ),
                "Kathgodam Railway Tower",
                LatLng(29.2747, 79.5234)
            ),
            Triple(
                Stop(id = "stop_3", name = "Rudrapur", lat = 28.9845, long = 79.4043, sequence = 3),
                "Rudrapur Industrial Tower",
                LatLng(28.9845, 79.4043)
            ),
            Triple(
                Stop(id = "stop_5", name = "Rampur", lat = 28.8033, long = 79.0250, sequence = 5),
                "Rampur City Tower",
                LatLng(28.8033, 79.0250)
            ),
            Triple(
                Stop(
                    id = "stop_7",
                    name = "Moradabad",
                    lat = 28.8386,
                    long = 78.7733,
                    sequence = 7
                ),
                "Moradabad Junction Tower",
                LatLng(28.8386, 78.7733)
            ),
            Triple(
                Stop(id = "stop_9", name = "Gajraula", lat = 28.8456, long = 78.2311, sequence = 9),
                "Gajraula Highway Tower",
                LatLng(28.8456, 78.2311)
            ),
            Triple(
                Stop(id = "stop_11", name = "Hapur", lat = 28.7307, long = 77.7759, sequence = 11),
                "Hapur Bypass Tower",
                LatLng(28.7307, 77.7759)
            ),
            Triple(
                Stop(
                    id = "stop_13",
                    name = "Ghaziabad",
                    lat = 28.6692,
                    long = 77.4538,
                    sequence = 13
                ),
                "Ghaziabad Central Tower",
                LatLng(28.6692, 77.4538)
            ),
            Triple(
                Stop(
                    id = "stop_15",
                    name = "Kaushambi",
                    lat = 28.6448,
                    long = 77.3186,
                    sequence = 15
                ),
                "Kaushambi Metro Tower",
                LatLng(28.6448, 77.3186)
            ),
            Triple(
                Stop(
                    id = "stop_18",
                    name = "Anand Vihar ISBT",
                    lat = 28.6469,
                    long = 77.3164,
                    sequence = 18
                ),
                "Anand Vihar Terminal Tower",
                LatLng(28.6469, 77.3164)
            )
        )
    }

    // Contribution Dialog
    if (showContributionDialog) {
        AlertDialog(
            onDismissRequest = { showContributionDialog = false },
            title = {
                Text(
                    text = "Coming Soon!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "The contribution feature will be added in Round 2. Stay tuned!\nMeanwhile you can simulate connecting to a tower.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showContributionDialog = false }
                ) {
                    Text("Ok")
                }
            },
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = " Tracking",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
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
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tracking Mode Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTrackingMode == 0,
                        onClick = { selectedTrackingMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        Text("Offline Tracking")
                    }
                    SegmentedButton(
                        selected = selectedTrackingMode == 1,
                        onClick = { selectedTrackingMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "GPS",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    ) {
                        Text("GPS Tracking")
                    }
                }
            }
            when (selectedTrackingMode) {
                0 -> {
                    when (val location = busLocation) {
                        is BusLocation.Active -> {
                            // Header Section
                            TripOverviewHeaderCellTracking()

                            // Content
                            ContentInBusOfflineTracking(location)
                        }


                        is BusLocation.Unknown -> {
                            // Header Section for Unknown state

                            Box(
                                modifier = Modifier
                                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Searching for location",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cell Tower Tracking",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Content for Unknown state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Searching for cell signal...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Looking for matching cell towers in database",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Contribute Button Card
                                Card(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                    imageVector = Icons.Default.Route,
                                                    contentDescription = "Contribute",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Help improve tracking",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Contribute cell tower data to database",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                showContributionDialog = true
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(
                                                text = "Contribute to DB",
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                ) {
                                                    append("Disclaimer:")
                                                }
                                                append("\nIf search for location is not working this may be because you are far away from the bus or the tower is not registered in our database due to government not providing latest cell tower data publicly.\nYou can:\nSimulate connecting to an available tower in the route.")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )


                                        Spacer(modifier = Modifier.height(12.dp))


                                        Button(
                                            onClick = {
                                                // Pick a random tower from the list
                                                val randomTower = simulatedTowers.random()
                                                simulatedLocation = BusLocation.Active(
                                                    estimatedPosition = randomTower.third,
                                                    nearestStop = randomTower.first,
                                                    distanceToStop = (100..500).random().toFloat(),
                                                    status = listOf(
                                                        StopStatus.APPROACHING,
                                                        StopStatus.AT_STOP,
                                                        StopStatus.DEPARTED
                                                    ).random(),
                                                    towerName = randomTower.second,
                                                    cellInfo = CellInfo(
                                                        mcc = 404,
                                                        mnc = listOf(45, 72, 86, 10).random(),
                                                        lac = (1000..9999).random(),
                                                        cid = (10000L..99999L).random()
                                                    )
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 12.dp
                                            )
                                        ) {
                                            Text(
                                                text = "Simulate Random Tower",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // GPS Tracking Selected
                    OnlineTrackingScreen(
                        viewModel = viewModel
                    )
                }

            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineBusTrackerLoading() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Offline Tracking",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Initializing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Connecting to Service",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Loading Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Connecting to tracking service...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please wait while we initialize",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}