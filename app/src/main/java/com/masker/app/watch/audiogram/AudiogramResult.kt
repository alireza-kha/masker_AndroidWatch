package com.masker.app.watch.audiogram

import org.json.JSONArray
import org.json.JSONObject

/**
 * نتیجه یک آزمون شنوایی کامل: آستانه شنوایی هر گوش در فرکانس‌های استاندارد آزمون.
 *
 * این کلاس و فرمت JSON آن دقیقاً با com.masker.app.audiogram.AudiogramResult در برنامه گوشی
 * یکسان است، چون همین JSON از طریق Wear Data Layer API برای ساعت ارسال می‌شود.
 *
 * مقادیر threshold بر حسب "dB کاهش نسبت به حداکثر خروجی دستگاه" هستند (مقیاس نسبی و
 * غیرکالیبره، نه dB HL بالینی). مقدار Float.NaN یعنی حتی در بلندترین سطح هم پاسخی ثبت نشد.
 */
data class AudiogramResult(
    val frequenciesHz: List<Double>,
    val rightThresholdsDb: FloatArray,
    val leftThresholdsDb: FloatArray,
    val timestampMillis: Long,
    val patientName: String = "",
    val patientAge: Int = 0
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("timestamp", timestampMillis)
        obj.put("patientName", patientName)
        obj.put("patientAge", patientAge)

        val freqArr = JSONArray()
        for (f in frequenciesHz) freqArr.put(f)
        obj.put("frequencies", freqArr)

        obj.put("right", floatArrayToJson(rightThresholdsDb))
        obj.put("left", floatArrayToJson(leftThresholdsDb))

        return obj
    }

    private fun floatArrayToJson(arr: FloatArray): JSONArray {
        val jsonArr = JSONArray()
        for (v in arr) jsonArr.put(if (v.isNaN()) JSONObject.NULL else v.toDouble())
        return jsonArr
    }

    companion object {
        fun fromJson(obj: JSONObject): AudiogramResult {
            val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            val patientName = obj.optString("patientName", "")
            val patientAge = obj.optInt("patientAge", 0)

            val freqArr = obj.optJSONArray("frequencies")
            val frequencies = mutableListOf<Double>()
            if (freqArr != null) {
                for (i in 0 until freqArr.length()) frequencies.add(freqArr.optDouble(i))
            }

            val right = jsonToFloatArray(obj.optJSONArray("right"), frequencies.size)
            val left = jsonToFloatArray(obj.optJSONArray("left"), frequencies.size)

            return AudiogramResult(frequencies, right, left, timestamp, patientName, patientAge)
        }

        private fun jsonToFloatArray(arr: JSONArray?, size: Int): FloatArray {
            val result = FloatArray(size) { Float.NaN }
            if (arr != null) {
                for (i in 0 until minOf(size, arr.length())) {
                    result[i] = if (arr.isNull(i)) Float.NaN else arr.optDouble(i).toFloat()
                }
            }
            return result
        }
    }
}
