package org.avmedia.remotevideocam.frameanalysis.motion

import android.os.Handler
import android.os.Looper
import android.util.Log

private const val RECOVERY_THRESHOLD_MS = 5_000L

private const val TAG = "MotionDetectionStateMachine"

class MotionDetectionStateMachine {

    interface Listener {

        fun onStateChanged(old: State, new: State)
    }

    private val handler = Handler(Looper.getMainLooper())

    enum class State {

        NOT_DETECTED,

        DETECTED,
    }

    var listener: Listener? = null

    var state = State.NOT_DETECTED
        private set(new) {
            Log.d(TAG, "New State $new")
            val old = field
            field = new
            scheduleRecovery(new)

            if (old != field) {
                listener?.onStateChanged(old, new)
            }
        }

    private fun scheduleRecovery(state: State) {
        handler.removeCallbacksAndMessages(null)
        if (state == State.DETECTED) {
            handler.postDelayed(
                {
                    Log.d(TAG, "execute recovery")
                    this.state = State.NOT_DETECTED
                },
                RECOVERY_THRESHOLD_MS,
            )
        }
    }

    fun update(event: MotionDetectionAction) {
        state = when (event) {
            MotionDetectionAction.ENABLED,
            MotionDetectionAction.DISABLED ->
                State.NOT_DETECTED

            MotionDetectionAction.DETECTED -> {
                State.DETECTED
            }
        }
    }
}