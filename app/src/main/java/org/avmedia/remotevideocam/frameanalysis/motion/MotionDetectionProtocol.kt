package org.avmedia.remotevideocam.frameanalysis.motion

import org.avmedia.remotevideocam.display.CameraStatusEventBus

enum class MotionDetectionProtocol {

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