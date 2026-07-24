package com.masker.app.watch.presentation

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import com.masker.app.watch.presentation.theme.MaskerWatchTheme
import com.masker.app.watch.service.MaskerPlaybackService
import com.masker.app.watch.sync.MaskerRepository
import com.masker.app.watch.sync.MaskerSyncState
import com.masker.app.watch.sync.PhoneSyncClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * صفحه اصلی ساعت: کنترل ساده به‌جای ۱۶ اسلایدر باند گوشی (که روی صفحه کوچک ساعت جا نمی‌شود) -
 * پخش/توقف، ولوم کلی، وضعیت سینک با گوشی، و توگل نوچ/مدولاسیون. شدت خودِ ۱۶ باند نویز و
 * فرکانس/پهنای نوچ همیشه از آخرین [MaskerSyncState] دریافتی از گوشی گرفته می‌شود.
 */
@Composable
fun WearApp() {
    val context = LocalContext.current
    val syncState by MaskerRepository.state.collectAsState()
    val isPlaying by MaskerRepository.isPlaying.collectAsState()

    var masterVolume by remember(syncState) { mutableStateOf(syncState?.masterVolume ?: 0.7f) }
    var notchEnabled by remember(syncState) { mutableStateOf(syncState?.notchEnabled ?: false) }
    var modulationEnabled by remember(syncState) { mutableStateOf(syncState?.modulationEnabled ?: false) }

    MaskerWatchTheme {
        Scaffold(timeText = { TimeText() }) {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "ماسکر شنوایی",
                        style = MaterialTheme.typography.title3,
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    Text(
                        text = syncStatusLabel(syncState),
                        style = MaterialTheme.typography.caption2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            val intent = Intent(context, MaskerPlaybackService::class.java).apply {
                                action = if (isPlaying) {
                                    MaskerPlaybackService.ACTION_STOP
                                } else {
                                    MaskerPlaybackService.ACTION_START
                                }
                            }
                            context.startForegroundService(intent)
                        },
                        modifier = Modifier
                            .size(ButtonDefaults.LargeButtonSize)
                            .padding(top = 6.dp)
                    ) {
                        Text(if (isPlaying) "■" else "▶", style = MaterialTheme.typography.title2)
                    }
                }

                item {
                    Text(
                        text = "ولوم کلی",
                        style = MaterialTheme.typography.caption1,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    InlineSlider(
                        value = masterVolume,
                        onValueChange = { value ->
                            masterVolume = value
                            MaskerPlaybackService.noiseEngine.masterVolume = value
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                        decreaseIcon = {
                            Icon(InlineSliderDefaults.Decrease, contentDescription = "کم‌تر")
                        },
                        increaseIcon = {
                            Icon(InlineSliderDefaults.Increase, contentDescription = "بیش‌تر")
                        }
                    )
                }

                item {
                    ToggleChip(
                        checked = notchEnabled,
                        onCheckedChange = { checked ->
                            notchEnabled = checked
                            val engine = MaskerPlaybackService.noiseEngine
                            engine.setNotch(
                                checked,
                                syncState?.notchFrequencyHz ?: engine.notchFrequencyHz,
                                syncState?.notchWidthOctaves ?: engine.notchWidthOctaves
                            )
                        },
                        label = { Text("حذف فرکانس وزوز") },
                        secondaryLabel = {
                            val freq = syncState?.notchFrequencyHz
                            Text(if (freq != null) "${freq.toInt()} هرتز" else "—")
                        },
                        toggleControl = {
                            Icon(
                                imageVector = ToggleChipDefaults.switchIcon(checked = notchEnabled),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }

                item {
                    ToggleChip(
                        checked = modulationEnabled,
                        onCheckedChange = { checked ->
                            modulationEnabled = checked
                            MaskerPlaybackService.noiseEngine.modulationEnabled = checked
                        },
                        label = { Text("مدولاسیون ۱۰ هرتز") },
                        toggleControl = {
                            Icon(
                                imageVector = ToggleChipDefaults.switchIcon(checked = modulationEnabled),
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Chip(
                        onClick = { PhoneSyncClient.requestSync(context) },
                        label = { Text("همگام‌سازی با گوشی") },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}

private fun syncStatusLabel(state: MaskerSyncState?): String {
    if (state == null) return "هنوز با گوشی همگام نشده"
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(state.timestampMillis))
    return if (state.audiogram != null) {
        "همگام‌شده ساعت $time"
    } else {
        "همگام‌شده ساعت $time (بدون آدیوگرام)"
    }
}
