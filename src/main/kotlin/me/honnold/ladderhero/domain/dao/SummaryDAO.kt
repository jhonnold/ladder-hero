package me.honnold.ladderhero.domain.dao

import me.honnold.ladderhero.domain.model.Summary
import me.honnold.ladderhero.domain.repository.SummaryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class SummaryDAO(
    private val summaryRepository: SummaryRepository,
    private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryDAO::class.java)
    }

    fun save(summary: Summary): Mono<Summary> =
        Mono.fromCallable {
            this.transactionTemplate.execute {
                this.summaryRepository.save(summary); summary
            }
        }
            .doFirst { logger.debug("Saving $summary") }
            .doOnSuccess { logger.debug("Successfully saved $summary") }
            .doOnError { logger.error("Unable to save $summary -- ${it.message}") }
            .subscribeOn(jdbcScheduler)
}