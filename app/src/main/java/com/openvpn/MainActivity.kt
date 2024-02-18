/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.openvpn.ui.theme.IcsopenvpnTheme


class MainActivity : ComponentActivity() {
    private lateinit var vpnConnectionWatcher: VpnConnectionWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnConnectionWatcher = (application as App).vpnConnectionWatcher

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by vpnConnectionWatcher.stateFlow.collectAsState()
                    val message by vpnConnectionWatcher.messageFlow.collectAsState()
                    MainScreenContent(
                        state = state,
                        message = message,
                        onConnectClick = { vpnConnectionWatcher.startVpn(this) },
                        onDisconnectClick = { vpnConnectionWatcher.stopVpn(this) })
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
