package com.hrithik.nearbyshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hrithik.nearbyshare.databinding.ActivityMainBinding
import com.hrithik.nearbyshare.dialogs.AdvertisingDialog
import com.hrithik.nearbyshare.dialogs.DiscoveringDialog
import com.hrithik.nearbyshare.helpers.FileExtractor

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.btnSend.setOnClickListener {
            val n = b.etVideoNumber.text.toString().toInt()
            val file = FileExtractor.getNthVideoFile(this, n)
            file?.let{
                AdvertisingDialog(this,it).show()
            } ?: kotlin.run { b.etVideoNumber.error = "Invalid file number" }
        }
        b.btnReceive.setOnClickListener {
            DiscoveringDialog(this).show()
        }
    }
}