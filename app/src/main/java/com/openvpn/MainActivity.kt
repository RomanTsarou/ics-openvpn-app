/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.openvpn.ui.theme.IcsopenvpnTheme
import de.blinkt.openvpn.core.VpnNotificationUtils


class MainActivity : ComponentActivity() {
    private lateinit var vpnConnectionWatcher: VpnConnectionWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("rom","onCreate $this, $savedInstanceState")
        VpnNotificationUtils.createNotificationChannels(application)
        vpnConnectionWatcher = VpnConnectionWatcher(this)
        if (Build.VERSION.SDK_INT >= 33
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        setContent {
            IcsopenvpnTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by vpnConnectionWatcher.stateFlow.collectAsState()
                    MainContent(
                        state = state,
                        onConnectClick = { vpnConnectionWatcher.startVpn() },
                        onDisconnectClick = { vpnConnectionWatcher.stopVpn() })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vpnConnectionWatcher.onResume()
    }

    override fun onPause() {
        super.onPause()
        vpnConnectionWatcher.onPause()
    }
}


@Composable
fun MainContent(
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    state: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "State: $state", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 48.dp)
        )
        Button(onClick = onConnectClick) {
            Text(text = "Connect")
        }
        Button(onClick = onDisconnectClick) {
            Text(text = "Disconnect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    IcsopenvpnTheme {
        MainContent(state = "CONNECTED")
    }
}