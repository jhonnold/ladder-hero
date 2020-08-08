package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.FileUpload
import org.slf4j.LoggerFactory
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.*

@Service
class FileUploadDAO(private val databaseClient: DatabaseClient) : DAO<FileUpload, UUID> {
    companion object {
        private val logger = LoggerFactory.getLogger(FileUploadDAO::class.java)
    }

    override fun findById(id: UUID): Mono<FileUpload> {
        return databaseClient.select()
            .from(FileUpload::class.java)
            .matching(where("id").`is`(id))
            .fetch()
            .first()
    }

    override fun save(entity: FileUpload): Mono<FileUpload> {
        return if (entity.id == null) this.create(entity) else this.update(entity)
    }

    override fun saveAll(entities: List<FileUpload>): Mono<List<FileUpload>> {
        TODO("Not yet implemented")
    }

    private fun update(entity: FileUpload): Mono<FileUpload> {
        val id = entity.id
            ?: return IllegalArgumentException("ID cannot be null when updating!").toMono()

        return databaseClient.update()
            .table(FileUpload::class.java)
            .using(entity)
            .fetch()
            .rowsUpdated()
            .flatMap { this.findById(id) }
            .doOnSuccess { logger.debug("Successfully updated $entity") }
            .doOnError { t -> logger.error("There was an issue updating $entity -- ${t.message}") }
    }

    private fun create(entity: FileUpload): Mono<FileUpload> {
        if (entity.id != null)
            return IllegalArgumentException("ID must be null when creating!").toMono()

        return databaseClient.insert()
            .into(FileUpload::class.java)
            .using(entity)
            .map { row -> row.get(0, UUID::class.java) }
            .first()
            .flatMap { uuid ->
                if (uuid == null)
                    DataRetrievalFailureException("Unable to load file upload by uuid").toMono()
                else
                    this.findById(uuid)
            }
            .doOnSuccess { logger.debug("Successfully saved $entity") }
            .doOnError { t -> logger.error("There was an issue saving $entity -- ${t.message}") }
    }
}