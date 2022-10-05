package com.hrithik.nearbyshare.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.hrithik.nearbyshare.databinding.LayoutSearchingToSendBinding
import java.io.File

class AdvertisingDialog(private val context: Context, private val file : File) {

    private lateinit var b: LayoutSearchingToSendBinding
    private val connectionsClient: ConnectionsClient by lazy{ Nearby.getConnectionsClient(context)}
    lateinit var friendCodeName : String
    lateinit var friendEndpointId : String


    fun show(){
        b = LayoutSearchingToSendBinding.inflate(LayoutInflater.from(context))
        b.apply{
            animLottie.visibility = View.VISIBLE
            tvSearching.text = "Searching connection..."
            btnStartTransfer.visibility = View.GONE
        }

        AlertDialog.Builder(context)
            .setView(b.root)
            .setCancelable(false)
            .show()

        startAdvertising()

    }

    // Callback for connecting to other devices: both the advertiser and the discoverer must
    // implement this callback.
    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
//            Log.d(TAG, "onConnectionInitiated: accepting connection")
            b.apply{
                animLottie.visibility = View.GONE
                tvSearching.text = connectionInfo.endpointName
                btnStartTransfer.apply{
                    visibility = View.VISIBLE
                    setOnClickListener {
                        connectionsClient.acceptConnection(endpointId,payloadCallback)
                    }
                }
            }
            friendCodeName = connectionInfo.endpointName
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // We're connected! Can now start sending and receiving data.

                    // if you were advertising, you can stop
                    connectionsClient.stopAdvertising()

                    friendEndpointId = endpointId
                    connectionsClient.sendPayload(friendEndpointId,Payload.fromBytes(file.readBytes()))
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
        override fun onPayloadReceived(p0: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(p0: String, update: PayloadTransferUpdate) {
            val sent = "File sent: ${update.bytesTransferred/update.totalBytes * 100}%"
            b.tvSearching.text = sent
        }
    }

    private fun startAdvertising(){
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        // Also we will demonstrate connectionLifecycleCallback later.
        connectionsClient.startAdvertising(
            "CodeName:${context.packageName}",
            context.packageName,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        )
    }



}