package org.avmedia.remotevideocam.frameanalysis.motion

import android.content.Context
import org.avmedia.remotevideocam.camera.DisplayToCameraEventBus
import org.avmedia.remotevideocam.display.CameraStatusEventBus
import org.avmedia.remotevideocam.display.ILocalConnection
import org.json.JSONObject

class MotionDetectionRemoteController(
    private val context: Context,
    private val connection: ILocalConnection,
) {
    private val notificationController = MotionNotificationController(context)

    fun setMotionDetection(enable: Boolean) {
        val value = if (enable) {
            MotionDetectionProtocol.ENABLED
        } else {
            MotionDetectionProtocol.DISABLED
        }
        val jsonString = "{${MotionDetectionProtocol.NAME}: \"$value\"}"
        connection.sendMessage(jsonString)
    }

    fun subscribe() {
        CameraStatusEventBus.addSubject(MotionDetectionProtocol.NAME)
        CameraStatusEventBus.subscribe(
            this.javaClass.simpleName,
            MotionDetectionProtocol.NAME,
        ) {
            if (MotionDetectionProtocol.DETECTED.name == it) {
                notificationController.showNotification("Motion Detected", "Details")
            }
        }
    }
}
