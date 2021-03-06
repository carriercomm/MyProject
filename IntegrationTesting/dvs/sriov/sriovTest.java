package dvs.sriov;

import static com.vmware.vcqa.util.Assert.assertTrue;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.vmware.vc.MethodFault;
import com.vmware.vcqa.ConnectAnchor;
import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.dvs.testframework.Step;
import com.vmware.vcqa.vim.dvs.testframework.StepReader;

public class sriovTest extends TestBase implements IDataDrivenTest
{

   private Object     sriovTestFramework = null;
   private Class<?>   testFrameworkClass = null;
   private StepReader xmlTester          = null;

   /**
    * This method executes the steps specified as part of the test group
    * 
    * @throws Exception
    */
   @Override
   @Test(description = "Set of all tests for lag api")
   public void test() throws Exception
   {
      List<Step> stepList = xmlTester.getSteps(DVSTestConstants.TEST);
      MethodFault expectedMethodFault = xmlTester.getExpectedMethodFault();
      try {
         Method method = testFrameworkClass.getDeclaredMethod("execute",
               List.class);
         method.invoke(this.sriovTestFramework, stepList);
         if (expectedMethodFault != null) {
            log.error("There was no exception thrown");
            throw new Exception("No exception thrown");
         }
      } catch (InvocationTargetException ex1) {
         try {
            throw ex1.getTargetException();
         } catch (Throwable ex2) {
            if (ex2 instanceof InvocationTargetException) {
               try {
                  InvocationTargetException excep = (InvocationTargetException) ex2;
                  throw (Exception) excep.getTargetException();
               } catch (Exception actualMethodFaultExcep) {
                  MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil
                  .getFault(actualMethodFaultExcep);
                  boolean result = actualMethodFault.getClass().equals(
                        expectedMethodFault.getClass());
                  assertTrue(result, "The expected and actual method faults "
                        + "match",
                  "The expected and actual method faults do not match");
               }
            }
         }
      }
   }

   /**
    * This method invokes the list of cleanup steps as provided by the user
    * 
    * @throws Exception
    * 
    */
   @Override
   @AfterMethod(alwaysRun = true)
   public boolean testCleanUp() throws Exception
   {
      List<Step> stepList = xmlTester.getSteps(DVSTestConstants.TEST_CLEANUP);
      Method method = testFrameworkClass.getDeclaredMethod("execute",
            List.class);
      method.invoke(this.sriovTestFramework, stepList);
      return true;
   }

   /**
    * This method initializes the current test parameters
    *
    * @throws Exception
    */
   public void initializeTest() throws Exception
   {
      if (DVSUtil.getvDsVersion().compareTo(DVSTestConstants.VDS_VERSION_55) < 0) {
          log.error("cannot execute sriov test " +
                "cases prior to dvs version 5.5.0");
          throw new Exception("cannot execute sriov test " +
                "cases prior to dvs version 5.5.0");
      }
      xmlTester = new StepReader(this.data);
      String testFrameworkClassName = xmlTester
      .getData(DVSTestConstants.TEST_FRAMEWORK);
      String beanPath = xmlTester.getData(DVSTestConstants.BEAN_DATA);
      testFrameworkClass = Class.forName(testFrameworkClassName);
      Constructor<?> constructor = testFrameworkClass.getConstructor(
            ConnectAnchor.class, String.class);
      sriovTestFramework = constructor
      .newInstance(this.connectAnchor, beanPath);
   }

   @Override
   @BeforeMethod(alwaysRun = true)
   public boolean testSetUp() throws Exception
   {
      initializeTest();
      List<Step> stepList = xmlTester.getSteps(DVSTestConstants.TEST_SETUP);
      /*
       * Execute the list of steps on the framework
       */
      Method method = testFrameworkClass.getDeclaredMethod("execute",
            List.class);
      method.invoke(this.sriovTestFramework, stepList);
      return true;
   }

   /**
    * This method retrieves either all the data-driven tests or one test based
    * on the presence of test id in the execution properties file.
    */
   @Factory
   @Parameters( { "dataFile" })
   public Object[] getTests(@Optional("") String dataFile) throws Exception
   {
      Object[] tests = TestExecutionUtils.getTests(this.getClass().getName(),
            dataFile);
      /*
       * Load the dvs execution properties file
       */
      String testId = TestUtil.getPropertyValue(this.getClass().getName(),
            DVSTestConstants.DVS_EXECUTION_PROP_FILE);
      if (testId == null) {
         String excludedTests = TestUtil.getPropertyValue("exclude",
               DVSTestConstants.DVS_EXECUTION_PROP_FILE);
         if (excludedTests != null) {
            String[] excludedTestIds = excludedTests.split(",");
            ArrayList<Object> testList = new ArrayList<Object>(Arrays
                  .asList(tests));
            for (Object o : tests) {
               TestBase testBase = (TestBase) o;
               for (String id : excludedTestIds) {
                  if (testBase.getTestId().contains(id)) {
                     testList.remove(o);
                     break;
                  }
               }
            }
            return testList.toArray();
         } else {
            return tests;
         }
      } else {
         for (Object test : tests) {
            if (test instanceof TestBase) {
               TestBase testBase = (TestBase) test;
               if (testBase.getTestId().equals(testId)) {
                  return new Object[] { testBase };
               }
            } else {
               log.error("The current test is not an instance of TestBase");
            }
         }
         log.error("The test id " + testId + "could not be found");
      }
      /*
       * TODO : Examine the possibility of a custom exception here since the
       * test id provided is wrong and the user needs to be notified of that.
       */
      return null;
   }

   /**
    * (non-Javadoc)
    * 
    * @see org.testng.ITest#getTestName()
    */
   public String getTestName()
   {
      return getTestId();
   }
}
