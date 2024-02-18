/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import de.blinkt.openvpn.LaunchVPN
import de.blinkt.openvpn.VpnProfile
import de.blinkt.openvpn.activities.DisconnectVPN
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.StatusListener
import de.blinkt.openvpn.core.VpnNotificationUtils
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream

class VpnConnectionWatcher(private val context: Application) {
    private val statusListener = StatusListener()
    val stateFlow = MutableStateFlow("")

    /**
     * Class for interacting with the main interface of the service.
     */
    private val stateListener = object : VpnStatus.StateListener {
        override fun updateState(
            state: String?,
            logmessage: String?,
            localizedResId: Int,
            level: ConnectionStatus,
            intent: Intent?
        ) {
//            when (level) {
//                LEVEL_CONNECTED -> TODO()
//                LEVEL_VPNPAUSED -> TODO()
//                LEVEL_CONNECTING_SERVER_REPLIED -> TODO()
//                LEVEL_CONNECTING_NO_SERVER_REPLY_YET -> TODO()
//                LEVEL_NONETWORK -> TODO()
//                LEVEL_NOTCONNECTED -> TODO()
//                LEVEL_START -> TODO()
//                LEVEL_AUTH_FAILED -> TODO()
//                LEVEL_WAITING_FOR_USER_INPUT -> TODO()
//                UNKNOWN_LEVEL -> TODO()
//            }
            Log.i("rom", "updateState: ${listOf(level, state, logmessage, localizedResId)}")
            Log.v("rom", "updateState localized: ${context.getString(localizedResId)}")
            stateFlow.value = level.name.substringAfter("LEVEL_")
        }

        init {
            VpnNotificationUtils.createNotificationChannels(context)
            statusListener.init(context)
        }

        override fun setConnectedVPN(uuid: String?) {
            Log.i("rom", "setConnectedVPN: uuid=$uuid")
        }

    }

    fun startVpn(activity: Activity) {
        GlobalScope.launch {
            val profile = ProfileManager.getInstance(context)
                .getProfileByName("Japan102")
                ?: createVpnProfile("Japan102", Serts.Japan102)

            val intent = Intent(context, LaunchVPN::class.java)
            intent.putExtra(LaunchVPN.EXTRA_KEY, profile.uuidString)
            intent.putExtra(LaunchVPN.EXTRA_HIDELOG, true)
            intent.putExtra(OpenVPNService.EXTRA_START_REASON, "my app")
            intent.setAction(Intent.ACTION_MAIN)
            activity.startActivity(intent)
        }
    }

    fun onResume() {
        VpnStatus.addStateListener(stateListener)
    }

    fun onPause() {
        VpnStatus.removeStateListener(stateListener)
    }

    fun stopVpn(activity: Activity) {
        val intent = Intent(context, DisconnectVPN::class.java)
//        intent.putExtra(DisconnectVPN.EXTRA_WITHOUT_CONFIRMATION,true)
        activity.startActivity(intent)
    }


    private fun createVpnProfile(name: String, ovpnConfig: String): VpnProfile {
        val profile = parseConfig(ovpnConfig.byteInputStream())
        profile.mName = name
        profile.mCompatMode = 20300
        profile.mUseLegacyProvider = true
        saveProfile(profile)
        return profile
    }

    private fun saveProfile(profile: VpnProfile) {
        val vpl = ProfileManager.getInstance(context)

        vpl.addProfile(profile)
        ProfileManager.saveProfile(context, profile)
        vpl.saveProfileList(context)
    }

    private fun parseConfig(inputStream: InputStream): VpnProfile {
        return inputStream.reader().use {
            ConfigParser().run {
                parseConfig(it)
                convertProfile()
            }
        }
    }
}