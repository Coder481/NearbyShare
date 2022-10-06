package com.hrithik.nearbyshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Strategy
import com.hrithik.nearbyshare.databinding.LayoutSearchingToSendBinding
import com.hrithik.nearbyshare.helpers.FileExtractor
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel : ViewModel() {



    val fileLiveData : MutableLiveData<File?> = MutableLiveData()
    fun getNthFile(n:Int){
        viewModelScope.launch {
            fileLiveData.postValue(FileExtractor.getNthVideoFile(n))
        }
    }

    fun startAdvertising(
        connectionsClient: ConnectionsClient,
        connectionLifecycleCallback: ConnectionLifecycleCallback,
        b: LayoutSearchingToSendBinding
    ){
        viewModelScope.launch {
            // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
            // Also we will demonstrate connectionLifecycleCallback later.
            val context = MyApp.getContext()
            connectionsClient.startAdvertising(
                "CodeName:${context.packageName}",
                context.packageName,
                connectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
            )
                .addOnFailureListener {
                    b.tvSearching.text = it.localizedMessage?:"Unknown error occurred"
                }
        }

    }
}