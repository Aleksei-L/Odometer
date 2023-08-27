package com.example.odometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
		if (ActivityCompat.checkSelfPermission(
				this,
				OdometerService.PERMISSION_STRING1
			) != PackageManager.PERMISSION_GRANTED
			||
			ActivityCompat.checkSelfPermission(
				this,
				OdometerService.PERMISSION_STRING2
			) != PackageManager.PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				this,
				arrayOf(OdometerService.PERMISSION_STRING1, OdometerService.PERMISSION_STRING2),
				PERMISSION_REQUEST_CODE
			)
		} else {
			val intent = Intent(this, OdometerService::class.java)
			bindService(intent, connection, Context.BIND_AUTO_CREATE)
		}
	}

	override fun onStop() {
		super.onStop()
		if (bound) {
			unbindService(connection)
			bound = false
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				val intent = Intent(this, OdometerService::class.java)
				bindService(intent, connection, Context.BIND_AUTO_CREATE)
			} else {
				val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

				val channel =
					NotificationChannel("123", "myChannel", NotificationManager.IMPORTANCE_DEFAULT)
				channel.description = "description"
				channel.enableLights(true)
				channel.enableVibration(true)
				manager.createNotificationChannel(channel)

				val builder = Notification.Builder(this, channel.id)
					.setSmallIcon(android.R.drawable.ic_menu_compass)
					.setContentTitle(resources.getString(R.string.app_name))
					.setContentText(resources.getString(R.string.permission_denied))
					.setAutoCancel(true)

				val intent = Intent(this, MainActivity::class.java)
				val pendingIntent =
					PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
				builder.setContentIntent(pendingIntent)

				manager.notify(NOTIFICATION_ID, builder.build())
			}
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

	companion object {
		private const val PERMISSION_REQUEST_CODE = 698
		private const val NOTIFICATION_ID = 423
	}
}
