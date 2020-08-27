package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Summary
import me.honnold.ladderhero.dao.domain.SummarySnapshot
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class SummarySnapshotDAO(private val databaseClient: DatabaseClient) : DAO<SummarySnapshot, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(SummarySnapshotDAO::class.java)
    }

    override fun findById(id: UUID): Mono<SummarySnapshot> {
        return databaseClient
            .select()
            .from(SummarySnapshot::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findAllBySummary(summary: Summary): Flux<SummarySnapshot> {
        val summaryId = summary.id

        return if (summaryId != null) this.findAllBySummaryId(summaryId) else Flux.empty()
    }

    fun findAllBySummaryId(summaryId: UUID): Flux<SummarySnapshot> {
        return databaseClient
            .select()
            .from(SummarySnapshot::class.java)
            .matching(Criteria.where("summary_id").`is`(summaryId))
            .fetch()
            .all()
    }

    override fun save(entity: SummarySnapshot): Mono<SummarySnapshot> {
        logger.debug("Saving $entity")

        return databaseClient
            .insert()
            .into(SummarySnapshot::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    DataRetrievalFailureException("Unable to load summary snapshot by uuid")
                        .toMono()
                else this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }

    override fun saveAll(entities: List<SummarySnapshot>): Mono<List<SummarySnapshot>> {
        logger.debug("Saving ${entities.size} SummarySnapshots")

        return databaseClient
            .insert()
            .into(SummarySnapshot::class.java)
            .using(entities.toFlux())
            .map { row -> row.get(0, UUID::class.java) }
            .all()
            .flatMap { id ->
                if (id == null)
                    throw DataRetrievalFailureException("Unable to load summary snapshot by uuid")

                databaseClient
                    .select()
                    .from(SummarySnapshot::class.java)
                    .matching(Criteria.where("id").`is`(id))
                    .fetch()
                    .all()
            }
            .collectList()
            .doOnSuccess { logger.debug("Successfully saved ${entities.size} SummarySnapshots") }
            .doOnError { t ->
                logger.error(
                    "There was an issue saving ${entities.size} SummarySnapshots -- ${t.message}"
                )
            }
    }
}
