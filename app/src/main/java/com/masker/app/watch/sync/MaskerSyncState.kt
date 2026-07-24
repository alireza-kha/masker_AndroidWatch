package com.masker.app.watch.sync

import com.masker.app.watch.audio.NoiseEngine
import com.masker.app.watch.audiogram.AudiogramResult
import org.json.JSONObject

/**
 * عکس کامل تنظیمات فعلی تب «ماسکر نویزی» در برنامه گوشی، که از طریق یک DataItem واحد
 * (مسیر [MaskerSyncProtocol.STATE_PATH]) به ساعت ارسال می‌شود تا ساعت بتواند دقیقاً همان
 * صدا را با همان پارامترها بازتولید کند.
 */
data class MaskerSyncState(
    val timestampMillis: Long,
    val bandGains: FloatArray,
    val masterVolume: Float,
    val leftVolume: Float,
    val rightVolume: Float,
    val notchEnabled: Boolean,
    val notchFrequencyHz: Double,
    val notchWidthOctaves: Float,
    val modulationEnabled: Boolean,
    val modulationDepth: Float,
    val audiogram: AudiogramResult?
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("timestamp", timestampMillis)

        val bandsArr = org.json.JSONArray()
        for (g in bandGains) bandsArr.put(g.toDouble())
        obj.put("bandGains", bandsArr)

        obj.put("masterVolume", masterVolume.toDouble())
        obj.put("leftVolume", leftVolume.toDouble())
        obj.put("rightVolume", rightVolume.toDouble())

        obj.put("notchEnabled", notchEnabled)
        obj.put("notchFrequencyHz", notchFrequencyHz)
        obj.put("notchWidthOctaves", notchWidthOctaves.toDouble())

        obj.put("modulationEnabled", modulationEnabled)
        obj.put("modulationDepth", modulationDepth.toDouble())

        if (audiogram != null) obj.put("audiogram", audiogram.toJson())
        return obj
    }

    companion object {
        fun fromJson(json: String): MaskerSyncState? {
            return try {
                val obj = JSONObject(json)
                val bandsArr = obj.optJSONArray("bandGains")
                val bandGains = FloatArray(NoiseEngine.BAND_COUNT) { i ->
                    if (bandsArr != null && i < bandsArr.length()) bandsArr.optDouble(i, 0.6).toFloat() else 0.6f
                }

                val audiogramObj = obj.optJSONObject("audiogram")
                val audiogram = audiogramObj?.let { AudiogramResult.fromJson(it) }

                MaskerSyncState(
                    timestampMillis = obj.optLong("timestamp", System.currentTimeMillis()),
                    bandGains = bandGains,
                    masterVolume = obj.optDouble("masterVolume", 0.7).toFloat(),
                    leftVolume = obj.optDouble("leftVolume", 1.0).toFloat(),
                    rightVolume = obj.optDouble("rightVolume", 1.0).toFloat(),
                    notchEnabled = obj.optBoolean("notchEnabled", false),
                    notchFrequencyHz = obj.optDouble("notchFrequencyHz", 4000.0),
                    notchWidthOctaves = obj.optDouble("notchWidthOctaves", 0.5).toFloat(),
                    modulationEnabled = obj.optBoolean("modulationEnabled", false),
                    modulationDepth = obj.optDouble("modulationDepth", 0.6).toFloat(),
                    audiogram = audiogram
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
