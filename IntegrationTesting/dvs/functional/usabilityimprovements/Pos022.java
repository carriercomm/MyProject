/*
 * ************************************************************************
 *
 * Copyright 2009-2010 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */

package dvs.functional.usabilityimprovements;

import com.vmware.vcqa.IDataDrivenTest;
import static com.vmware.vcqa.util.Assert.assertNotNull;
import static com.vmware.vcqa.util.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.vmware.vc.DistributedVirtualSwitchManagerCompatibilityResult;
import com.vmware.vc.DistributedVirtualSwitchManagerDvsProductSpec;
import com.vmware.vc.DistributedVirtualSwitchManagerHostContainer;
import com.vmware.vc.DistributedVirtualSwitchManagerHostDvsFilterSpec;
import com.vmware.vc.DistributedVirtualSwitchProductSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.dvs.DistributedVirtualSwitchManager;

/**
 * DESCRIPTION:<BR>
 * (Test Case to verify checkCompatibility api for three specs that contain<BR>
 * HostArrayFilter, HostContainerFilter, HostDvsMembershipFilter) <BR>
 * TARGET: VC <BR>
 * <BR>
 * SETUP:<BR>
 * 1.Get the compatible hosts for given vDs version<BR>
 * TEST:<BR>>
 * 2.Invoke checkCompatibility method by passing container as
 * computeResourceMor, <BR>>
 * (of the first host) as container and set recursive flag to false with <BR>>
 * inclusive set to true and and switchProductSpec as valid ProductSpec for<BR>
 * the new DVSs<BR>
 * 3.Invoke create dvs with new spec by adding hosts returned by <BR>
 * checkCompatibility method<BR>
 * CLEANUP:<br>
 * 4. Destroy vDs<br>
 */
public class Pos022 extends TestBase implements IDataDrivenTest
{
   /*
    * private data variables
    */
   private DistributedVirtualSwitchManager dvsManager = null;
   private HostSystem hostSystem = null;
   private DistributedVirtualSwitch DVS = null;
   private ManagedObjectReference dvsManagerMor = null;
   private Folder folder = null;
   private ManagedObjectReference[] allHosts = null;
   private DistributedVirtualSwitchProductSpec productSpec = null;
   private ManagedObjectReference dvsMor = null;
   private DistributedVirtualSwitchManagerHostDvsFilterSpec hostContainerFilter =
            null;
   private DistributedVirtualSwitchManagerHostContainer hostContainer = null;
   private DistributedVirtualSwitchManagerHostContainer rootContainer = null;
   private DistributedVirtualSwitchManagerCompatibilityResult[] expectedCompatibilityResult =
            null;
   private DistributedVirtualSwitchManagerCompatibilityResult[] actualCompatibilityResult =
            null;
   private String vDsVersion = null;

   /**
    * Factory method to create the data driven tests.
    *
    * @return Object[] TestBase objects.
    *
    * @throws Exception
    */
   @Factory
   @Parameters({"dataFile"})
   public Object[] getTests(@Optional("")String dataFile) throws Exception {
      return TestExecutionUtils.getTests(this.getClass().getName(), dataFile);
   }
   public String getTestName()
   {
      return getTestId();
   }

   /**
    * This method will set the Description
    */
   @Override
   public void setTestDescription()
   {
      setTestDescription("1. Get the compatible hosts for given vDs version\n"
               + " 2. Invoke checkCompatibility method by passing the"
               + " computeResourceMor (of the first host) as container and"
               + " set recursive flag to "
               + "false with inclusive set to true and switchProductSpec"
               + " as valid" + " ProductSpec for the new DVS\n"
               + " 3.Invoke create dvs with new spec by adding hosts"
               + " returned by checkCompatibility method ");
   }

   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      dvsManager = new DistributedVirtualSwitchManager(connectAnchor);
      hostSystem = new HostSystem(connectAnchor);
      folder = new Folder(connectAnchor);
      DVS = new DistributedVirtualSwitch(connectAnchor);
      dvsManagerMor = dvsManager.getDvSwitchManager();
      vDsVersion = this.data.getString(DVSTestConstants.NEW_VDS_VERSION);
      productSpec = DVSUtil.getProductSpec(connectAnchor, vDsVersion);
      allHosts =
               dvsManager.queryCompatibleHostForNewDVS(dvsManagerMor,
                        this.folder.getDataCenter(), true, productSpec);
      assertNotNull(allHosts, "Found required number of hosts ",
               "Unable to find required number of hosts");

      /*
       * Create expected CompatibilityResult here
       */
      expectedCompatibilityResult = new DistributedVirtualSwitchManagerCompatibilityResult[1];

      expectedCompatibilityResult[0] = new DistributedVirtualSwitchManagerCompatibilityResult();
      expectedCompatibilityResult[0].setHost(allHosts[0]);
      expectedCompatibilityResult[0].getError().clear();
      /*
       * Create hostFilterSpec here
       */
      hostContainer =
               DVSUtil.createHostContainer(this.hostSystem
                        .getParentNode(allHosts[0]), false);
      hostContainerFilter =
               DVSUtil.createHostContainerFilter(hostContainer, true);
      rootContainer = DVSUtil.createHostContainer(
               this.folder.getDataCenter(), true);
      assertNotNull(hostContainer, "created  hosts container",
               "Unable to create hosts container");

      return true;
   }

   @Test(description = "1. Get the compatible hosts for given vDs version\n"
               + " 2. Invoke checkCompatibility method by passing the"
               + " computeResourceMor (of the first host) as container and"
               + " set recursive flag to "
               + "false with inclusive set to true and switchProductSpec"
               + " as valid" + " ProductSpec for the new DVS\n"
               + " 3.Invoke create dvs with new spec by adding hosts"
               + " returned by checkCompatibility method ")
   public void test()
      throws Exception
   {
      DistributedVirtualSwitchManagerDvsProductSpec spec =
               new DistributedVirtualSwitchManagerDvsProductSpec();
      spec.setDistributedVirtualSwitch(null);
      spec.setNewSwitchProductSpec(productSpec);
      actualCompatibilityResult =
               this.dvsManager
                        .queryCheckCompatibility(
                                 dvsManagerMor,
                                 this.rootContainer,
                                 spec,
                                 new DistributedVirtualSwitchManagerHostDvsFilterSpec[] { hostContainerFilter });
      assertTrue((DVSUtil.verifyCompatibilityResults(
               expectedCompatibilityResult, actualCompatibilityResult)),
               "Unable verify CompatibilityResults");
      this.dvsMor =
               folder.createDistributedVirtualSwitch(TestUtil.getShortTime(),
                        vDsVersion, this.allHosts);
      assertNotNull(dvsMor, "Successfully created the DVSwitch",
               "Null returned for Distributed Virtual Switch MOR");

   }

   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      if (this.dvsMor != null) {
         assertTrue(this.DVS.destroy(this.dvsMor),
                  "dvsMor destroyed successfully",
                  "dvsMor could not be removed");
      }
      return true;
   }
   

}
