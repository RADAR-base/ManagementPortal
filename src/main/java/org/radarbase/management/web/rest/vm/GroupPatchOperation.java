package org.radarbase.management.web.rest.vm;

import java.util.List;

/**
 * A POJO for PATCH .../groups/{groupName}/subjects
 *   [
 *     {"op": "add", "value": [{"login": "sub1"}, {"id": 2}]},
 *     {"op": "remove", "value": [{"login": "sub3"}]}
 *   ]
 * request.
 */
public class GroupPatchOperation {
    private String op;
    private List<SubjectPatchValue> value;

    public String getOp() {
        return op;
    }

    public List<SubjectPatchValue> getValue() {
        return value;
    }

    public static class SubjectPatchValue {
        private Long id;
        private String login;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }
    }
}
