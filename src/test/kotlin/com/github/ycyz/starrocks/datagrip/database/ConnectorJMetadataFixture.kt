package com.github.ycyz.starrocks.datagrip.database

import com.mysql.cj.NativeSession
import com.mysql.cj.ServerVersion
import com.mysql.cj.conf.ConnectionUrl
import com.mysql.cj.jdbc.DatabaseMetaDataInformationSchema
import com.mysql.cj.jdbc.JdbcConnection
import com.mysql.cj.jdbc.JdbcPropertySetImpl
import java.lang.reflect.Proxy
import java.sql.DatabaseMetaData
import java.util.Properties
import java.util.concurrent.locks.ReentrantLock

/** Real Connector/J URL parsing and metadata, with no server connection or SQL. */
internal fun connectorJConnection(url: String, properties: Map<String, String>): JdbcConnection {
    val host = ConnectionUrl.getConnectionUrlInstance(url, Properties().apply { putAll(properties) }).mainHost
    val propertySet = JdbcPropertySetImpl().apply { initializeProperties(host.exposeAsProperties()) }
    val session = object : NativeSession(host, propertySet) {
        override fun getIdentifierQuoteString() = "`"
    }
    val lock = ReentrantLock()
    lateinit var metadata: DatabaseMetaData
    val connection = Proxy.newProxyInstance(JdbcConnection::class.java.classLoader, arrayOf(JdbcConnection::class.java)) { _, method, _ ->
        when (method.name) {
            "getPropertySet" -> propertySet
            "getSession" -> session
            "getExceptionInterceptor" -> null
            "getConnectionLock" -> lock
            "getServerVersion" -> ServerVersion(8, 0, 34)
            "getMetaData" -> metadata
            else -> error("Unexpected Connector/J call: ${method.name}")
        }
    } as JdbcConnection
    metadata = object : DatabaseMetaDataInformationSchema(connection, null, null) {}
    return connection
}
