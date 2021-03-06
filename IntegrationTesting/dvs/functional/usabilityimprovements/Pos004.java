/*
 * ************************************************************************
 *
 * Copyright 2009-2010 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */

package dvs.functional.usabilityimprovements;

import java.util.Vector;

import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.IDataDrivenTest;
import static com.vmware.vcqa.util.Assert.assertNotNull;
import static com.vmware.vcqa.util.Assert.assertTrue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.vmware.vc.ClusterConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchManagerCompatibilityResult;
import com.vmware.vc.DistributedVirtualSwitchManagerDvsProductSpec;
import com.vmware.vc.DistributedVirtualSwitchManagerHostContainer;
import com.vmware.vc.DistributedVirtualSwitchProductSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.LogUtil;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.ClusterComputeResource;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.VirtualMachine;
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
 * 1.Get the compatible hosts for given vDs version and move hosts to cluster<BR>
 * TEST:<BR>>
 * 2.Invoke checkCompatibility method by passing container as cluster, <BR>>
 * dvsMembership as null and switchProductSpec as valid ProductSpec for<BR>
 * the new DVSs<BR>
 * 3.Invoke create dvs with new spec by adding hosts returned by <BR>
 * checkCompatibility method<BR>
 * CLEANUP:<br>
 * 4. Destroy vDs<br>
 */
public class Pos004 extends TestBase implements IDataDrivenTest 
{
   /*
    * private data variables
    */
   private DistributedVirtualSwitchManager dvsManager = null;
   private DistributedVirtualSwitch DVS = null;
   private ManagedObjectReference dvsManagerMor = null;
   private Folder folder = null;
   private ManagedObjectReference[] allHosts = null;
   private DistributedVirtualSwitchProductSpec productSpec = null;
   private ManagedObjectReference dvsMor = null;
   private DistributedVirtualSwitchManagerHostContainer hostContainer = null;
   private DistributedVirtualSwitchManagerCompatibilityResult[] expectedCompatibilityResult =
            null;
   private DistributedVirtualSwitchManagerCompatibilityResult[] actualCompatibilityResult =
            null;
   private String vDsVersion = null;
   private HostSystem hostSystem = null;
   private ManagedObjectReference hostFolderMor = null;
   private ManagedObjectReference clusterMor = null;
   private boolean moved = false;
   private ClusterComputeResource clusterComputeResource = null;
   private ManagedObjectReference dcMor = null;
   private final int TIMEOUT_MULTIPLIER = 3;

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

   public void setTestDescription()
   {
      setTestDescription("1.Get the compatible hosts for given vDs version"
               + " and move hosts to  cluster\n"
               + " 2. Invoke checkCompatibility method by passing\n"
               + " container as cluster, "
               + " dvsMembership as null and switchProductSpec "
               + "as valid ProductSpec for  the new DVS\n"
               + " 3.Invoke create dvs with new spec by adding hosts"
               + " returned by checkCompatibility method ");
   }

   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      ClusterConfigSpec clusterSpec = null;
      dvsManager = new DistributedVirtualSwitchManager(connectAnchor);
      folder = new Folder(connectAnchor);
      DVS = new DistributedVirtualSwitch(connectAnchor);
      hostSystem = new HostSystem(connectAnchor);
      dvsManagerMor = dvsManager.getDvSwitchManager();
      clusterComputeResource = new ClusterComputeResource(connectAnchor);
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
      expectedCompatibilityResult =
               new DistributedVirtualSwitchManagerCompatibilityResult[allHosts.length];
      for (int i = 0; i < allHosts.length; i++) {
         hostFolderMor = hostSystem.getHostFolder(allHosts[0]);
         expectedCompatibilityResult[i] =
                  new DistributedVirtualSwitchManagerCompatibilityResult();
         expectedCompatibilityResult[i].setHost(allHosts[i]);
         expectedCompatibilityResult[i].getError().clear();
      }
      /*
       * Create hostFilterSpec here
       */
      dcMor = folder.getDataCenter();
      hostFolderMor = folder.getHostFolder(dcMor);
      clusterSpec = folder.createClusterSpec();
      clusterMor =
               folder.createCluster(hostFolderMor, getTestId() + "-cluster",
                        clusterSpec);
      assertTrue(clusterComputeResource.moveInto(clusterMor, allHosts),
               "hosts  moved successfully ",
               "Unable to move the hosts to cluster");
      moved = true;
      hostContainer = DVSUtil.createHostContainer(clusterMor, true);
      assertNotNull(hostContainer, "created  hosts container",
               "Unable to create hosts container");
      return true;
   }

   @Test(description = "1.Get the compatible hosts for given vDs version"
               + " and move hosts to  cluster\n"
               + " 2. Invoke checkCompatibility method by passing\n"
               + " container as cluster, "
               + " dvsMembership as null and switchProductSpec "
               + "as valid ProductSpec for  the new DVS\n"
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
               this.dvsManager.queryCheckCompatibility(dvsManagerMor,
                        hostContainer, spec, null);
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
      if (moved) {
          /*
           * reset the hosts back
           */
         hostFolderMor = folder.getHostFolder(dcMor);
         VirtualMachine ivm = new VirtualMachine(connectAnchor);
         ivm.powerOffVMs(ivm.getAllVM());
         try {
             hostSystem.enterMaintenanceModes(TestUtil.arrayToVector(allHosts),
                 TestConstants.ENTERMAINTENANCEMODE_TIMEOUT
                 * TIMEOUT_MULTIPLIER, false);
             assertTrue((folder.moveInto(hostFolderMor, allHosts)),
                 "Moved hosts  successfully", " Move hosts failed ");
         } catch (Exception e) {
             log.error("Unable to enter MT mode or enter MT mode timeout!");
         } finally {
             try {
                 hostSystem.exitMaintenanceModes(TestUtil.arrayToVector(allHosts),
                     TestConstants.EXIT_MAINTMODE_DEFAULT_TIMEOUT_SECS);
             } catch (Exception e) {
                 log.error("Unable to exit MT mode or currently not in MT mode!");
             } finally {
                 assertTrue((folder.destroy(clusterMor)),
                     "Successfully destroyed cluster",
                     "Unable to  destroy cluster");
             }
         }
      }
      return true;
   }
 }
