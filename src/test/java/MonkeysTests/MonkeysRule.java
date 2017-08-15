//package MonkeysTests;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.jvnet.hudson.test.JenkinsRule;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/9/2017.
 */
//public class MonkeysRule extends JenkinsRule {

//    public void setDirectory(File directory) throws Exception {
//        File[] files = directory.listFiles();
//        for(int i = 0; i < files.length; i ++) {
//            Jenkins jenkins = this.getInstance();
//            FreeStyleProject project = this.createFreeStyleProject();
//            project.updateByXml((Source) new StreamSource(new FileInputStream(files[i])));
//            FreeStyleBuild build = project.scheduleBuild2(0).get();
//            if(!build.getResult().equals(Result.SUCCESS)) {
//                System.out.println(files[i].getName());
//            }
//            assertEquals(build.getResult(), Result.SUCCESS);

//            FreeStyleProject project = (FreeStyleProject)jenkins.createProjectFromXML("WindowsAzureStorage" + i,
//                    new FileInputStream(files[i]));
//            FreeStyleBuild build = project.scheduleBuild2(0).get();
//            assertEquals(build.getResult(), Result.SUCCESS);
//        }
//    }

//}
