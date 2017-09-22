package com.microsoft.test.RegressionTestFrame;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.UUID;

public class SetRandomEnvVarsBuilder extends Builder implements SimpleBuildStep {

    private final String key;
    private final int valueLen;


    @DataBoundConstructor
    public SetRandomEnvVarsBuilder(final String key, final int valueLen) {
        this.key = key;
        this.valueLen = valueLen;
    }

    private String generateRandomString(final int length) {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("[^a-z0-9]", "a").substring(0, length);
    }

    public String getKey() {
        return this.key;
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        try {
            String value = generateRandomString(valueLen);
            DescribableList<NodeProperty<?>, NodePropertyDescriptor> descriptorDescribableList =
                    Jenkins.getInstance().getGlobalNodeProperties();
            List<EnvironmentVariablesNodeProperty> environmentVariablesNodePropertyList =
                    descriptorDescribableList.getAll(EnvironmentVariablesNodeProperty.class);
            if (environmentVariablesNodePropertyList.size() == 0) {
                descriptorDescribableList.add(new EnvironmentVariablesNodeProperty(
                        new EnvironmentVariablesNodeProperty.Entry(key, value)));
            } else if (environmentVariablesNodePropertyList.size() == 1) {
                environmentVariablesNodePropertyList.get(0).getEnvVars().put(key, value);
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public SetRandomEnvVarsDescriptor getDescriptor() {
        return (SetRandomEnvVarsDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class SetRandomEnvVarsDescriptor extends BuildStepDescriptor<Builder> {

        public SetRandomEnvVarsDescriptor() {
            load();
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Set Random Env Vars";
        }
    }
}
