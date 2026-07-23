package com.odysseus.app.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vibrator: Vibrator
) {

    /**
     * Short, light tick for user actions like sending a message.
     */
    fun vibrateSend() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null,
            durationMs = 12,
            amplitude = 80
        )
    }

    /**
     * Subtle single tick when AI response streaming starts.
     */
    fun vibrateStreamStart() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null,
            durationMs = 10,
            amplitude = 60
        )
    }

    /**
     * Optional extremely subtle stream tick as words are received.
     */
    fun vibrateStreamTick() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK else null,
            durationMs = 5,
            amplitude = 40
        )
    }

    /**
     * Double-tap pattern on completion of AI streaming response.
     */
    fun vibrateStreamFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator.vibrate(effect)
        } else {
            val timings = longArrayOf(0, 20, 120, 20)
            val amplitudes = intArrayOf(0, 150, 0, 150)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        }
    }

    /**
     * Medium tick when generation is manually stopped.
     */
    fun vibrateStop() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else null,
            durationMs = 30,
            amplitude = 180
        )
    }

    /**
     * Standard move / action tick, suitable for copying text.
     */
    fun vibrateCopy() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_CLICK else null,
            durationMs = 15,
            amplitude = 120
        )
    }

    /**
     * Standard long-press haptic response.
     */
    fun vibrateLongPress() {
        vibratePredefinedOrOneShot(
            predefinedEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_HEAVY_CLICK else null,
            durationMs = 45,
            amplitude = 160
        )
    }

    /**
     * Two short pulses indicating an error or warning.
     */
    fun vibrateError() {
        val timings = longArrayOf(0, 60, 100, 60)
        val amplitudes = intArrayOf(0, 220, 0, 220)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrator.vibrate(effect)
    }

    private fun vibratePredefinedOrOneShot(predefinedEffect: Int?, durationMs: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && predefinedEffect != null) {
            val effect = VibrationEffect.createPredefined(predefinedEffect)
            vibrator.vibrate(effect)
        } else {
            val safeAmplitude = amplitude.coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(durationMs, safeAmplitude)
            vibrator.vibrate(effect)
        }
    }
}
