/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfiguredvs;

import static com.vmware.vcqa.util.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DVSNameArrayUplinkPortPolicy;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;

import dvs.CreateDVSTestBase;

/**
 * Reconfigure an existing DVS as follows: - Set the DVSConfigSpec.configVersion
 * to a valid config version string - Set the DVSConfigSpec.name to a valid
 * string - Set DVSConfigSpec.maxPort to 0 - Set DVSConfigSpec.numPort to 0 -
 * Set DVSConfigSpec.uplinkPortPolicy to an array of 32 uplink elements
 */
public class Pos009 extends CreateDVSTestBase
{

   /*
    * private data variables
    */
   private DistributedVirtualSwitch iDistributedVirtualSwitch = null;
   private DVSConfigSpec deltaConfigSpec = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Create a DVSwitch inside a valid folder");
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
               this.iDistributedVirtualSwitch = new DistributedVirtualSwitch(
                        connectAnchor);
               this.configSpec = new DVSConfigSpec();
               this.configSpec.setName(this.getClass().getName());
               this.dvsMOR = this.iFolder.createDistributedVirtualSwitch(
                        this.networkFolderMor, this.configSpec);
               if (this.dvsMOR != null) {
                  log.info("Successfully created the DVSwitch");
                  this.deltaConfigSpec = new DVSConfigSpec();
                  String dvsName = DVSTestConstants.DVS_CREATE_NAME_PREFIX
                           + this.getTestId();
                  DVSNameArrayUplinkPortPolicy uplinkPolicyInst = new DVSNameArrayUplinkPortPolicy();
                  String[] uplinkPortNames = new String[32];
                  for (int i = 0; i < 32; i++)
                     uplinkPortNames[i] = "Uplink" + i;
                  String validConfigVersion = this.iDistributedVirtualSwitch.getConfig(
                           dvsMOR).getConfigVersion();
                  uplinkPolicyInst.getUplinkPortName().clear();
                  uplinkPolicyInst.getUplinkPortName().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(uplinkPortNames));
                  this.deltaConfigSpec.setConfigVersion(validConfigVersion);
                  this.deltaConfigSpec.setName(dvsName);
                  this.deltaConfigSpec.setMaxPorts(0);
                  this.deltaConfigSpec.setNumStandalonePorts(0);
                  this.deltaConfigSpec.setUplinkPortPolicy(uplinkPolicyInst);
                  status = true;
               } else {
                  log.error("Cannot create the distributed virtual "
                           + "switch with the config spec passed");
               }
            } else {
               log.error("Failed to create the network folder");
            }
         }
     
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that creates the DVS.
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Create a DVSwitch inside a valid folder")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
     
         status = this.iDistributedVirtualSwitch.reconfigure(this.dvsMOR,
                  this.deltaConfigSpec);
         assertTrue(status, "Test Failed");
     
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