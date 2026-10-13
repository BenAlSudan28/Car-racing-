package com.velocityrush.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

class GameAudio {

    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null

    @Volatile
    private var running = false

    @Volatile
    var enabled = true

    fun startEngine() {
        if (running || !enabled) return

        val sampleRate = 22050

        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (minBuffer <= 0) return

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        val track = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(minBuffer * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        running = true

        track.play()

        audioThread = Thread {
            val buffer = ShortArray(1024)
            var phase = 0.0

            while (running) {
                for (i in buffer.indices) {
                    phase += 2.0 * PI * 95.0 / sampleRate

                    if (phase > 2.0 * PI) {
                        phase -= 2.0 * PI
                    }

                    val fundamental = sin(phase)
                    val harmonic = sin(phase * 2.03) * 0.25

                    val sample = (
                        (fundamental + harmonic) * 2200
                    ).toInt()

                    buffer[i] = sample
                        .coerceIn(
                            Short.MIN_VALUE.toInt(),
                            Short.MAX_VALUE.toInt()
                        )
                        .toShort()
                }

                if (running) {
                    track.write(
                        buffer,
                        0,
                        buffer.size
                    )
                }
            }
        }.apply {
            name = "VelocityRushAudio"
            isDaemon = true
            start()
        }
    }

    fun stop() {
        running = false

        try {
            audioThread?.join(300)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        audioThread = null

        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: IllegalStateException) {
        }
    }

    fun release() {
        stop()

        try {
            audioTrack?.release()
        } catch (_: IllegalStateException) {
        }

        audioTrack = null
    }
}
