/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package RegressionTests;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AzureJenkinsSuite extends Suite {
    public static final String DEFAULT_JOB_FILE_PATTERN = "*.xml";
    public static final String DEFAULT_CONFIG_DIR = "src/test/resources/config";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface JobsConfig {
        String path() default DEFAULT_CONFIG_DIR;

        String filePattern() default DEFAULT_JOB_FILE_PATTERN;
    }

    private static final List<Runner> NO_RUNNERS = Collections.<Runner>emptyList();

    private final ArrayList<Runner> runners = new ArrayList<>();

    public AzureJenkinsSuite(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        createRunners();
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    private void createRunners() throws InitializationError {
        for (File file : loadJobs()) {
            AzureJenkinsRunner runner = new AzureJenkinsRunner(getTestClass().getJavaClass(), file);
            runners.add(runner);
        }
    }

    private List<File> loadJobs() {
        String configDir = DEFAULT_CONFIG_DIR;
        String filePattern = DEFAULT_JOB_FILE_PATTERN;

        for (Class<?> eachClass : getSuperClasses(getTestClass().getJavaClass())) {
            JobsConfig jobsConfig = eachClass.getAnnotation(JobsConfig.class);
            if (jobsConfig != null) {
                configDir = jobsConfig.path();
                filePattern = jobsConfig.filePattern();
                break;
            }
        }

        return loadJobsFromResources(configDir, filePattern);
    }

    private List<File> loadJobsFromResources(String dir, String filePattern) {
        File folder = new File(dir);
        if (folder.exists()) {
            File[] files = folder.listFiles((FilenameFilter) new WildcardFileFilter(filePattern));
            return Arrays.asList(files);
        }
        return new ArrayList<>();
    }

    private List<Class<?>> getSuperClasses(Class<?> testClass) {
        ArrayList<Class<?>> results = new ArrayList<Class<?>>();
        Class<?> current = testClass;
        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }
        return results;
    }

    private final class AzureJenkinsRunner extends BlockJUnit4ClassRunner {
        private File file;

        AzureJenkinsRunner(Class<?> type, File file) throws InitializationError {
            super(type);
            this.file = file;
        }

        @Override
        public Object createTest() throws Exception {
            Source source = new StreamSource(new FileInputStream(file));
            return getTestClass().getOnlyConstructor().newInstance(source);
        }

        @Override
        protected String getName() {
            return file.getName();
        }

        @Override
        protected String testName(FrameworkMethod method) {
            return method.getName() + getName();
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }

        @Override
        protected Annotation[] getRunnerAnnotations() {
            return new Annotation[0];
        }

        @Override
        protected Statement classBlock(RunNotifier notifier) {
            return childrenInvoker(notifier);
        }
    }
}
