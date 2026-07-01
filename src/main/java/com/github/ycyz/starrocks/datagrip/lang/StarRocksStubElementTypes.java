package com.github.ycyz.starrocks.datagrip.lang;

public interface StarRocksStubElementTypes {
    StarRocksNamedStubElementType STARROCKS_TABLE_NAME = new StarRocksNamedStubElementType("STARROCKS_TABLE_NAME");
    StarRocksNamedStubElementType STARROCKS_COLUMN_NAME = new StarRocksNamedStubElementType("STARROCKS_COLUMN_NAME");
    StarRocksNamedStubElementType STARROCKS_CTE_NAME = new StarRocksNamedStubElementType("STARROCKS_CTE_NAME");
    StarRocksNamedStubElementType STARROCKS_CTE_COLUMN_NAME = new StarRocksNamedStubElementType("STARROCKS_CTE_COLUMN_NAME");
    StarRocksNamedStubElementType STARROCKS_TABLE_ALIAS = new StarRocksNamedStubElementType("STARROCKS_TABLE_ALIAS");
    StarRocksNamedStubElementType STARROCKS_TABLE_ALIAS_COLUMN_NAME = new StarRocksNamedStubElementType("STARROCKS_TABLE_ALIAS_COLUMN_NAME");
    StarRocksNamedStubElementType STARROCKS_WINDOW_NAME = new StarRocksNamedStubElementType("STARROCKS_WINDOW_NAME");
    StarRocksNamedStubElementType STARROCKS_SELECT_ALIAS = new StarRocksNamedStubElementType("STARROCKS_SELECT_ALIAS");
}
