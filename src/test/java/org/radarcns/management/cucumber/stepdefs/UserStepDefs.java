package org.radarcns.management.cucumber.stepdefs;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.radarcns.management.security.JwtAuthenticationFilter;
import org.radarcns.management.web.rest.OAuthHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.radarcns.management.web.rest.UserResource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserStepDefs extends StepDefs {

    @Autowired
    private UserResource userResource;

    @Autowired
    private HttpServletRequest servletRequest;

    private MockMvc restUserMockMvc;

    @Before
    public void setup() throws ServletException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.init(new MockFilterConfig());
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
            .addFilter(filter).defaultRequest(get("/").with(OAuthHelper.bearerToken())).build();
    }

    @When("^I search user '(.*)'$")
    public void i_search_user_admin(String userId) throws Throwable {
        actions = restUserMockMvc.perform(get("/api/users/" + userId)
                .accept(MediaType.APPLICATION_JSON));
    }

    @Then("^the user is found$")
    public void the_user_is_found() throws Throwable {
        actions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Then("^his last name is '(.*)'$")
    public void his_last_name_is(String lastName) throws Throwable {
        actions.andExpect(jsonPath("$.lastName").value(lastName));
    }

}
