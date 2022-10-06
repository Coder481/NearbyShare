package com.hrithik.nearbyshare.dialogs

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.hrithik.nearbyshare.AppViewModel
import com.hrithik.nearbyshare.databinding.LayoutSearchingToSendBinding
import kotlinx.coroutines.launch
import java.io.File

class AdvertisingDialog(private val vm:AppViewModel, private val context: Context, private val file : File) {

    private lateinit var dialog: AlertDialog
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

        dialog = AlertDialog.Builder(context)
            .setView(b.root)
            .setCancelable(false)
            .show()


        vm.startAdvertising(connectionsClient,connectionLifecycleCallback,b)

    }

    // Callback for connecting to other devices: both the advertiser and the discoverer must
    // implement this callback.
    private val connectionLifecycleCallback = object: ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
//            Log.d(TAG, "onConnectionInitiated: accepting connection")
            vm.viewModelScope.launch {
                b.apply{
                    animLottie.visibility = View.GONE
                    tvSearching.text = connectionInfo.endpointName
                    btnStartTransfer.apply{
                        visibility = View.VISIBLE
                        setOnClickListener {
                            connectionsClient.acceptConnection(endpointId,payloadCallback)
                                .addOnSuccessListener {
                                    Log.d("Advertising Dialog","Connection Accepted Successfully")
                                }
                                .addOnFailureListener {
                                    b.tvSearching.text = it.localizedMessage?:"Unknown error occurred"
                                    Log.d("Advertising Dialog",it.localizedMessage?:"Unknown error")
                                }
                            this.visibility = View.GONE
                        }
                    }
                }
                friendCodeName = connectionInfo.endpointName
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {

            vm.viewModelScope.launch{
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.

                        // if you were advertising, you can stop
                        connectionsClient.stopAdvertising()

                        friendEndpointId = endpointId
                        b.tvSearching.text = "Sending file..."
                        val pfd = context.contentResolver.openFileDescriptor(Uri.fromFile(file),"r")
                            ?: kotlin.run { b.tvSearching.text = "File not found"
                            return@launch}
                        connectionsClient.sendPayload(endpointId,Payload.fromFile(pfd))
                            .addOnSuccessListener {
                                b.tvSearching.text = "Payload Sent Success"
                            }
                            .addOnFailureListener {
                                Log.e("Advertising",it.toString())
                                b.tvSearching.text = it.localizedMessage ?: "Unknown error in sending payload"
                            }
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
        }

        override fun onDisconnected(endpointId: String) {
            b.tvSearching.text = "Connection disconnected"
        }
    }

    private val payloadCallback = object : PayloadCallback(){
        override fun onPayloadReceived(p0: String, payload: Payload) {
            // Not required as only sending payload, not receiving
            Log.e("AdvertisingDialog","-> onPayloadReceived")
        }

        override fun onPayloadTransferUpdate(p0: String, update: PayloadTransferUpdate) {
            val sent = "File sent: ${(update.bytesTransferred/update.totalBytes) * 100}%"
            b.tvSearching.text = if(update.status == PayloadTransferUpdate.Status.SUCCESS) {
                dialog.setCancelable(true)
//                connectionsClient.stopAllEndpoints()
                "Done"
            } else "Sent: ${update.bytesTransferred}bytes"
            Log.d("AdvertisingDialog","-> Sending file:$sent")
        }
    }





}