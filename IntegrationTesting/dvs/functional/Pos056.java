/*
 * ************************************************************************
 *
 * Copyright 2010 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.functional;

import static com.vmware.vc.HostSystemConnectionState.CONNECTED;
import static com.vmware.vcqa.util.Assert.assertNotNull;
import static com.vmware.vcqa.util.Assert.assertTrue;
import static com.vmware.vcqa.vim.MessageConstants.HOST_GET_FAIL;
import static com.vmware.vcqa.vim.MessageConstants.VM_DEL_FAIL;
import static com.vmware.vcqa.vim.MessageConstants.VM_DEL_PASS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.util.VersionConstants;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.VirtualMachine;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.dvs.DistributedVirtualSwitchHelper;
import com.vmware.vcqa.vim.host.HostSystemInformation;

/**
 * DESCRIPTION:<br>
 * (Increasing the Proxy Port limit number) <br>
 * TARGET: VC <br>
 * NOTE : PR#511841 <br>
 * <br>
 * SETUP:<br>
 * 1. Set MAX PORTS to 258 while creating vDs <BR>
 * 2. Create 25 vms with 10 nic cards and one vm with 7 nic cards <br>
 * TEST:<br>
 * 3. Add 25 static DVPGs with 10 ports and one static DVPG with 7 ports<BR>
 * 4. reconfigure Vms to connect to static DVPGs <br>
 * CLEANUP:<br>
 * 5. Delete VMs<br>
 * 6. Destroy vDs<br>
 */
public class Pos056 extends TestBase
{
   private ManagedObjectReference dvsMor = null;
   private String dvSwitchUuid = null;
   private String early = DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING;
   private ManagedObjectReference hostMor = null;
   private DistributedVirtualSwitch iDVS = null;
   private HostSystem ihs = null;
   private VirtualMachine ivm = null;
   private Vector<ManagedObjectReference> vmMors = null;
   private final int MAX_PORTS = 258;
   private final int VM_COUNT = 26;
   private String hostName;

   @Override
   public void setTestDescription()
   {
      super.setTestDescription(" Test case to address PR#511841 "
               + "(Increasing the Proxy Port limit number) \n"
               + " 1. Set MAX PORTS to 258 while creating vDs   . \n"
               + " 2. Create 25 vms  with 10 nic cards and one vm with 7 nic cards "
               + " 3. Add 25 static DVPGs  with ten ports and one static DVPG with 7 ports\n"
               + " 4. Reconfigure Vms to connect to static DVPG \n"
               + " Reconfigure should succeed on all VMS. \n");
   }

   @Override
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {

      HashMap<ManagedObjectReference, HostSystemInformation> hostsMap = null;
      List<ManagedObjectReference> hostList = new ArrayList<ManagedObjectReference>(
               1);
      this.ivm = new VirtualMachine(connectAnchor);
      this.iDVS = new DistributedVirtualSwitchHelper(connectAnchor);
      this.ihs = new HostSystem(connectAnchor);
      hostsMap = this.ihs.getAllHosts(VersionConstants.ESX4x, CONNECTED);
      assertNotNull(hostsMap, "The host map is null");
      this.hostMor = hostsMap.keySet().iterator().next();
      assertNotNull(this.hostMor, HOST_GET_FAIL);
      hostName = this.ihs.getHostName(hostMor);
      hostList.add(hostMor);
      dvsMor = DVSUtil.createDVSWithMAXPorts(connectAnchor, hostList,
               MAX_PORTS, null);
      DVSConfigInfo configInfo = this.iDVS.getConfig(this.dvsMor);
      this.dvSwitchUuid = configInfo.getUuid();
      this.vmMors = DVSUtil.createVms(connectAnchor, hostMor, VM_COUNT - 1, 9);
      this.vmMors.addAll((DVSUtil.createVms(connectAnchor, hostMor, 1, 6)));
      assertNotNull(vmMors, " Failed to create required number of vms");
      return true;
   }

   @Override
   @Test(description = " Test case to address PR#511841 "
               + "(Increasing the Proxy Port limit number) \n"
               + " 1. Set MAX PORTS to 258 while creating vDs   . \n"
               + " 2. Create 25 vms  with 10 nic cards and one vm with 7 nic cards "
               + " 3. Add 25 static DVPGs  with ten ports and one static DVPG with 7 ports\n"
               + " 4. Reconfigure Vms to connect to static DVPG \n"
               + " Reconfigure should succeed on all VMS. \n")
   public void test()
      throws Exception
   {

      for (ManagedObjectReference vmMor : this.vmMors) {
         Vector<DistributedVirtualSwitchPortConnection> portConns = new Vector<DistributedVirtualSwitchPortConnection>(
                  10);
         /*
          * create 10 DVport PortConnections on each VM
          */
         int noOfEthernetCards = DVSUtil.getAllVirtualEthernetCardDevices(
                  vmMor, connectAnchor).size();
         String pgKey = this.iDVS.addPortGroup(dvsMor, this.early,
                  noOfEthernetCards, this.ivm.getVMName(vmMor) + " DVPG");
         /*
          * Build the port connections for all the ethernet cards
          */
         for (int i = 0; i < noOfEthernetCards; i++) {
            portConns.add(DVSUtil.buildDistributedVirtualSwitchPortConnection(
                     dvSwitchUuid, null, pgKey));
         }
         /*
          * Reconfig VM here to connect to
          * DistributedVirtualSwitchPortConnection
          */

         VirtualMachineConfigSpec[] vmConfigSpec = null;
         vmConfigSpec = DVSUtil.getVMConfigSpecForDVSPort(vmMor, connectAnchor,
                  TestUtil.vectorToArray(portConns));
         assertTrue((vmConfigSpec != null && vmConfigSpec.length == 2
                  && vmConfigSpec[0] != null && vmConfigSpec[1] != null),
                  "Successfully obtained the original and the updated virtual"
                           + " machine config spec",
                  "Can not reconfigure the virtual machine to use the "
                           + "DV port");
         assertTrue(this.ivm.reconfigVM(vmMor, vmConfigSpec[0]),
                  "Successfully reconfigured the virtual machine to use "
                           + "the DV port",
                  "Failed to  reconfigured the virtual machine to use "
                           + "the DV port");

      }
   }

   @Override
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      if (vmMors != null && vmMors.size() > 0) {
         assertTrue(this.ivm.destroy(vmMors), VM_DEL_PASS, VM_DEL_FAIL);

      }
      if (this.dvsMor != null) {
         assertTrue(this.iDVS.destroy(this.dvsMor), " Failed to destroy vDs");
      }

      return true;
   }

}