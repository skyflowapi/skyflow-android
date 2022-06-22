/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.collect.elements.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator


object VibrationHelper {
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun vibrate(context: Context, duration: Int) {
        if (hasVibrationPermission(context)) {
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(duration.toLong())
        }
    }

    private fun hasVibrationPermission(context: Context): Boolean {
        return context.packageManager.checkPermission(Manifest.permission.VIBRATE,
                context.packageName) == PackageManager.PERMISSION_GRANTED
    }
}
