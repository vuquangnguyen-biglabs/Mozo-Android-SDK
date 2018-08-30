package com.biglabs.mozo.sdk.services

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials
import com.estimote.proximity_sdk.api.ProximityObserver
import com.estimote.proximity_sdk.api.ProximityObserverBuilder
import com.estimote.proximity_sdk.api.ProximityZoneBuilder

class BeaconService private constructor(context: Context) {

    private val proximityObserver: ProximityObserver
    private var observationHandler: ProximityObserver.Handler? = null

    init {
        proximityObserver = ProximityObserverBuilder(context, EstimoteCloudCredentials(ESTIMOTE_APP_ID, ESTIMOTE_APP_TOKEN))
                .withAnalyticsReportingDisabled()
                .onError {
                    Toast.makeText(context, "proximityObserver onError:" + it.message, Toast.LENGTH_SHORT).show()
                }
                .build()

        startRanging(context)
    }

    private fun startRanging(context: Context) {
        Handler().postDelayed({
            Toast.makeText(context, "start Ranging", Toast.LENGTH_SHORT).show()
            val venueZone = ProximityZoneBuilder()
                    .forTag("BiglabsCompany")
                    .inFarRange()
                    .onEnter {
                        Toast.makeText(context, "venueZone onEnter", Toast.LENGTH_SHORT).show()
                    }
                    .onExit {
                        Toast.makeText(context, "venueZone onExit", Toast.LENGTH_SHORT).show()
                    }
                    .onContextChange {
                        Toast.makeText(context, "venueZone onContextChange", Toast.LENGTH_SHORT).show()
                    }
                    .build()
//            observationHandler = proximityObserver.startObserving(venueZone)

        }, 5000)
    }

    fun stopScan() {
        observationHandler?.stop()
    }

    companion object {

        private const val ESTIMOTE_APP_ID = "mozosdk-oqh"
        private const val ESTIMOTE_APP_TOKEN = "76edde3928a11914b0465a82f13ba3cc"

        @Volatile
        private var instance: BeaconService? = null

        @Synchronized
        internal fun getInstance(context: Context): BeaconService {
            if (instance == null) {
                instance = BeaconService(context)
            }
            return instance as BeaconService
        }
    }
}