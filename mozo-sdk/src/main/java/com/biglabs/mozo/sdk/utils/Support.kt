package com.biglabs.mozo.sdk.utils

import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class Support {
    companion object {

        fun generateQRCode(str: String, size: Int) = BarcodeEncoder().encodeBitmap(str, BarcodeFormat.QR_CODE, size, size)
    }
}