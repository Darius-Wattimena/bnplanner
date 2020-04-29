package nl.greaper.bnplanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class BnplannerApplication : SpringBootServletInitializer()

fun main(args: Array<String>) {
    runApplication<BnplannerApplication>(*args)
}
