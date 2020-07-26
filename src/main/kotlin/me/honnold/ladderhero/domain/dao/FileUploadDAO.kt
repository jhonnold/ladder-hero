package me.honnold.ladderhero.domain.dao

import me.honnold.ladderhero.domain.model.FileUpload
import me.honnold.ladderhero.domain.repository.FileUploadRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler

@Service
class FileUploadDAO(
    private val fileUploadRepository: FileUploadRepository,
    private val jdbcScheduler: Scheduler,
    private val transactionTemplate: TransactionTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(FileUploadDAO::class.java)
    }

    fun save(fileUpload: FileUpload): Mono<FileUpload> =
        Mono.fromCallable {
            this.transactionTemplate.execute {
                this.fileUploadRepository.save(fileUpload); fileUpload
            }
        }
            .doFirst { logger.debug("Saving $fileUpload") }
            .doOnSuccess { logger.debug("Successfully saved $fileUpload") }
            .doOnError { logger.error("Unable to save $fileUpload -- ${it.message}") }
            .subscribeOn(jdbcScheduler)
}