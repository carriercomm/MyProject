/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.merge;

import static com.vmware.vcqa.util.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.NumericRange;
import com.vmware.vc.VMwareDVSConfigInfo;
import com.vmware.vc.VMwareDVSPortSetting;
import com.vmware.vc.VmwareDistributedVirtualSwitchTrunkVlanSpec;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.ManagedEntity;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DistributedVirtualSwitchHelper;

/**
 * Create two dvswitches with the following configuration: configuration:
 * dest_defaultPortConfig Valid vlanIds (start = 10, end = 20), valid
 * src_defaultPortConfig Valid vlanIds (start = 15, end = 35 ). Merge the two
 * dvswitches.
 */
public class Pos015 extends TestBase
{
   private Folder iFolder = null;
   private ManagedEntity iManagedEntity = null;
   private ManagedObjectReference rootFolder = null;
   private ManagedObjectReference destDvsMor = null;
   private ManagedObjectReference srcDvsMor = null;
   private DVSConfigSpec configSpec = null;
   private DistributedVirtualSwitchHelper iDVSwitch = null;
   private ManagedObjectReference dcMor = null;
   private VMwareDVSPortSetting src = null;
   private VMwareDVSPortSetting dest = null;
   private VMwareDVSConfigInfo destDvsConfigInfo = null;
   private NumericRange[] vlanRange = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      setTestDescription("Create two dvswitches with the following "
               + "configuration: dest_defaultPortConfig with Valid vlanIds (start"
               + " = 10, end = 20), valid src_defaultPortConfig Valid vlanIds "
               + "(start = 15, end = 35). Merge the two dvswitches.");
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
      String destDvsName = DVSTestConstants.DVS_CREATE_NAME_PREFIX
               + this.getTestId() + DVSTestConstants.DVS_DESTINATION_SUFFIX;
      String srcDvsName = DVSTestConstants.DVS_CREATE_NAME_PREFIX
               + this.getTestId() + DVSTestConstants.DVS_SOURCE_SUFFIX;
      log.info("Test setup Begin:");
     
         this.iFolder = new Folder(connectAnchor);
         this.iManagedEntity = new ManagedEntity(connectAnchor);
         this.iDVSwitch = new DistributedVirtualSwitchHelper(connectAnchor);
         this.dcMor = this.iFolder.getDataCenter();
         if (this.dcMor != null) {
            // create the destination dvs
            this.configSpec = new DVSConfigSpec();
            this.configSpec.setConfigVersion("");
            this.configSpec.setName(destDvsName);
            dest = new VMwareDVSPortSetting();
            VmwareDistributedVirtualSwitchTrunkVlanSpec vlanSpec = new VmwareDistributedVirtualSwitchTrunkVlanSpec();
            // set the destn vlan port range
            NumericRange vlanIdRange = new NumericRange();
            vlanIdRange.setStart(10);
            vlanIdRange.setEnd(20);
            vlanRange = new NumericRange[1];
            vlanRange[0] = vlanIdRange;
            vlanSpec.getVlanId().clear();
            vlanSpec.getVlanId().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(vlanRange));
            vlanSpec.setInherited(false);
            dest.setVlan(vlanSpec);
            this.configSpec.setDefaultPortConfig(dest);
            destDvsMor = this.iFolder.createDistributedVirtualSwitch(
                     this.iFolder.getNetworkFolder(dcMor), configSpec);
            // create the src dvs
            this.configSpec.setName(srcDvsName);
            this.src = new VMwareDVSPortSetting();

            // set the vlan id range for the src switch
            vlanIdRange.setStart(15);
            vlanIdRange.setEnd(35);
            this.src.setVlan(vlanSpec);
            this.configSpec.setDefaultPortConfig(src);
            srcDvsMor = this.iFolder.createDistributedVirtualSwitch(
                     this.iFolder.getNetworkFolder(dcMor), configSpec);
            // store the destn dvs config info for matching description
            // property
            if (srcDvsMor != null && destDvsMor != null) {
               log.info("Successfully created the source and destination"
                        + " distributed virtual switches");
               this.destDvsConfigInfo = iDVSwitch.getConfig(this.destDvsMor);
               if (this.destDvsConfigInfo != null
                        && this.destDvsConfigInfo.getDefaultPortConfig() != null
                        && this.destDvsConfigInfo.getDefaultPortConfig() instanceof VMwareDVSPortSetting) {
                  this.dest = (VMwareDVSPortSetting) this.destDvsConfigInfo.getDefaultPortConfig();
                  if (this.dest.getVlan() != null
                           && this.dest.getVlan() instanceof VmwareDistributedVirtualSwitchTrunkVlanSpec) {
                     vlanSpec = (VmwareDistributedVirtualSwitchTrunkVlanSpec) this.dest.getVlan();
                     this.vlanRange = com.vmware.vcqa.util.TestUtil.vectorToArray(vlanSpec.getVlanId(), com.vmware.vc.NumericRange.class);
                     status = true;
                  } else {
                     log.error("The vlan spec is not valid");
                  }
               } else {
                  log.error("the destination config info is not valid");
               }
            } else {
               log.error("Could not create the source or "
                        + "destination distributed virtual " + "switches");
            }
         } else {
            log.error("There is no datacenter in the setup");
         }
     
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that merges two distributed virtual switches
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Create two dvswitches with the following "
               + "configuration: dest_defaultPortConfig with Valid vlanIds (start"
               + " = 10, end = 20), valid src_defaultPortConfig Valid vlanIds "
               + "(start = 15, end = 35). Merge the two dvswitches.")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
     
         if (this.iDVSwitch.merge(destDvsMor, srcDvsMor)) {
            log.info("Successfully merged the switches");
            if (this.iManagedEntity.isExists(srcDvsMor)) {
               log.error("Source DVS still exists.");
            } else {
               if (this.iDVSwitch.isExists(this.destDvsMor)) {
                  VMwareDVSConfigInfo mergedDVSConfigInfo = (VMwareDVSConfigInfo) this.iDVSwitch.getConfig(destDvsMor);
                  if (mergedDVSConfigInfo != null) {
                     VMwareDVSPortSetting portSetting = (VMwareDVSPortSetting) mergedDVSConfigInfo.getDefaultPortConfig();
                     VmwareDistributedVirtualSwitchTrunkVlanSpec mergedSpec = (VmwareDistributedVirtualSwitchTrunkVlanSpec) portSetting.getVlan();
                     if (TestUtil.compareArray(com.vmware.vcqa.util.TestUtil.vectorToArray(mergedSpec.getVlanId(), com.vmware.vc.NumericRange.class),
                              this.vlanRange)) {
                        log.info("Merged config info matched");
                        status = true;
                     } else {
                        log.error("Merged config info not matched.");
                     }
                  } else {
                     log.error("Merged DVS config info is null.");
                  }
               } else {
                  log.error("Destn DVS does not exist.");
               }
            }
         } else {
            log.error("Failed to merge the switches");
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
      boolean status = false;
     
         // check if src dvs exists
         if (this.iManagedEntity.isExists(this.srcDvsMor)) {
            // check if able to destroy it
            status = this.iManagedEntity.destroy(this.srcDvsMor);
         } else {
            status = true; // src does not exist, so set status as true
         }
         // check if destn dvs exists
         if (this.iManagedEntity.isExists(this.destDvsMor)) {
            // destroy the destn
            status &= this.iManagedEntity.destroy(this.destDvsMor);
         } else {
            status &= true; // the clean up is still true if destn is not
            // present
         }
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}