package org.avmedia.remotevideocam.display

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import org.avmedia.remotevideocam.display.customcomponents.VideoViewWebRTC
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionRemoteController

@SuppressLint("StaticFieldLeak")
object Display : Fragment() {
    private var connection: ILocalConnection = NetworkServiceConnection
    private var motionDetectionRemoteController: MotionDetectionRemoteController? = null

    fun init(
        context: Context,
        videoView: VideoViewWebRTC,
        motionDetectionButton: ImageButton
    ) {
        connection.init(context)
        videoView.init()
        CameraDataListener.init(connection)

        if (motionDetectionRemoteController == null) {
            motionDetectionRemoteController = MotionDetectionRemoteController(
                context,
                connection
            ).apply {
                subscribe()
            }
        }
        motionDetectionButton.setOnClickListener {
            val enabled = !motionDetectionButton.isSelected
            motionDetectionButton.isSelected = enabled
            motionDetectionRemoteController?.setMotionDetection(enabled)
        }

    }

    fun connect(context: Context?) {
        connection.connect(context)
    }

    fun disconnect(context: Context?) {
        connection.disconnect(context)
    }
}