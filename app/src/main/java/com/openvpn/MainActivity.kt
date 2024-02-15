/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openvpn.ui.theme.IcsopenvpnTheme


class MainActivity : ComponentActivity() {
    private lateinit var vpnConnectionWatcher: VpnConnectionWatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnConnectionWatcher = VpnConnectionWatcher(this)

        setContent {
            IcsopenvpnTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(onConnectClick = { vpnConnectionWatcher.prepareVpn() },
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        vpnConnectionWatcher.onActivityResult(requestCode, resultCode, data)
    }
}


@Composable
fun MainContent(
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
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
        MainContent()
    }
}