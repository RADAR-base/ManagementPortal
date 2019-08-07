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
import org.radarbase.auth.jersey.exception.HttpApplicationException
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.ws.rs.core.Context
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

    override fun toResponse(exception: HttpApplicationException): Response {
        logger.error("[{}] {} - {}: {}", exception.status, uriInfo.absolutePath, exception.code, exception.detailedMessage)

        val stringEncoder = BufferRecyclers.getJsonStringEncoder()
        val quotedError = stringEncoder.quoteAsUTF8(exception.code).toString(UTF_8)
        val quotedDescription = stringEncoder.quoteAsUTF8(exception.detailedMessage).toString(UTF_8)
        return Response.status(exception.status)
                .header("Content-Type", "application/json; charset=utf-8")
                .entity("{\"error\":\"$quotedError\","
                        + "\"error_description\":\"$quotedDescription\"}")
                .build()

    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpApplicationExceptionMapper::class.java)
    }
}
