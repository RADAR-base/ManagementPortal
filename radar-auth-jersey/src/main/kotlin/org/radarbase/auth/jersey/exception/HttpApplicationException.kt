/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey.exception

import java.lang.RuntimeException
import javax.ws.rs.core.Response

open class HttpApplicationException(val status: Int, val code: String, val detailedMessage: String? = null, val additionalHeaders: List<Pair<String, String>> = listOf()) : RuntimeException("[$status] $code: ${detailedMessage ?: "no message"}") {
    constructor(status: Response.Status, code: String, detailedMessage: String? = null, additionalHeaders: List<Pair<String, String>> = listOf()) : this(status.statusCode, code, detailedMessage, additionalHeaders)
}
