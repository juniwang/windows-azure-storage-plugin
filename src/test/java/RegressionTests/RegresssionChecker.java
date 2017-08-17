/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package RegressionTests;

import com.microsoft.jenkins.azurecommons.telemetry.AppInsightsGlobalConfig;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;
import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public abstract class RegresssionChecker {
    private Source jobSource;

    public RegresssionChecker(Source source) {
        this.jobSource = source;
        AppInsightsGlobalConfig.get().setAppInsightsEnabled(false);
    }

    protected void verify() {
        // did nothing here. Override this method to do any internal/external validation.
    }

    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    public void regressionTest() {
        assertNotEquals(jobSource, null);
    }
}
