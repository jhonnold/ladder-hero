package me.honnold.ladderhero.web.api.v1

import me.honnold.ladderhero.dao.domain.Replay
import me.honnold.ladderhero.service.domain.ReplayService
import me.honnold.ladderhero.service.dto.replay.ReplaySummary
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val replayService: ReplayService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReplayController::class.java)
    }

    @GetMapping
    fun getReplays(): Flux<ReplaySummary> {
        return this.replayService.getReplays()
    }

    @GetMapping("/{lookup}")
    fun getReplay(@PathVariable lookup: String): Mono<Replay> {
        return this.replayService.getReplay(lookup)
    }
}