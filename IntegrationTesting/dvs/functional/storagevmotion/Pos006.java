/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */

package dvs.functional.storagevmotion;

import static com.vmware.vcqa.TestConstants.VM_VIRTUALDEVICE_DISK;
import static com.vmware.vcqa.TestConstants.VM_VIRTUALDEVICE_ETHERNET_PCNET32;
import static com.vmware.vcqa.vim.dvs.DVSTestConstants.DVPORTGROUP_TYPE_LATE_BINDING;

import java.util.Vector;

import org.testng.annotations.BeforeMethod;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.HostVirtualNic;
import com.vmware.vc.HostVirtualNicSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vc.VirtualMachineRelocateSpec;
import com.vmware.vc.VirtualMachineRelocateSpecDiskLocator;
import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.Datastore;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.DatastoreInformation;
import com.vmware.vcqa.vim.host.VmotionSystem;

/**
 * Storage vmotion the disk of a powered on VM via a vmotion virtual nic that is
 * connected to an late binding portgroup of the DVSwitch to a different
 * datastore on a host that has multiple datastores. The disk moves to the
 * destination datastore.
 */
public class Pos006 extends SVMFunctionalTestBase
{
   public void setTestDescription()
   {
      setTestDescription("Storage vmotion the disk of a powered on VM via a "
               + "vmotion virtual nic that is connected to a "
               + "latebinding portgroup of the DVSwitch to a different "
               + "datastore on a host that has multiple datastores. "
               + "The disk moves to the destination datastore.");
   }

   /**
    * Method to set up the Environment for the test.
    * 
    * @param connectAnchor Reference to the ConnectAnchor object.
    * @return Return true, if test set up was sucessful false, if test set up
    *         was not sucessful
    */
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      boolean setupDone = false;
      String portgroupKey = null;
      DVSConfigInfo configInfo = null;
      DistributedVirtualSwitchPortConnection portConnection = null;
      VirtualMachineConfigSpec vmConfigSpec = null;
      Vector<DatastoreInformation> hostDatastores = null;
      DatastoreInformation dataStoreInformation = null;
      HostVirtualNic vmotionNic = null;
      HostVirtualNicSpec updatedVnicSpec = null;
      Vector<Integer> diskIDs = null;
      VirtualMachineRelocateSpecDiskLocator diskLocator = null;
      ManagedObjectReference dsMor = null;

         if (super.testSetUp()) {
            this.iVmotionSystem = new VmotionSystem(connectAnchor);
            this.vmotionSystem = this.iVmotionSystem.getVMotionSystem(this.hostMor);
            Datastore ds = new Datastore(this.connectAnchor);
            String hostName = this.ihs.getHostName(this.hostMor);
            if (this.vmotionSystem != null) {
               configInfo = this.iDVS.getConfig(this.dvsMor);
               portgroupKey = this.iDVS.addPortGroup(this.dvsMor,
                        DVPORTGROUP_TYPE_LATE_BINDING, 2, this.getTestId()
                                 + "-lpg");
               if (portgroupKey != null) {
                  portConnection = new DistributedVirtualSwitchPortConnection();
                  portConnection.setPortgroupKey(portgroupKey);
                  portConnection.setSwitchUuid(configInfo.getUuid());

                  vmConfigSpec = DVSUtil.buildCreateVMCfg(connectAnchor,
                           portConnection, VM_VIRTUALDEVICE_ETHERNET_PCNET32,
                           this.getTestId() + "-VM", this.hostMor);
                  if (vmConfigSpec != null) {
                     this.vmMor = new Folder(super.getConnectAnchor()).createVM(
                              this.ivm.getVMFolder(), vmConfigSpec,
                              this.ihs.getResourcePool(this.hostMor).get(0),
                              this.hostMor);
                     if (this.vmMor != null) {
                        this.vmName = this.ivm.getName(this.vmMor);
                        dataStoreInformation = this.ivm.getDatastoresInfo(vmMor).get(
                                 0);
                        if (dataStoreInformation.getDatastoreMor() != null) {
                           hostDatastores = this.ihs.getDatastoresInfo(this.hostMor);
                           if (hostDatastores != null
                                    && hostDatastores.size() > 1) {
                              for (DatastoreInformation datastoreInfo : hostDatastores) {
                                 if (datastoreInfo.getName().equals(
                                          dataStoreInformation.getName())) {
                                    continue;
                                 } else if (datastoreInfo.isAccessible()
                                                 && ds.isDsWritable(datastoreInfo, hostName, ihs)) {
                                    dsMor = datastoreInfo.getDatastoreMor();
                                    break;
                                 }
                              }
                              if (dsMor != null) {
                                 log.info("Found the datastore to relocate the"
                                          + " VM to");
                                 diskIDs = this.ivm.getDeviceInfo(this.vmMor,
                                          VM_VIRTUALDEVICE_DISK);
                                 if (diskIDs != null && diskIDs.size() == 1) {
                                    diskLocator = new VirtualMachineRelocateSpecDiskLocator();
                                    diskLocator.setDiskId(diskIDs.get(0));
                                    diskLocator.setDatastore(dsMor);
                                 }
                                 if (diskLocator != null) {
                                    this.vmRelocateSpec = new VirtualMachineRelocateSpec();
                                    this.vmRelocateSpec.getDisk().clear();
                                    this.vmRelocateSpec.getDisk().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new VirtualMachineRelocateSpecDiskLocator[] { diskLocator }));
                                    if (this.ivm.setVMState(
                                             this.vmMor, VirtualMachinePowerState.POWERED_ON, false)) {
                                       log.info("Successfully powered on "
                                                + "the VM " + this.vmName);
                                       portConnection = new DistributedVirtualSwitchPortConnection();
                                       portConnection.setSwitchUuid(configInfo.getUuid());
                                       portConnection.setPortgroupKey(portgroupKey);
                                       vnicDevice = DVSUtil.addVnic(connectAnchor, hostMor, portConnection);
                                       if (vnicDevice != null) {
                                           boolean status = iVmotionSystem.selectVnic(vmotionSystem,
                                               vnicDevice);
                                           if (status) {
                                              log.info("Successfully selected the added vnic to be "
                                                       + "vmotion virtual nic");
                                              setupDone = true;
                                           }
                                       }
                                    } else {
                                       log.error("Can not power on the VM "
                                                + this.vmName);
                                    }
                                 }
                              } else {
                                 log.error("Can not find the datastore to "
                                          + "relocate to");
                              }
                           } else {
                              log.error("Can not find more than one datastore "
                                       + "on the host "
                                       + this.ihs.getName(this.hostMor));
                           }
                        }
                     } else {
                        log.error("Can not create a new VM");
                     }
                  } else {
                     log.error("Can not create a new virtual machine config spec");
                  }
               } else {
                  log.error("There are no standalone dv ports on the DVS");
               }
            }
         }
     
      Assert.assertTrue(setupDone, "Setup failed");
      return setupDone;
   }
}