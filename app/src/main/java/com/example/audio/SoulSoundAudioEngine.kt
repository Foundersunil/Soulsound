package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.model.FrequencyItem
import com.example.data.model.SoundMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SoulSoundAudioEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private var timerJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<FrequencyItem?>(null)
    val currentTrack: StateFlow<FrequencyItem?> = _currentTrack.asStateFlow()

    private val _currentSoundMode = MutableStateFlow(SoundMode.HARMONIC_DRONE)
    val currentSoundMode: StateFlow<SoundMode> = _currentSoundMode.asStateFlow()

    private val _volume = MutableStateFlow(0.85f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(30 * 60 * 1000L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerSeconds: StateFlow<Int?> = _sleepTimerSeconds.asStateFlow()

    private val _liveAmplitude = MutableStateFlow(0.1f)
    val liveAmplitude: StateFlow<Float> = _liveAmplitude.asStateFlow()

    private var leftPhase = 0.0
    private var rightPhase = 0.0
    private var harmonicPhase = 0.0

    fun playTrack(track: FrequencyItem, targetDurationMinutes: Int? = null) {
        val sameTrack = _currentTrack.value?.id == track.id
        _currentTrack.value = track
        val dur = (targetDurationMinutes ?: track.durationMinutes) * 60 * 1000L
        _totalDurationMs.value = if (dur <= 0L) Long.MAX_VALUE else dur

        if (!sameTrack) {
            _progressMs.value = 0L
        }

        startAudio()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            if (_currentTrack.value != null) {
                startAudio()
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        stopSynthesis()
    }

    fun seekTo(positionMs: Long) {
        val total = _totalDurationMs.value
        _progressMs.value = positionMs.coerceIn(0L, if (total == Long.MAX_VALUE) Long.MAX_VALUE else total)
    }

    fun setSoundMode(mode: SoundMode) {
        _currentSoundMode.value = mode
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun setDuration(minutes: Int) {
        val ms = if (minutes <= 0) Long.MAX_VALUE else minutes * 60 * 1000L
        _totalDurationMs.value = ms
    }

    fun setSleepTimer(minutes: Int?) {
        if (minutes == null || minutes <= 0) {
            _sleepTimerSeconds.value = null
            timerJob?.cancel()
            timerJob = null
        } else {
            _sleepTimerSeconds.value = minutes * 60
            startTimerCountdown()
        }
    }

    private fun startTimerCountdown() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && (_sleepTimerSeconds.value ?: 0) > 0) {
                delay(1000L)
                val remaining = (_sleepTimerSeconds.value ?: 1) - 1
                if (remaining <= 0) {
                    _sleepTimerSeconds.value = null
                    // Fade out volume before pause
                    for (i in 10 downTo 0) {
                        setVolume((i / 10f) * _volume.value)
                        delay(100L)
                    }
                    pause()
                    break
                } else {
                    _sleepTimerSeconds.value = remaining
                }
            }
        }
    }

    private fun startAudio() {
        stopSynthesis()
        _isPlaying.value = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            audioTrack?.setVolume(_volume.value)
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
            return
        }

        synthesisJob = scope.launch(Dispatchers.Default) {
            val numFrames = 2048
            val pcmBuffer = ShortArray(numFrames * 2) // Stereo (L, R)

            var lastProgressUpdate = System.currentTimeMillis()

            while (isActive && _isPlaying.value) {
                val track = _currentTrack.value ?: break
                val mode = _currentSoundMode.value
                val vol = _volume.value

                // If Hz is ultra low (e.g. 3.2 Hz Delta), use a calming 432 Hz carrier detuned by 3.2 Hz
                val baseFreq = if (track.hz < 20f) 432.0f else track.hz
                val binauralOffset = if (track.binauralBeatHz > 0f) track.binauralBeatHz else 5.0f

                val leftFreq = when (mode) {
                    SoundMode.PURE_FREQUENCY -> baseFreq.toDouble()
                    SoundMode.HARMONIC_DRONE -> (baseFreq - 0.25).toDouble()
                    SoundMode.BINAURAL_BEAT -> (baseFreq - binauralOffset / 2.0).toDouble()
                }

                val rightFreq = when (mode) {
                    SoundMode.PURE_FREQUENCY -> baseFreq.toDouble()
                    SoundMode.HARMONIC_DRONE -> (baseFreq + 0.25).toDouble()
                    SoundMode.BINAURAL_BEAT -> (baseFreq + binauralOffset / 2.0).toDouble()
                }

                val leftPhaseIncrement = 2.0 * PI * leftFreq / sampleRate
                val rightPhaseIncrement = 2.0 * PI * rightFreq / sampleRate
                val harmonicIncrement = 2.0 * PI * (baseFreq * 2.0) / sampleRate

                var sumRms = 0.0

                for (i in 0 until numFrames) {
                    val lSine = sin(leftPhase)
                    val rSine = sin(rightPhase)

                    // Natural overtones for sound therapy warmth
                    val overtone = if (mode == SoundMode.HARMONIC_DRONE) {
                        sin(harmonicPhase) * 0.18 + sin(harmonicPhase * 1.5) * 0.08
                    } else 0.0

                    // Gentle breathing acoustic pulse (0.1 Hz)
                    val breathingMod = 0.92 + 0.08 * sin(leftPhase * 0.001)

                    val leftSample = ((lSine + overtone * 0.6) * breathingMod * vol).coerceIn(-1.0, 1.0)
                    val rightSample = ((rSine + overtone * 0.6) * breathingMod * vol).coerceIn(-1.0, 1.0)

                    pcmBuffer[i * 2] = (leftSample * Short.MAX_VALUE).toInt().toShort()
                    pcmBuffer[i * 2 + 1] = (rightSample * Short.MAX_VALUE).toInt().toShort()

                    sumRms += abs(leftSample)

                    leftPhase += leftPhaseIncrement
                    if (leftPhase >= 2.0 * PI) leftPhase -= 2.0 * PI

                    rightPhase += rightPhaseIncrement
                    if (rightPhase >= 2.0 * PI) rightPhase -= 2.0 * PI

                    harmonicPhase += harmonicIncrement
                    if (harmonicPhase >= 2.0 * PI) harmonicPhase -= 2.0 * PI
                }

                val trackObj = audioTrack
                if (trackObj != null && trackObj.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    trackObj.write(pcmBuffer, 0, pcmBuffer.size)
                }

                val avgRms = (sumRms / numFrames).toFloat()
                _liveAmplitude.value = (avgRms * 1.6f).coerceIn(0.08f, 1.0f)

                val now = System.currentTimeMillis()
                val delta = now - lastProgressUpdate
                if (delta >= 500) {
                    lastProgressUpdate = now
                    val newProgress = _progressMs.value + delta
                    val total = _totalDurationMs.value
                    if (total != Long.MAX_VALUE && newProgress >= total) {
                        _progressMs.value = total
                        pause()
                        break
                    } else {
                        _progressMs.value = newProgress
                    }
                }
            }
        }
    }

    private fun stopSynthesis() {
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _liveAmplitude.value = 0.05f
    }

    fun release() {
        stopSynthesis()
        timerJob?.cancel()
    }
}
