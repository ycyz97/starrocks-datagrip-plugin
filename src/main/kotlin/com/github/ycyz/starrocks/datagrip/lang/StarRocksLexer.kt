package com.github.ycyz.starrocks.datagrip.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType

/**
 * The shared lexical stream for non-parser consumers. Syntax highlighters may
 * decorate its tokens, but parsing rules live exclusively in the JFlex lexer.
 */
open class StarRocksLexer : LexerBase() {
    private val delegate = StarRocksParserLexer()

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        delegate.start(buffer, startOffset, endOffset, initialState)
    }

    override fun getState(): Int = delegate.state

    override fun getTokenType(): IElementType? {
        return delegate.tokenType?.let(::tokenTypeForParserToken)
    }

    override fun getTokenStart(): Int = delegate.tokenStart

    override fun getTokenEnd(): Int = delegate.tokenEnd

    override fun advance() {
        delegate.advance()
    }

    override fun getBufferSequence(): CharSequence = delegate.bufferSequence

    override fun getBufferEnd(): Int = delegate.bufferEnd

    protected open fun tokenTypeForParserToken(tokenType: IElementType): IElementType = tokenType

    protected fun tokenText(): String = bufferSequence
        .subSequence(tokenStart, tokenEnd)
        .toString()

    protected fun nextNonWhitespaceChar(): Char? {
        var offset = tokenEnd
        while (offset < bufferEnd && bufferSequence[offset].isWhitespace()) {
            offset++
        }
        return bufferSequence.getOrNull(offset)
    }
}
