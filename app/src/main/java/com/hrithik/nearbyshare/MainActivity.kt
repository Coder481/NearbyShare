package com.hrithik.nearbyshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.hrithik.nearbyshare.databinding.ActivityMainBinding
import com.hrithik.nearbyshare.dialogs.AdvertisingDialog
import com.hrithik.nearbyshare.dialogs.DiscoveringDialog
import com.hrithik.nearbyshare.helpers.FileExtractor

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val vm : AppViewModel by lazy {
        ViewModelProvider(this)[AppViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        vm.fileLiveData.observe(this){
            it?.let{
                AdvertisingDialog(vm, this,it).show()
            } ?: kotlin.run { b.etVideoNumber.error = "Invalid file number" }
        }
        b.btnSend.setOnClickListener {
            val str = b.etVideoNumber.text.toString()
            if(str.isEmpty()) {
                b.etVideoNumber.error = "Invalid file number"
                return@setOnClickListener
            }
            val n = b.etVideoNumber.text.toString().toInt()
            vm.getNthFile(n)
        }
        b.btnReceive.setOnClickListener {
            DiscoveringDialog(this).show()
        }
    }
}