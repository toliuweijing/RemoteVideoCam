package org.avmedia.remotevideocam.frameanalysis.motion

import android.content.Context
import org.avmedia.remotevideocam.display.CameraStatusEventBus

class MotionDetectionRemoteController(
    private val context: Context,
) {
    private val notificationController = MotionNotificationController(context)

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
