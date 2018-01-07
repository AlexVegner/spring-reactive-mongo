package com.example.reactive

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.mongodb.core.ReactiveMongoClientFactoryBean
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.toFlux

@SpringBootApplication
class ReactiveApplication

fun main(args: Array<String>) {
    //runApplication<ReactiveApplication>(*args)
    SpringApplicationBuilder()
            .sources(ReactiveApplication::class.java)
            .initializers(beans {
                bean {
                    ApplicationRunner {
                        val customerRepository = ref<CustomerRepository>()
                        val results = arrayOf("Alex", "Eugene", "Olga")
                                .toFlux()
                                .flatMap { customerRepository.save(Customer(name = it)) }

                        customerRepository
                                .deleteAll()
                                .thenMany(results)
                                .thenMany(customerRepository.findAll())
                                .subscribe { println(it) }
                    }
                }
                bean {
                    router {
                        val customerRepository = ref<CustomerRepository>()
                        GET("/customer") {ServerResponse.ok().body(customerRepository.findAll())}
                        GET("/customer/{id}") {ServerResponse.ok().body(customerRepository.findById( it.pathVariable("id")))}
                    }

                }
            })
            .run(*args)
}

interface CustomerRepository: ReactiveMongoRepository<Customer, String>

data class Customer(var id: String? = null, var name: String? = null)
