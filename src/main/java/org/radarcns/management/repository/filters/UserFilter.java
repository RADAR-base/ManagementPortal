package org.radarcns.management.repository.filters;

import org.apache.commons.lang.StringUtils;
import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class UserFilter implements Specification<User> {
    private String login;
    private String email;
    private String projectName;
    private String authority;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(login)) {
            predicates
                .add(cb.like(cb.lower(root.get("login")), "%" + login.trim().toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(email)) {
            predicates
                .add(cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%"));
        }

        if (StringUtils.isNotBlank(projectName)) {
            Join<User, Role> roleJoin = root.join("roles");
            Join<Role, Project> projectJoin = roleJoin.join("project");
            predicates.add(cb.like(cb.lower(projectJoin.get("projectName")),
                    "%" + projectName.trim().toLowerCase() + "%"));
        }
        if (StringUtils.isNotBlank(authority)) {
            Join<User, Role> roleJoin = root.join("roles");
            Join<Role, Authority> authorityJoin = roleJoin.join("authority");
            predicates.add(cb.like(cb.lower(authorityJoin.get("name")),
                    "%" + authority.trim().toLowerCase() + "%"));
        }
        return predicates.isEmpty() ? null
                : cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
