package com.savent.recognition.face

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    private var networkRequest: NetworkRequest? = null

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
            networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    }

    @SuppressLint("NewApi")
    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {

            if(isCurrentOnline())
                launch { send(ConnectivityObserver.Status.Available) }
            else
                launch { send(ConnectivityObserver.Status.Unavailable) }

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(ConnectivityObserver.Status.Available) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.Unavailable) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(ConnectivityObserver.Status.Losing) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.Lost) }
                }


            }

            networkRequest?.let { connectivityManager.registerNetworkCallback(it, callback) }
                ?: connectivityManager.registerDefaultNetworkCallback(callback)

            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }

    private fun isCurrentOnline(): Boolean{
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?: return false

        if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            return true

        return false
    }
}