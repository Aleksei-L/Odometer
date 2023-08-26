package com.example.odometer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
	private lateinit var odometer: OdometerService
	private var bound = false
	private val connection = object : ServiceConnection {
		override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
			val odometerBinder = binder as OdometerService.OdometerBinder
			odometer = odometerBinder.getOdometer()
			bound = true
		}

		override fun onServiceDisconnected(componentName: ComponentName) {
			bound = false
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		displayDistance()
	}

	override fun onStart() {
		super.onStart()
		val intent = Intent(this, OdometerService::class.java)
		bindService(intent, connection, Context.BIND_AUTO_CREATE)
	}

	override fun onStop() {
		super.onStop()
		if (bound) {
			unbindService(connection)
			bound = false
		}
	}

	private fun displayDistance() {
		val distanceView = findViewById<TextView>(R.id.distance)
		val handler = Handler()
		handler.post(object : Runnable {
			override fun run() {
				var distance = 0.0
				if (bound)
					distance = odometer.getDistance()
				val distanceStr = String.format(
					Locale.getDefault(),
					"%1$,.2f miles", distance
				)
				distanceView.text = distanceStr
				handler.postDelayed(this, 1000)
			}
		})
	}
}
