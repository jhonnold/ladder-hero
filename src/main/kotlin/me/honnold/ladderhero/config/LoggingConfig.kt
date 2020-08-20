package me.honnold.ladderhero.config

import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebFilter

@Configuration
open class LoggingConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(LoggingConfig::class.java)
    }

    @Bean
    open fun loggingFilter(): WebFilter {
        return WebFilter { exchange, chain ->
            val start = Date()
            val request = exchange.request
            val result = chain.filter(exchange)

            result.doOnTerminate {
                val time = Date().time - start.time
                logger.info(
                    "${exchange.response.rawStatusCode} ${request.method} ${request.path.pathWithinApplication()} [${request.queryParams}] ${time}ms")
            }
        }
    }
}
