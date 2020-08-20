package me.honnold.ladderhero.dao

import java.util.*
import me.honnold.ladderhero.dao.domain.User
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserDAO(private val databaseClient: DatabaseClient) : DAO<User, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(UserDAO::class.java)
    }

    override fun findById(id: UUID): Mono<User> {
        return databaseClient
            .select()
            .from(User::class.java)
            .matching(Criteria.where("id").`is`(id))
            .fetch()
            .first()
    }

    fun findByUsername(username: String): Mono<User> {
        return databaseClient
            .select()
            .from(User::class.java)
            .matching(Criteria.where("username").`is`(username))
            .fetch()
            .first()
    }

    override fun save(entity: User): Mono<User> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<User>): Mono<List<User>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: User): Mono<User> {
        val id =
            entity.id
                ?: return IllegalArgumentException("ID cannot be null when updating!").toMono()

        return databaseClient
            .update()
            .table(User::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: User): Mono<User> {
        if (entity.id != null)
            return IllegalArgumentException("ID must be null when creating!").toMono()

        return databaseClient
            .insert()
            .into(User::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    Mono.error<User>(DataRetrievalFailureException("Unable to save Summary!"))
                else this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}
