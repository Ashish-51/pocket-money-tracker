package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

@Composable
fun NotificationPermissionCard() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    if (!hasPermission) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FintechPrimary.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, FintechPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = "Info", tint = FintechPrimary)
                    Text("Enable Notifications", fontWeight = FontWeight.Bold, color = FintechOnSurface)
                }
                Text(
                    "Get reminders before your subscriptions are due to avoid surprise charges.",
                    fontSize = 14.sp,
                    color = FintechOnSurfaceVariant,
                    lineHeight = 20.sp
                )
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val activity = context as? android.app.Activity
                            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ?: false
                            
                            val prefs = context.getSharedPreferences("perms", Context.MODE_PRIVATE)
                            val firstTime = prefs.getBoolean("first_time_notif", true)
                            if (!shouldShowRationale && !firstTime) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } else {
                                prefs.edit().putBoolean("first_time_notif", false).apply()
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechPrimary)
                ) {
                    Text("Allow", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
