package me.honnold.ladderhero.web.api.v2

import me.honnold.ladderhero.service.domain.ReplayService
import me.honnold.ladderhero.service.dto.replay.v2.ReplayDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v2/replays")
class ReplayController(private val replayService: ReplayService) {
    @GetMapping("/{lookup}")
    fun getReplay(@PathVariable lookup: String): Mono<ReplayDetails> {
        return this.replayService.getReplay(lookup)
    }
}
