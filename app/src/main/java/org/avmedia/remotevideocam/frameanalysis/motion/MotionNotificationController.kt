package org.avmedia.remotevideocam.frameanalysis.motion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.avmedia.remotevideocam.R

private const val CHANNEL_MOTION_DETECTED = "motion_detected"
private const val ID_MOTION_DETECTED = 1
private val VIBRATION_PATTERN = longArrayOf(0, 250, 250, 250)

class MotionNotificationController(private val context: Context) {

    private val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(CHANNEL_MOTION_DETECTED, name, importance).apply {
                description = descriptionText
                vibrationPattern = VIBRATION_PATTERN
                setSound(notificationUri, audioAttributes)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, text: String) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            showToast(title)
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_MOTION_DETECTED)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_motion_detection)
            .setContentText(text)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setVibrate(VIBRATION_PATTERN)
            .setSound(notificationUri)
            .build()

        NotificationManagerCompat.from(context).cancel(ID_MOTION_DETECTED)
        NotificationManagerCompat.from(context).notify(ID_MOTION_DETECTED, notification)
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}