package nl.greaper.bnplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class BnplannerApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<BnplannerApplication>(*args)
}
