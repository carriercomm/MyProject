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

import com.vmware.vc.DistributedVirtualSwitchHostMemberConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicBacking;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.MethodFault;
import com.vmware.vc.NotFound;
import com.vmware.vc.VMwareDVSConfigSpec;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.host.NetworkSystem;

import dvs.CreateDVSTestBase;

/**
 * Create a DVS inside a valid folder with the following parameters set in the
 * config spec. - ManagedObjectReference set to a valid folder object -
 * DVSConfigSpec.configVersion is set to an empty string - DVSConfigSpec.name is
 * set to "Create DVS-Neg014" -
 * DistributedVirtualSwitchHostMemberConfigSpec.operation set to 'remove' -
 * DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host Mor -
 * DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic proxy
 * selection
 */
public class Neg014 extends CreateDVSTestBase
{
   /*
    * private data variables
    */
   private HostSystem ihs = null;
   private Vector<ManagedObjectReference> allHosts = null;
   private ManagedObjectReference hostMor = null;
   private NetworkSystem iNetworkSystem = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Create a DVS inside a valid folder with the "
               + "following parameters set in the config spec:"
               + " - ManagedObjectReference set to a valid folder object,\n"
               + " - DVSConfigSpec.configVersion is set to an empty string,\n"
               + " - DVSConfigSpec.name is set to 'Create DVS-Neg014',\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.operation set to "
               + "'remove',\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host "
               + "Mor,\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic "
               + "proxy selection");
   }

   /**
    * Method to setup the environment for the test.
    * 
    * @param connectAnchor ConnectAnchor object
    * @return boolean true if successful, false otherwise.
    */
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean status = false;
      DistributedVirtualSwitchHostMemberConfigSpec dvsHostMemberConfigSpecInst = null;
      log.info("Test setup Begin:");
     
         if (super.testSetUp()) {
            this.ihs = new HostSystem(connectAnchor);
            this.iNetworkSystem = new NetworkSystem(connectAnchor);
            allHosts = this.ihs.getAllHost();
            if (allHosts != null) {
               this.hostMor = (ManagedObjectReference) allHosts.get(0);
            } else {
               log.error("Valid Host MOR not found");
            }
            this.networkFolderMor = this.iFolder.getNetworkFolder(this.dcMor);
            if (this.networkFolderMor != null) {
               DistributedVirtualSwitchHostMemberPnicSpec pnicSpec = null;
               DistributedVirtualSwitchHostMemberPnicBacking pnicBacking = null;
               this.configSpec = new VMwareDVSConfigSpec();
               this.configSpec.setConfigVersion("");
               this.configSpec.setName(this.getTestId());
               String[] physicalNics = iNetworkSystem.getPNicIds(hostMor);
               if (physicalNics != null) {
                  pnicSpec = new DistributedVirtualSwitchHostMemberPnicSpec();
                  pnicSpec.setPnicDevice(physicalNics[0]);
                  pnicSpec.setUplinkPortKey(null);
               }
               pnicBacking = new DistributedVirtualSwitchHostMemberPnicBacking();
               pnicBacking.getPnicSpec().clear();
               pnicBacking.getPnicSpec().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberPnicSpec[] { pnicSpec }));
               dvsHostMemberConfigSpecInst = new DistributedVirtualSwitchHostMemberConfigSpec();
               dvsHostMemberConfigSpecInst.setBacking(pnicBacking);
               dvsHostMemberConfigSpecInst.setOperation(TestConstants.CONFIG_SPEC_REMOVE);
               dvsHostMemberConfigSpecInst.setHost(this.hostMor);
               this.configSpec.getHost().clear();
               this.configSpec.getHost().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberConfigSpec[] { dvsHostMemberConfigSpecInst }));
               status = true;
            } else {
               log.error("Failed to create network folder.");
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
   @Test(description = "Create a DVS inside a valid folder with the "
               + "following parameters set in the config spec:"
               + " - ManagedObjectReference set to a valid folder object,\n"
               + " - DVSConfigSpec.configVersion is set to an empty string,\n"
               + " - DVSConfigSpec.name is set to 'Create DVS-Neg014',\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.operation set to "
               + "'remove',\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.host set to a valid host "
               + "Mor,\n"
               + " - DistributedVirtualSwitchHostMemberConfigSpec.proxy set a a valid pnic "
               + "proxy selection")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
      try {
         this.dvsMOR = this.iFolder.createDistributedVirtualSwitch(
                  this.networkFolderMor, this.configSpec);
         log.error("The API did not throw Exception");
      } catch (Exception actualMethodFaultExcep) {
         MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil.getFault(actualMethodFaultExcep);
         NotFound expectedMethodFault = new NotFound();
         status = TestUtil.checkMethodFault(actualMethodFault,
                  expectedMethodFault);
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
     
         if (this.dvsMOR == null && this.networkFolderMor != null) {
            this.dvsMOR = this.iFolder.getDistributedVirtualSwitch(
                     this.networkFolderMor, this.getTestId());
         }
         status &= super.testCleanUp();
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}