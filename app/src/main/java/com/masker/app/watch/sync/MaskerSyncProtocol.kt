package com.masker.app.watch.sync

/**
 * ثابت‌های پروتکل همگام‌سازی بین برنامه گوشی (MaskerWithHeadphone_16Band) و این برنامه ساعت،
 * از طریق Wear Data Layer API. این مقادیر باید در هر دو سمت یکسان باشند؛ نگه‌داری تغییرات
 * هماهنگ بین دو ریپو، مسئولیت توسعه‌دهنده است.
 *
 * - [STATE_PATH]: مسیر DataItem که تنظیمات کامل ماسکر نویزی (باندها، ولوم، نوچ، مدولاسیون
 *   و آخرین اودیوگرام) را حمل می‌کند. گوشی هر بار که یکی از این مقادیر تغییر کند، این
 *   DataItem را به‌روز می‌کند و گوگل پلی سرویسز آن را خودکار با ساعت همگام می‌کند.
 * - [STATE_KEY]: کلید داخل DataMap که رشته JSON کامل وضعیت را نگه می‌دارد (به‌جای فیلدهای
 *   جداگانه، برای سادگی سریال‌سازی/عدم‌سریال‌سازی یکسان با فرمت [MaskerSyncState]).
 * - [REQUEST_SYNC_PATH]: مسیر پیامی که ساعت برای درخواست ارسال فوری آخرین وضعیت (مثلاً هنگام
 *   باز شدن اپ ساعت) به گوشی می‌فرستد.
 */
object MaskerSyncProtocol {
    const val STATE_PATH = "/masker/state"
    const val STATE_KEY = "state_json"
    const val REQUEST_SYNC_PATH = "/masker/request_sync"
}
