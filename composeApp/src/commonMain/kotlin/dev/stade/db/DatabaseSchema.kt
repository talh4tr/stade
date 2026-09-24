package dev.stade.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver

/** Signals that an existing database cannot safely be opened by this version. */
class DatabaseSchemaException : IllegalStateException("The local database needs a compatible update before it can be opened.")

internal object DatabaseSchema {
    /**
     * Checks the columns required before any service can touch persisted data.
     * This check is read-only so a failed check cannot recreate tables or discard
     * a user's messages and identity.
     */
    fun requireCompatible(driver: SqlDriver) {
        val compatible = runCatching {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT mlkemPublicKey, mldsaPublicKey, groupProto FROM Contact LIMIT 0",
                mapper = { _: SqlCursor -> QueryResult.Value(Unit) },
                parameters = 0
            )
        }.isSuccess
        if (!compatible) throw DatabaseSchemaException()
    }
}
