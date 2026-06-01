package co.theportman.way_of_the_goat.data.database

import androidx.room.withTransaction

/**
 * Abstracts "run this block atomically" so the repository can be unit-tested
 * with a no-op fake, while production delegates to Room's real transaction.
 */
interface TransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}

/** Production implementation backed by a Room [WayOfTheGoatDatabase]. */
class RoomTransactionRunner(
    private val database: WayOfTheGoatDatabase,
) : TransactionRunner {
    override suspend operator fun <R> invoke(block: suspend () -> R): R =
        database.withTransaction(block)
}
