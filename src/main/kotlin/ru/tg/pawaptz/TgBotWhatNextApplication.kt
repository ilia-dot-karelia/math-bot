package ru.tg.pawaptz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TgBotWhatNextApplication

fun main(args: Array<String>) {
    Thread.setDefaultUncaughtExceptionHandler { a, b -> println("$a: $b") }
    runApplication<TgBotWhatNextApplication>(*args)
}
