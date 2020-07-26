package me.honnold.ladderhero.domain.dao

import me.honnold.ladderhero.domain.model.SummarySnapshot
import me.honnold.ladderhero.domain.repository.SummarySnapshotRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class SummarySnapshotDAO(
    private val summarySnapshotRepository: SummarySnapshotRepository,
    private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummarySnapshotDAO::class.java)
    }

    fun save(snapshot: SummarySnapshot): Mono<SummarySnapshot> =
        Mono.fromCallable {
            this.transactionTemplate.execute {
                this.summarySnapshotRepository.save(snapshot)
                snapshot
            }
        }
            .doFirst { logger.debug("Saving $snapshot") }
            .doOnSuccess { logger.debug("Successfully saved $snapshot") }
            .doOnError { logger.error("Unable to save $snapshot -- ${it.message}") }
            .subscribeOn(jdbcScheduler)
}