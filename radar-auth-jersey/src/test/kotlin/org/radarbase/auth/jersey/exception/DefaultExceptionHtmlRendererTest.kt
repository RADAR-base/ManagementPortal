/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.exception

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.radarbase.auth.jersey.exception.DefaultExceptionHtmlRenderer
import org.radarbase.auth.jersey.exception.HttpApplicationException
import javax.ws.rs.core.Response

internal class DefaultExceptionHtmlRendererTest {

    @Test
    fun render4xx() {
        val renderer = DefaultExceptionHtmlRenderer()
        val ex = HttpApplicationException(Response.Status.BAD_REQUEST, "code", "message")
        val result = renderer.render(ex)

        assertThat(result, containsString("<h1>Bad request (status code 400)</h1>"))
        assertThat(result, containsString("<p><i>code</i> - message</p>"))
    }

    @Test
    fun render5xx() {
        val renderer = DefaultExceptionHtmlRenderer()
        val ex = HttpApplicationException(Response.Status.INTERNAL_SERVER_ERROR, "code", "message")
        val result = renderer.render(ex)

        assertThat(result, containsString("<h1>Server error (status code 500)</h1>"))
        assertThat(result, containsString("<p><i>code</i> - message</p>"))
    }
}
