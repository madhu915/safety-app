package com.livinideas.googlemapsdirectionsample

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {
    private val scope= CoroutineScope(SupervisorJob()+Dispatchers.IO)
    private lateinit var locationClient: LocationTracking
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient= LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            START->start()
            STOP->stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(){
        val notification=NotificationCompat.Builder(this,"location")
            .setContentTitle("Tracking intialised...")
            .setContentText("Location:NA")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        locationClient
            .getCurrentLocation(10000L)
            .catch { e->e.printStackTrace() }
            .onEach { location ->
                val lat=location.latitude.toString()
                val lng=location.longitude.toString()
                val coordinates= "This is my location : ( $lat, $lng )"


                val updates=notification.setContentText(
                    "Location Coordinates:($lat,$lng)"
                )
                notificationManager.notify(1,updates.build())
                val message = "Hi this is my location 📍: ( $lat , $lng )"
                val phno="9361038746"

                /** Creating a pending intent which will be broadcasted when an sms message is successfully sent */
                val piSent = PendingIntent.getBroadcast(baseContext, 0, Intent("sent_msg"), FLAG_MUTABLE)

                /** Creating a pending intent which will be broadcasted when an sms message is successfully delivered */
                val piDelivered =
                    PendingIntent.getBroadcast(baseContext, 0, Intent("delivered_msg"), FLAG_MUTABLE)

                /** Getting an instance of SmsManager to sent sms message from the application*/
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage("$phno", null, message, piSent, piDelivered)
            }
            .launchIn(scope)
        startForeground(1,notification.build())
    }

    private fun stop(){
        stopForeground(true)
        stopSelf()

    }


    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object{
        const val START="START"
        const val STOP="STOP"

    }
}
