package com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WarningPopup(
    isWarned: Boolean, warningContent: WarningContent,
    onDismiss: () -> Unit
) {
    if (isWarned){
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = warningContent.title) },
            text = { Text(text = warningContent.message) },
            confirmButton = {
                Text(
                    text = "OK",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onDismiss() },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

data class WarningContent(
    val title: String,
    val message: String
)