package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Replay
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class ReplayDAO(private val databaseClient: DatabaseClient) : DAO<Replay, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayDAO::class.java)
    }

    override fun findById(id: UUID): Mono<Replay> {
        return databaseClient.select()
            .from(Replay::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findBySlug(slug: String): Mono<Replay> {
        return databaseClient.select()
            .from(Replay::class.java)
            .matching(Criteria.where("slug").`is`(slug))
            .fetch()
            .first()
    }

    fun findAll(page: Pageable): Flux<Replay> {
        return databaseClient.select()
            .from(Replay::class.java)
            .orderBy(Sort.by("playedAt").descending())
            .page(page)
            .fetch()
            .all()
    }

    override fun save(entity: Replay): Mono<Replay> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: Collection<Replay>): Flux<Replay> {
        TODO("Not yet implemented")
    }

    private fun update(entity: Replay): Mono<Replay> {
        val id = entity.id
            ?: throw IllegalArgumentException("ID cannot be null when updating!")

        return databaseClient.update()
            .table(Replay::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: Replay): Mono<Replay> {
        if (entity.id != null) throw IllegalArgumentException("ID must be null when creating!")

        return databaseClient.insert()
            .into(Replay::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    throw DataRetrievalFailureException("Unable to save replay!")

                this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}