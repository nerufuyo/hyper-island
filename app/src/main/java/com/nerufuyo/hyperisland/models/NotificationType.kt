package com.nerufuyo.hyperisland.models

import androidx.annotation.StringRes
import com.nerufuyo.hyperisland.R

enum class NotificationType(@StringRes val labelRes: Int) {
    STANDARD(R.string.type_standard),
    MESSAGE(R.string.type_message),
    PROGRESS(R.string.type_progress),
    DOWNLOAD(R.string.type_download),
    MEDIA(R.string.type_media),
    NAVIGATION(R.string.type_nav),
    CALL(R.string.type_call),
    TIMER(R.string.type_timer)
}
