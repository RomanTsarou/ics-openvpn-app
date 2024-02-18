/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openvpn.ui.theme.IcsopenvpnTheme

@Composable
fun MainScreenContent(
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {},
    state: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "VPN state:\n$state", style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
        )
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            onClick = onConnectClick,
            enabled = state == "NOTCONNECTED",
        ) {
            Text(text = "Connect")
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            onClick = onDisconnectClick,
            enabled = state != "NOTCONNECTED",
        ) {
            Text(text = "Disconnect")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    IcsopenvpnTheme {
        MainScreenContent(state = "CONNECTED", message = "Waiting for server reply")
    }
}