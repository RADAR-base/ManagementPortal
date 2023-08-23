package org.radarbase.management.client

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

class MPProjectSerializer : JsonTransformingSerializer<MPProject>(MPProject.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element
        val organization = element["organization"]
        return if (organization == null || organization is JsonNull || organization is JsonObject) {
            // MP 2.0 structure
            element
        } else {
            // MP 0.x structure
            val elementMap = element.toMutableMap()
            elementMap["organization"] = JsonNull
            elementMap["organizationName"] = organization
            return JsonObject(elementMap)
        }
    }
}
