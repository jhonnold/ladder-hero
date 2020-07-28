package me.honnold.ladderhero.dao

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DAO<T, ID> {
    fun findById(id: ID): Mono<T>
    fun save(entity: T): Mono<T>
    fun saveAll(entities: Collection<T>): Flux<T>
}