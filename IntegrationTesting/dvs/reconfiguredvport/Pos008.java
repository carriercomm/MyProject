/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfiguredvport;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.ClusterConfigSpecEx;
import com.vmware.vc.ConfigSpecOperation;
import com.vmware.vc.DVPortConfigSpec;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;

import dvs.CreateDVSTestBase;

/**
 * Reconfigure DVPort. See setTestDescription for detailed description
 */
public class Pos008 extends CreateDVSTestBase
{

   /*
    * private data variables
    */
   private DistributedVirtualSwitch iDVS = null;
   private DVPortConfigSpec[] portConfigSpecs = null;
   private ManagedObjectReference clusterExMor = null;
   private String clusterName = null;
   private final int DVS_PORT_NUM = 1;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Reconfigure DVPort and set a valid "
               + "cluster ComputerResource Mor in scope property");
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
               this.iDVS = new DistributedVirtualSwitch(connectAnchor);
               configSpec = new DVSConfigSpec();
               configSpec.setName(this.getClass().getName());
               configSpec.setNumStandalonePorts(DVS_PORT_NUM);
               dvsMOR = this.iFolder.createDistributedVirtualSwitch(
                        this.networkFolderMor, this.configSpec);
               if (this.dvsMOR != null) {
                  log.info("Successfully created the DVSwitch");
                  List<String> portKeyList = iDVS.fetchPortKeys(dvsMOR, null);
                  if (portKeyList != null && portKeyList.size() == DVS_PORT_NUM) {
                     ManagedObjectReference folderMor = (ManagedObjectReference) iFolder.getAllHostFolders().get(
                              0);
                     clusterName = getTestId();
                     clusterExMor = iFolder.createClusterEx(folderMor,
                              clusterName, new ClusterConfigSpecEx());
                     portConfigSpecs = new DVPortConfigSpec[DVS_PORT_NUM];
                     portConfigSpecs[0] = new DVPortConfigSpec();
                     portConfigSpecs[0].setKey(portKeyList.get(0));
                     portConfigSpecs[0].getScope().clear();
                     portConfigSpecs[0].getScope().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new ManagedObjectReference[] { clusterExMor }));
                     portConfigSpecs[0].setOperation(ConfigSpecOperation.EDIT.value());
                     status = true;
                  } else {
                     log.error("Can't get correct port keys");
                  }
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
   @Test(description = "Reconfigure DVPort and set a valid "
               + "cluster ComputerResource Mor in scope property")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
     
         status = this.iDVS.reconfigurePort(this.dvsMOR, this.portConfigSpecs);
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
         if (this.clusterExMor != null) {
            if (iFolder.destroy(clusterExMor)) {
               log.info("cluster " + this.clusterName + " deleted");
            } else {
               log.error("Can't delete cluster " + this.clusterName);
               status = false;
            }
         }
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}