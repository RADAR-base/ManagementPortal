package org.radarbase.management.client

import io.ktor.http.*
import java.io.IOException

class HttpStatusException(
    val code: HttpStatusCode,
    message: String,
) : IOException(message)
