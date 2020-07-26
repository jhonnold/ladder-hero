package me.honnold.ladderhero

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
open class LadderHeroApplication

fun main(args: Array<String>) {
    runApplication<LadderHeroApplication>(*args)
}