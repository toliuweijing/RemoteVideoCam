package org.avmedia.remotevideocam.display

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.ImageView
import org.avmedia.remotevideocam.display.customcomponents.VideoViewWebRTC
import timber.log.Timber

private const val TAG = "VideoStreamStatusController"
class VideoStreamStatusController {

    lateinit var view: ImageView
    lateinit var viewWebRTC: VideoViewWebRTC

    private val handler = Handler(Looper.getMainLooper())

    fun init(view: ImageView, videoViewWebRTC: VideoViewWebRTC) {
        this.view = view
        this.viewWebRTC = videoViewWebRTC

        checkStatus()
    }

    private fun checkStatus() {
        val frozen = SystemClock.elapsedRealtime() - viewWebRTC.videoFrameSystemClockMs > 1_000
        Timber.tag(TAG).d("frozen $frozen, current ${SystemClock.elapsedRealtime()}, last ${viewWebRTC.videoFrameSystemClockMs}")
        updateStatus(frozen)

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            checkStatus()
        }, 1_000)
    }

    private fun updateStatus(frozen: Boolean) {
        val filter = if (frozen) {
            Color.RED
        } else {
            Color.GREEN
        }
        view.setColorFilter(filter)
    }
}