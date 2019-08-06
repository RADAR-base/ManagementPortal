/*
 * Copyright (c) 2019. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

package org.radarbase.auth.jersey

data class AuthConfig(
        val managementPortalUrl: String? = null,
        val jwtResourceName: String,
        val jwtIssuer: String? = null,
        val jwtECPublicKeys: List<String>? = null,
        val jwtRSAPublicKeys: List<String>? = null,
        val jwtKeystorePath: String? = null,
        val jwtKeystorePassword: String? = null,
        val jwtKeystoreAlias: String? = null
        )
