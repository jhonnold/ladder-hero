package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Summary
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class SummaryDAO(private val databaseClient: DatabaseClient) : DAO<Summary, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryDAO::class.java)
    }

    override fun findById(id: UUID): Mono<Summary> {
        return databaseClient.select()
            .from(Summary::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    override fun save(entity: Summary): Mono<Summary> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<Summary>): Mono<List<Summary>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: Summary): Mono<Summary> {
        val id = entity.id
            ?: throw IllegalArgumentException("ID cannot be null when updating!")

        return databaseClient.update()
            .table(Summary::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: Summary): Mono<Summary> {
        if (entity.id != null) throw IllegalArgumentException("ID must be null when creating!")

        return databaseClient.insert()
            .into(Summary::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    throw DataRetrievalFailureException("Unable to save Summary!")

                this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}