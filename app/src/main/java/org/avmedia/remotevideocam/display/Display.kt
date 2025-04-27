package org.avmedia.remotevideocam.display

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.avmedia.remotevideocam.display.customcomponents.VideoViewWebRTC
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionRemoteController

@SuppressLint("StaticFieldLeak")
object Display : Fragment() {
    private var connection: ILocalConnection = NetworkServiceConnection
    private val motionDetectionRemoteController = MotionDetectionRemoteController(connection)
    private val videoStreamStatusController = VideoStreamStatusController()

    fun init(
        context: Context,
        videoView: VideoViewWebRTC,
        motionDetectionButton: ImageButton,
        videoStreamIndicator: ImageView
    ) {
        connection.init(context)
        videoView.init()
        CameraDataListener.init(connection)
        videoStreamStatusController.init(videoStreamIndicator, videoView)

        motionDetectionRemoteController.init(context)
        motionDetectionButton.setOnClickListener {
            val enabled = !it.isSelected
            it.isSelected = enabled
            motionDetectionRemoteController.toggleMotionDetection(enabled)
        }
    }

    fun connect(context: Context?) {
        connection.connect(context)
    }

    fun disconnect(context: Context?) {
        connection.disconnect(context)
    }
}