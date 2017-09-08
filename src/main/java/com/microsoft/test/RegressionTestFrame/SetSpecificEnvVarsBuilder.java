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

/**
 * Created by t-yuhang on 9/6/2017.
 */
public class SetSpecificEnvVarsBuilder extends Builder implements SimpleBuildStep {
    private final String key;
    private final String value;

    @DataBoundConstructor
    public SetSpecificEnvVarsBuilder(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        try {
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
    public SetSpecificEnvVarsDescriptor getDescriptor() {
        return (SetSpecificEnvVarsDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class SetSpecificEnvVarsDescriptor extends BuildStepDescriptor<Builder> {

        public SetSpecificEnvVarsDescriptor() {
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
