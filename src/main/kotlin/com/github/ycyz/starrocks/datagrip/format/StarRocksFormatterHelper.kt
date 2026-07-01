package com.github.ycyz.starrocks.datagrip.format

import com.github.ycyz.starrocks.datagrip.lang.StarRocksElementTypes
import com.intellij.psi.tree.IElementType
import com.intellij.sql.formatter.SqlFormatterHelper
import com.intellij.sql.formatter.model.SqlBlock
import com.intellij.sql.formatter.model.SqlCreateTableBlock
import com.intellij.sql.formatter.model.SqlStatementBlock
import com.intellij.sql.formatter.model.SqlUnsortedBlock
import com.intellij.sql.formatter.model.SqlViewBlock
import com.intellij.sql.psi.SqlCompositeElementTypes
import kotlin.reflect.KFunction0

class StarRocksFormatterHelper : SqlFormatterHelper {
    override val basicBlockCreation: Map<IElementType, KFunction0<out SqlBlock>> =
        mapOf(
            SqlCompositeElementTypes.SQL_SELECT_STATEMENT to ::SqlStatementBlock,
            SqlCompositeElementTypes.SQL_INSERT_STATEMENT to ::SqlStatementBlock,
            SqlCompositeElementTypes.SQL_UPDATE_STATEMENT to ::SqlStatementBlock,
            SqlCompositeElementTypes.SQL_DELETE_STATEMENT to ::SqlStatementBlock,
            SqlCompositeElementTypes.SQL_MERGE_STATEMENT to ::SqlStatementBlock,
            SqlCompositeElementTypes.SQL_CREATE_TABLE_STATEMENT to ::SqlCreateTableBlock,
            StarRocksElementTypes.TABLE_DDL_STATEMENT to ::SqlCreateTableBlock,
            SqlCompositeElementTypes.SQL_CREATE_VIEW_STATEMENT to ::SqlViewBlock,
            SqlCompositeElementTypes.SQL_CREATE_MATERIALIZED_VIEW_STATEMENT to ::SqlViewBlock,
            StarRocksElementTypes.VIEW_STATEMENT to ::SqlViewBlock,
            StarRocksElementTypes.MATERIALIZED_VIEW_STATEMENT to ::SqlViewBlock,
            StarRocksElementTypes.SELECT_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.FROM_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.WHERE_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.GROUP_BY_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.HAVING_CLAUSE to ::SqlUnsortedBlock,
            SqlCompositeElementTypes.SQL_QUALIFY_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.QUALIFY_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.WINDOW_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.ORDER_BY_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.LIMIT_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.SET_OPERATION_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.SELECT_ITEM to ::SqlUnsortedBlock,
            StarRocksElementTypes.TABLE_REFERENCE to ::SqlUnsortedBlock,
            StarRocksElementTypes.PREDICATE_EXPRESSION to ::SqlUnsortedBlock,
            StarRocksElementTypes.ORDERING_ITEM to ::SqlUnsortedBlock,
            StarRocksElementTypes.GROUPING_ITEM to ::SqlUnsortedBlock,
            SqlCompositeElementTypes.SQL_FUNCTION_CALL to ::SqlUnsortedBlock,
            StarRocksElementTypes.FUNCTION_CALL to ::SqlUnsortedBlock,
            SqlCompositeElementTypes.SQL_TYPE_ELEMENT to ::SqlUnsortedBlock,
            StarRocksElementTypes.TABLE_COLUMN_LIST to ::SqlUnsortedBlock,
            StarRocksElementTypes.COLUMN_DEFINITION to ::SqlUnsortedBlock,
            StarRocksElementTypes.KEY_MODEL_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.KEY_COLUMN to ::SqlUnsortedBlock,
            StarRocksElementTypes.COMMENT_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.PARTITION_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.PARTITION_EXPRESSION to ::SqlUnsortedBlock,
            StarRocksElementTypes.DISTRIBUTION_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.DISTRIBUTION_EXPRESSION to ::SqlUnsortedBlock,
            StarRocksElementTypes.BUCKETS_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.REFRESH_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.PROPERTIES_CLAUSE to ::SqlUnsortedBlock,
            StarRocksElementTypes.PROPERTY_PAIR to ::SqlUnsortedBlock
        )
}
