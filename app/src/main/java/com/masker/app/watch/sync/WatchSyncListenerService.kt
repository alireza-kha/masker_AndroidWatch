package com.masker.app.watch.sync

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/**
 * سرویس گوش‌به‌زنگ Wear Data Layer API: هر بار که برنامه گوشی وضعیت جدید ماسکر را
 * (از طریق DataItem با مسیر [MaskerSyncProtocol.STATE_PATH]) منتشر کند، این سرویس بیدار
 * می‌شود، JSON را می‌خواند و در [MaskerRepository] به‌روزرسانی می‌کند (که خودش بلافاصله
 * روی موتور صدای در حال پخش هم اعمال می‌شود).
 */
class WatchSyncListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            for (event in dataEvents) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val item = event.dataItem
                if (item.uri.path != MaskerSyncProtocol.STATE_PATH) continue

                val dataMap = DataMapItem.fromDataItem(item).dataMap
                val json = dataMap.getString(MaskerSyncProtocol.STATE_KEY) ?: continue
                val state = MaskerSyncState.fromJson(json) ?: continue

                MaskerRepository.onStateReceived(applicationContext, state)
            }
        } finally {
            dataEvents.release()
        }
    }
}
