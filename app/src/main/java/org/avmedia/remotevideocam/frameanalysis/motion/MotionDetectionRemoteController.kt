package org.avmedia.remotevideocam.frameanalysis.motion

import android.widget.ImageButton
import org.avmedia.remotevideocam.display.CameraStatusEventBus
import org.avmedia.remotevideocam.display.ILocalConnection
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionStateMachine.*
import org.json.JSONObject

/**
 * Represents the remote control of the motion detection feature.
 * 1. remotely toggle the motion detection in the Camera side.
 * 2. shows a local notification when motion is detected.
 */
class MotionDetectionRemoteController(
    private val connection: ILocalConnection,
) : Listener {
    private var notificationController: MotionNotificationController? = null
    private val motionDetectionStateMachine = MotionDetectionStateMachine()

    fun init(view: ImageButton) {
        if (notificationController == null) {
            notificationController = MotionNotificationController(view.context)
            subscribe()
        }
        motionDetectionStateMachine.listener = this
    }

    fun toggleMotionDetection(enable: Boolean) {
        val value = if (enable) {
            MotionDetectionAction.ENABLED
        } else {
            MotionDetectionAction.DISABLED
        }
        val jsonString = JSONObject()
            .put(MotionDetectionAction.NAME, value)
            .toString()
        connection.sendMessage(jsonString)
    }

    fun subscribe() {
        CameraStatusEventBus.addSubject(MotionDetectionAction.NAME)
        CameraStatusEventBus.subscribe(
            this.javaClass.simpleName,
            MotionDetectionAction.NAME,
        ) {
            it?.toMotionDetectionAction()?.let { action ->
                motionDetectionStateMachine.update(action)
            }
        }
    }

    override fun onStateChanged(old: State, new: State) {
        if (old == State.NOT_DETECTED && new == State.DETECTED) {
            notificationController?.showNotification("Motion Detected", "Details")
        }
    }
}
