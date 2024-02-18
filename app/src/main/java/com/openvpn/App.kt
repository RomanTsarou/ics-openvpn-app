/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.app.Application

class App : Application() {
    lateinit var vpnConnectionWatcher: VpnConnectionWatcher
        private set

    override fun onCreate() {
        super.onCreate()
        vpnConnectionWatcher = VpnConnectionWatcher(this)
    }
}