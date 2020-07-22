package me.honnold.ladderhero

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class LadderHeroApplication

fun main(args: Array<String>) {
    runApplication<LadderHeroApplication>(*args)
}