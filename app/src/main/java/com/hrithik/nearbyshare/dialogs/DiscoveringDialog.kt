package com.hrithik.nearbyshare.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.hrithik.nearbyshare.databinding.LayoutSearchingToReceiveBinding
import com.hrithik.nearbyshare.helpers.FilesFetcher

class DiscoveringDialog(private val context : Context) {

    private lateinit var dialog: AlertDialog
    private lateinit var b: LayoutSearchingToReceiveBinding
    private val connectionsClient: ConnectionsClient by lazy{ Nearby.getConnectionsClient(context)}
    lateinit var payload : Payload
    var connectionEnd = false

    fun show(){
        b = LayoutSearchingToReceiveBinding.inflate(LayoutInflater.from(context))
        b.apply{
            animLottie.visibility = View.VISIBLE
            tvSearching.text = "Searching connection..."
            btnStartTransfer.visibility = View.GONE
        }

        dialog = AlertDialog.Builder(context)
            .setView(b.root)
            .setCancelable(false)
            .show()

        startDiscovery()
    }



    // Callback for connecting to other devices
    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId,payloadCallback)
                .addOnFailureListener {
                    dialog.setCancelable(true)
                    b.tvSearching.text = it.localizedMessage ?: "Unknown connection error"
                }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // if you were discovery, you can stop
                    connectionsClient.stopDiscovery()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // The connection was rejected by one or both sides.
                    b.tvSearching.text = "Connection rejected"
                    dialog.setCancelable(true)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // The connection broke before it was able to be accepted.
                    b.tvSearching.text = result.status.statusMessage ?: "Unknown Error"
                    dialog.setCancelable(true)
                }
                else -> {
                    // Unknown status code
                    b.tvSearching.text = "Unknown Error!"
                    dialog.setCancelable(true)
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            if(!connectionEnd)b.tvSearching.text = "Connection disconnected"
        }
    }

    // Callback for payload shared
    private val payloadCallback = object : PayloadCallback(){
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // save payload in a variable, so that when transfer
            // succeed we can save payload as file in storage
            if(payload.type == Payload.Type.FILE)
                this@DiscoveringDialog.payload = payload

            Log.e("DiscoveringDialog","Receiving Complete:${payload.asFile()?.asUri()}")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // If file shared successfully show Done, and if not then show data transferred
            b.tvSearching.text = if(update.status == PayloadTransferUpdate.Status.SUCCESS) {
                dialog.setCancelable(true)
                FilesFetcher.saveFileFromUri(
                    payload
                )
                connectionEnd = true
                connectionsClient.disconnectFromEndpoint(endpointId)
                "Done\nConnection End"
            } else "Received: ${(update.bytesTransferred)/1024}KB \nTotal: ${(update.totalBytes)/1024}KB"
        }
    }

    // Start discovering connection
    private fun startDiscovery(){
        val endpointDiscoveryCallback:EndpointDiscoveryCallback =
            object:EndpointDiscoveryCallback(){
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    b.apply {
                        tvSearching.text = info.endpointName
                        animLottie.visibility = View.GONE
                        btnStartTransfer.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                requestConnection(endpointId)
                                this.visibility = View.GONE
                                b.tvSearching.text = "Sending request..."
                            }
                        }
                    }
                }

                override fun onEndpointLost(endpointId: String) {
                    b.tvSearching.text = "Connection Lost"
                }

            }

        connectionsClient.startDiscovery(context.packageName,endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
            .addOnFailureListener {
                b.tvSearching.text = it.localizedMessage?:"Unknown error occurred"
            }
    }

    private fun requestConnection(endpointId: String) {
        connectionsClient.requestConnection(
            "${Build.BRAND}: ${Build.MODEL}",
            endpointId,
            connectionLifecycleCallback
        )
            .addOnSuccessListener {
                Log.d("Discovering Dialog","Connection Requested Successfully")
            }
            .addOnFailureListener {
                b.tvSearching.text = it.localizedMessage?:"Unknown error occurred"
                Log.d("Discovering Dialog",it.localizedMessage?:"Unknown")
            }
    }
}