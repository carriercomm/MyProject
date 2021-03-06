/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.vmops.reconfigurevm;

import static com.vmware.vc.VirtualMachinePowerState.POWERED_OFF;
import static com.vmware.vcqa.TestConstants.VM_VIRTUALDEVICE_ETHERNET_PCNET32;
import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.MethodFault;
import com.vmware.vc.NotFound;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.dvs.DVSUtil;

import dvs.vmops.VMopsBase;

/**
 * Reconfigure a VM on a standalone host to connect to an existing standalone
 * DVPort. Build DVPortConnection with invalid DVSwitchUuid. The device is of
 * type VirtualPCNet32,the backing is of type DVPort backing and the port
 * connection is a DVPort connection.
 */
public class Neg002 extends VMopsBase
{
   /*
    * Private data variables
    */
   private VirtualMachineConfigSpec vmConfigSpec = null;
   private DistributedVirtualSwitchPortConnection dvsPortConnection = null;
   private String portKey = null;
   private ManagedObjectReference vmMor = null;
   private String dvSwitchUuid = null;

   /**
    * Set description of this test.
    */
   @Override
   public void setTestDescription()
   {
      setTestDescription("Reconfigure a VM on a Standalone host to connect to"
               + " an existing standalone DVPort.The device is of type "
               + " VirtualPCNet32.Build DVPortConnection with invalid DVSUuid.");
   }

   /**
    * Method to setup the environment for the test. 1. create the DVSwitch. 2.
    * Create the Standalone DVPort. 3. Create the DVPortConnection with invalid
    * DVSwitchUuid. 4. Create the Default VMConfigSpec.
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
      log.info("test setup Begin:");
      if (super.testSetUp()) {

            hostMor = ihs.getStandaloneHost();
            log.info("Host MOR: " + hostMor);
            log.info("Host Name: " + ihs.getHostName(hostMor));
            // create the DVS by using standalone host.
            dvsMor = iFolder.createDistributedVirtualSwitch(dvsName, hostMor);
            Thread.sleep(10000);// Sleep for 10 Sec
            nwSystemMor = ins.getNetworkSystem(hostMor);
            if (ins.refresh(nwSystemMor)) {
               log.info("refreshed");
            }
            // add the pnics to DVS
            hostNetworkConfig = iDVSwitch.getHostNetworkConfigMigrateToDVS(
                     dvsMor, hostMor);
            if (hostNetworkConfig[0] != null && hostNetworkConfig[1] != null) {
               log.info("Found the network config.");
               // update the network to use the DVS.
               networkUpdated = ins.updateNetworkConfig(nwSystemMor,
                        hostNetworkConfig[0], TestConstants.CHANGEMODE_MODIFY);
               if (networkUpdated) {
                  portKeys = iDVSwitch.addStandaloneDVPorts(dvsMor, 1);
                  if (portKeys != null) {
                     log.info("Successfully get the standalone DVPortkeys");
                     portKey = portKeys.get(0);
                     DVSConfigInfo info = iDVSwitch.getConfig(dvsMor);
                     dvSwitchUuid = info.getUuid();
                     // create the DistributedVirtualSwitchPortConnection
                     // object.
                     dvsPortConnection = buildDistributedVirtualSwitchPortConnection(
                              "xyz", portKey, null);
                     // create the VM.
                     vmConfigSpec = buildDefaultSpec(hostMor,
                              VM_VIRTUALDEVICE_ETHERNET_PCNET32);
                     log.info("Successfully created default VMConfig spec.");
                     vmMor = new Folder(super.getConnectAnchor()).createVM(
                              ivm.getVMFolder(), vmConfigSpec,
                              ihs.getPoolMor(hostMor), hostMor);
                     if (vmMor != null) {
                        log.info("Successfully creted VM.");
                        status = true;
                     } else {
                        log.error("Failed to crete VM.");
                     }
                  } else {
                     log.error("Failed the get the Standalone DVPortKeys.");
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
    * Test. 1. Create the DeltaConfigSpec. 2. Reconfigure the VirtualMachine
    * Configuration.
    *
    * @param connectAnchor ConnectAnchor.
    */
   @Test(description = "Reconfigure a VM on a Standalone host to connect to"
               + " an existing standalone DVPort.The device is of type "
               + " VirtualPCNet32.Build DVPortConnection with invalid DVSUuid.")
   public void test()
      throws Exception
   {
      try {
         // Reconfigure a VM with DVSwitchUuid as null.
         VirtualMachineConfigSpec deltaConfigSpec = null;
         deltaConfigSpec = DVSUtil.getVMConfigSpecForDVSPort(
                  vmMor,
                  connectAnchor,
                  new DistributedVirtualSwitchPortConnection[] { dvsPortConnection })[0];
         if (ivm.reconfigVM(vmMor, deltaConfigSpec)) {
            log.error("Successfully reconfigure the VM."
                     + "API didn't throw any exception.");
         }
         com.vmware.vcqa.util.Assert.assertTrue(false, "No Exception Thrown!");
      } catch (Exception excep) {
         com.vmware.vc.MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil.getFault(excep);
         com.vmware.vc.MethodFault expectedMethodFault = new NotFound();
         com.vmware.vcqa.util.Assert.assertTrue(
                  com.vmware.vcqa.util.TestUtil.checkMethodFault(
                           actualMethodFault, expectedMethodFault),
                  "MethodFault mismatch!");
      }
   }

   /**
    * Method to restore the state as it was before the test is started.
    *
    * @param connectAnchor ConnectAnchor object
    * @return <code>true</code> if successful.
    */
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
      try {
         if (vmMor != null && ivm.setVMState(vmMor, POWERED_OFF, false)) {
            status = destroy(vmMor);// destroy the VM.
         } else {
            log.warn("VM not found");
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
