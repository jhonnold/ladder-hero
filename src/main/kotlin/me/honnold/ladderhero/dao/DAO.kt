package me.honnold.ladderhero.dao

import reactor.core.publisher.Mono

interface DAO<T, ID> {
    fun findById(id: ID): Mono<T>
    fun save(entity: T): Mono<T>
    fun saveAll(entities: List<T>): Mono<List<T>>
}
