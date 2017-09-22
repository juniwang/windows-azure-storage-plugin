package RegressionTests;

import com.microsoft.test.RegressionTestFrame.AzureJenkinsFrame;
import com.microsoft.test.RegressionTestFrame.AzureJenkinsSuite;
import hudson.model.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.transform.Source;

import static org.junit.Assert.assertEquals;

@RunWith(AzureJenkinsSuite.class)
//@AzureJenkinsSuite.JobsConfig(path = "src\\test\\resources", filePattern = "*.xml")
public class RegressionIT extends AzureJenkinsFrame {
    public RegressionIT(Source source) {
        super(source);
    }

    @After
    public void tearDown() throws Exception {
        deleteContainerOnAzure("storageCredentialId", "${containername}");
    }
}