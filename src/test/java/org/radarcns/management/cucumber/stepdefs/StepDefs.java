package org.radarcns.management.cucumber.stepdefs;

import org.radarcns.management.ManagementPortalApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = ManagementPortalApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
