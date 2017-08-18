package RegressionTests;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;

/**
 * Created by t-yuhang on 8/18/2017.
 */
public class JenkinsSuite extends Suite {
    public static final String DEFAULT_CONFIG_DIR = "src\\test\\resources";
    public static final String DEFAULT_JOB_FILE_PATTERN = "*.xml";

    private static final List<Runner> NO_RUNNERS = Collections.emptyList();
    private final ArrayList<Runner> runners = new ArrayList();

    public JenkinsSuite(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        this.createRunners(this.loadJobs());
    }

    protected List<Runner> getChildren() {
        return this.runners;
    }

    private void createRunners(List<File> fileList) throws InitializationError{

    }

    private List<File> loadJobs() {
        String configDir = DEFAULT_CONFIG_DIR;
        String filePattern = DEFAULT_JOB_FILE_PATTERN;
    }

    private class AzureJenkinsRunner extends BlockJUnit4ClassRunner {
        private File file;

        AzureJenkinsRunner(Class<?> type, File file) throws InitializationError {
            super(type);
            this.file = file;
        }

        protected String getName() {
            return file.getName();
        }

        protected String testName(FrameworkMethod method) {
            return method.getName() + getName();
        }

        protected Statement classBlock(RunNotifier notifier) {
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
