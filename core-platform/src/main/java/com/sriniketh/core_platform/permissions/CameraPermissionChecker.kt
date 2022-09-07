package com.sriniketh.core_platform.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import javax.inject.Inject

class CameraPermissionCheckerImpl @Inject constructor() : CameraPermissionChecker {

    override fun hasPermissionBeenGranted(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun requestCameraPermission(fragment: Fragment, successCallback: () -> Unit, failureCallback: () -> Unit) {
        requestPermssionLauncher(fragment, successCallback, failureCallback).launch(Manifest.permission.CAMERA)
    }

    private fun requestPermssionLauncher(fragment: Fragment, successCallback: () -> Unit, failureCallback: () -> Unit) =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                successCallback()
            } else {
                failureCallback()
            }
        }
}

interface CameraPermissionChecker {
    fun hasPermissionBeenGranted(context: Context): Boolean
    fun requestCameraPermission(fragment: Fragment, successCallback: () -> Unit, failureCallback: () -> Unit)
}
