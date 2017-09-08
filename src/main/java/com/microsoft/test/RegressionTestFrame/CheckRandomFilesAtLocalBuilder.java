package com.microsoft.test.RegressionTestFrame;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 9/4/2017.
 */
public class CheckRandomFilesAtLocalBuilder extends Builder implements SimpleBuildStep {

    private String filePath = "";
    private final int fileNum;
    private String filePattern = "";
    private int fileLen;

    @DataBoundConstructor
    public CheckRandomFilesAtLocalBuilder(final int fileNum) {
        this.fileNum = fileNum;
    }

    @DataBoundSetter
    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    @DataBoundSetter
    public void setFilePattern(final String filePattern) {
        this.filePattern = filePattern;
    }

    @DataBoundSetter
    public void setFileLen(final int fileLen) {
        this.fileLen = fileLen;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public int getFileNum() {
        return this.fileNum;
    }

    public String getFilePattern() {
        return this.filePattern;
    }

    public int getFileLen() {
        return this.fileLen;
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        File file = new File((new FilePath(workspace, this.filePath)).getRemote());
        File[] files = file.listFiles();
        assertEquals(files.length, fileNum);
    }

    @Override
    public CheckRandomFilesAtLocalDescriptor getDescriptor() {
        return (CheckRandomFilesAtLocalDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class CheckRandomFilesAtLocalDescriptor extends BuildStepDescriptor<Builder> {

        public CheckRandomFilesAtLocalDescriptor() {
            load();
        }

        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Check Random Files At Local";
        }
    }
}
