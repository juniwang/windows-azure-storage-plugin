package com.microsoft.test.RegressionTestFrame;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.security.ACL;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
public class AzureJenkinsFrame {

    private Source source;

    public AzureJenkinsFrame(final Source source) {
        this.source = source;
    }

    private FreeStyleProject project;
    private FreeStyleBuild build;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    protected AzureCredentials getAzureCredentials(final String storageCredentialId) throws Exception {
        AzureCredentials azureCredentials = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        AzureCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(storageCredentialId));
        return azureCredentials;
    }

    protected String generateRandomString(final int length) {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("[^a-z0-9]", "a").substring(0, length);
    }

    protected void setEnvVar(final String key) throws Exception {
        setEnvVar(key, "");
    }

    protected void setEnvVar(final String key, final String value) throws Exception {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList =
                j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList =
                descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if (environmentVariablesNodePropertyList.size() == 0) {
            descriptorDescribableList.add(new EnvironmentVariablesNodeProperty(
                    new EnvironmentVariablesNodeProperty.Entry(key, value)));
        } else if (environmentVariablesNodePropertyList.size() == 1) {
            environmentVariablesNodePropertyList.get(0).getEnvVars().put(key, value);
        }
    }

    protected String getEnvVar(final String key) {
        return getEnvVar(key, "");
    }

    protected String getEnvVar(final String key, final String value) {
        DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList =
                j.jenkins.getGlobalNodeProperties();
        List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList =
                descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
        if (environmentVariablesNodePropertyList.size() == 1) {
            return environmentVariablesNodePropertyList.get(0).getEnvVars().get(key, value);
        } else {
            return value;
        }
    }

    protected void deleteDirAtLocal(final String filepath) throws Exception {
        File dir = new File(filepath);
        dir.deleteOnExit();
    }

    protected FreeStyleProject getProject() {
        return project;
    }

    protected FreeStyleBuild getBuild() {
        return build;
    }

    protected Result getBuildStatus() {
        return build.getResult();
    }

    @Test
    public void test() throws Exception {
        project = j.createFreeStyleProject();
        project.updateByXml(this.source);
        build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, getBuildStatus());
    }
}