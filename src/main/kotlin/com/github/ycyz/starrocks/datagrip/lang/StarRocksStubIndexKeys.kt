package com.github.ycyz.starrocks.datagrip.lang

import java.util.Locale

object StarRocksStubIndexKeys {
    fun nameKeys(name: String): Set<String> {
        val keys = linkedSetOf<String>()
        addNameKeys(keys, name)
        return keys
    }

    fun splitQualifiedIdentifier(text: String): List<String> {
        val result = mutableListOf<String>()
        val segment = StringBuilder()
        var inBacktick = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            if (char == '`') {
                if (inBacktick && index + 1 < text.length && text[index + 1] == '`') {
                    segment.append("``")
                    index += 2
                    continue
                }
                inBacktick = !inBacktick
                segment.append(char)
                index++
                continue
            }
            if (char == '.' && !inBacktick) {
                result += segment.toString()
                segment.clear()
                index++
                continue
            }
            segment.append(char)
            index++
        }
        result += segment.toString()
        return result
    }

    private fun addNameKeys(keys: MutableSet<String>, name: String) {
        val normalized = StarRocksNamedStubElement.normalizeName(name)
        if (normalized.isBlank()) {
            return
        }
        keys += normalized
        keys += normalized.lowercase(Locale.ROOT)
    }
}
