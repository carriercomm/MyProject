package dvs.functional.opaquenetwork;

import static com.vmware.vcqa.util.Assert.assertNotNull;
import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import xDB_9_0_3.v;

import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.HostConnectSpec;
import com.vmware.vc.HostOpaqueNetworkInfo;
import com.vmware.vc.HostVirtualNic;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualDevice;
import com.vmware.vc.VirtualDeviceConfigSpec;
import com.vmware.vc.VirtualDeviceConfigSpecOperation;
import com.vmware.vc.VirtualDeviceConnectInfo;
import com.vmware.vc.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vc.VirtualMachineMovePriority;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vc.VirtualMachineRelocateSpec;
import com.vmware.vcqa.LogUtil;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.util.NetworkUtil;
import com.vmware.vcqa.util.VmHelper;
import com.vmware.vcqa.vim.ComputeResource;
import com.vmware.vcqa.vim.Datacenter;
import com.vmware.vcqa.vim.DistributedVirtualPortgroup;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.VirtualMachine;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.DatastoreSystem;
import com.vmware.vcqa.vim.host.NetworkSystem;
import com.vmware.vcqa.vim.host.VmotionSystem;
import com.vmware.vcqa.vim.provisioning.ProvisioningOpsStorageHelper;


/**
 * Move the vm connected to one host to another within the same datacenter
 *
 * @author ssaidapetpach
 *
 */

public class T13 extends TestBase
{

   private Folder folder = null;
   private ManagedObjectReference dcMor = null;
   private ManagedObjectReference srcVdsMor = null;
   private ManagedObjectReference destVdsMor = null;
   private DistributedVirtualSwitch dvs = null;
   private HostSystem hostSystem = null;
   private VmHelper vmHelper = null;
   private Datacenter dc = null;
   private List<ManagedObjectReference> hostMorList = null;
   private ComputeResource compResource = null;
   private VirtualMachine vm = null;
   private ManagedObjectReference vmMor = null;
   private DistributedVirtualPortgroup dvpg = null;
   private ManagedObjectReference dvpgMor = null;
   private List<ManagedObjectReference> vmMorList = null;
   private NetworkSystem nwSystem = null;
   private ManagedObjectReference nsMor = null;
   private ManagedObjectReference hostMor = null;
   private DatastoreSystem dataStoreSystem = null;
   private ManagedObjectReference datastoreSystemMor = null;
   private VirtualMachineConfigSpec reconfigVmSpec = null;
   private String[] dvportKey = null;
   private String[] destDvPortKey = null;
   private ManagedObjectReference dsMor = null;
   private ProvisioningOpsStorageHelper storageHelper = null;
   private ManagedObjectReference destHostMor = null;
   private ManagedObjectReference srcnsmor = null;
   private ManagedObjectReference destnsmor = null;
   private VirtualMachineConfigSpec origVMConfigSpec = null;
   private List<HostOpaqueNetworkInfo> opaqueNetworkInfo = null;

   public void initialize()
      throws Exception
   {
      folder = new Folder(connectAnchor);
      dvs = new DistributedVirtualSwitch(connectAnchor);
      hostSystem = new HostSystem(connectAnchor);
      vmHelper = new VmHelper(connectAnchor);
      dc = new Datacenter(connectAnchor);
      compResource = new ComputeResource(connectAnchor);
      vm = new VirtualMachine(connectAnchor);
      dvpg = new DistributedVirtualPortgroup(connectAnchor);
      nwSystem = new NetworkSystem(connectAnchor);
      dataStoreSystem = new DatastoreSystem(connectAnchor);
      this.dcMor = folder.getDataCenter();
      if (dcMor == null) {
         dcMor = folder.createDatacenter(folder.getRootFolder(), "dc");
      }
      assertNotNull(dcMor, "Found a valid datacenter in the inventory",
                    "Failed to find a datacenter in the inventory");
      storageHelper = new ProvisioningOpsStorageHelper(connectAnchor);
   }

   public void cleanUpRelocate()
            throws Exception
   {
      //cleanupVm();
      VirtualMachineRelocateSpec cleanupSpec = new VirtualMachineRelocateSpec();
      cleanupSpec.setHost(hostMor);
      cleanupSpec.setPool(hostSystem.getResourcePool(this.hostMor).get(0));
      vm.relocateVM(vmMor, cleanupSpec,
               VirtualMachineMovePriority.DEFAULT_PRIORITY);
      vm.reconfigVM(vmMor, origVMConfigSpec);
   }

   public void cleanupVm()
      throws Exception{
      Map<String,String> ethernetCardNetworkMap = NetworkUtil.
               getEthernetCardNetworkMap(vmMor, connectAnchor);
      for(String deviceName : ethernetCardNetworkMap.keySet()){
         ethernetCardNetworkMap.put(deviceName, "VM Network");
      }
      NetworkUtil.reconfigureVMConnectToPortgroup(vmMor,
                           connectAnchor, ethernetCardNetworkMap);
   }

   public void registerVm()
            throws Exception{
      //hostMorList = hostSystem.getAllHost();
      //hostMor = hostMorList.get(0);
      int count =1;
      this.datastoreSystemMor  = dataStoreSystem.getDatastoreSystem(
               this.destHostMor);
      ManagedObjectReference datastoreMor = dataStoreSystem.
               addNasVol("fvt-1", "10.115.160.201", "nfs-network",
                        this.datastoreSystemMor);
      /*vmMor = vmHelper.registerVmFromDatastore("rhel-netioc-vm-2",
                                               "nfs-network",hostMor);
      log.info("rhel-netioc-vm-2 registered on host : " +
               hostSystem.getHostName(hostMor));*/
   }

   public void unregisterVms()
      throws Exception
   {
      List<ManagedObjectReference> vmMorList = vm.getAllVM();
      if(vmMorList != null && vmMorList.size() >= 1){
         for(ManagedObjectReference vm_Mor : vmMorList){
            assertTrue(vm.setVMState(vm_Mor,
                       VirtualMachinePowerState.POWERED_OFF,
                       false),"Successfully powered off the virtual machine",
                       "Failed to power off the virtual machine");
            assertTrue(vm.unregisterVM(vm_Mor),"Successfully unregistered vm : "
                       + vm_Mor,"Failed to unregister vm : " + vm_Mor);
         }
      }
   }

   public void destroyVds()
      throws Exception
   {
      dcMor = folder.getDataCenter();
      if(dcMor != null){
         List<ManagedObjectReference> vdsMorList = folder.
                  getAllDistributedVirtualSwitch(folder.
                                                 getNetworkFolder(dcMor));
         List<ManagedObjectReference> vmList = vm.getAllVM();
         if(vdsMorList != null){
            for(ManagedObjectReference m : vdsMorList){
               dvs.destroy(m);
            }
         }
      }
   }

   public void addHost(String vcip,
                       List<String> esxipList,
                       int port)
      throws Exception
   {
      /*
       * Create datacenter
       */
      //dcMor = folder.getDataCenter();
      if (dcMor == null) {
         // dvs.destroy(dcMor);
         dcMor = folder.createDatacenter(folder.getRootFolder(), "dc");
      }
      HostConnectSpec hostConnectSpec = null;
      for(String esxip : esxipList){
         ManagedObjectReference hostMor =
               hostSystem.addStandaloneHost(dc.getHostFolder(dcMor),
                     hostSystem.createHostConnectSpec(esxip, true) ,
                     null, true);
         if(hostMor != null){
            System.out.println("The host : " + esxip +
                  "was successfully added");
            }
      }
      hostMor = hostSystem.getConnectedHost(false);
      //log.info("The host size : " + hostMorList.size());
      /*List<ManagedObjectReference> vmMorList = hostSystem.
              getAllVirtualMachine(hostMor);
      if (vmMorList != null && vmMorList.size() >= 1) {
         for (ManagedObjectReference vmMor : vmMorList) {
            vm.destroy(vmMor);
         }
      }
      List<ManagedObjectReference> vdsList = folder.
               getAllDistributedVirtualSwitch(folder.getNetworkFolder(dcMor));
      if (vdsList != null && vdsList.size() >= 1) {
         dvs.destroy(srcVdsMor);
      }*/
   }


   public void selectVnic(ManagedObjectReference host)
      throws Exception
   {
      VmotionSystem vmotionSystem = new VmotionSystem(connectAnchor);
      ManagedObjectReference vmotionMor = vmotionSystem.getVMotionSystem(host);
      HostVirtualNic vnic = vmotionSystem.getVmotionVirtualNic(vmotionMor,
                                    host);
      if(vnic == null){
          HostVirtualNic origVnic = nwSystem.getVirtualNic(nwSystem.
                   getNetworkSystem(hostMor), "Management Network", true);
          vmotionSystem.selectVnic(vmotionMor, origVnic.getDevice());
      }
   }

   @BeforeMethod
   public boolean testSetUp()
      throws Exception
   {
      /*
       * Create datacenter
       */
      initialize();

                try {
                        DVSUtil.startNsxa(connectAnchor, "root", "ca$hc0w", "vmnic1");
                        DVSUtil.testbedSetup(connectAnchor);
                } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return false;
                }

      /*
       * TODO : Plug the actual host IPs here
       */
      this.hostMor = hostSystem.getAllHost().get(0);
      this.destHostMor = hostSystem.getAllHost().get(1);
          this.vmMor = hostSystem.getVMs
                              (this.hostMor, VirtualMachinePowerState.POWERED_OFF).get(0);
      this.dsMor = this.storageHelper.getVMConfigDatastore(vmMor);
      /*
       * Enable vmotion on both the host vnics
       */
      selectVnic(hostMor);
      selectVnic(destHostMor);
      srcnsmor = nwSystem.getNetworkSystem(hostMor);
      destnsmor = nwSystem.getNetworkSystem(destHostMor);
      List<HostOpaqueNetworkInfo> opaqueNetworkInfo = nwSystem.
                                getNetworkInfo(srcnsmor).getOpaqueNetwork();
      assertTrue(opaqueNetworkInfo != null && opaqueNetworkInfo.size() > 0,
                         "The list of opaque networks is not null",
                         "The list of opaque networks is null");
      /*
       * Get the number of ethernet card devices on the vm
       */
      int numCards = DVSUtil.getAllVirtualEthernetCardDevices(vmMor,
                     connectAnchor).size();

      /*
       * Reconfigure the virtual machine to connect to an opaque network
       */
      Map<String,String> vmEthernetMap = NetworkUtil.
                                getEthernetCardNetworkMap(vmMor, connectAnchor);
      Set<String> ethernetCardDevicesSet = vmEthernetMap.keySet();
      /*
       * Compute a new ethernet card network map
       */
      Map<String,HostOpaqueNetworkInfo> ethernetCardNetworkMap = new
                      HashMap<String,HostOpaqueNetworkInfo>();
      for(String ethernetCard : ethernetCardDevicesSet){
                        ethernetCardNetworkMap.put(ethernetCard, opaqueNetworkInfo.get(0));
      }
      /*
       * Reconfigure the virtual machine to connect to opaque network
       */
          this.origVMConfigSpec = NetworkUtil.
                                               reconfigureVMConnectToOpaqueNetwork(vmMor,
                                               ethernetCardNetworkMap, connectAnchor);
      assertTrue(vm.setVMState(vmMor, VirtualMachinePowerState.POWERED_ON,
                 true),"Successfully powered on the vm","Failed to power on " +
                 "the vm");
          Thread.sleep(30000);
      return true;
   }

   @Test
   public void test()
      throws Exception
   {
      /*
       * Generate the vm relocate spec for moving the vm from one host
       * to another
       */
      VirtualMachineRelocateSpec relocateSpec = new
               VirtualMachineRelocateSpec();
      relocateSpec.setFolder(this.folder.getVMFolder(dcMor));
      relocateSpec.setHost(this.destHostMor);
      relocateSpec.setPool(hostSystem.getResourcePool(this.destHostMor).
                           get(0));
      //relocateSpec.setDatastore(this.dsMor);

      assertTrue(vm.relocateVM(vmMor, relocateSpec,
                 VirtualMachineMovePriority.DEFAULT_PRIORITY),
                 "Successfully relocated the vm with the destination " +
                 "network backing","Failed to relocate the vm with the " +
                 "destination network backing");
          assertTrue(vm.getIPAddress(vmMor) != null, "vm ip is not null", "vm ip is null");
      assertTrue(DVSUtil.checkNetworkConnectivity(
            hostSystem.getIPAddress(hostMor),vm.getIPAddress(vmMor),null),
        "VM is accessible on the network",
        "VM is not accessible on the network");
   }

   @AfterMethod
   public boolean testCleanUp()
      throws Exception
   {
           boolean cleanupWorked = true;
           try {
                   if (vmMor != null) {
                           if (vm.getVMState(vmMor).equals(VirtualMachinePowerState.POWERED_ON)) {
                                   assertTrue(vm.setVMState(vmMor, VirtualMachinePowerState.POWERED_OFF,
                                                   true),"Successfully powered off the vm","Failed to power off " +
                                                   "the vm");
                           }
                   }
                   try {
                   cleanUpRelocate();
                   } catch (Exception e) {

                   }
           } catch (Throwable t) {
                   t.printStackTrace();
                        cleanupWorked = false;
                        DVSUtil.testbedTeardown(connectAnchor, true);
           }
                try {
                        DVSUtil.testbedTeardown(connectAnchor);
                } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        cleanupWorked = false;
                }
                try {
                        DVSUtil.stopNsxa(connectAnchor, "root", "ca$hc0w");
                } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        cleanupWorked = false;
                }
                assertTrue(cleanupWorked, "Cleanup Succeeded !", "Cleanup Failed !");
        return true;
   }
}

