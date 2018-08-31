package com.biglabs.mozo.sdk.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import com.biglabs.mozo.sdk.R
import com.biglabs.mozo.sdk.services.WalletService

internal class WrapperActivity : Activity() {

    private var targetPermission: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null && intent!!.hasExtra(TARGET_PERMISSION)) {
            this.targetPermission = intent.getStringExtra(TARGET_PERMISSION)
        } else {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        targetPermission?.run {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@WrapperActivity, this)) {
                AlertDialog.Builder(this@WrapperActivity)
                        .setTitle(R.string.dialog_storage_permission_title)
                        .setMessage(R.string.dialog_storage_permission_msg)
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setOnDismissListener { this@WrapperActivity.finish() }
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this@WrapperActivity, arrayOf(targetPermission), PERMISSIONS_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        finish()
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                WalletService.getInstance().onPermissionsResult(permissions, grantResults)
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST = 0x1
        private const val TARGET_PERMISSION = "target-permission"

        fun startRequestPermission(context: Context, permission: String) {
            val intent = Intent(context, WrapperActivity::class.java)
            intent.putExtra(TARGET_PERMISSION, permission)
            context.startActivity(intent)
        }
    }
}