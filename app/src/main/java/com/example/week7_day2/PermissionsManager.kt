package com.example.week7_day2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open class PermissionsManager(val context : Context, var manager: IPermissionManager) {
    val PERMISSION_INDEX_ID = 777
//    private lateinit var manager: IPermissionManager


//    fun PermissionManager(context: Context) {
//        this.context = context
//        manager = context as IPermissionManager
//    }

    open fun checkForPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed; request the permission
                requestPermission()
            }
        } else {
            manager.onPermissionResult(true)
        }
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_INDEX_ID)
    }

    fun permissionResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_INDEX_ID -> {
                manager.onPermissionResult(grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    interface IPermissionManager {
        fun onPermissionResult(isGranted: Boolean)
    }
}