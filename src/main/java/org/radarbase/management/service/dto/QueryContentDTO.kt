package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.ContentType

class QueryContentDTO {

    var id : Long? = null ;
    var type: ContentType? = null
    var heading: String? = null
    var value: String?  = null
    var imageBlob: String? = null
    var imageAltText: String? = null
    var queryGroupId: Long? = null
}
