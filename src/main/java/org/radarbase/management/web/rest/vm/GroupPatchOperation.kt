package org.radarbase.management.web.rest.vm

/**
 * A POJO for PATCH .../groups/{groupName}/subjects
 * [
 * {"op": "add", "value": [{"login": "sub1"}, {"id": 2}]},
 * {"op": "remove", "value": [{"login": "sub3"}]}
 * ]
 * request.
 */
class GroupPatchOperation {
    var op: String? = null
    var value: List<SubjectPatchValue>? = null

    class SubjectPatchValue {
        var id: Long? = null
        var login: String? = null
    }
}
