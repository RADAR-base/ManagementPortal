package org.radarcns.management.repository.filters;

import org.radarcns.management.domain.Authority;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Role;
import org.radarcns.management.domain.User;
import org.radarcns.management.web.rest.util.FilterUtil;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.radarcns.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarcns.auth.authorization.AuthoritiesConstants.PARTICIPANT;

public class UserFilter implements Specification<User> {
    private String login;
    private String email;
    private String projectName;
    private String authority;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();
        Join<User, Role> roleJoin = root.join("roles");
        Join<Role, Authority> authorityJoin = roleJoin.join("authority");
        Predicate filterParticipants = builder.and(
                builder.not(authorityJoin.get("name").in(PARTICIPANT, INACTIVE_PARTICIPANT)));
        predicates.add(filterParticipants);

        if (FilterUtil.isValid(login)) {
            predicates.add(builder.like(
                    builder.lower(root.get("login")), "%" + login.trim().toLowerCase() + "%"));
        }
        if (FilterUtil.isValid(email)) {
            predicates.add(builder.like(
                    builder.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%"));
        }

        if (FilterUtil.isValid(projectName)) {
            Join<Role, Project> projectJoin = roleJoin.join("project");
            predicates.add(builder.like(builder.lower(projectJoin.get("projectName")),
                    "%" + projectName.trim().toLowerCase() + "%"));
        }
        if (FilterUtil.isValid(authority)) {
            Predicate filterByAuthority = builder.and(builder.like(
                    builder.lower(authorityJoin.get("name")), "%" + authority.trim().toLowerCase() + "%"),
                    filterParticipants);
            predicates.add(filterByAuthority);

        } else {
            predicates.add(filterParticipants);
        }

        if (predicates.isEmpty()) {
            return null;
        } else {
            query.distinct(true);
            return builder.and(predicates.toArray(new Predicate[0]));
        }
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
