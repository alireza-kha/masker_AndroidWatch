package com.masker.app.watch.storage

import android.content.Context

/**
 * ذخیره‌سازی آخرین وضعیت دریافت‌شده از گوشی (به‌صورت رشته JSON خام)، تا با بستن و باز کردن
 * دوباره ساعت یا از دست رفتن موقت اتصال به گوشی، آخرین تنظیمات ماسکر از بین نروند.
 */
object WatchSettingsStorage {

    private const val PREFS_NAME = "masker_watch_settings"
    private const val KEY_LAST_STATE_JSON = "last_state_json"

    fun saveLastStateJson(context: Context, json: String) {
        prefs(context).edit().putString(KEY_LAST_STATE_JSON, json).apply()
    }

    fun loadLastStateJson(context: Context): String? {
        return prefs(context).getString(KEY_LAST_STATE_JSON, null)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
