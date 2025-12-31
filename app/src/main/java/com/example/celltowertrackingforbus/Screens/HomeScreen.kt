package com.example.celltowertrackingforbus.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.celltowertrackingforbus.RoomDatabase.TowersDatabase
import com.example.celltowertrackingforbus.UploadingViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun HomeScreen(
    innerPaddingValues: PaddingValues,
    db: TowersDatabase,
    viewModel: UploadingViewModel
){
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues),
        contentAlignment = Alignment.Center
    ){
        if (!viewModel.isLoading) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ){
                Button(
                    onClick = { viewModel.uploadTowersSuspend(db, context) },
                    content = { Text("Create SQLite Database -\n Upload towers", textAlign = TextAlign.Center) }
                )
                Button(
                    onClick = { viewModel.emptyTowersTableSuspend(db) },
                    content = { Text("Empty Table") }
                )

                Button(
                    onClick = { viewModel.startTracking(db, context) },
                    modifier = Modifier.padding(top = 16.dp),
                    content = { Text("Start Live Tracking") }
                )

                Button(
                    onClick = { viewModel.stopTracking() },
                    modifier = Modifier.padding(top = 8.dp),
                    content = { Text("Stop Tracking") }
                )

                Text(
                    text = "Status: ${viewModel.locationStatus}",
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                viewModel.currentTowerInfo?.let { info ->
                    Text(
                        text = "Connected Tower (${info.type}):\nMCC: ${info.mcc}, MNC: ${info.mnc}\nLAC: ${info.lac}, CID: ${info.cid}, lat: ${info.lat}, long: ${info.long}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                viewModel.currentLocation?.let { tower ->
                    Text(
                        text = "Current Location:\nLat: ${tower.lat}\nLong: ${tower.long}",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                Button(
                    onClick = {viewModel.uploadMissingTowerSuspend(db)},
                    content = { Text("Push Tower DB", textAlign = TextAlign.Center) },
                    enabled = viewModel.isNotFound
                )
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen(
        PaddingValues(4.dp),
        db = Room.databaseBuilder(
            LocalContext.current,
            TowersDatabase::class.java,
            "towers.db",
        ).fallbackToDestructiveMigration().build(),
        viewModel = viewModel()
    )
}