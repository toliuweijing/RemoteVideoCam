package org.avmedia.remotevideocam.frameanalysis.motion.backgroundsubtractor

import org.opencv.core.Mat
import org.webrtc.TextureBufferImpl

interface BackgroundSubtractor {

    fun apply(textureBufferImpl: TextureBufferImpl): Mat?

    fun release()
}