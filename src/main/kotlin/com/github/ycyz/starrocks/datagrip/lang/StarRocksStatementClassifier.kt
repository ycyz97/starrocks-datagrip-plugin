package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lang.PsiBuilder

object StarRocksStatementClassifier {
    fun classify(builder: PsiBuilder): StarRocksStatementFamily? {
        return StarRocksStatementWordsClassifier.classify { offset -> wordAt(builder, offset) }
    }

    fun classifyWords(words: List<String>): StarRocksStatementFamily? {
        return StarRocksStatementWordsClassifier.classify(words)
    }

    private fun wordAt(builder: PsiBuilder, offset: Int): String? {
        val marker = builder.mark()
        var current = 0
        var result: String? = null
        while (!builder.eof() && builder.tokenText != ";" && current <= offset) {
            val text = builder.tokenText
            if (text != null && text.firstOrNull()?.isLetter() == true) {
                if (current == offset) {
                    result = text.uppercase()
                    break
                }
                current++
            }
            builder.advanceLexer()
        }
        marker.rollbackTo()
        return result
    }
}
