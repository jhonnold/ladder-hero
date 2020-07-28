package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.SummarySnapshot
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import java.util.*

@Service
class SummarySnapshotDAO(private val databaseClient: DatabaseClient) : DAO<SummarySnapshot, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(SummarySnapshotDAO::class.java)
    }

    override fun save(entity: SummarySnapshot): Mono<SummarySnapshot> {
        logger.debug("Saving $entity")

        return databaseClient.insert()
            .into(SummarySnapshot::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    throw DataRetrievalFailureException("Unable to load file upload by uuid")

                databaseClient.select()
                    .from(SummarySnapshot::class.java)
                    .matching(Criteria.where("id").`is`(uuid))
                    .fetch()
                    .first()
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }

    override fun saveAll(entities: Collection<SummarySnapshot>): Flux<SummarySnapshot> {
        logger.debug("Saving ${entities.size} SummarySnapshots")

        return databaseClient.insert()
            .into(SummarySnapshot::class.java)
            .using(entities.toFlux())
            .map { row -> row.get(0, UUID::class.java) }
            .all()
            .collectList()
            .flatMapMany { ids ->
                databaseClient.select()
                    .from(SummarySnapshot::class.java)
                    .matching(Criteria.where("id").`in`(ids))
                    .fetch()
                    .all()
            }
            .doOnComplete { logger.debug("Successfully saved ${entities.size} SummarySnapshots") }
            .doOnError { t -> logger.error("There was an issue saving ${entities.size} SummarySnapshots -- ${t.message}") }
    }

    override fun findById(id: UUID): Mono<SummarySnapshot> {
        TODO("Not yet implemented")
    }
}