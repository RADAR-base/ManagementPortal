/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.inject

import com.fasterxml.jackson.core.util.BufferRecyclers
import org.radarbase.auth.jersey.exception.ExceptionHtmlRenderer
import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider
import kotlin.text.Charsets.UTF_8

@Provider
@Singleton
class HttpApplicationExceptionMapper : ExceptionMapper<HttpApplicationException> {
    @Context
    private lateinit var uriInfo: UriInfo

    @Context lateinit var requestContext: ContainerRequestContext

    @Context lateinit var htmlRenderer: ExceptionHtmlRenderer

    override fun toResponse(exception: HttpApplicationException): Response {
        var mediaType = requestContext.acceptableMediaTypes
                .firstOrNull { type -> type in setOf(MediaType.WILDCARD_TYPE, MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE, MediaType.TEXT_PLAIN) }
                ?: MediaType.TEXT_PLAIN_TYPE

        logger.error("[{}] {} <{}> - {}: {}", exception.status, uriInfo.absolutePath, mediaType, exception.code, exception.detailedMessage)

        val entity = when (mediaType) {
            MediaType.APPLICATION_JSON_TYPE -> {
                val stringEncoder = BufferRecyclers.getJsonStringEncoder()
                val quotedError = stringEncoder.quoteAsUTF8(exception.code).toString(UTF_8)
                val quotedDescription = exception.detailedMessage?.let {
                    '"' + stringEncoder.quoteAsUTF8(it).toString(UTF_8) + '"'
                } ?: "null"

                "{\"error\":\"$quotedError\",\"error_description\":$quotedDescription}"
            }
            MediaType.TEXT_HTML_TYPE -> htmlRenderer.render(exception)
            else -> {
                mediaType = MediaType.TEXT_PLAIN_TYPE
                "[${exception.status}] ${exception.code}: ${exception.detailedMessage ?: "unknown reason"}"
            }
        }

        val responseBuilder = Response.status(exception.status)
                .entity(entity)
                .header("Content-Type", mediaType.withCharset("utf-8").toString())

        exception.additionalHeaders.forEach { (name, value) ->
            responseBuilder.header(name, value)
        }

        return responseBuilder.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpApplicationExceptionMapper::class.java)
    }
}
