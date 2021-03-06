package dvs.functional.opaquenetwork;

import static com.vmware.vcqa.TestConstants.VM_DEFAULT_GUEST_WINDOWS;
import static com.vmware.vcqa.TestConstants.VM_VIRTUALDEVICE_SCSI_BUSL_CONTROLLER;
import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.HostOpaqueNetworkInfo;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.VirtualDevice;
import com.vmware.vc.VirtualDeviceConfigSpec;
import com.vmware.vc.VirtualEthernetCard;
import com.vmware.vc.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vc.VirtualEthernetCardOpaqueNetworkBackingInfo;
import com.vmware.vc.VirtualMachineConfigSpec;
import com.vmware.vc.VirtualMachinePowerState;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.NetworkUtil;
import com.vmware.vcqa.util.VmHelper;
import com.vmware.vcqa.vim.Datacenter;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.HostSystem;
import com.vmware.vcqa.vim.VirtualMachine;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.DatastoreSystem;
import com.vmware.vcqa.vim.host.NetworkSystem;
import com.vmware.vcqa.vim.provisioning.ProvisioningOpsStorageHelper;

public class T2 extends TestBase{

        private Folder folder = null;
        private HostSystem hostSystem = null;
        private VmHelper vmHelper = null;
        private Datacenter dc = null;
        private VirtualMachine vm = null;
        private NetworkSystem nwSystem = null;
        private DatastoreSystem dataStoreSystem = null;
        private ManagedObjectReference dcMor = null;
        private ProvisioningOpsStorageHelper storageHelper = null;
        private ManagedObjectReference hostMor = null;
        private NetworkSystem ins = null;
        private ManagedObjectReference nsMor = null;
        private ManagedObjectReference vmMor = null;
        private ManagedObjectReference baseVmMor = null;



        public void initialize() throws Exception {
                folder  = new Folder(connectAnchor);
                hostSystem = new HostSystem(connectAnchor);
                vmHelper = new VmHelper(connectAnchor);
                dc = new Datacenter(connectAnchor);
                vm = new VirtualMachine(connectAnchor);
                nwSystem = new NetworkSystem(connectAnchor);
                dataStoreSystem = new DatastoreSystem(connectAnchor);
                storageHelper = new ProvisioningOpsStorageHelper(connectAnchor);
                ins = new NetworkSystem(connectAnchor);
                hostMor = hostSystem.getConnectedHost(null);

        }


        @BeforeMethod
        public boolean testSetUp() throws Exception {
                /*
                 * Init code for all entities in the inventory
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
                 * Query for the opaque network
                 */
                nsMor = ins.getNetworkSystem(hostMor);
                List<HostOpaqueNetworkInfo> opaqueNetworkInfo = ins.
                                getNetworkInfo(nsMor).getOpaqueNetwork();
                assertTrue(opaqueNetworkInfo != null && opaqueNetworkInfo.size() == 2,
                          "The list of opaque networks is not null",
                          "The list of opaque networks is null");

                /*
                 *  Create a default vm spec
                 */
                VirtualMachineConfigSpec vmConfigSpec = null;
            final Vector<String> deviceTypesVector = new Vector<String>();
            ManagedObjectReference poolMor = hostSystem.getResourcePool(hostMor).
                                                     get(0);
            assertTrue(poolMor != null, "The resource pool mor on the host is " +
                               "not null","The resource pool mor on the host is null");
            deviceTypesVector.add(TestConstants.VM_VIRTUALDEVICE_DISK);
            deviceTypesVector.add(VM_VIRTUALDEVICE_SCSI_BUSL_CONTROLLER);
            deviceTypesVector.add(TestConstants.
                                          VM_VIRTUALDEVICE_ETHERNET_PCNET32);
            deviceTypesVector.add(TestConstants.
                                          VM_VIRTUALDEVICE_ETHERNET_E1000);
            deviceTypesVector.add(TestConstants.
                                      VM_VIRTUALDEVICE_ETHERNET_E1000E);
            deviceTypesVector.add(TestConstants.
                                          VM_VIRTUALDEVICE_ETHERNET_VMXNET);
            deviceTypesVector.add(TestConstants.
                                      VM_VIRTUALDEVICE_ETHERNET_VMXNET2);
            deviceTypesVector.add(TestConstants.
                                      VM_VIRTUALDEVICE_ETHERNET_VMXNET3);
            // create the VMCfg with the default devices.
            vmConfigSpec = vm.createVMConfigSpec(poolMor, "Sample-vm-T2",
                          VM_DEFAULT_GUEST_WINDOWS, deviceTypesVector, null);
                List<VirtualDeviceConfigSpec> deviceSpecList =
                                vmConfigSpec.getDeviceChange();
                int i = 0;
                for(VirtualDeviceConfigSpec spec : deviceSpecList){
                        VirtualDevice device = spec.getDevice();
                        if(device instanceof VirtualEthernetCard){
                                VirtualEthernetCard vEthernetDevice =
                                                (VirtualEthernetCard)device;
                                 VirtualEthernetCardOpaqueNetworkBackingInfo opaqueNetworkBackingInfo
                                        = NetworkUtil.createOpaqueNetworkBackingInfo(
                                                        opaqueNetworkInfo.get(0).getOpaqueNetworkId(),
                                                        opaqueNetworkInfo.get(0).getOpaqueNetworkType());
                                vEthernetDevice.setBacking(opaqueNetworkBackingInfo);
                i++;
                        }
                }
                /*
                 * Create the vm in this step
                 */
                 vmMor = folder.createVM(vm.getVMFolder(),
                                 vmConfigSpec, poolMor,
                                 hostMor);
                 return true;
        }

        @Test
        public void test() throws Exception {
                /*
                 * Power on the vm
                 */
                assertTrue(vm.setVMState(vmMor, VirtualMachinePowerState.POWERED_ON,
                                false),"Successfully powered on the virtual machine",
                                "Failed to power on the virtual machine");

                log.info("src vm ip = " + vm.getIPAddress(vmMor));

        }

        @AfterMethod
        public boolean testCleanUp() throws Exception {
                boolean cleanupWorked = true;
                try {
                        if(vmMor != null){
                                /*
                                 * Power off the vm
                                 */
                                assertTrue(vm.setVMState(vmMor, VirtualMachinePowerState.POWERED_OFF,
                                                false),"Successfully powered on the virtual machine",
                                                "Failed to power on the virtual machine");
                                /*
                                 * Destroy the vm
                                 */
                                vm.destroy(vmMor);
                        }
                } catch (Throwable t) {
                        t.printStackTrace();
                        cleanupWorked = false;
                        DVSUtil.testbedTeardown(connectAnchor, true);
                }
                try {
                        DVSUtil.testbedTeardown(connectAnchor, false);
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
