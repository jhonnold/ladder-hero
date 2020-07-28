package me.honnold.ladderhero.dao

import me.honnold.ladderhero.dao.domain.Replay
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

class ReplayDAO : DAO<Replay, UUID> {
    override fun findById(id: UUID): Mono<Replay> {
        TODO("Not yet implemented")
    }

    override fun save(entity: Replay): Mono<Replay> {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<Replay>): Flux<Replay> {
        TODO("Not yet implemented")
    }
}