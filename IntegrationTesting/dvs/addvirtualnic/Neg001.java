/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.addvirtualnic;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.HostVirtualNicSpec;
import com.vmware.vc.MethodFault;
import com.vmware.vc.NotFound;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;

import dvs.VNicBase;

/**
 * Add a vnic to connect to an existing standalone port on an existing DVSwitch.
 * The distributedVirtualPort is of type DistributedVirtualSwitchPortConnection.
 * Build DVPortConnection with invalid standalone DVPortKey.
 */
public class Neg001 extends VNicBase
{
   private DistributedVirtualSwitchPortConnection dvsPortConnection = null;
   private HostVirtualNicSpec hostVNicSpec = null;
   private String vNicId = null;

   /**
    * Set test description.
    */
   @Override
   public void setTestDescription()
   {
      setTestDescription("Add a vnic to connect to an "
               + "existing standalone port on an existing DVSwitch. "
               + "The distributedVirtualPort is of type DistributedVirtualSwitchPortConnection."
               + "Build DVPortConnection with invalid standalone DVPortKey.");
   }

   /**
    * Method to setup the environment for the test. 1. Create the DVSwitch. 2.
    * Get the DVPortKey. 3. Build the DVPortConnection. 4. Create
    * HostVirtualNicSpec Object and set the values.
    * 
    * @param connectAnchor ConnectAnchor object
    * @return <code>true</code> if setup is successful.
    */
   @Override
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean status = false;
      List<String> portKeys = null;
      String dvSwitchUuid = null;
      log.info("test setup Begin:");
      if (super.testSetUp()) {
        
            hostMor = ihs.getStandaloneHost();
            log.info("Host MOR: " + hostMor);
            log.info("Host Name: " + ihs.getHostName(hostMor));
            // create the DVS by using standalone host.
            dvsMor = iFolder.createDistributedVirtualSwitch(dvsName, hostMor);
            log.info("dvsMor :" + dvsMor);
            Thread.sleep(10000);// Sleep for 10 Sec
            nwSystemMor = ins.getNetworkSystem(hostMor);
            if (ins.refresh(nwSystemMor)) {
               log.info("refreshed");
            }
            // add the pnics to DVS
            hostNetworkConfig = iDVSwitch.getHostNetworkConfigMigrateToDVS(
                     dvsMor, hostMor);
            if ((hostNetworkConfig != null) && (hostNetworkConfig.length == 2)
                     && (hostNetworkConfig[0] != null)
                     && (hostNetworkConfig[1] != null)) {
               log.info("Found the network config.");
               // update the network to use the DVS.
               networkUpdated = ins.updateNetworkConfig(nwSystemMor,
                        hostNetworkConfig[0], TestConstants.CHANGEMODE_MODIFY);
               if (networkUpdated) {
                  // Get the DVPortKey.
                  portKeys = iDVSwitch.addStandaloneDVPorts(dvsMor, 1);
                  if (portKeys != null) {
                     DVSConfigInfo info = iDVSwitch.getConfig(dvsMor);
                     dvSwitchUuid = info.getUuid();
                     // create the DistributedVirtualSwitchPortConnection
                     // object.
                     dvsPortConnection = buildDistributedVirtualSwitchPortConnection(
                              dvSwitchUuid, "xyz", null);
                     hostVNicSpec = ins.createVNicSpecification();
                     hostVNicSpec.setDistributedVirtualPort(dvsPortConnection);
                     status = true;
                  } else {
                     log.error("Failed to get the standalone DVPortkeys ");
                  }
               } else {
                  log.error("Failed to find network config.");
               }
            }
        
      }
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Test. 1. Add the VirtualNic.
    * 
    * @param connectAnchor ConnectAnchor.
    */
   @Override
   @Test(description = "Add a vnic to connect to an "
               + "existing standalone port on an existing DVSwitch. "
               + "The distributedVirtualPort is of type DistributedVirtualSwitchPortConnection."
               + "Build DVPortConnection with invalid standalone DVPortKey.")
   public void test()
      throws Exception
   {
      boolean status = false;
      MethodFault expectedFault = new NotFound();
      try {
         // Add a VirtualNic to the Network System.
         vNicId = ins.addVirtualNic(nwSystemMor, "", hostVNicSpec);
         if (vNicId != null) {
            log.error("Successfully add the Virtual NIC."
                     + "API didn't throw any exception.");
         }
      } catch (Exception actualMethodFaultExcep) {
         MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil.getFault(actualMethodFaultExcep);
         status = TestUtil.checkMethodFault(
                  actualMethodFault.getFaultCause().getFault(), expectedFault);
      }
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test is started.
    * 
    * @param connectAnchor ConnectAnchor object
    * @return <code>true</code> if successful.
    */
   @Override
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
      try {
         if ((vNicId != null)
                  && ins.removeServiceConsoleVirtualNic(nwSystemMor, vNicId)) {
            log.error("Successfully removed the service console Virtual NIC. "
                     + vNicId);
         } else {
            if (vNicId == null) {
               log.info("unable to find the vNicId.");
            } else {
               log.error("Unable to remove the Virtual NIC " + vNicId);
               status = false;
            }
         }
      } catch (Exception e) {
         TestUtil.handleException(e);
      }
      try {
         if (networkUpdated) {
            // restore the network to use the DVS.
            status &= ins.updateNetworkConfig(nwSystemMor,
                     hostNetworkConfig[1], TestConstants.CHANGEMODE_MODIFY);
         }
      } catch (Exception e) {
         TestUtil.handleException(e);
      } finally {
         status &= super.testCleanUp();
      }
      assertTrue(status, "Cleanup failed");
      return status;
   }
}
