package me.honnold.ladderhero.domain.dao

import me.honnold.ladderhero.domain.model.Replay
import me.honnold.ladderhero.domain.repository.ReplayRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class ReplayDAO(
    private val replayRepository: ReplayRepository,
    private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayDAO::class.java)
    }

    fun save(replay: Replay): Mono<Replay> =
        Mono.fromCallable {
            this.transactionTemplate.execute {
                this.replayRepository.save(replay); replay
            }
        }
            .doFirst { logger.debug("Saving $replay") }
            .doOnSuccess { logger.debug("Successfully saved $replay") }
            .doOnError { logger.error("Unable to save $replay -- ${it.message}") }
            .subscribeOn(jdbcScheduler)
}