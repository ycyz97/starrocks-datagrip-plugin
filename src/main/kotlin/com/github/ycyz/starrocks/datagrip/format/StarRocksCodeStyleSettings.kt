package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.sql.formatter.settings.SqlCodeStyleSettings

class StarRocksCodeStyleSettings(container: CodeStyleSettings) : SqlCodeStyleSettings("StarRocksCodeStyleSettings", container) {
    override fun getCorrespondedDialect(): Language = StarRocksDialect.INSTANCE
}
