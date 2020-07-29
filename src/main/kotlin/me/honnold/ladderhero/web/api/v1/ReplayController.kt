package me.honnold.ladderhero.web.api.v1

import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.service.domain.ReplayService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val replayService: ReplayService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @GetMapping
    fun getReplays(
        @RequestParam(defaultValue = "25") size: Int,
        @RequestParam(defaultValue = "1") page: Int
    ): Flux<Replay> {
        return this.replayService.getReplays(PageRequest.of(page - 1, size))
    }

    @GetMapping("/{lookup}")
    fun getReplay(@PathVariable lookup: String): Mono<Replay> {
        return this.replayService.getReplay(lookup)
    }
}