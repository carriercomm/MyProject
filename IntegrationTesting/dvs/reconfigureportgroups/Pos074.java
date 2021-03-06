/*
 * ************************************************************************
 *
 * Copyright 2009-2010 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfigureportgroups;

import static com.vmware.vcqa.vim.MessageConstants.VM_POWEROFF_PASS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.vmware.vc.DVPortgroupConfigSpec;
import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VMwareDVSConfigSpec;
import com.vmware.vc.VMwareDVSPortSetting;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualPortgroup;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.VirtualMachine;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;

/**
 * DESCRIPTION:<br>
 * Reconfigure PortGroup to a DistributedVirtualSwitch with IpfixEnabled parameter
 * set to true/false in VMPortConfigPolicy <br>
 * SETUP:<br>
 * 1. Create a DVS by adding a host to VC.<br>
 * 2. Add a portgroup containing one standalone port <br>
 * TEST:<br>
 * 3. ReconfigurePortGroup with the PortConfigSpecs with IpfixConfig Objects set <br>
 * 4. Create a VM and reconfigure the VM to connect to the DV Port <br>
 * 5. Verify the IpfixConfig Settings <br>
 * CLEANUP:<br>
 * 6. Destroy the created VM<br>
 * 7. Destroy the created DVS <br>
 *
 */

public class Pos074 extends TestBase implements IDataDrivenTest
{

   private Folder folder =  null;
   private DistributedVirtualSwitch dvs = null;
   private DistributedVirtualPortgroup dvPortgroup=null;
   private ManagedObjectReference networkFolderMor;
   private ManagedObjectReference dcMor = null;
   private VirtualMachine vm;
   private HostSystem hs;
   private VMwareDVSConfigSpec configSpec = null;
   private ManagedObjectReference dvsMor;
   private String dvSwitchUuid;
   private VMwareDVSPortSetting dvPort;
   private DVPortgroupConfigSpec dvPortgroupConfigSpec;
   private ManagedObjectReference hostMor;
   private DVPortgroupConfigSpec[] dvPortgroupConfigSpecArray;
   private List<ManagedObjectReference> dvPortgroupMorList;
   private Vector<ManagedObjectReference> vmMors = null;
   private DistributedVirtualSwitchPortConnection dvsConn;

   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean setUpDone = false;
      this.folder = new Folder(connectAnchor);
      this.hs = new HostSystem(connectAnchor);
      this.dvPortgroup = new DistributedVirtualPortgroup(connectAnchor);
      this.dcMor = this.folder.getDataCenter();
      this.networkFolderMor = this.folder.getNetworkFolder(this.dcMor);
      this.hostMor = this.hs.getConnectedHost(null);
      this.vm = new VirtualMachine(connectAnchor);
      this.networkFolderMor = this.folder.getNetworkFolder(this.dcMor);
      Assert.assertNotNull(this.networkFolderMor,
               "Unable to get Networkfolder Mor");
      this.dvs = new DistributedVirtualSwitch(connectAnchor);
      // Create DVS by adding a host
      this.configSpec = new VMwareDVSConfigSpec();
      this.configSpec = (VMwareDVSConfigSpec) DVSUtil.addHostsToDVSConfigSpec(
               configSpec, Arrays.asList(this.hostMor));
      this.configSpec.setName(this.getClass().getName());
      this.dvsMor = this.folder.createDistributedVirtualSwitch(
               this.networkFolderMor, this.configSpec);
      DVSConfigInfo configInfo = this.dvs.getConfig(this.dvsMor);
      this.dvSwitchUuid = configInfo.getUuid();
      // Set IpfixEnabled to true/false
      dvPort = new VMwareDVSPortSetting();
      dvPort.setIpfixEnabled(DVSUtil.getBoolPolicy(false,
               this.data.getBoolean(DVSTestConstants.IP_FIX_ENABLED_KEY)));
      this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
      this.dvPortgroupConfigSpec.setConfigVersion("");
      this.dvPortgroupConfigSpec.setName(this.getTestId());
      this.dvPortgroupConfigSpec.setDefaultPortConfig(dvPort);
      this.dvPortgroupConfigSpec.setType(DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING);
      this.dvPortgroupConfigSpec.setNumPorts(1);
      dvPortgroupConfigSpecArray = new DVPortgroupConfigSpec[] { dvPortgroupConfigSpec };
      // Create portGroup
      dvPortgroupMorList = dvs.addPortGroups(dvsMor, dvPortgroupConfigSpecArray);
      Assert.assertNotNull(dvPortgroupMorList, "Could not create a port Group.");
      Assert.assertTrue(
               dvPortgroupMorList.size() == dvPortgroupConfigSpecArray.length,
               "Could not add all the portgroups");
      setUpDone = true;
      return setUpDone;
   }

   @Test(description="Reconfigure PortGroup to a DistributedVirtualSwitch with " +
   		"IpfixEnabled parameter set to true/false in VMPortConfigPolicy")
   public void test()
   throws Exception
   {
      Assert.assertTrue(dvPortgroupMorList != null
               && dvPortgroupMorList.size() > 0,
               "There are no port groups to reconfigure.");
      ManagedObjectReference dvPortgroupMor = dvPortgroupMorList.get(0);
      // Reconfigure DVPortGroup
      this.dvPortgroupConfigSpec.setConfigVersion(this.dvPortgroup.getConfigInfo(
               dvPortgroupMorList.get(0)).getConfigVersion());
      Assert.assertTrue(this.dvPortgroup.reconfigure(dvPortgroupMorList.get(0),
               dvPortgroupConfigSpec), "Successfully reconfigured portgroup",
               "Unable to reconfigure portgroup");
      // Create 1 VM
      this.vmMors = DVSUtil.createVms(connectAnchor, hostMor, 1, 0);
      // Add VMs to DVPortGroup
      for (ManagedObjectReference vmMor : this.vmMors) {
         dvsConn = new DistributedVirtualSwitchPortConnection();
         String pgKey = dvPortgroup.getConfigInfo(dvPortgroupMor).getKey();
         String portKey = dvPortgroup.getPortKeys(dvPortgroupMor).get(0);
         ArrayList<String> portKeys = new ArrayList<String>();
         portKeys.add(portKey);
         dvsConn.setPortgroupKey(pgKey);
         dvsConn.setSwitchUuid(this.dvSwitchUuid);
         VirtualMachineConfigSpec[] vmConfigSpec = null;
         vmConfigSpec = DVSUtil.getVMConfigSpecForDVSPort(
                  vmMor,
                  connectAnchor,
                  new DistributedVirtualSwitchPortConnection[] { DVSUtil.buildDistributedVirtualSwitchPortConnection(
                           dvSwitchUuid, null, pgKey) });
         Assert.assertTrue((vmConfigSpec != null && vmConfigSpec.length == 2
                  && vmConfigSpec[0] != null && vmConfigSpec[1] != null),
                  "Successfully obtained the original and the updated virtual"
                           + " machine config spec",
                  "Can not reconfigure the virtual machine to use the "
                           + "DV port");
         Assert.assertTrue(this.vm.reconfigVM(vmMor, vmConfigSpec[0]),
                  "Successfully reconfigured the virtual machine to use "
                           + "the DV port",
                  "Failed to  reconfigured the virtual machine to use "
                           + "the DV port");
         Assert.assertTrue(DVSUtil.performVDSPortVerifcation(connectAnchor,
                  this.hostMor, vmMor, dvsConn, this.dvSwitchUuid),
                  " Failed to verify port connection  and/or PortPersistenceLocation for VM : "
                           + vm.getName(vmMor));
         // Verify PortSettings from VC & hostd side.
         Assert.assertTrue(DVSUtil.verifyIpfixPortSettingFromParent(
                  connectAnchor, dvsMor, dvPort, portKeys),
                  "Verification of IpfixPortSetting failed");
      }
   }

   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
      if (this.vmMors != null) {
         Assert.assertTrue(vm.setVMsState(vmMors, VirtualMachinePowerState.POWERED_OFF, false),
                  VM_POWEROFF_PASS, VM_POWEROFF_PASS);
         vm.destroy(vmMors);
      }
      if (this.dvPortgroupMorList != null) {
         for (ManagedObjectReference mor : dvPortgroupMorList) {
            status &= this.dvPortgroup.destroy(mor);
         }
      }
      if (this.dvsMor != null) {
         status &= this.dvs.destroy(dvsMor);
      }
      Assert.assertTrue(status, "Cleanup failed");
      return status;
   }

   /**
    * This method retrieves either all the data-driven tests or one
    * test based on the presence of test id in the execution properties
    * file.
    *
    * @return Object[]
    *
    * @throws Exception
    */
   @Factory
   @Parameters({"dataFile"})
   public Object[] getTests(@Optional("") String dataFile)
      throws Exception {
      Object[] tests = TestExecutionUtils.getTests(this.getClass().getName(),
         dataFile);
      /*
       * Load the dvs execution properties file
       */
      String testId = TestUtil.getPropertyValue(this.getClass().getName(),
         DVSTestConstants.DVS_EXECUTION_PROP_FILE);
      if(testId == null){
         return tests;
      } else {
         for(Object test : tests){
            if(test instanceof TestBase){
               TestBase testBase = (TestBase)test;
               if(testBase.getTestId().equals(testId)){
                  return new Object[]{testBase};
               }
            } else {
               log.error("The current test is not an instance of TestBase");
            }
         }
         log.error("The test id " + testId + "could not be found");
      }
      /*
       * TODO : Examine the possibility of a custom exception here since
       * the test id provided is wrong and the user needs to be notified of
       * that.
       */
      return null;
   }

   public String getTestName()
   {
      return getTestId();
   }
}
