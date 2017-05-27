package org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.extensions

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder
import reactor.core.publisher.Mono

internal fun <T : Throwable, K : Any> T.toMono(): Mono<K> = Mono.error(this)!!
internal fun <T : Any> T.toMono() = Mono.just(this)!!
internal infix fun <T : Any> BodyBuilder.withBody(value: T) = this.contentType(APPLICATION_JSON_UTF8).toBody(value)
internal fun <T : Any> BodyBuilder.toBody(value: T) = this.body(value.toMono(), value.javaClass)!!

