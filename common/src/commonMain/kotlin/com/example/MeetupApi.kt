package com.example

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


class MeetupApi {
    private val httpClient = HttpClient()

    fun upcomingEvents(latitude: Double, longitude: Double, success: (Response) -> Unit, error: (Exception) -> Unit) {
        GlobalScope.apply {
            launch(ApplicationDispatcher) {
                try {

                    val jsonData = httpClient.get<String> {
                        url {
                            protocol = URLProtocol.HTTPS
                            port = 443
                            host = "api.meetup.com"
                            encodedPath = "/find/upcoming_events?lat=$latitude&lon=$longitude&radius=100&key=104d76161542243a70322c3b5033151"
                        }
                    }

                    val response = Json(JsonConfiguration.Stable.copy(strictMode = false)).parse(Response.serializer(), jsonData)
                    success(response)
                } catch (exception: Exception) {
                    error(exception)
                }
            }
        }
    }
}

@Serializable
data class Response(val city: City,
                    val events: List<Event>)

@Serializable
data class City(val id: Long,
                val city: String,
                val lat: Double,
                val lon: Double,
                val state: String,
                val country: String,
                val zip: String,
                val member_count: Int)

@Serializable
data class Event(val id: String,
                 val name: String,
                 val venue: Venue? = null)

@Serializable
data class Venue(val id: Long,
                 val name: String,
                 val lat: Double,
                 val lon: Double)
