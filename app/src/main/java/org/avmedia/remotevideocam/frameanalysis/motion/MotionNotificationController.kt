package org.avmedia.remotevideocam.frameanalysis.motion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.avmedia.remotevideocam.R

private const val CHANNEL_MOTION_DETECTED = "motion_detected"
private const val ID_MOTION_DETECTED = 0

class MotionNotificationController(private val context: Context) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_MOTION_DETECTED, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            showToast("motion detected")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTION_DETECTED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_motion_detection)
            .setContentText("A motion was detected at 5:PM")
            .setContentTitle("Motion Detected")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ID_MOTION_DETECTED, notification)
    }


    fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}