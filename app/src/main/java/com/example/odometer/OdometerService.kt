package com.example.odometer

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.util.Random

class OdometerService : Service() {
	private val binder = OdometerBinder()
	private val random = Random()

	override fun onBind(intent: Intent): IBinder = binder

	fun getDistance(): Double = random.nextDouble()

	class OdometerBinder : Binder() {
		fun getOdometer(): OdometerService = OdometerService()
	}
}
