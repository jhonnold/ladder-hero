package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.dao.domain.Summary
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class SummaryDAO(private val databaseClient: DatabaseClient) : DAO<Summary, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(SummaryDAO::class.java)
    }

    override fun findById(id: UUID): Mono<Summary> {
        return databaseClient
            .select()
            .from(Summary::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findAllByReplay(replay: Replay): Flux<Summary> {
        val replayId = replay.id

        return if (replayId != null) this.findAllByReplayId(replayId) else Flux.empty()
    }

    fun findAllByReplayId(replayId: UUID): Flux<Summary> {
        return databaseClient
            .select()
            .from(Summary::class.java)
            .matching(Criteria.where("replay_id").`is`(replayId))
            .fetch()
            .all()
    }

    override fun save(entity: Summary): Mono<Summary> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<Summary>): Mono<List<Summary>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: Summary): Mono<Summary> {
        val id =
            entity.id
                ?: return IllegalArgumentException("ID cannot be null when updating!").toMono()

        return databaseClient
            .update()
            .table(Summary::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: Summary): Mono<Summary> {
        if (entity.id != null)
            return IllegalArgumentException("ID must be null when creating!").toMono()

        return databaseClient
            .insert()
            .into(Summary::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null) DataRetrievalFailureException("Unable to save Summary!").toMono()
                else this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}
