package org.radarcns.management.repository.filters;

import org.apache.commons.lang.StringUtils;
import org.radarcns.management.domain.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class UserFilter implements Specification<User>
{
    private String login;
    private String email;
    private String projectName;
    private String authority;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb)
    {

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(login))
        {
            predicates.add(cb.equal(root.get("login"), login));
        }
        if (StringUtils.isNotBlank(email))
        {
            predicates.add(cb.equal(root.get("email"), email));
        }
//        if (StringUtils.isNotBlank(projectName))
//        {
//            predicates.add(cb.equal(root.get("vehicle"), projectName));
//        }
//        if (StringUtils.isNotBlank(authority))
//        {
//            predicates.add(cb.equal(root.get("identifier"), authority));
//        }

        return predicates.size() <= 0 ? null : cb.and(predicates.toArray(new Predicate[predicates.size()]));
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
