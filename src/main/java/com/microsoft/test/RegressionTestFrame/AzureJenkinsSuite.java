package com.microsoft.test.RegressionTestFrame;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.File;

/**
 * Created by t-yuhang on 8/18/2017.
 */
public class AzureJenkinsSuite extends Suite {

    public static final String DEFAULT_CONFIG_DIR = "src\\test\\resources";
    public static final String DEFAULT_JOB_FILE_PATTERN = "*.xml";

    private static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private final ArrayList<Runner> runners = new ArrayList();

    public AzureJenkinsSuite(final Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        this.createRunners(this.loadJobs());
    }

    protected List<Runner> getChildren() {
        return this.runners;
    }

    private void createRunners(final List<File> fileList) throws InitializationError {
        for (File file: fileList) {
            AzureJenkinsRunner runner = new AzureJenkinsRunner(getTestClass().getJavaClass(), file);
            this.runners.add(runner);
        }
    }

    private List<File> loadJobs() {
        String configDir = DEFAULT_CONFIG_DIR;
        String filePattern = DEFAULT_JOB_FILE_PATTERN;

        return loadJobsFromResources(configDir, filePattern);
    }

    private List<File> loadJobsFromResources(final String dir, final String filepattern) {
        File folder = new File(dir);
        if (folder.exists()) {
            File[] files = folder.listFiles((FilenameFilter) new WildcardFileFilter(filepattern));
            return Arrays.asList(files);
        } else {
            return new ArrayList<>();
        }
    }

    private class AzureJenkinsRunner extends BlockJUnit4ClassRunner {
        private File file;

        AzureJenkinsRunner(final Class<?> type, final File file) throws InitializationError {
            super(type);
            this.file = file;
        }

        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(new StreamSource(new FileInputStream(file)));
        }

        protected String getName() {
            return file.getName();
        }

        protected String testName(final FrameworkMethod method) {
            return method.getName() + this.getName();
        }

        protected void validateConstructor(final List<Throwable> errors) {
            this.validateOnlyOneConstructor(errors);
        }

        protected Statement classBlock(final RunNotifier notifier) {
            return this.childrenInvoker(notifier);
        }

        protected Annotation[] getRunnerAnnotations() {
            return new Annotation[0];
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface JobsConfig {
        String path() default DEFAULT_CONFIG_DIR;

        String filePattern() default DEFAULT_JOB_FILE_PATTERN;
    }
}
