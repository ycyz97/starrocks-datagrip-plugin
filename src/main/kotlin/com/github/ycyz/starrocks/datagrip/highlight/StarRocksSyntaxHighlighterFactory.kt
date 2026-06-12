package com.github.ycyz.starrocks.datagrip.highlight

import com.intellij.sql.dialects.base.SqlSyntaxHighlighterFactory
import com.github.ycyz.starrocks.datagrip.dialect.StarRocksDialect

class StarRocksSyntaxHighlighterFactory : SqlSyntaxHighlighterFactory.Base(StarRocksDialect.INSTANCE)
