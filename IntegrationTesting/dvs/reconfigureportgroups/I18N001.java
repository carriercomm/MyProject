/*
 * ************************************************************************
 *
 * Copyright 2009 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfigureportgroups;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVPortgroupConfigSpec;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.i18n.I18NDataProvider;
import com.vmware.vcqa.i18n.I18NDataProviderConstants;
import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualPortgroup;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.ManagedEntity;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;

/**
 * Reconfigure an existing distributed virtual switch with the following
 * parameters set: DVPortgroupConfigSpec.ConfigVersion is set to an empty string
 * DVPortgroupConfigSpec.Name is set to a valid I18N string
 */
public class I18N001 extends TestBase
{
   /*
    * private data variables
    */
   private Folder iFolder = null;
   // i18n: iDataProvider object
   private I18NDataProvider iDataProvider = null;
   private DistributedVirtualPortgroup iDVPortgroup = null;
   private ManagedEntity iManagedEntity = null;
   private ManagedObjectReference nwFolder = null;
   private ManagedObjectReference dvsMor = null;
   private ManagedObjectReference dvPGmor = null;
   private DVSConfigSpec dvsConfigSpec = null;
   private DistributedVirtualSwitch iDVSwitch = null;
   private DVPortgroupConfigSpec dvPortgroupConfigSpec = null;
   private List<ManagedObjectReference> dvPortgroupMorList = null;
   private DVPortgroupConfigSpec[] dvPortgroupConfigSpecArray = null;
   private ManagedObjectReference dcMor = null;
   private ArrayList<String> dvsNameArr = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      setTestDescription("Reconfigure a portgroup in an existing "
               + "distributed virtual switch with a valid"
               + " I18N string as name");
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
      log.info("Test setup Begin:");
      try {
         // i18n: get DataProvider object from factory implementation.
         iDataProvider = new I18NDataProvider();
         this.iFolder = new Folder(connectAnchor);
         this.iDVSwitch = new DistributedVirtualSwitch(connectAnchor);
         this.iDVPortgroup = new DistributedVirtualPortgroup(connectAnchor);
         this.iManagedEntity = new ManagedEntity(connectAnchor);
         // i18n: call into getData with the propertyKey and length of
         // substrings
         dvsNameArr = iDataProvider.getData(
                  I18NDataProviderConstants.MULTI_LANG_KEY,
                  I18NDataProviderConstants.MAX_STRING_LENGTH);

         this.dcMor = this.iFolder.getDataCenter();
         Assert.assertNotNull(this.dcMor,
                  "Successfully found  the DataCenter ",
                  "Failed to find the DataCenter");
         this.nwFolder = this.iFolder.getNetworkFolder(this.dcMor);
         for (String dvsName : dvsNameArr) {
            log.info("dvsName to be updated = " + dvsName);
            dvsMor = this.createDvs(dvsName);
            Assert.assertNotNull(dvsMor,
                     "Successfully created the distributed "
                              + "virtual switch :" + dvsName,
                     "Failed to create the distributed " + "virtual switch:"
                              + dvsName);
            assertTrue(
                     (iDVSwitch.getConfig(dvsMor).getName().equals(dvsName)),
                     " Verified the name", " Unable to verify the name");
         }
         status = true;

      } catch (Exception e) {
         TestUtil.handleException(e);
      }
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that adds and Reconfigures portgroup to the distributed virtual
    * switch with configVersion set to an empty string, name set I18N string
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Reconfigure a portgroup in an existing "
               + "distributed virtual switch with a valid"
               + " I18N string as name")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
      try {

         for (String dvsName : dvsNameArr) {
            dvsMor = this.iFolder.getDistributedVirtualSwitch(
                     this.iFolder.getNetworkFolder(dcMor), dvsName);
            String dvPortName = new StringBuffer(dvsName).reverse().toString();
            dvPortgroupMorList = this.addPortGroups(dvsMor, dvPortName);
            assertTrue(
                     (dvPortgroupMorList != null && dvPortgroupMorList.size() > 0),
                     "Successfully added all the portgroups",
                     "Could not add all the portgroups");

            this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
            this.dvPortgroupConfigSpec.setConfigVersion(this.iDVPortgroup.getConfigInfo(
                     dvPortgroupMorList.get(0)).getConfigVersion());
            this.dvPortgroupConfigSpec.setName(dvsName);
            this.dvPortgroupConfigSpec.setType(DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING);
            assertTrue((this.iDVPortgroup.reconfigure(
                     dvPortgroupMorList.get(0), dvPortgroupConfigSpec)),
                     "Successfully reconfigured the portgroup",
                     "There are no portgroups to be reconfigured");
            dvPGmor = this.iDVPortgroup.getDVPortgroupByName(this.dvsMor,
                     dvsName);
            Assert.assertNotNull(dvPGmor, "Successfully found the portgroup ",
                     "Unable to find the portgroup");
         }
         status = true;
      } catch (Exception e) {
         status = false;
         TestUtil.handleException(e);
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
      try {
         if (dvsNameArr != null && dvsNameArr.size() > 0) {
            for (String dvsName : dvsNameArr) {
               this.dvsMor = this.iFolder.getDistributedVirtualSwitch(
                        this.nwFolder, dvsName);
               dvPGmor = this.iDVPortgroup.getDVPortgroupByName(this.dvsMor,
                        dvsName);
               assertTrue(this.iManagedEntity.destroy(dvPGmor),
                        "Successfully destroyed the  port group ",
                        "Unable to destroy the portgroup");
               assertTrue(this.iManagedEntity.destroy(dvsMor),
                        "Successfully deleted DVS", "Unable to delete DVS");
            }
         }

      } catch (Exception e) {
         TestUtil.handleException(e);
         status = false;
      }
      assertTrue(status, "Cleanup failed");
      return status;
   }

   /**
    * This method creates DVS
    */
   private ManagedObjectReference createDvs(String name)
      throws Exception
   {
      ManagedObjectReference dvsMor = null;
      this.dvsConfigSpec = new DVSConfigSpec();
      this.dvsConfigSpec.setConfigVersion("");
      this.dvsConfigSpec.setName(name);
      dvsMor = this.iFolder.createDistributedVirtualSwitch(this.nwFolder,
               dvsConfigSpec);
      return dvsMor;

   }

   private List<ManagedObjectReference> addPortGroups(ManagedObjectReference dvsMor,
                                                      String name)
      throws Exception
   {
      List<ManagedObjectReference> dvPortgroupMorList = null;

      this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
      this.dvPortgroupConfigSpec.setConfigVersion("");
      this.dvPortgroupConfigSpec.setName(name);
      this.dvPortgroupConfigSpec.setType(DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING);
      if (dvPortgroupConfigSpec != null) {
         dvPortgroupConfigSpecArray = new DVPortgroupConfigSpec[] { dvPortgroupConfigSpec };
         dvPortgroupMorList = iDVSwitch.addPortGroups(dvsMor,
                  dvPortgroupConfigSpecArray);
         if (dvPortgroupMorList != null) {
            if (dvPortgroupMorList.size() == dvPortgroupConfigSpecArray.length) {
               log.info("Successfully added all the portgroups");

            } else {
               log.error("Could not add all the portgroups");
            }
         } else {
            log.error("No portgroups were added");
         }
      }
      return dvPortgroupMorList;
   }
}
