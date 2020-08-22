package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Player
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class PlayerDAO(private val databaseClient: DatabaseClient) : DAO<Player, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(PlayerDAO::class.java)
    }

    override fun findById(id: UUID): Mono<Player> {
        return databaseClient
            .select()
            .from(Player::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findByProfileId(id: Long): Mono<Player> {
        return databaseClient
            .select()
            .from(Player::class.java)
            .matching(Criteria.where("profileId").`is`(id))
            .fetch()
            .first()
    }

    override fun save(entity: Player): Mono<Player> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<Player>): Mono<List<Player>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: Player): Mono<Player> {
        val id =
            entity.id
                ?: return IllegalArgumentException("ID cannot be null when updating!").toMono()

        return databaseClient
            .update()
            .table(Player::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: Player): Mono<Player> {
        if (entity.id != null)
            return IllegalArgumentException("ID must be null when creating!").toMono()

        return databaseClient
            .insert()
            .into(Player::class.java)
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
