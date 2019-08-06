/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.exception

import java.lang.RuntimeException
import javax.ws.rs.core.Response

open class HttpApplicationException(val status: Int, val code: String, val detailedMessage: String?) : RuntimeException("[$status] $code: $detailedMessage") {
    constructor(status: Response.Status, code: String, detailedMessage: String?) : this(status.statusCode, code, detailedMessage)
}
