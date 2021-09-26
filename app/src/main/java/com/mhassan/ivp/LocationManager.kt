package com.mhassan.ivp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.location.*

@SuppressLint("MissingPermission")
class LocationManager(val mPlayer: SimpleExoPlayer, context: Context){
    private val locationUpdateInterval : Long = 3000
    private val locationFastUpdateInterval : Long = 1000
    private val videoReloadDistance : Int = 10
    private var locationRequest : LocationRequest

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // init var with null value
    private var lastKnownLocation: Location? = null

    init {

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocation = location
        }

        locationRequest = LocationRequest.create()
        locationRequest.interval = locationUpdateInterval
        locationRequest.fastestInterval = locationFastUpdateInterval
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return

            for (location in locationResult.locations){
                if(location != null){
                    if(lastKnownLocation == null){
                        lastKnownLocation = location
                    }else {
                        val distance = location.distanceTo(lastKnownLocation).toDouble()
                        if (distance > videoReloadDistance) {
                            lastKnownLocation = location
                            mPlayer.seekTo(0)
                            mPlayer.playWhenReady = true
                        }
                    }
                }
            }
        }
    }

    fun startLocationUpdates(){
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}