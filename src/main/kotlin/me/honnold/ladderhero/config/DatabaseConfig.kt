package me.honnold.ladderhero.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Configuration
open class DatabaseConfig {
    @Value("\${spring.datasource.maximum-pool-size}")
    private var poolSize: Int = 0

    @Bean
    open fun jdbcScheduler(): Scheduler = Schedulers.fromExecutor(Executors.newFixedThreadPool(poolSize))

    @Bean
    open fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate = TransactionTemplate(transactionManager)
}