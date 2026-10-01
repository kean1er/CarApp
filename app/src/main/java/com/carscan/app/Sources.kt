package com.carscan.app

import java.net.URLEncoder

class Source(val title: String, val hint: String, val url: (String) -> String)

object Sources {
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    // Список можно править: {plate} подставляется как номер без пробелов.
    val forPlate = listOf(
        Source("Поиск в Яндексе", "Объявления и фото по номеру") { p ->
            "https://yandex.ru/search/?text=" + enc("$p история автомобиля")
        },
        Source("Номерограм через поиск", "Продажи, пробеги, фото") { p ->
            "https://duckduckgo.com/?q=" + enc("$p номерограм")
        },
        Source("Проверка ГИБДД", "Нужен VIN, капчу вводишь сам") { _ ->
            "https://xn--90adear.xn--p1ai/check/auto"
        }
    )
}
