/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.exception

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStreamWriter


class DefaultExceptionHtmlRenderer: ExceptionHtmlRenderer {
    private val errorTemplates: Map<Int, Mustache>

    private val template4xx: Mustache
    private val template5xx: Mustache

    init {
        val mf = DefaultMustacheFactory()

        val loadTemplate = { code: String ->
            javaClass.getResourceAsStream("$code.html")
                    ?.use { stream ->
                        try {
                            stream.bufferedReader().use {
                                mf.compile(it, "$code.html")
                            }
                        } catch (ex: IOException) {
                            logger.error("Failed to read error template $code.html: {}", ex.toString())
                            null
                        }
                    }
        }

        errorTemplates = (400..599)
                .mapNotNull { code ->
                    loadTemplate(code.toString())
                            ?.let { code to it }
                }
                .toMap()

        template4xx = checkNotNull(loadTemplate("4xx")) { "Missing 4xx.html template" }
        template5xx = checkNotNull(loadTemplate("5xx")) { "Missing 5xx.html template" }
    }

    override fun render(exception: HttpApplicationException): String {
        val template = errorTemplates.getOrElse(exception.status) {
            if (exception.status in 400..499) {
                template4xx
            } else {
                template5xx
            }
        }

        return ByteArrayOutputStream().use { outStream ->
            OutputStreamWriter(outStream).use { template.execute(it, exception) }
            outStream.toString("UTF-8")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultExceptionHtmlRenderer::class.java)
    }
}
