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

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 9/4/2017.
 */
public class CheckRandomFilesAtLocalBuilder extends Builder implements SimpleBuildStep {

    private int filenum;
    private String filepattern = ".txt";
    private int filelen;

    @DataBoundConstructor
    public CheckRandomFilesAtLocalBuilder(final int filenum, final String filepattern, final int filelen) {
        this.filenum = filenum;
        this.filepattern = filepattern;
        this.filelen = filelen;
    }

    public int getFilenum() {
        return this.filenum;
    }

    public String getFilepattern() {
        return this.filepattern;
    }

    public int getFilelen() {
        return this.filelen;
    }

    @Override
    public void perform(final Run<?, ?> build, final FilePath workspace,
                        final Launcher launcher, final TaskListener listener) {
        try {
            File file = new File(workspace.getRemote());
            File[] files = file.listFiles();
            assertEquals(files.length, filelen);
        } catch (Exception ex) {

        }
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
