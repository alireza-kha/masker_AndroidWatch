package com.masker.app.watch.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable

/**
 * درخواست ارسال فوری آخرین وضعیت ماسکر از گوشی متصل، برای وقتی که کاربر اپ ساعت را باز
 * می‌کند یا دستی دکمه «همگام‌سازی» را می‌زند (به‌جای منتظر ماندن برای تغییر بعدی تنظیمات
 * در گوشی).
 */
object PhoneSyncClient {

    private const val TAG = "PhoneSyncClient"

    fun requestSync(context: Context) {
        val appContext = context.applicationContext
        val messageClient = Wearable.getMessageClient(appContext)
        Wearable.getNodeClient(appContext).connectedNodes
            .addOnSuccessListener { nodes ->
                for (node in nodes) {
                    messageClient.sendMessage(node.id, MaskerSyncProtocol.REQUEST_SYNC_PATH, ByteArray(0))
                        .addOnFailureListener { e -> Log.w(TAG, "sendMessage failed to ${node.id}", e) }
                }
            }
            .addOnFailureListener { e -> Log.w(TAG, "connectedNodes failed", e) }
    }
}
