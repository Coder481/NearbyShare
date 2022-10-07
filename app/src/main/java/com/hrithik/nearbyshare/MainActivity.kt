package com.hrithik.nearbyshare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.hrithik.nearbyshare.databinding.ActivityMainBinding
import com.hrithik.nearbyshare.dialogs.AdvertisingDialog
import com.hrithik.nearbyshare.dialogs.DiscoveringDialog

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val vm : AppViewModel by lazy {
        ViewModelProvider(this)[AppViewModel::class.java]
    }

    // Permissions array based on OS Version
    private val permissionsArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        vm.fileLiveData.observe(this){
            it?.let{
                AdvertisingDialog(vm, this,it).show()
            } ?: kotlin.run { b.etVideoNumber.error = "File not found" }
        }
        b.btnSend.setOnClickListener {
            // Check permission before performing action
            if(askPermissions()) return@setOnClickListener

            val str = b.etVideoNumber.text.toString()
            if(str.isEmpty()) {
                b.etVideoNumber.error = "Invalid file number"
                return@setOnClickListener
            }
            val n = b.etVideoNumber.text.toString().toInt()
            vm.getNthFile(n)
        }
        b.btnReceive.setOnClickListener {
            // Check permission before performing action
            if(askPermissions()) return@setOnClickListener

            DiscoveringDialog(this).show()
        }
    }

    private fun askPermissions() : Boolean{
        if(!genPermissionsAreGranted()) {
            Toast.makeText(this,"Allow permission to work properly",Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(permissionsArray)
            return true
        }
        return false

    }

    // Check permissions granted based on OS version
    private fun genPermissionsAreGranted(): Boolean {
        val res = (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            res && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
                    && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        else res
    }
}