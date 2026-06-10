package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun NotificationIcon(viewModel: MainViewModel) {
    val notifications by viewModel.notifications.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = FintechOnSurface)
            if (notifications.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(FintechError)
                    )
                }
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(FintechSurface).widthIn(max = 300.dp)
        ) {
            if (notifications.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No upcoming payments", color = FintechOnSurfaceVariant, fontSize = 14.sp) },
                    onClick = { expanded = false }
                )
            } else {
                Text(
                    text = "Upcoming Payments",
                    fontWeight = FontWeight.Bold,
                    color = FintechOnSurface,
                    modifier = Modifier.padding(16.dp, 8.dp)
                )
                HorizontalDivider(color = FintechOutline.copy(alpha = 0.2f))
                notifications.forEach { msg ->
                    DropdownMenuItem(
                        text = { Text(msg, color = FintechOnSurface, fontSize = 14.sp, lineHeight = 18.sp) },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}
