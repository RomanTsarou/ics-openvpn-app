/*
 * Copyright (c) 2012-2024 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.openvpn

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback

class VpnConnectionWatcher(private val activity: Activity) {

    private var vpnStart: Boolean = false
    private var mService: IOpenVPNAPIService? = null

    /**
     * Class for interacting with the main interface of the service.
     */
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = IOpenVPNAPIService.Stub.asInterface(service)
            try {
                // Request permission to use the API
                val i: Intent? = mService!!.prepare(activity.packageName)
                if (i != null) {
                    activity.startActivityForResult(i, ICS_OPENVPN_PERMISSION)
                } else {
                    onActivityResult(ICS_OPENVPN_PERMISSION, Activity.RESULT_OK, null)
                }
            } catch (e: RemoteException) {
//                logger.log("openvpn service connection failed: " + e.message)
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null
        }
    }


    private val mCallback: IOpenVPNStatusCallback = object : IOpenVPNStatusCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        @Throws(RemoteException::class)
        override fun newStatus(uuid: String?, state: String?, message: String?, level: String?) {
            Log.i("rom", "newStatus $level: $state - $message")
//            val msg = Message.obtain(
//                mHandler, MainFragment.MSG_UPDATE_STATE,
//                "$state|$message"
//            )
//            if (state == "AUTH_FAILED" || state == "CONNECTRETRY") {
//                auth_failed = true
//            }
//            if (!auth_failed) {
//                try {
//                    setStatus(state)
//                    updateConnectionStatus(state)
//                } catch (e: Exception) {
//                    logger.log("openvpn status callback failed: " + e.message)
//                    e.printStackTrace()
//                }
//                msg.sendToTarget()
//            }
//            if (auth_failed) {
//                binding.logTv.setText("AUTHORIZATION FAILED!!")
//                setStatus("CONNECTRETRY")
//            }
//            if (state == "CONNECTED") {
//                auth_failed = false
//                if (ActivityCompat.checkSelfPermission(
//                        getContext(),
//                        Manifest.permission.POST_NOTIFICATIONS
//                    ) !== PackageManager.PERMISSION_GRANTED
//                ) {
//                    ActivityCompat.requestPermissions(
//                        getActivity(),
//                        arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
//                        MainFragment.NOTIFICATIONS_PERMISSION_REQUEST_CODE
//                    )
//                }
//                bindTimerService()
//            } else {
//                unbindTimerService()
//            }
        }
    }

    /**
     * Prepare for vpn connect with required permission
     */

    fun prepareVpn() {
        if (hasInternet()) runCatching {
            // Checking permission for network monitor
            val intent = mService!!.prepareVPNService()
            if (intent != null) {
                activity.startActivityForResult(intent, 1)
            } else {
                startVpn() //Already have permission
            }

            // Update connection status
            status("connect")
        }.onFailure { it.printStackTrace() }
        else {
            println("you have no internet connection !!")
        }
    }

    private fun startVpn() {
        runCatching {
            val config = Serts.Japan
            val profile = mService!!.addNewVPNProfile("Japan", false, config)
            Log.i("rom","profile.mUUID=${profile.mUUID}")
            mService!!.startProfile(profile.mUUID)
            mService!!.startVPN(config)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun hasInternet(): Boolean {
        return true // FIXME
    }

    fun onResume() {
        bindService()
    }

    fun onPause() {
        activity.unbindService(mConnection)
    }

    /**
     * Stop vpn
     * @return boolean: VPN status
     */
    fun stopVpn(): Boolean {
        try {
            mService?.disconnect()
            status("connect")
            vpnStart = false
            return true
        } catch (e: RemoteException) {
//            logger.log("openvpn disconnect failed: " + e.message)
            e.printStackTrace()
        }
        return false
    }

//    /**
//     * Resume vpn
//     * @return boolean: VPN status
//     */
//    fun resumeVpn() {
//        try {
//            mService!!.resume()
//            status("connected")
//            vpnStart = true
//        } catch (e: RemoteException) {
////            logger.log("openvpn connection resume failed: " + e.message)
//            e.printStackTrace()
//        }
//    }

    private fun status(value: String) {
        Log.i("rom", "Status: $value")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ICS_OPENVPN_PERMISSION) {
            try {
                mService!!.registerStatusCallback(mCallback)
            } catch (e: RemoteException) {
//                logger.log("openvpn status callback failed: " + e.message)
                e.printStackTrace()
            }
        }
    }

    private fun bindService() {
        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)
        icsopenvpnService.setPackage(activity.packageName)
        activity.bindService(icsopenvpnService, mConnection, Context.BIND_AUTO_CREATE)
    }

}

private const val ICS_OPENVPN_PERMISSION = 7