//package MonkeysTests;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import jenkins.model.Jenkins;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by t-yuhang on 8/9/2017.
 */
//@RunWith(Parameterized.class)
//public class MonkeysTest {

//    @ClassRule
//    public static MonkeysRule monkeysRule = new MonkeysRule();

//    private File file;
//
//    public MonkeysTest(File file) {
//        this.file = file;
//    }

//    @Test
//    public void monkeysTest() throws Exception {
//        Jenkins jenkins = monkeysRule.getInstance();
//        FreeStyleProject project = (FreeStyleProject)jenkins.createProjectFromXML("AzureStorage",
//                new FileInputStream(new File("D:\\jenkinsci\\windows-azure-storage-plugin\\config\\config_3.0.xml")));
//        FreeStyleBuild build = project.scheduleBuild2(0).get();
//        assertEquals(build.getResult(), Result.SUCCESS);

//        String configDir = "src\\test\\resources";
//        monkeysRule.setDirectory(new File(configDir));
//    }

//    @Test
//    public void test1() {
//        System.out.println("test1");
//    }
//
//    @Parameterized.Parameters
//    public static Collection<Object[]> fileList() {
//        File[] files = new File("src\\test\\resources").listFiles();
//        Collection<Object[]> fileList = new ArrayList<Object[]>();
//        for(File file: files) {
//            System.out.println(file.getName());
//            Object[] fileArg = new Object[]{file};
//            fileList.add(fileArg);
//        }
//        System.out.println();
//        return fileList;
//    }
//}
