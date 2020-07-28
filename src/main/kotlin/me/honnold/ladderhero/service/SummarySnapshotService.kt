package me.honnold.ladderhero.service

import me.honnold.ladderhero.dao.domain.SummarySnapshot
import me.honnold.ladderhero.repository.SummarySnapshotRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SummarySnapshotService(private val summarySnapshotRepository: SummarySnapshotRepository) {
    companion object {
        private val logger = LoggerFactory.getLogger(SummarySnapshotRepository::class.java)
    }

    fun saveAll(snapshots: Iterable<SummarySnapshot>): Mono<List<SummarySnapshot>> {
        return this.summarySnapshotRepository.saveAll(snapshots)
            .collectList()
            .doFirst { logger.debug("Starting to save summary snapshots") }
            .doOnSuccess { logger.debug("Successfully saved ${it.size} summary snapshots") }
            .doOnError { logger.error("Unable to save summary snapshots -- ${it.message}") }
    }
}