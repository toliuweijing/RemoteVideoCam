package org.avmedia.remotevideocam.display

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import org.avmedia.remotevideocam.display.customcomponents.VideoViewWebRTC
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionRemoteController

@SuppressLint("StaticFieldLeak")
object Display : Fragment() {
    private var connection: ILocalConnection = NetworkServiceConnection
    private var motionDetectionRemoteController: MotionDetectionRemoteController? = null

    fun init(
        context: Context,
        videoView: VideoViewWebRTC
    ) {
        connection.init(context)
        videoView.init()
        CameraDataListener.init(connection)

        motionDetectionRemoteController = MotionDetectionRemoteController(context).apply {
            subscribe()
        }
    }

    fun connect(context: Context?) {
        connection.connect(context)
    }

    fun disconnect(context: Context?) {
        connection.disconnect(context)
    }
}