package dev.stade.db

import app.cash.sqldelight.db.QueryResult
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

private const val KEY = "7777777777777777777777777777777777777777777777777777777777777777"
private const val TEMP_STORE_MEMORY = 2L

class TempStoreTest {

    @Test
    fun temporaryTablesAndSortsStayInMemory() {
        val dir = Files.createTempDirectory("temp-store").toFile()
        try {
            val driver = DriverFactory().create(File(dir, "stade.db").absolutePath, KEY)
            val db = StadeDb(driver)
            db.stadeDbQueries.putKv("k", "v".encodeToByteArray())

            val mode = driver.executeQuery(
                identifier = null,
                sql = "PRAGMA temp_store",
                mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null) },
                parameters = 0
            ).value
            driver.close()

            assertEquals(
                TEMP_STORE_MEMORY,
                mode,
                "temporary files are written outside the encrypted database, so they must never reach the disk"
            )
        } finally {
            dir.deleteRecursively()
        }
    }
}
