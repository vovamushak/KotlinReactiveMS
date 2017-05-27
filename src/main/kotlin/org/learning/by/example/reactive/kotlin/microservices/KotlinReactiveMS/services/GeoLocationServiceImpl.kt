package org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.services

import org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.exceptions.GeoLocationNotFoundException
import org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.exceptions.GetGeoLocationException
import org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.exceptions.InvalidParametersException
import org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.model.GeoLocationResponse
import org.learning.by.example.reactive.kotlin.microservices.KotlinReactiveMS.model.GeographicCoordinates
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

internal class GeoLocationServiceImpl(val endPoint: String, val webClient: WebClient = WebClient.create())
    : GeoLocationService {

    private companion object {
        const val ADDRESS_PARAMETER = "?address="
        const val MISSING_ADDRESS = "missing address"
        const val OK_STATUS = "OK"
        const val ZERO_RESULTS = "ZERO_RESULTS"
        const val ERROR_GETTING_LOCATION = "error getting location"
        const val ADDRESS_NOT_FOUND = "address not found"
    }

    override fun fromAddress(addressMono: Mono<String>) =
            addressMono
                    .transform(this::buildUrl)
                    .transform(this::get)
                    .transform(this::geometryLocation)!!

    internal fun buildUrl(addressMono: Mono<String>) =
            addressMono.flatMap {
                if (it == "") Mono.error(InvalidParametersException(MISSING_ADDRESS))
                else Mono.just(endPoint + ADDRESS_PARAMETER + it)
            }

    internal fun get(urlMono: Mono<String>) =
            urlMono.flatMap {
                webClient.get()
                        .uri(it)
                        .accept(MediaType.APPLICATION_JSON)
                        .exchange()
                        .flatMap { it.bodyToMono(GeoLocationResponse::class.java) }
            }

    internal fun geometryLocation(geoLocationResponseMono: Mono<GeoLocationResponse>) =
            geoLocationResponseMono.flatMap {
                when (it.status) {
                    OK_STATUS -> with(it.results[0].geometry.location) { Mono.just(GeographicCoordinates(lat, lng)) }
                    ZERO_RESULTS -> Mono.error(GeoLocationNotFoundException(ADDRESS_NOT_FOUND))
                    else -> Mono.error(GetGeoLocationException(ERROR_GETTING_LOCATION))
                }
            }
}
