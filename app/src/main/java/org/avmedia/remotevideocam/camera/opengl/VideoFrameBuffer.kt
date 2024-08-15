package org.avmedia.remotevideocam.camera.opengl

import android.graphics.Matrix
import android.opengl.GLES20
import org.avmedia.remotevideocam.frameanalysis.motion.makeReleaseRunnable
import org.webrtc.GlTextureFrameBuffer
import org.webrtc.GlUtil
import org.webrtc.RendererCommon.GlDrawer
import org.webrtc.TextureBufferImpl
import org.webrtc.VideoFrame
import org.webrtc.VideoFrame.TextureBuffer
import org.webrtc.VideoFrameDrawer

class VideoFrameBuffer(
    private val frameBuffer: GlTextureFrameBuffer,
    private val delegate: TextureBuffer
) : VideoFrame.TextureBuffer by delegate {

    fun bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.frameBufferId)
    }

    fun unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }
}

inline fun VideoFrameBuffer.draw(renderRunnable: () -> Unit) {
    try {
        bind()
        renderRunnable()
    } finally {
        unbind()
    }
}

fun TextureBufferImpl.toVideoFrameBuffer(
    glDrawer: GlDrawer,
): VideoFrameBuffer {
    val frameBuffer = GlTextureFrameBuffer(GLES20.GL_RGBA)
    frameBuffer.setSize(width, height)

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.frameBufferId)

    VideoFrameDrawer.drawTexture(
        glDrawer,
        this,
        Matrix(),
        width,
        height,
        0,
        0,
        width,
        height,
    )

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    GlUtil.checkNoGLES2Error("Fails to render to texture")

    val delegate = TextureBufferImpl(
        width,
        height,
        VideoFrame.TextureBuffer.Type.RGB,
        frameBuffer.textureId,
        Matrix(),
        toI420Handler,
        yuvConverter, // lifecycle is aligned with SurfaceTextureHelper
        toI420Handler.makeReleaseRunnable(frameBuffer)
    )

    return VideoFrameBuffer(frameBuffer, delegate)
}
