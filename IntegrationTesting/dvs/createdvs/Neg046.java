/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.createdvs;

import static com.vmware.vcqa.util.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.InvalidArgument;
import com.vmware.vc.MethodFault;
import com.vmware.vc.VMwareDVSConfigSpec;
import com.vmware.vc.VMwareDVSPortSetting;
import com.vmware.vc.VMwareUplinkPortOrderPolicy;
import com.vmware.vc.VmwareUplinkPortTeamingPolicy;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.dvs.DVSUtil;

import dvs.CreateDVSTestBase;

/**
 * Create a DVS inside a valid folder with the following parameters set in the
 * config spec. - DVSConfigSpec.configVersion is set to an empty string -
 * DVSConfigSpec.name is set to "CreateDVS-Neg046" - DVPortSetting.blocked is
 * set to false - uplinkPortOrder.activeUplinkPort is set to an invalid array
 * containing an invalid uplink port name
 */
public class Neg046 extends CreateDVSTestBase
{

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Create a DVS inside a valid folder with the"
               + " following parameters set in the config spec:\n"
               + "  - DVSConfigSpec.configVersion is set to an empty string\n"
               + "  - DVSConfigSpec.name is set to 'CreateDVS-Neg046'\n"
               + "  - DVPortSetting.blocked is set to false\n"
               + "  - uplinkPortOrder.activeUplinkPort is set to an invalid array "
               + "containing an invalid uplink port name.");
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
      log.info("Test setup Begin:");
     
         if (super.testSetUp()) {
            this.networkFolderMor = this.iFolder.getNetworkFolder(this.dcMor);
            if (this.networkFolderMor != null) {
               VMwareDVSPortSetting portSetting = new VMwareDVSPortSetting();
               VmwareUplinkPortTeamingPolicy uplinkTeamingPolicy = new VmwareUplinkPortTeamingPolicy();
               this.configSpec = new VMwareDVSConfigSpec();
               this.configSpec.setConfigVersion("");
               this.configSpec.setName(this.getTestId());
               portSetting.setBlocked(DVSUtil.getBoolPolicy(false, false));
               VMwareUplinkPortOrderPolicy portOrderPolicy = new VMwareUplinkPortOrderPolicy();
               portOrderPolicy.getActiveUplinkPort().clear();
               portOrderPolicy.getActiveUplinkPort().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new String[] {
                        "invalidUplink1", "@#$%^&*", "77777777","%^78778" }));
               portOrderPolicy.setInherited(false);
               uplinkTeamingPolicy.setUplinkPortOrder(portOrderPolicy);
               portSetting.setUplinkTeamingPolicy(uplinkTeamingPolicy);
               this.configSpec.setDefaultPortConfig(portSetting);
               status = true;
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
   @Test(description = "Create a DVS inside a valid folder with the"
               + " following parameters set in the config spec:\n"
               + "  - DVSConfigSpec.configVersion is set to an empty string\n"
               + "  - DVSConfigSpec.name is set to 'CreateDVS-Neg046'\n"
               + "  - DVPortSetting.blocked is set to false\n"
               + "  - uplinkPortOrder.activeUplinkPort is set to an invalid array "
               + "containing an invalid uplink port name.")
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
         InvalidArgument expectedMethodFault = new InvalidArgument();
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
     
         status &= super.testCleanUp();
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}