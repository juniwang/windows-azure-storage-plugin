package com.microsoft.test.RegressionTestFrame;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoftopentechnologies.windowsazurestorage.helper.AzureCredentials;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.util.UUID;

/**
 * Created by t-yuhang on 9/4/2017.
 */
public class CheckRandomFilesAtLocalBuilder extends Builder implements SimpleBuildStep {

    private final String filepath;
    private int filenum;
    private String filepattern = ".txt";
    private int filelen;

    @DataBoundConstructor
    public CheckRandomFilesAtLocalBuilder(final String filepath, final int filenum, final String filepattern, final int filelen) {
        this.filepath = filepath;
        this.filenum = filenum;
        this.filepattern = filepattern;
        this.filelen = filelen;
    }

    public String getFilepath() {
        return this.filepath;
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
            final EnvVars envVars = build.getEnvironment(listener);
            String expFileP = Util.replaceMacro(filepath, envVars);

            String exp = filepat
            FilePath filePath = build.getWorkspace();
            File file = new File(filePath.getRemote());
            File[] files = file.listFiles();
            assertEquals(files.length, 50);
            for (File f: files) {
                String content = FileUtils.readFileToString(f);
                assertEquals(f.getName().trim(), fileHashMap.get(content).trim());
            }
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
