package com.example.data.model

data class FrequencyItem(
    val id: String,
    val hz: Float,
    val name: String,
    val shortDesc: String,
    val longDesc: String,
    val category: String, // Focus, Sleep, Meditation, Relaxation, Energy, Balance, Abundance, Manifestation
    val tags: List<String>,
    val intendedExperience: List<String>,
    val suggestedContext: String,
    val durationMinutes: Int = 30,
    val isFeatured: Boolean = false,
    val isPremium: Boolean = false,
    val binauralBeatHz: Float = 0f, // 0 = none, e.g. 4.0 for Delta, 6.0 for Theta, 10.0 for Alpha
    val harmonicWarmth: Float = 0.35f,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayHz: String
        get() = if (hz % 1.0f == 0.0f) "${hz.toInt()} Hz" else "$hz Hz"
}

enum class SoundMode(val label: String, val description: String) {
    PURE_FREQUENCY("Pure Tone", "Direct mathematical Solfeggio frequency sine wave"),
    HARMONIC_DRONE("Harmonic Drone", "Warm ambient acoustic resonance with natural overtones"),
    BINAURAL_BEAT("Binaural Waves", "Harmonic stereo detuning for targeted brainwave entrainment")
}
