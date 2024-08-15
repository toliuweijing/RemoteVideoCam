package org.avmedia.remotevideocam.camera

import android.opengl.GLES20
import androidx.tracing.trace
import org.webrtc.VideoFrame
import org.webrtc.VideoProcessor
import org.webrtc.VideoSink

private const val TAG = "VideoProcessorImpl"

/**
 * A default implementation of VideoProcessor. It is responsible to release the new video frame
 * after the use of the video sink set by the webrtc framework.
 */
class VideoProcessorImpl(
    private val frameProcessors: List<FrameProcessor>
) : VideoProcessor {

    interface FrameProcessor {

        fun process(frame: VideoFrame): VideoFrame
    }

    private var defaultVideoSink: VideoSink? = null;

    override fun onCapturerStarted(success: Boolean) {
    }

    override fun onCapturerStopped() {
    }

    override fun onFrameCaptured(videoFrame: VideoFrame) = trace("$TAG.onFrameCaptured") {
        var frame = videoFrame;

        frameProcessors.forEach { processor ->
            frame = processor.process(frame)
        }

        if (frame != videoFrame) {
            GLES20.glFlush()
        }

        defaultVideoSink?.onFrame(frame)

        // Manually release the new frame. Skip if it is unmodified.
        if (frame != videoFrame) {
            frame.release()
        }
    }

    /**
     * Video sink is set by WebRTC framework automatically. See details in [VideoSource].
     */
    override fun setSink(videoSink: VideoSink?) {
        defaultVideoSink = videoSink
    }
}
