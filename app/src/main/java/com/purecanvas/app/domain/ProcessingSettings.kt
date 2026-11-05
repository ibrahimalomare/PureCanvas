package com.purecanvas.app.domain

data class ProcessingSettings(
    val denoise: Float = 0.25f,
    val sharpness: Float = 0.18f,
    val contrast: Float = 1.05f,
    val brightness: Float = 0f,
    val saturation: Float = 1.04f,
    val warmth: Float = 0f,
    val detail: Float = 0.15f,
) {
    companion object { val Default = ProcessingSettings() }
}
