package com.github.ycyz.starrocks.datagrip.database

import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread

/** Minimal MySQL text protocol peer for testing actual JDBC namespace commands. */
internal class NamespaceProtocolServer : AutoCloseable {
    private val listener = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
    val port = listener.localPort
    val commands = CopyOnWriteArrayList<String>()
    @Volatile var catalog = "default_catalog"
    @Volatile var database = ""
    @Volatile var reject: String? = null
    @Volatile private var failure: Throwable? = null
    private var client: Socket? = null
    private val worker = thread(isDaemon = true, name = "native-jdbc-protocol-test") {
        try {
            listener.accept().use { socket ->
                client = socket
                socket.soTimeout = 10000
                val input = socket.getInputStream()
                val output = socket.getOutputStream()
                fun send(sequence: Int, bytes: ByteArray) {
                    output.write(byteArrayOf(bytes.size.toByte(), (bytes.size shr 8).toByte(), (bytes.size shr 16).toByte(), sequence.toByte()))
                    output.write(bytes)
                    output.flush()
                }
                fun read(): ByteArray {
                    val header = input.readNBytes(4)
                    if (header.size != 4) throw EOFException()
                    val size = (header[0].toInt() and 255) or ((header[1].toInt() and 255) shl 8) or ((header[2].toInt() and 255) shl 16)
                    return input.readNBytes(size)
                }
                val capabilities = 0x000AA20D
                send(0, bytes {
                    write(10); zero("5.7.44-StarRocks"); little(1, 4); write("12345678".toByteArray()); write(0)
                    little(capabilities, 2); write(33); little(2, 2); little(capabilities shr 16, 2); write(21)
                    write(ByteArray(10)); zero("abcdefghijkl"); zero("mysql_native_password")
                })
                read()
                send(2, ok())
                while (true) {
                    val request = read()
                    if (request.isEmpty() || request[0].toInt() == 1) break
                    if (request[0].toInt() != 3) { send(1, ok()); continue }
                    val sql = request.copyOfRange(1, request.size).toString(Charsets.UTF_8)
                    commands += sql
                    if (sql == reject) {
                        send(1, bytes { write(255); little(5203, 2); write("#42000Access denied".toByteArray()) })
                        continue
                    }
                    val clean = sql.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "").trim()
                    when {
                        clean.startsWith("SET CATALOG ", true) -> { catalog = clean.substringAfter("SET CATALOG ").removeSurrounding("`"); database = ""; send(1, ok()) }
                        clean.startsWith("USE ", true) -> { database = clean.substringAfterLast(".").removePrefix("USE ").removeSurrounding("`"); send(1, ok()) }
                        clean.startsWith("SELECT", true) -> {
                            val aliases = Regex("(?i)\\bAS\\s+(\\w+)").findAll(clean).map { it.groupValues[1] }.toList()
                            val names = aliases.ifEmpty { listOf(clean.removePrefix("SELECT ").trim()) }
                            send(1, byteArrayOf(names.size.toByte()))
                            var seq = 2
                            for (name in names) send(seq++, bytes {
                                for (part in listOf("def", "", "", "", name, name)) field(part)
                                write(12); little(33, 2); little(1024, 4); write(253); little(0, 2); write(0); little(0, 2)
                            })
                            send(seq++, eof())
                            send(seq++, bytes { names.forEach { field(value(it)) } })
                            send(seq, eof())
                        }
                        else -> send(1, ok())
                    }
                }
            }
        } catch (error: Throwable) { if (!listener.isClosed) failure = error }
    }

    private fun value(name: String): String = when (name.lowercase()) {
        "@@catalog" -> catalog
        "database()" -> database
        "character_set_client", "character_set_connection", "character_set_results", "character_set_server" -> "utf8mb4"
        "collation_server", "collation_connection" -> "utf8mb4_general_ci"
        "system_time_zone", "time_zone" -> "UTC"
        "transaction_isolation" -> "REPEATABLE-READ"
        "max_allowed_packet" -> "67108864"
        "auto_increment_increment" -> "1"
        "interactive_timeout", "wait_timeout", "net_write_timeout" -> "28800"
        "sql_mode", "init_connect" -> ""
        else -> "0"
    }

    fun assertHealthy() { failure?.let { throw AssertionError("Protocol peer failed", it) } }
    override fun close() { listener.close(); client?.close(); worker.join(2000); assertHealthy() }
    private fun ok() = byteArrayOf(0, 0, 0, 2, 0, 0, 0)
    private fun eof() = byteArrayOf(254.toByte(), 0, 0, 2, 0)
    private fun bytes(block: ByteArrayOutputStream.() -> Unit) = ByteArrayOutputStream().apply(block).toByteArray()
    private fun ByteArrayOutputStream.zero(s: String) { write(s.toByteArray()); write(0) }
    private fun ByteArrayOutputStream.field(s: String) { val b = s.toByteArray(); require(b.size < 251); write(b.size); write(b) }
    private fun ByteArrayOutputStream.little(n: Int, size: Int) { repeat(size) { write(n ushr (it * 8) and 255) } }
}
