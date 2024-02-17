/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import de.blinkt.openvpn.R

object VpnNotificationUtils {
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Background message
//        var name: CharSequence? =context. getString(R.string.channel_name_background)
//        var channel = NotificationChannel(
//            OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
//            name, NotificationManager.IMPORTANCE_MIN
//        )
//        channel.description = context.getString(R.string.channel_description_background)
//        channel.enableLights(false)
//        channel.lightColor = Color.DKGRAY
//        mNotificationManager.createNotificationChannel(channel)

        // Connection status change messages
        val channel = NotificationChannel(
            OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
            context.getString(R.string.channel_name_status),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = context.getString(R.string.channel_description_status)
//        channel.enableLights(true)
//        channel.lightColor = Color.BLUE
        notificationManager.createNotificationChannel(channel)


        // Urgent requests, e.g. two factor auth
//        name = context.getString(R.string.channel_name_userreq)
//        mChannel = NotificationChannel(
//            OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
//            name, NotificationManager.IMPORTANCE_HIGH
//        )
//        mChannel.description = getString(R.string.channel_description_userreq)
//        mChannel.enableVibration(true)
//        mChannel.lightColor = Color.CYAN
//        mNotificationManager.createNotificationChannel(mChannel)
    }
}