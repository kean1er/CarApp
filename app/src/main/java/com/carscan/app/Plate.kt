package com.carscan.app

object Plate {
    // Буквы, разрешённые на российских номерах: А В Е К М Н О Р С Т У Х
    const val LETTERS = "\u0410\u0412\u0415\u041A\u041C\u041D\u041E\u0420\u0421\u0422\u0423\u0425"
    private const val LAT = "ABEKMHOPCTYX"

    data class Info(val plate: String, val region: String, val regionName: String?)

    fun normalize(s: String): String = buildString {
        for (c in s.uppercase()) {
            if (c.isLetterOrDigit()) {
                val i = LAT.indexOf(c)
                append(if (i >= 0) LETTERS[i] else c)
            }
        }
    }

    private fun toDigit(c: Char): Char? = when (c) {
        '\u041E' -> '0'
        '\u0412' -> '8'
        '\u0417' -> '3'
        '\u0411' -> '6'
        '\u0427' -> '4'
        'I', 'L' -> '1'
        'Z' -> '2'
        'S' -> '5'
        else -> null
    }

    private fun toLetter(c: Char): Char? = when (c) {
        '0' -> '\u041E'
        '8' -> '\u0412'
        else -> null
    }

    // Формат: буква, 3 цифры, 2 буквы, 2-3 цифры региона. Заодно чиним типичные ошибки OCR.
    private fun fix(w: String): String? {
        val kinds = if (w.length == 9) "LDDDLLDDD" else "LDDDLLDD"
        val sb = StringBuilder()
        for (i in w.indices) {
            var c = w[i]
            if (kinds[i] == 'D') {
                if (!c.isDigit()) c = toDigit(c) ?: return null
            } else {
                if (c.isDigit()) c = toLetter(c) ?: return null
                else if (LETTERS.indexOf(c) < 0) return null
            }
            sb.append(c)
        }
        return sb.toString()
    }

    fun findIn(text: String): String? {
        val n = normalize(text)
        for (len in intArrayOf(9, 8)) {
            var i = 0
            while (i + len <= n.length) {
                val f = fix(n.substring(i, i + len))
                if (f != null) return f
                i++
            }
        }
        return null
    }

    fun parse(raw: String): Info? {
        val n = normalize(raw)
        if (n.length != 8 && n.length != 9) return null
        val p = fix(n) ?: return null
        val reg = p.substring(6)
        return Info(p, reg, Regions.name(reg))
    }

    fun pretty(p: String): String = if (p.length >= 8) p.substring(0, 6) + " " + p.substring(6) else p
}
