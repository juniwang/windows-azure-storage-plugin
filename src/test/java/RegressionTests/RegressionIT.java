package RegressionTests;

import com.microsoft.test.RegressionTestFrame.AzureJenkinsFrame;
import com.microsoft.test.RegressionTestFrame.AzureJenkinsSuite;
import hudson.model.Result;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.xml.transform.Source;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
@RunWith(AzureJenkinsSuite.class)
public class RegressionIT extends AzureJenkinsFrame {

    public RegressionIT(Source source) {
        super(source);
    }

    String containername;

    @Before
    public void setUp() throws Exception {
        containername = generateRandomString(12);
        setEnvVar("containername", containername);
    }
}
