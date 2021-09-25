package com.mhassan.ivp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {

    // request code used when requesting permissions
    private val PERMISSION_REQUEST_CODE = 88

    // delay before video starts playing
    private val delayBeforeStart = 4000L
    private lateinit var locationManager: LocationManager
    private lateinit var mPlayer : SimpleExoPlayer

    // video url
    val videoUri : String = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val txtLocation : TextView = findViewById(R.id.txtLocation)

        // used for testing will be removed when done using it 
        val playerView : PlayerView = findViewById<PlayerView>(R.id.player_view)

        mPlayer = SimpleExoPlayer.Builder(this).build()
        playerView.player = mPlayer

        val mediaItem: MediaItem = MediaItem.fromUri(videoUri)
        mPlayer.setMediaItem(mediaItem)
        mPlayer.prepare()

        // start video play after delay.
        runBlocking {
            launch { // launch a new coroutine
                delay(delayBeforeStart) // non-blocking delay
                mPlayer.play() // start the video play
            }
        }

        locationManager = LocationManager(mPlayer, this)
    }

    override fun onResume() {
        super.onResume()
        // check location permissions
        if (checkPermission()) {
            // if premission granted start location service
            locationManager.startLocationService()
        }else{
            // if permissions not granted request permission.
            requestPermission();
        }
    }

    override fun onPause() {
        super.onPause()

        locationManager.stopLocationUpdates()
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val fineLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val coarseLocationAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (fineLocationAccepted && coarseLocationAccepted) {
                    locationManager.startLocationService()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION),
                                                    PERMISSION_REQUEST_CODE)
                                        }
                                    })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }
}