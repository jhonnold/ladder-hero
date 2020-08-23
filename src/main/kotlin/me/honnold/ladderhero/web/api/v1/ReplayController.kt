package me.honnold.ladderhero.web.api.v1

import me.honnold.ladderhero.service.domain.ReplayService
import me.honnold.ladderhero.service.dto.replay.ReplayDetails
import me.honnold.ladderhero.service.dto.replay.ReplaySummary
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/replays")
class ReplayController(private val replayService: ReplayService) {
    @GetMapping
    fun getReplays(
        @RequestParam(defaultValue = "25")
        size: Int,
        @RequestParam(defaultValue = "1")
        page: Int,
        @RequestParam(required = false)
        profileId: Long?
    ): Flux<ReplaySummary> {
        val pageRequest = PageRequest.of(page - 1, size)

        return this.replayService.getReplays(pageRequest, profileId)
    }

    @GetMapping("/{lookup}")
    fun getReplay(@PathVariable lookup: String): Mono<ReplayDetails> {
        return this.replayService.getReplay(lookup)
    }
}
