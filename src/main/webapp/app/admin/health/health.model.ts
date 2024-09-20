/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */

export type HealthStatus = 'UP' | 'DOWN' | 'UNKNOWN' | 'OUT_OF_SERVICE';

export type HealthKey =
    'diskSpace'
    | 'mail'
    | 'ping'
    | 'livenessState'
    | 'readinessState'
    | 'db';

export interface Health {
    status: HealthStatus;
    components: {
        [key in HealthKey]?: HealthDetails;
    };
}

export interface HealthDetails {
    status: HealthStatus;
    details?: { [key: string]: unknown };
}
