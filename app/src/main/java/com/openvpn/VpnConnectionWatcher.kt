/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import de.blinkt.openvpn.IVpnConnectionWatcher
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.core.ConnectionStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class VpnConnectionWatcher(context: Application) : IVpnConnectionWatcher(context) {
    val stateFlow = MutableStateFlow(ConnectionStatus.UNKNOWN_LEVEL.name)
    val messageFlow = MutableStateFlow("")

    override fun onUpdateState(
        state: String?,
        logmessage: String?,
        localizedMessage: String,
        level: ConnectionStatus,
        intent: Intent?
    ) {
        Log.i("VpnConnectionWatcher", "updateState: $level | $state | $logmessage | $localizedMessage")
        stateFlow.value = level.name.substringAfter("LEVEL_")
        messageFlow.value = localizedMessage
    }

    fun startVpn(activity: Activity) {
        GlobalScope.launch {
            val profile = getProfileByName("Japan102")
                ?: createVpnProfile("Japan102", Serts.Japan102)

            connect(activity, profile)
        }
    }

    fun stopVpn(activity: Activity) {
        disconnect(activity, true)
    }

    private fun createVpnProfile(name: String, ovpnConfig: String): VpnProfile {
        val profile = createProfile(name, ovpnConfig.byteInputStream()) {
            // VpnProfile from vpngate.net needs the CompatMode
            mCompatMode = 20300
            mUseLegacyProvider = true
        }
        return profile
    }

}