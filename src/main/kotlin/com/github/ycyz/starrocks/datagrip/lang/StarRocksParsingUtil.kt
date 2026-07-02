package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder

internal object StarRocksParsingUtil {
    fun word(builder: PsiBuilder): String? = builder.tokenText?.uppercase()

    fun tokenIs(builder: PsiBuilder, text: String): Boolean = builder.tokenText.equals(text, ignoreCase = true)

    fun consumeWord(builder: PsiBuilder, text: String): Boolean {
        if (!tokenIs(builder, text)) {
            return false
        }
        builder.advanceLexer()
        return true
    }

    fun skipNoise(builder: PsiBuilder) {
        while (!builder.eof() && builder.tokenText.isNullOrBlank()) {
            builder.advanceLexer()
        }
    }

    fun isIdentifier(builder: PsiBuilder): Boolean {
        val text = builder.tokenText
        return text != null &&
            text.isNotBlank() &&
            text != "." &&
            text.firstOrNull()?.let { it == '_' || it == '`' || it.isLetter() } == true
    }

    fun consumeBalancedTail(builder: PsiBuilder, stopWords: Set<String> = emptySet()) {
        var parenDepth = 0
        var angleDepth = 0
        while (!builder.eof()) {
            val text = builder.tokenText
            val upper = text?.uppercase()
            if (parenDepth == 0 && angleDepth == 0 && (text == ";" || upper in stopWords)) {
                return
            }
            when (text) {
                "(" -> parenDepth++
                ")" -> if (parenDepth > 0) parenDepth-- else return
                "<" -> angleDepth++
                ">" -> if (angleDepth > 0) angleDepth--
            }
            builder.advanceLexer()
        }
    }

    fun consumeQualifiedIdentifier(builder: PsiBuilder): Boolean {
        var consumed = false
        var expectPart = true
        while (!builder.eof()) {
            val text = builder.tokenText
            when {
                expectPart && isIdentifier(builder) -> {
                    builder.advanceLexer()
                    consumed = true
                    expectPart = false
                }
                !expectPart && text == "." -> {
                    builder.advanceLexer()
                    expectPart = true
                }
                else -> return consumed
            }
        }
        return consumed
    }

    fun containsWordBeforeStatementEnd(builder: PsiBuilder, words: Set<String>): Boolean {
        val marker = builder.mark()
        try {
            while (!builder.eof() && builder.tokenText != ";") {
                if (word(builder) in words) {
                    return true
                }
                builder.advanceLexer()
            }
            return false
        } finally {
            marker.rollbackTo()
        }
    }
}
