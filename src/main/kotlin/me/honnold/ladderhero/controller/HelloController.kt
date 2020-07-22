package me.honnold.ladderhero.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class HelloController {
    companion object {
        private val logger = LoggerFactory.getLogger(HelloController::class.java)
    }

    @GetMapping
    fun hello(): Mono<String> {
        logger.trace("A TRACE Message")
        logger.debug("A DEBUG Message")
        logger.info("An INFO Message")
        logger.warn("A WARN Message")
        logger.error("An ERROR Message")

        return Mono.just("Hello, World!")
    }
}