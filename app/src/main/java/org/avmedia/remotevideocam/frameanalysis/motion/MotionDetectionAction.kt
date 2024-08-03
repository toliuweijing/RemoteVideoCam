package org.avmedia.remotevideocam.frameanalysis.motion

import org.avmedia.remotevideocam.display.CameraStatusEventBus
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionAction.DETECTED
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionAction.DISABLED
import org.avmedia.remotevideocam.frameanalysis.motion.MotionDetectionAction.ENABLED

enum class MotionDetectionAction {

    ENABLED,

    DISABLED,

    DETECTED,

    ;

    companion object {

        const val NAME = "MOTION_DETECTION"

        init {
            CameraStatusEventBus.addSubject(NAME)
        }
    }
}

fun String.toMotionDetectionAction(): MotionDetectionAction {
    return when (this) {
        ENABLED.name -> ENABLED
        DISABLED.name -> DISABLED
        DETECTED.name -> DETECTED
        else -> throw IllegalArgumentException("$this fails to map")
    }
}
