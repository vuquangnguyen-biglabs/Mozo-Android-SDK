package com.biglabs.mozo.sdk.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import java.io.Serializable

internal class WrapperActivity : AppCompatActivity() {

    private var targetPermission: String? = null
    private var targetCallback: WrapperCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null && intent!!.hasExtra(TARGET_PERMISSION)) {
            this.targetPermission = intent.getStringExtra(TARGET_PERMISSION)
            this.targetCallback = intent.getSerializableExtra(TARGET_CALLBACK) as WrapperCallback
        } else {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        targetPermission?.let {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("eto")
                        .setMessage("ano")
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(targetPermission), PERMISSIONS_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    targetCallback?.callback?.invoke()
                }
                finish()
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST = 0x1
        private const val TARGET_PERMISSION = "target-permission"
        private const val TARGET_CALLBACK = "target-callback"

        fun startRequestPermission(context: Context, permission: String, callback: () -> Unit) {
            val intent = Intent(context, WrapperActivity::class.java)
            intent.putExtra(TARGET_PERMISSION, permission)
            intent.putExtra(TARGET_CALLBACK, WrapperCallback(callback))
            context.startActivity(intent)
        }
    }

    private class WrapperCallback(val callback: () -> Unit) : Serializable
}