/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn

import android.app.Activity
import android.app.Application
import android.content.Intent
import de.blinkt.openvpn.activities.DisconnectVPN
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.ConnectionStatus
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.StatusListener
import de.blinkt.openvpn.core.VpnNotificationUtils
import de.blinkt.openvpn.core.VpnStatus
import java.io.InputStream

abstract class IVpnConnectionWatcher(val context: Application) {
    private val statusListener = StatusListener()
    private val stateListener = object : VpnStatus.StateListener {
        override fun updateState(
            state: String?,
            logmessage: String?,
            localizedResId: Int,
            level: ConnectionStatus,
            intent: Intent?
        ) {
            onUpdateState(state, logmessage, context.getString(localizedResId), level, intent)
        }

        override fun setConnectedVPN(uuid: String?) = Unit
    }

    init {
        VpnNotificationUtils.createNotificationChannels(context)
        statusListener.init(context)
    }

    abstract fun onUpdateState(
        state: String?,
        logmessage: String?,
        localizedMessage: String,
        level: ConnectionStatus,
        intent: Intent?
    )

    fun onResume() {
        VpnStatus.addStateListener(stateListener)
    }

    fun onPause() {
        VpnStatus.removeStateListener(stateListener)
    }

    fun connect(activity: Activity, profile: VpnProfile) {
        val intent = Intent(activity, LaunchVPN::class.java)
        intent.putExtra(LaunchVPN.EXTRA_KEY, profile.uuidString)
        intent.putExtra(LaunchVPN.EXTRA_HIDELOG, true)
        intent.putExtra(OpenVPNService.EXTRA_START_REASON, "my app")
        intent.setAction(Intent.ACTION_MAIN)
        activity.startActivity(intent)
    }

    fun disconnect(activity: Activity, withoutConfirmation: Boolean) {
        val intent = Intent(activity, DisconnectVPN::class.java)
        intent.putExtra(DisconnectVPN.EXTRA_WITHOUT_CONFIRMATION, withoutConfirmation)
        activity.startActivity(intent)
    }

    fun getProfileByName(name: String): VpnProfile? {
        return ProfileManager.getInstance(context)
            .getProfileByName(name)
    }

    fun createProfile(
        name: String,
        ovpnInputStream: InputStream,
        dsl: VpnProfile.() -> Unit
    ): VpnProfile {
        val profile = parseConfig(ovpnInputStream)
        profile.mName = name
        dsl(profile)
        saveProfile(profile)
        return profile
    }

    private fun saveProfile(profile: VpnProfile) {
        val profileManager = ProfileManager.getInstance(context)
        profileManager.addProfile(profile)
        ProfileManager.saveProfile(context, profile)
        profileManager.saveProfileList(context)
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