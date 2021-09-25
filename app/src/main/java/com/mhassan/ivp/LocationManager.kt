package com.mhassan.ivp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.location.*

@SuppressLint("MissingPermission")
class LocationManager(mPlayer: SimpleExoPlayer, context: Context){
    val mPlayer: SimpleExoPlayer = mPlayer
    val context: Context = context

    val locationUpdateInterval : Long = 3000
    val locationFastUpdateInterval : Long = 1000
    val videoReloadDistance : Int = 10
    lateinit var locationRequest : LocationRequest

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // init var with null value
    private var lastKnownLocaiton: Location? = null

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocaiton = location
        }

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(locationUpdateInterval);
        locationRequest.setFastestInterval(locationFastUpdateInterval);
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return

            for (location in locationResult.locations){
                if(location != null){
                    if(lastKnownLocaiton == null){
                        lastKnownLocaiton = location
                    }else {
                        val distance = location.distanceTo(lastKnownLocaiton).toDouble()
                        if (distance > videoReloadDistance) {
                            lastKnownLocaiton = location
                            mPlayer.seekTo(0);
                            mPlayer.setPlayWhenReady(true);
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