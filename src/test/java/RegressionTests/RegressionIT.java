package RegressionTests;

import hudson.model.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.xml.transform.Source;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/21/2017.
 */
@RunWith(AzureJenkinsSuite.class)
public class RegressionIT extends AzureJenkinsFrame {

    public RegressionIT(Source source) {
        super(source);
    }

    String storageCredentialId;
    String containername;

    @Before
    public void setUp() throws Exception {
        containername = GenerateRandomString(12);
        setEnvVar("containername", containername);
    }

    public void verify() {
        assertEquals(Result.SUCCESS, getBuildStatus());
    }

//    @After
//    public void tearDown() throws Exception {
//        deleteContainerOnAzure(storageCredentialId, containername);
//    }
}
