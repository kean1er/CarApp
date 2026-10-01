package com.carscan.app

import android.net.Uri
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class Kind { START, CAR, WEB }

class Tab(val kind: Kind) {
    val id: Long = System.nanoTime()
    var title by mutableStateOf(if (kind == Kind.START) "Избранное" else "Загрузка")
    var url by mutableStateOf("")
    var plate by mutableStateOf("")
    var model by mutableStateOf("")
    var photo by mutableStateOf<Uri?>(null)
    var canBack by mutableStateOf(false)
    var canForward by mutableStateOf(false)
    var web: WebView? = null

    fun refreshTitle() {
        val parts = listOf(model, if (plate.isNotEmpty()) Plate.pretty(plate) else "").filter { it.isNotBlank() }
        title = if (parts.isEmpty()) "Машина" else parts.joinToString(" · ")
    }
}
