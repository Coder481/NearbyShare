package com.hrithik.nearbyshare.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.hrithik.nearbyshare.databinding.LayoutSearchingToReceiveBinding
import com.hrithik.nearbyshare.helpers.FilesFetcher

class DiscoveringDialog(private val context : Context) {

    private lateinit var b: LayoutSearchingToReceiveBinding
    private val connectionsClient: ConnectionsClient by lazy{ Nearby.getConnectionsClient(context)}
    lateinit var friendCodeName : String
    lateinit var friendEndpointId : String

    fun show(){
        b = LayoutSearchingToReceiveBinding.inflate(LayoutInflater.from(context))
        b.apply{
            animLottie.visibility = View.VISIBLE
            tvSearching.text = "Searching connection..."
            btnStartTransfer.visibility = View.GONE
        }

        AlertDialog.Builder(context)
            .setView(b.root)
            .setCancelable(false)
            .show()

        startDiscovery()
    }



    // Callback for connecting to other devices: both the advertiser and the discoverer must
    // implement this callback.
    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
//            Log.d(TAG, "onConnectionInitiated: accepting connection")
            /*b.apply{
                animLottie.visibility = View.GONE
                tvSearching.text = endpointId
                btnStartTransfer.apply{
                    visibility = View.VISIBLE
                    setOnClickListener {
                    }
                }
            }*/
            connectionsClient.acceptConnection(endpointId,payloadCallback)
            friendCodeName = connectionInfo.endpointName
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // We're connected! Can now start sending and receiving data.

                    // if you were discovery, you can stop
                    connectionsClient.stopDiscovery()

                    friendEndpointId = endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // The connection was rejected by one or both sides.
                    b.tvSearching.text = "Connection rejected"
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // The connection broke before it was able to be accepted.
                    b.tvSearching.text = result.status.statusMessage ?: "Unknown Error"
                }
                else -> {
                    // Unknown status code
                    b.tvSearching.text = "Unknown Error!"
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
//            Log.d(TAG, "onDisconnected: from friend")
            // perform necessary clean up
        }
    }

    private val payloadCallback = object : PayloadCallback(){
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if(payload.type == Payload.Type.BYTES)
                FilesFetcher.saveVideoFile(payload.asBytes())
            b.apply{
                tvSearching.text = "Done"
                btnStartTransfer.visibility =  View.GONE
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            val sent = "File received: ${update.bytesTransferred/update.totalBytes * 100}%"
            b.tvSearching.text = sent
        }
    }

    private fun startDiscovery(){
        val endpointDiscoveryCallback:EndpointDiscoveryCallback =
            object:EndpointDiscoveryCallback(){
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    b.apply {
                        tvSearching.text = info.endpointName
                        btnStartTransfer.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                connectionsClient.requestConnection(
                                    "CodeName:${context.packageName}",
                                    endpointId,
                                    connectionLifecycleCallback
                                )
                            }
                        }
                    }
                }

                override fun onEndpointLost(endpointId: String) {
//                    Log.d(TAG, "onEndpointLost")
                    b.tvSearching.text = "Connection Lost"
                }

            }
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(context.packageName,endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
    }
}