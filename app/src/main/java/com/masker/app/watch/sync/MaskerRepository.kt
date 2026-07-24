package com.masker.app.watch.sync

import android.content.Context
import com.masker.app.watch.service.MaskerPlaybackService
import com.masker.app.watch.storage.WatchSettingsStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * منبع واحد حقیقت وضعیت ماسکر روی ساعت: آخرین [MaskerSyncState] دریافت‌شده از گوشی (یا
 * بازیابی‌شده از حافظه محلی) و اینکه پخش زنده روشن است یا نه. هم UI (Compose) و هم
 * [com.masker.app.watch.sync.WatchSyncListenerService] از همین شیء استفاده می‌کنند.
 */
object MaskerRepository {

    private val _state = MutableStateFlow<MaskerSyncState?>(null)
    val state: StateFlow<MaskerSyncState?> = _state

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    @Volatile
    private var loadedFromDisk = false

    /** در اولین استفاده، آخرین وضعیت ذخیره‌شده روی حافظه ساعت را بازیابی می‌کند */
    fun ensureLoaded(context: Context) {
        if (loadedFromDisk) return
        loadedFromDisk = true
        val json = WatchSettingsStorage.loadLastStateJson(context) ?: return
        val restored = MaskerSyncState.fromJson(json) ?: return
        _state.value = restored
        applyToEngine(restored)
    }

    /** فراخوانی‌شده وقتی یک DataItem جدید از گوشی برسد؛ هم ذخیره می‌کند هم فوراً روی موتور صدا اعمال می‌کند */
    fun onStateReceived(context: Context, newState: MaskerSyncState) {
        _state.value = newState
        WatchSettingsStorage.saveLastStateJson(context, newState.toJson().toString())
        applyToEngine(newState)
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    private fun applyToEngine(state: MaskerSyncState) {
        val engine = MaskerPlaybackService.noiseEngine
        for (i in state.bandGains.indices) {
            if (i < engine.bandGains.size) engine.bandGains[i] = state.bandGains[i]
        }
        engine.masterVolume = state.masterVolume
        engine.leftVolume = state.leftVolume
        engine.rightVolume = state.rightVolume
        engine.setNotch(state.notchEnabled, state.notchFrequencyHz, state.notchWidthOctaves)
        engine.modulationEnabled = state.modulationEnabled
        engine.modulationDepth = state.modulationDepth
    }
}
