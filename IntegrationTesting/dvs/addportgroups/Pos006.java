/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.addportgroups;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVPortgroupConfigSpec;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.ManagedEntity;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;

/**
 * Add a portgroup to an existing distributed virtual switch with the following
 * parameters set: DVPortgroupConfigSpec.ConfigVersion is set to an empty string
 * DVPortgroupConfigSpec.Name is set to a valid string
 * DVPortgroupConfigSpec.Description is set to a valid string
 * DVPortgroupConfigSpec.NumPorts is less than maximum number of ports in the
 * existing dvswitch DVPortgroupConfigSpec.Type is set to "early binding"
 */
public class Pos006 extends TestBase
{
   private Folder iFolder = null;
   private ManagedEntity iManagedEntity = null;
   private ManagedObjectReference rootFolderMor = null;
   private ManagedObjectReference dvsMor = null;
   private DVSConfigSpec dvsConfigSpec = null;
   private DistributedVirtualSwitch iDVSwitch = null;
   private ManagedObjectReference dvPortgroupMor = null;
   private DVPortgroupConfigSpec dvPortgroupConfigSpec = null;
   private List<ManagedObjectReference> dvPortgroupMorList = null;
   private DVPortgroupConfigSpec[] dvPortgroupConfigSpecArray = null;
   private ManagedObjectReference dcMor = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      setTestDescription("Add a portgroup to an existing"
               + "distributed virtual switch with a valid "
               + "number of ports which is less than the "
               + "maximum number of ports in the dvswitch");
   }

   /**
    * Method to setup the environment for the test. This method creates a
    * distributed virtual switch.
    * 
    * @param connectAnchor ConnectAnchor object
    * @return boolean true if successful, false otherwise.
    */
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean status = false;
      int len = 0;
      log.info("Test setup Begin:");
     
         this.iFolder = new Folder(connectAnchor);
         this.iDVSwitch = new DistributedVirtualSwitch(connectAnchor);
         this.iManagedEntity = new ManagedEntity(connectAnchor);
         this.rootFolderMor = this.iFolder.getRootFolder();
         if (this.rootFolderMor != null) {
            this.dvsConfigSpec = new DVSConfigSpec();
            this.dvsConfigSpec.setConfigVersion("");
            this.dvsConfigSpec.setName(this.getClass().getName());
            this.dvsConfigSpec.setMaxPorts(new Integer(2));
            this.dcMor = this.iFolder.getDataCenter();
            dvsMor = this.iFolder.createDistributedVirtualSwitch(
                     this.iFolder.getNetworkFolder(dcMor), dvsConfigSpec);
            if (dvsMor != null) {
               log.info("Successfully created the distributed "
                        + "virtual switch");
               this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
               this.dvPortgroupConfigSpec.setConfigVersion("");
               this.dvPortgroupConfigSpec.setName(this.getTestId());
               this.dvPortgroupConfigSpec.setDescription(DVSTestConstants.DVPORTGROUP_VALID_DESCRIPTION);
               this.dvPortgroupConfigSpec.setNumPorts(1);
               this.dvPortgroupConfigSpec.setType(DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING);
               status = true;
            } else {
               log.error("Failed to create the distributed "
                        + "virtual switch");
            }
         } else {
            log.error("Failed to find a folder");
         }
     
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that adds a portgroup to the distributed virtual switch with
    * configVersion set to an empty string, name set to a valid string,
    * description set to a valid string, numPorts less than the maximum number
    * of ports in the dvswitch
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Add a portgroup to an existing"
               + "distributed virtual switch with a valid "
               + "number of ports which is less than the "
               + "maximum number of ports in the dvswitch")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
     
         if (dvPortgroupConfigSpec != null) {
            dvPortgroupConfigSpecArray = new DVPortgroupConfigSpec[] { dvPortgroupConfigSpec };
            dvPortgroupMorList = iDVSwitch.addPortGroups(dvsMor,
                     dvPortgroupConfigSpecArray);
            if (dvPortgroupMorList != null) {
               if (dvPortgroupMorList.size() == dvPortgroupConfigSpecArray.length) {
                  log.info("Successfully added all the portgroups");
                  status = true;
               } else {
                  log.error("Could not add all the portgroups");
               }
            } else {
               log.error("No portgroups were added");
            }
         }
     
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test is started. Destroy
    * the portgroup, followed by the distributed virtual switch
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
     
         /*if(this.dvPortgroupMorList != null){
            for(ManagedObjectReference mor: dvPortgroupMorList){
               status &= this.iManagedEntity.destroy(mor);
            }  
         }*/
         if (this.dvsMor != null) {
            status &= this.iManagedEntity.destroy(dvsMor);
         }
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}
