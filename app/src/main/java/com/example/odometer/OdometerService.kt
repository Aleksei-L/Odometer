package com.example.odometer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.ActivityCompat

class OdometerService : Service() {
	private val binder = OdometerBinder()
	private var listener: LocationListener? = null
	private var locManager: LocationManager? = null

	override fun onCreate() {
		super.onCreate()
		listener = LocationListener { location ->
			if (lastLocation == null)
				lastLocation = location
			distanceInMeters += location.distanceTo(lastLocation!!)
			lastLocation = location
		}
		locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

		if (ActivityCompat.checkSelfPermission(
				this,
				android.Manifest.permission.ACCESS_FINE_LOCATION
			) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this,
				android.Manifest.permission.ACCESS_COARSE_LOCATION
			) == PackageManager.PERMISSION_GRANTED
		) {
			val provider = locManager!!.getBestProvider(Criteria(), true)
			if (provider != null)
				locManager!!.requestLocationUpdates(provider, 1000, 1f, listener!!)
		}
	}

	override fun onBind(intent: Intent): IBinder = binder

	override fun onDestroy() {
		super.onDestroy()
		if (locManager != null && listener != null) {
			if (ActivityCompat.checkSelfPermission(
					this,
					android.Manifest.permission.ACCESS_FINE_LOCATION
				) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
					this,
					android.Manifest.permission.ACCESS_COARSE_LOCATION
				) == PackageManager.PERMISSION_GRANTED
			)
				locManager!!.removeUpdates(listener!!)
			locManager = null
			listener = null
		}
	}

	fun getDistance(): Double = distanceInMeters / 1609.344

	class OdometerBinder : Binder() {
		fun getOdometer(): OdometerService = OdometerService()
	}

	companion object {
		private var distanceInMeters = 0.0
		private var lastLocation: Location? = null
		const val PERMISSION_STRING1 = android.Manifest.permission.ACCESS_COARSE_LOCATION
		const val PERMISSION_STRING2 = android.Manifest.permission.ACCESS_FINE_LOCATION
	}
}
