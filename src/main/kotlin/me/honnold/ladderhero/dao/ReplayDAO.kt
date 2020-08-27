package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.dao.value.ReplayDetailRow
import me.honnold.ladderhero.dao.value.ReplaySummaryRow
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class ReplayDAO(private val databaseClient: DatabaseClient) : DAO<Replay, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayDAO::class.java)
    }

    override fun findById(id: UUID): Mono<Replay> {
        return databaseClient
            .select()
            .from(Replay::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findBySlug(slug: String): Mono<Replay> {
        return databaseClient
            .select()
            .from(Replay::class.java)
            .matching(Criteria.where("slug").`is`(slug))
            .fetch()
            .first()
    }

    fun findDetailsById(id: UUID): Flux<ReplayDetailRow> {
        return databaseClient
            .execute(ReplayDetailRow.ID_QUERY)
            .bind("id", id)
            .`as`(ReplayDetailRow::class.java)
            .fetch()
            .all()
    }

    fun findDetailsBySlug(slug: String): Flux<ReplayDetailRow> {
        return databaseClient
            .execute(ReplayDetailRow.SLUG_QUERY)
            .bind("slug", slug)
            .`as`(ReplayDetailRow::class.java)
            .fetch()
            .all()
    }

    fun findAll(pageRequest: PageRequest): Flux<ReplaySummaryRow> {
        return databaseClient
            .execute(ReplaySummaryRow.ALL_QUERY)
            .bind("offset", pageRequest.offset)
            .bind("limit", pageRequest.pageSize)
            .`as`(ReplaySummaryRow::class.java)
            .fetch()
            .all()
    }

    fun findAllByProfileId(profileId: Long, pageRequest: PageRequest): Flux<ReplaySummaryRow> {
        return databaseClient
            .execute(ReplaySummaryRow.PROFILE_ID_QUERY)
            .bind("profileId", profileId)
            .bind("offset", pageRequest.offset)
            .bind("limit", pageRequest.pageSize)
            .`as`(ReplaySummaryRow::class.java)
            .fetch()
            .all()
    }

    override fun save(entity: Replay): Mono<Replay> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<Replay>): Mono<List<Replay>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: Replay): Mono<Replay> {
        val id =
            entity.id
                ?: return IllegalArgumentException("ID cannot be null when updating!").toMono()

        return databaseClient
            .update()
            .table(Replay::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: Replay): Mono<Replay> {
        if (entity.id != null)
            return IllegalArgumentException("ID must be null when creating!").toMono()

        return databaseClient
            .insert()
            .into(Replay::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null) DataRetrievalFailureException("Unable to save replay!").toMono()
                else this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}
