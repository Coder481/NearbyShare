package com.hrithik.nearbyshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.hrithik.nearbyshare.databinding.ActivityMainBinding
import com.hrithik.nearbyshare.helpers.SortFiles

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val connectionsClient: ConnectionsClient by lazy{ Nearby.getConnectionsClient(this)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

    }
}