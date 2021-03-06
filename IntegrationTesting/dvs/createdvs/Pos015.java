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

import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DVSContactInfo;
import com.vmware.vcqa.util.TestUtil;

import dvs.CreateDVSTestBase;

/**
 * Create a DVS inside a valid folder with the following parameters set in the
 * config spec. DVSConfigSpec.configVersion is set to an empty string.
 * DVSConfigSpec.name is set to "CreateDVS-Pos015"
 * DVSConfigSpec.DVSContactInfo.name is set to 'Contact CreateDVS-Pos015'
 * DVSConfigSpec.DVSContactInfo.contact is set to
 * "Contact CreateDVS-Pos015 Description"
 */
public class Pos015 extends CreateDVSTestBase
{
   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Create a DVSwitch inside a valid folder with"
               + " the following parameters set in the config " + "spec:\n"
               + "1.DVSConfigSpec.configVersion is set to an "
               + "empty string,\n" + "2.DVSConfigSpec.name is set to "
               + "'CreateDVS-Pos015',\n"
               + "3.DVSConfigSpec.DVSContactInfo.name is set to"
               + "'Contact CreateDVS-Pos015',\n"
               + "4.DVSConfigSpec.DVSContactInfo.contact is set"
               + " to 'Contact CreateDVS-Pos015 Description'");
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
      String dvsName = this.getTestId();
      String dvsContactName = "Contact " + dvsName;
      String dvsContactDescription = "Contact " + dvsName + " Description";
      log.info("Test setup Begin:");
     
         if (super.testSetUp()) {
            this.networkFolderMor = this.iFolder.getNetworkFolder(this.dcMor);
            if (this.networkFolderMor != null) {
               this.configSpec = new DVSConfigSpec();
               DVSContactInfo contactInfo = new DVSContactInfo();
               this.configSpec.setConfigVersion("");
               this.configSpec.setName(dvsName);
               contactInfo.setName(dvsContactName);
               contactInfo.setContact(dvsContactDescription);
               this.configSpec.setContact(contactInfo);
               status = true;
            } else {
               log.error("Failed to create the network folder");
            }
         } else {
            log.error("Test setup failed.");
         }
     
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that creates the DVS.
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Create a DVSwitch inside a valid folder with"
               + " the following parameters set in the config " + "spec:\n"
               + "1.DVSConfigSpec.configVersion is set to an "
               + "empty string,\n" + "2.DVSConfigSpec.name is set to "
               + "'CreateDVS-Pos015',\n"
               + "3.DVSConfigSpec.DVSContactInfo.name is set to"
               + "'Contact CreateDVS-Pos015',\n"
               + "4.DVSConfigSpec.DVSContactInfo.contact is set"
               + " to 'Contact CreateDVS-Pos015 Description'")
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
               if (iDistributedVirtualSwitch.validateDVSConfigSpec(this.dvsMOR,
                        this.configSpec, null)) {
                  status = true;
               } else {
                  log.info("The config spec of the Distributed Virtual Switch"
                           + "is not created as per specifications");
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