/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.createdvs;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DVSNameArrayUplinkPortPolicy;
import com.vmware.vc.DistributedVirtualSwitchHostMemberConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicBacking;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.NetworkSystem;

import dvs.CreateDVSTestBase;

/**
 * Create DVSwitch with the following parameters with the following parameters
 * set in the configSpec: - DVSConfigSpec.maxPort set to a valid number equal to
 * numPorts - DVSConfigSpec.numPort set to a valid number equal to the number of
 * uplinks ports per host - DVSConfigSpec.uplinkPortPolicy set to a valid array
 * that is equal to the max number of pnics per host.({uplink1, ..., uplink32})
 * - DistributedVirtualSwitchHostMemberConfigSpec.operation set to add -
 * DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host Mor -
 * DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic proxy
 * selection with pnic spec having a valid pnic device and uplinkPortKey set to
 * null
 */
public class Pos016 extends CreateDVSTestBase
{
   /*
    * private data variables
    */
   private HostSystem ihs = null;
   private Vector<ManagedObjectReference> allHosts = null;
   private ManagedObjectReference[] hostMors = null;
   private NetworkSystem iNetworkSystem = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Create DVSwitch with the following parameters"
               + " with the following parameters set in the configSpec:\n"
               + "- DVSConfigSpec.maxPort set to a valid number equal to numPorts\n"
               + "- DVSConfigSpec.numPort set to a valid number equal to the number of"
               + "uplinks ports per host\n"
               + "- DVSConfigSpec.uplinkPortPolicy set to a valid array that is equal "
               + "to the max number of pnics per host.\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.operation set to add\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host Mor\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic proxy selection "
               + "with pnic spec having a valid pnic device and uplinkPortKey set to "
               + "null");
   }

   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean status = false;
      this.hostMors = new ManagedObjectReference[2];
      DistributedVirtualSwitchHostMemberPnicSpec hostPnicSpec = null;
      DistributedVirtualSwitchHostMemberPnicBacking hostPnicBacking = null;
      DistributedVirtualSwitchHostMemberConfigSpec[] hostConfigSpecElement = new DistributedVirtualSwitchHostMemberConfigSpec[2];
      DVSNameArrayUplinkPortPolicy uplinkPolicyInst = new DVSNameArrayUplinkPortPolicy();
      String dvsName = this.getTestId();
      log.info("Test setup Begin:");
     
         if (super.testSetUp()) {
            this.ihs = new HostSystem(connectAnchor);
            this.iNetworkSystem = new NetworkSystem(connectAnchor);
            allHosts = this.ihs.getAllHost();
            if ((allHosts != null) && (allHosts.size() >= 2)) {
               this.hostMors[0] = (ManagedObjectReference) allHosts.get(0);
               this.hostMors[1] = (ManagedObjectReference) allHosts.get(1);
            } else {
               log.error("Valid Host MOR not found");
            }
            this.networkFolderMor = this.iFolder.getNetworkFolder(this.dcMor);
            if (this.networkFolderMor != null) {
               this.configSpec = new DVSConfigSpec();
               this.configSpec.setConfigVersion("");
               this.configSpec.setName(dvsName);
               this.configSpec.setMaxPorts(4);
               this.configSpec.setNumStandalonePorts(2);
               String[] uplinkPortNames = new String[] { "Uplink1" };
               uplinkPolicyInst.getUplinkPortName().clear();
               uplinkPolicyInst.getUplinkPortName().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(uplinkPortNames));
               this.configSpec.setUplinkPortPolicy(uplinkPolicyInst);
               String[] hostPhysicalNics = null;
               for (int i = 0; i < 2; i++) {
                  hostPhysicalNics = iNetworkSystem.getPNicIds(this.hostMors[i]);
                  if (hostPhysicalNics != null) {
                     hostConfigSpecElement[i] = new DistributedVirtualSwitchHostMemberConfigSpec();
                     hostPnicSpec = new DistributedVirtualSwitchHostMemberPnicSpec();
                     hostPnicSpec.setPnicDevice(hostPhysicalNics[0]);
                     hostPnicSpec.setUplinkPortKey(null);
                     hostPnicBacking = new DistributedVirtualSwitchHostMemberPnicBacking();
                     hostPnicBacking.getPnicSpec().clear();
                     hostPnicBacking.getPnicSpec().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberPnicSpec[] { hostPnicSpec }));
                     hostConfigSpecElement[i].setBacking(hostPnicBacking);
                     hostConfigSpecElement[i].setHost(this.hostMors[i]);
                     hostConfigSpecElement[i].setOperation(TestConstants.CONFIG_SPEC_ADD);
                     hostConfigSpecElement[i].setMaxProxySwitchPorts(new Integer(
                              uplinkPortNames.length + 1));
                  } else {
                     log.error("No free pnics found on the host.");
                  }
               }
               this.configSpec.getHost().clear();
               this.configSpec.getHost().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(hostConfigSpecElement));
               status = true;
            } else {
               log.error("Failed to create the network folder");
            }
         } else {
            log.error("Failed to login");
         }
     

      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that creates the DVS.
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Create DVSwitch with the following parameters"
               + " with the following parameters set in the configSpec:\n"
               + "- DVSConfigSpec.maxPort set to a valid number equal to numPorts\n"
               + "- DVSConfigSpec.numPort set to a valid number equal to the number of"
               + "uplinks ports per host\n"
               + "- DVSConfigSpec.uplinkPortPolicy set to a valid array that is equal "
               + "to the max number of pnics per host.\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.operation set to add\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host Mor\n"
               + "- DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic proxy selection "
               + "with pnic spec having a valid pnic device and uplinkPortKey set to "
               + "null")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
     
         if (this.configSpec != null) {
            this.dvsMOR = this.iFolder.createDistributedVirtualSwitch(
                     this.networkFolderMor, this.configSpec);
            if (this.dvsMOR != null) {
               log.info("Successfully created the DVSwitch");
               Thread.sleep(10000);
               if (iDistributedVirtualSwitch.validateDVSConfigSpec(this.dvsMOR,
                        this.configSpec, null)) {
                  if (DVSUtil.checkNetworkConnectivity(
                           this.ihs.getIPAddress(this.hostMors[0]), null)) {
                     log.info("Network connection established with the host");
                     if (DVSUtil.checkNetworkConnectivity(
                              this.ihs.getIPAddress(this.hostMors[1]), null)) {
                        log.info("Network connection established with the "
                                 + "host");
                        status = true;
                     } else {
                        log.error("Network connection could not be "
                                 + "established with the second host");
                     }
                  } else {
                     log.error("Network connection could not be "
                              + "established with the first host");
                  }
               } else {
                  log.info("The config spec of the Distributed Virtual "
                           + "Switch is not created as per specifications");
               }
            } else {
               log.error("Cannot create the distributed "
                        + "virtual switch with the config spec passed");
            }
         }
     
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test is started.
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
     
         status &= super.testCleanUp();
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}