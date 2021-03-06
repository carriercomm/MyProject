/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfigureportgroups;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVPortConfigSpec;
import com.vmware.vc.DVPortSetting;
import com.vmware.vc.DVPortgroupConfigSpec;
import com.vmware.vc.DVPortgroupPolicy;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DistributedVirtualPort;
import com.vmware.vc.DistributedVirtualSwitchHostMemberConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicBacking;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicSpec;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.DistributedVirtualSwitchPortCriteria;
import com.vmware.vc.HostNetworkConfig;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualDevice;
import com.vmware.vc.VirtualMachineConfigInfo;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.util.VersionConstants;
import com.vmware.vcqa.vim.DistributedVirtualPortgroup;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.ManagedEntity;
import com.vmware.vcqa.vim.VirtualMachine;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.NetworkSystem;

/**
 * Reconfigure an early binding portgroup to an existing distributed virtual
 * switch with portConfigResetAtDisconnect set to true and
 * settingBlockOverrideAllowed set to true
 */
public class Pos044 extends TestBase
{
   private Folder iFolder = null;
   private ManagedEntity iManagedEntity = null;
   private ManagedObjectReference rootFolderMor = null;
   private ManagedObjectReference dvsMor = null;
   private DVSConfigSpec dvsConfigSpec = null;
   private DistributedVirtualSwitch iDVSwitch = null;
   private VirtualMachine iVirtualMachine = null;
   private HostSystem iHostSystem = null;
   private NetworkSystem iNetworkSystem = null;
   private ManagedObjectReference dvPortgroupMor = null;
   private ManagedObjectReference hostMor = null;
   private ManagedObjectReference vmMor = null;
   private ManagedObjectReference nsMor = null;
   private DVPortgroupConfigSpec dvPortgroupConfigSpec = null;
   private HostNetworkConfig[] hostNetworkConfig = null;
   private HostNetworkConfig originalNetworkConfig = null;
   private VirtualMachinePowerState vmPowerState = null;
   private DistributedVirtualSwitchHostMemberConfigSpec hostConfigSpecElement = null;
   private List<ManagedObjectReference> dvPortgroupMorList = null;
   private DVPortgroupConfigSpec[] dvPortgroupConfigSpecArray = null;
   private DistributedVirtualSwitchPortConnection portConnection = null;
   private VirtualMachineConfigSpec origVMConfigSpec = null;
   private DistributedVirtualPortgroup iDVPortgroup = null;
   private String portgroupKey = null;
   private Map<String, List<String>> usedPorts = null;
   private VirtualMachineConfigSpec[] updatedDeltaConfigSpec = null;
   private DistributedVirtualSwitchHostMemberPnicSpec[] dvsHostMemberPnicSpec = null;
   private ManagedObjectReference dcMor = null;
   private String portKey = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      setTestDescription("Reconfigure an early binding portgroup to an "
               + "existing distributed virtual switch with "
               + "portConfigResetAtDisconnect set to true and "
               + "settingBlockOverrideAllowed set to true");
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
      final String dvsName = DVSTestConstants.DVS_CREATE_NAME_PREFIX
               + getTestId();
      int i = 0;
      DistributedVirtualSwitchHostMemberPnicBacking pnicBacking = null;
      VirtualMachineConfigInfo vmConfigInfo = null;
      VirtualDevice[] vds = null;
      Vector allVMs = null;
      DVPortSetting portSetting = null;
      DVPortConfigSpec portConfigSpec = null;
      usedPorts = new HashMap<String, List<String>>();
      log.info("Test setup Begin:");
      try {
         this.iFolder = new Folder(connectAnchor);
         this.iDVSwitch = new DistributedVirtualSwitch(connectAnchor);
         this.iDVPortgroup = new DistributedVirtualPortgroup(connectAnchor);
         this.iVirtualMachine = new VirtualMachine(connectAnchor);
         this.iHostSystem = new HostSystem(connectAnchor);
         this.iManagedEntity = new ManagedEntity(connectAnchor);
         this.iNetworkSystem = new NetworkSystem(connectAnchor);
         this.dcMor = this.iFolder.getDataCenter();
         /*
          * Get a standalone host of version 4.0
          */
         this.hostMor = this.iHostSystem.getAllHost().get(0);
         /*
          * If there is no standalone host available,  try to 
          * get a clustered host of version 4.0
          */
         if (this.hostMor == null) {
            this.hostMor = this.iHostSystem.getClusteredHost();
            if (TestUtil.compareVersion(
                     this.iHostSystem.getHostVersion(this.hostMor),
                     VersionConstants.ESX400) != VersionConstants.EQUAL_VERSION) {
               this.hostMor = null;
            }
         }
         if (hostMor != null) {
            this.rootFolderMor = this.iFolder.getRootFolder();
            if (this.dcMor != null) {
               this.dvsConfigSpec = new DVSConfigSpec();
               this.dvsConfigSpec.setConfigVersion("");
               this.dvsConfigSpec.setName(dvsName);
               this.dvsConfigSpec.setNumStandalonePorts(10);
               hostConfigSpecElement = new DistributedVirtualSwitchHostMemberConfigSpec();
               hostConfigSpecElement.setHost(hostMor);
               hostConfigSpecElement.setOperation(TestConstants.CONFIG_SPEC_ADD);
               /*
                * TODO Check whether the pnic devices need to be
                * set in the DistributedVirtualSwitchHostMemberPnicSpec
                */
               pnicBacking = new DistributedVirtualSwitchHostMemberPnicBacking();
               pnicBacking.getPnicSpec().clear();
               pnicBacking.getPnicSpec().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberPnicSpec[] {}));
               hostConfigSpecElement.setBacking(pnicBacking);
               this.dvsConfigSpec.getHost().clear();
               this.dvsConfigSpec.getHost().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberConfigSpec[] { hostConfigSpecElement }));
               dvsMor = this.iFolder.createDistributedVirtualSwitch(
                        this.iFolder.getNetworkFolder(dcMor), dvsConfigSpec);
               if (dvsMor != null) {
                  log.info("Successfully created the distributed "
                           + "virtual switch");
                  hostNetworkConfig = this.iDVSwitch.getHostNetworkConfigMigrateToDVS(
                           this.dvsMor, this.hostMor);
                  this.nsMor = this.iNetworkSystem.getNetworkSystem(hostMor);
                  if (nsMor != null) {
                     this.iNetworkSystem.refresh(nsMor);
                     Thread.sleep(5000);
                     this.iNetworkSystem.updateNetworkConfig(nsMor,
                              hostNetworkConfig[0],
                              TestConstants.CHANGEMODE_MODIFY);
                     this.originalNetworkConfig = hostNetworkConfig[1];

                  } else {
                     log.error("Network system MOR is null");
                  }
                  allVMs = this.iHostSystem.getVMs(hostMor, null);
                  /*
                   * Get the first VM in the list of VMs.
                   */
                  if (allVMs != null) {
                     this.vmMor = (ManagedObjectReference) allVMs.get(0);
                  }
                  if (this.vmMor != null) {
                     this.vmPowerState = this.iVirtualMachine.getVMState(vmMor);
                     this.origVMConfigSpec = this.iVirtualMachine.getVMConfigSpec(vmMor);
                     if (this.iVirtualMachine.setVMState(vmMor, VirtualMachinePowerState.POWERED_OFF, false)) {
                        log.info("Successfully powered off the"
                                 + " virtual machine");
                        portKey = this.iDVSwitch.getFreeStandaloneDVPortKey(
                                 dvsMor, usedPorts);
                        if (portKey != null) {
                           portSetting = new DVPortSetting();
                           portSetting.setBlocked(DVSUtil.getBoolPolicy(false,
                                    false));
                           portConfigSpec = new DVPortConfigSpec();
                           portConfigSpec.setKey(portKey);
                           portConfigSpec.setOperation(TestConstants.CONFIG_SPEC_EDIT);
                           portConfigSpec.setSetting(portSetting);
                           if (this.iDVSwitch.reconfigurePort(dvsMor,
                                    new DVPortConfigSpec[] { portConfigSpec })) {
                              log.info("Successfully reconfigured"
                                       + " the port");
                              portConnection = new DistributedVirtualSwitchPortConnection();
                              portConnection.setPortKey(portKey);
                              portConnection.setSwitchUuid(this.iDVSwitch.getConfig(
                                       dvsMor).getUuid());
                              updatedDeltaConfigSpec = DVSUtil.getVMConfigSpecForDVSPort(
                                       vmMor,
                                       connectAnchor,
                                       new DistributedVirtualSwitchPortConnection[] { portConnection });
                              if (this.iVirtualMachine.reconfigVM(vmMor,
                                       updatedDeltaConfigSpec[0])) {
                                 log.info("Successfully "
                                          + "reconfigured the VM to "
                                          + "connect to the port");
                                 if (this.iVirtualMachine.setVMState(vmMor, VirtualMachinePowerState.POWERED_ON, false)) {
                                    log.info("Successfully "
                                             + "powered on the VM");
                                    this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
                                    this.dvPortgroupConfigSpec.setName(this.getTestId());
                                    this.dvPortgroupConfigSpec.setType(DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING);
                                    dvPortgroupMorList = this.iDVSwitch.addPortGroups(
                                             dvsMor,
                                             new DVPortgroupConfigSpec[] { dvPortgroupConfigSpec });
                                    if (dvPortgroupMorList != null
                                             && dvPortgroupMorList.get(0) != null) {
                                       log.info("Successfully "
                                                + "added the " + "portgroup");
                                       status = true;
                                    } else {
                                       log.error("Failed to add"
                                                + " the portgroups");
                                    }
                                 } else {
                                    log.error("Failed to "
                                             + "power on the VM");
                                 }
                              } else {
                                 log.error("Failed to "
                                          + "reconfigure the VM to "
                                          + "connect to the port");
                              }
                           } else {
                              log.error("Failed to reconfigure "
                                       + "the port");
                           }
                        }
                     }
                  } else {
                     log.error("Failed to find a virtual machine");
                  }
               } else {
                  log.error("Failed to create the distributed "
                           + "virtual switch");
               }
            } else {
               log.error("Failed to find a folder");
            }
         } else {
            log.error("Failed to login");
         }
      } catch (Exception e) {
         TestUtil.handleException(e);
      }
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that reconfigures an early binding portgroup with scope set to a
    * virtual machine MOR and reconfigures a VM-vnic to connect to this
    * portgroup
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Reconfigure an early binding portgroup to an "
               + "existing distributed virtual switch with "
               + "portConfigResetAtDisconnect set to true and "
               + "settingBlockOverrideAllowed set to true")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
      DVPortgroupPolicy policy = null;
      DVPortSetting portgroupSetting = null;
      DVPortConfigSpec[] portConfigSpec = null;
      List<DistributedVirtualPort> dvPorts = null;
      DistributedVirtualSwitchPortCriteria portCriteria = null;
      try {
         this.dvPortgroupConfigSpec = new DVPortgroupConfigSpec();
         this.dvPortgroupConfigSpec.setConfigVersion(this.iDVPortgroup.getConfigInfo(
                  this.dvPortgroupMorList.get(0)).getConfigVersion());
         policy = new DVPortgroupPolicy();
         policy.setPortConfigResetAtDisconnect(true);
         policy.setBlockOverrideAllowed(true);
         policy.setLivePortMovingAllowed(true);
         this.dvPortgroupConfigSpec.setPolicy(policy);
         portgroupSetting = new DVPortSetting();
         portgroupSetting.setBlocked(DVSUtil.getBoolPolicy(false, true));
         this.dvPortgroupConfigSpec.setDefaultPortConfig(portgroupSetting);
         if (this.iDVPortgroup.reconfigure(dvPortgroupMorList.get(0),
                  this.dvPortgroupConfigSpec)) {
            log.info("Successfully reconfigured the portgroup");
            portgroupKey = this.iDVPortgroup.getKey(dvPortgroupMorList.get(0));
            if (portgroupKey != null) {
               if (this.iDVSwitch.movePort(dvsMor, new String[] { portKey },
                        portgroupKey)) {
                  log.info("Successfully moved the port into the "
                           + "portgroup");
                  if (this.iVirtualMachine.reconfigVM(vmMor,
                           updatedDeltaConfigSpec[1])) {
                     log.info("Successfully reconfigured the VM to "
                              + "disconnect from the port");
                     portCriteria = new DistributedVirtualSwitchPortCriteria();
                     portCriteria.getPortKey().clear();
                     portCriteria.getPortKey().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new String[] { portKey }));
                     dvPorts = this.iDVSwitch.fetchPorts(dvsMor, portCriteria);
                     if (dvPorts != null && dvPorts.get(0) != null) {
                        log.info("Successfully obtained the port");
                        if (dvPorts.get(0).getConfig().getSetting() != null) {
                           status = true;
                           log.info("The port configuration has  "
                                    + "been reset");
                        } else {
                           log.error("The port configuration has not"
                                    + " been reset");
                        }
                     } else {
                        log.error("Failed to obtain the port "
                                 + "config spec");
                     }
                  } else {
                     log.error("Successfully reconfigured the VM to "
                              + "disconnect from the port");
                  }
               } else {
                  log.error("Failed to move the port into the "
                           + "portgroup");
               }
            } else {
               log.error("Could not get the portgroup key");
            }
         } else {
            log.error("Coud not reconfigure the portgroup");
         }
      } catch (Exception e) {
         TestUtil.handleException(e);
      }
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test was started. Restore
    * the original state of the VM.Destroy the portgroup, followed by the
    * distributed virtual switch
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
      try {
         /*
          * Restore the power state of the virtual machine
          */
         status &= this.iVirtualMachine.setVMState(vmMor, this.vmPowerState,
                  false);
         /*
          * Restore the original config spec of the virtual machine
          */
         status &= this.iVirtualMachine.reconfigVM(vmMor,
                  updatedDeltaConfigSpec[1]);
         /*
          * Restore the original network config
          */
         if (this.originalNetworkConfig != null) {
            status &= this.iNetworkSystem.updateNetworkConfig(nsMor,
                     originalNetworkConfig, TestConstants.CHANGEMODE_MODIFY);
         }
      } catch (Exception e) {
         TestUtil.handleException(e);
         status = false;
      } finally {
         try {
            if (this.dvsMor != null) {
               status &= this.iManagedEntity.destroy(dvsMor);
            }
         } catch (Exception ex) {
            TestUtil.handleException(ex);
            status = false;
         }
      }
      assertTrue(status, "Cleanup failed");
      return status;
   }
}
