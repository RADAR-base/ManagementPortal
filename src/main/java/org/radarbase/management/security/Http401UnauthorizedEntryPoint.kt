/*
 * Copyright 2016-2017 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster project, see http://www.jhipster.tech/
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.radarbase.management.security

import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Returns a 401 error code (Unauthorized) to the client.
 */
class Http401UnauthorizedEntryPoint : AuthenticationEntryPoint {
    /**
     * Always returns a 401 error code to the client.
     */
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest, response: HttpServletResponse,
        arg2: AuthenticationException
    ) {
        log.debug("Pre-authenticated entry point called. Rejecting access")
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Http401UnauthorizedEntryPoint::class.java)
    }
}
