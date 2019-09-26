package com.example.week7_day2

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

open abstract class PermissionsManager {
    val PERMISSION_INDEX_ID = 777
    internal abstract var manager: IPermissionManager
    internal abstract var context: Context

    open fun checkForPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSION_INDEX_ID
                )
            }
        } else {
            manager.onPermissionResult(true)
        }
    }

    fun requestPermission() {

    }

    fun permissionResult() {

    }

    interface IPermissionManager {
        fun onPermissionResult(isGranted: Boolean)
    }
}