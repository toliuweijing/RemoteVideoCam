package org.avmedia.remotevideocam.frameanalysis.motion

enum class MotionDetectionProtocol {

    ENABLED,

    DISABLED,

    DETECTED,

    ;

    companion object {
        const val KEY = "MOTION_DETECTION"
    }
}