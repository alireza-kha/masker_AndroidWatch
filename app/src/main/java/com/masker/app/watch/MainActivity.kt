package com.masker.app.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.masker.app.watch.presentation.WearApp
import com.masker.app.watch.sync.MaskerRepository
import com.masker.app.watch.sync.PhoneSyncClient

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MaskerRepository.ensureLoaded(applicationContext)
        setContent {
            WearApp()
        }
    }

    override fun onResume() {
        super.onResume()
        // هر بار که ساعت روی صفحه می‌آید، از گوشی (در صورت اتصال) درخواست آخرین تنظیمات را می‌کند
        PhoneSyncClient.requestSync(applicationContext)
    }
}
