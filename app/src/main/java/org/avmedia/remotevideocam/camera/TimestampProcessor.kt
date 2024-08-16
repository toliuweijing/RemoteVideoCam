package org.avmedia.remotevideocam.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.SystemClock
import org.avmedia.remotevideocam.camera.opengl.VideoFrameBuffer
import org.avmedia.remotevideocam.camera.opengl.draw
import org.avmedia.remotevideocam.camera.opengl.toVideoFrameBuffer
import org.avmedia.remotevideocam.opengl.GlTextureDrawer
import org.webrtc.GlRectDrawer
import org.webrtc.GlUtil
import org.webrtc.RendererCommon
import org.webrtc.TextureBufferImpl
import org.webrtc.VideoFrame
import kotlin.math.min

private val matToTextureTransform = Matrix().apply {
    preTranslate(0.5f, 0.5f)
    preScale(1f, -1f)
    preTranslate(-0.5f, -0.5f)
}

class GlTransparentTextureDrawer : GlRectDrawer() {

}

/**
 * Used to overlay a timestamp to the video frame.
 */
class TimestampProcessor : VideoProcessorImpl.FrameProcessor {

    private val glDrawer = GlRectDrawer()
    private val opacityDrawer = GlTextureDrawer()
    private var timestampMs = SystemClock.elapsedRealtime()

    private var bitmap: Bitmap? = null
    private var texId: Int = 0

    override fun process(frame: VideoFrame): VideoFrame {
        val opacity = calculateOpacityAndUpdateTimestamp()
        if (opacity == 0f) {
            return frame
        }

        val buffer = frame.buffer
        val videoFrameBuffer = if (buffer is TextureBufferImpl) {
            buffer.toVideoFrameBuffer(glDrawer)
        } else if (buffer is VideoFrameBuffer) {
            buffer
        } else {
            return frame
        }

        if (texId == 0) {
            texId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D)
            bitmap = prepareBitmap()
            uploadToTex(texId)
        }

        videoFrameBuffer.draw {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            val transform = RendererCommon.convertMatrixFromAndroidGraphicsMatrix(
                matToTextureTransform
            )

            val indicatorRadius = calculateIndicatorRadius(buffer.width, buffer.height)
            val viewportX = if (buffer.width < buffer.height) {
                buffer.width / 2 - indicatorRadius
            } else {
                0
            }
            val viewportY = if (buffer.height < buffer.width) {
                buffer.height / 2 - indicatorRadius
            } else {
                0
            }

            val opacity = calculateOpacityAndUpdateTimestamp()
            opacityDrawer.draw(
                viewportX,
                viewportY,
                2 * indicatorRadius,
                2 * indicatorRadius,
                transform,
                texId,
//                opacity,
            )

            GLES20.glDisable(GLES20.GL_BLEND)
        }

        return VideoFrame(videoFrameBuffer, frame.rotation, frame.timestampNs)
    }

    private fun calculateIndicatorRadius(frameWidth: Int, frameHeight: Int): Int {
        val edge = min(frameWidth, frameHeight)
        val radiusRatio = 0.02 * 0.7
        return (edge * radiusRatio).toInt()
    }

    private fun calculateOpacityAndUpdateTimestamp(): Float {
        val durationMs = (SystemClock.elapsedRealtime() - timestampMs) % 2_000
//        timestampMs = SystemClock.elapsedRealtime()

        return if (durationMs > 1_000) {
            1f
        } else {
            0f
        }
    }

    private fun prepareBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            isAntiAlias = true
        }
        canvas.drawOval(0f, 0f, 10f, 10f, paint)
        return bitmap
    }

    private fun uploadToTex(texId: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        bitmap?.let {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, it, 0)
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun release() {
        glDrawer.release()
        opacityDrawer.release()
        GLES20.glDeleteTextures(1, intArrayOf(texId), 0)
    }
}