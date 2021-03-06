/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.updatevirtualnic;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchHostMemberConfigSpec;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicBacking;
import com.vmware.vc.DistributedVirtualSwitchHostMemberPnicSpec;
import com.vmware.vc.DistributedVirtualSwitchPortConnection;
import com.vmware.vc.DistributedVirtualSwitchPortCriteria;
import com.vmware.vc.HostHostBusAdapter;
import com.vmware.vc.HostInternetScsiHba;
import com.vmware.vc.HostInternetScsiHbaAuthenticationProperties;
import com.vmware.vc.HostNetworkConfig;
import com.vmware.vc.HostStorageDeviceInfo;
import com.vmware.vc.HostSystemConnectionState;
import com.vmware.vc.HostVirtualNic;
import com.vmware.vc.HostVirtualNicConfig;
import com.vmware.vc.HostVirtualNicSpec;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.util.VersionConstants;
import com.vmware.vcqa.vim.host.StorageConstants;
import com.vmware.vcqa.vim.host.StorageSystem;

import dvs.VNicBase;

/**
 * Update a existing vnic to connect to an existing dv switch Update the
 * Authentication properties valid hba id and chap Auth disabled and chap name
 * and chap secret are null.
 */
public class Pos012 extends VNicBase
{
   private String dvSwitchUuid = null;
   private HostVirtualNicSpec origVnicSpec = null;
   private String vNicdevice = null;
   private String portKey = null;
   private ManagedObjectReference storageSystemMor = null;
   private HostInternetScsiHbaAuthenticationProperties orgAuthenticationProperties = null;
   private String iscsiHbaId = null;
   private boolean isScsiAuthPropertiesChanged = false;
   private StorageSystem iscsi = null;

   /**
    * Set test description.
    */
   @Override
   public void setTestDescription()
   {
      setTestDescription("Update a existing vnic to connect to an existing"
               + " dvswitch\n"
               + "Update the Authentication properties valid hba id and "
               + "chap Auth disabled and chap name and chap secret are null.");
   }

   /**
    * Method to setup the environment for the test.
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
      DVSConfigSpec dvsConfigSpec = null;
      List<String> portKeys = null;
      DistributedVirtualSwitchHostMemberConfigSpec hostMember = null;
      DistributedVirtualSwitchHostMemberPnicBacking pnicBacking = null;
      DistributedVirtualSwitchHostMemberPnicSpec pnicSpec = null;
      DistributedVirtualSwitchPortCriteria portCriteria = null;
      HashMap allHosts = null;
      log.info("test setup Begin:");
      if (super.testSetUp()) {
        
            allHosts = ihs.getAllHosts(VersionConstants.ESX4x, HostSystemConnectionState.CONNECTED);
            Set hostsSet = allHosts.keySet();
            if ((hostsSet != null) && (hostsSet.size() > 0)) {
               Iterator hostsItr = hostsSet.iterator();
               if (hostsItr.hasNext()) {
                  hostMor = (ManagedObjectReference) hostsItr.next();
               }
            }
            if (hostMor != null) {
               iscsi = new StorageSystem(connectAnchor);
               storageSystemMor = iscsi.getStorageSystem(hostMor);
               iscsi = new StorageSystem(connectAnchor);
               /*
                * Check for free Pnics
                */
               String[] freePnics = ins.getPNicIds(this.hostMor);
               if ((freePnics != null) && (freePnics.length > 0)) {
                  nwSystemMor = ins.getNetworkSystem(hostMor);
                  if (nwSystemMor != null) {
                     hostMember = new DistributedVirtualSwitchHostMemberConfigSpec();
                     hostMember.setOperation(TestConstants.CONFIG_SPEC_ADD);
                     hostMember.setHost(this.hostMor);
                     pnicBacking = new DistributedVirtualSwitchHostMemberPnicBacking();
                     pnicSpec = new DistributedVirtualSwitchHostMemberPnicSpec();
                     pnicSpec.setPnicDevice(freePnics[0]);
                     pnicBacking.getPnicSpec().clear();
                     pnicBacking.getPnicSpec().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberPnicSpec[] { pnicSpec }));
                     hostMember.setBacking(pnicBacking);
                     dvsConfigSpec = new DVSConfigSpec();
                     dvsConfigSpec.setConfigVersion("");
                     dvsConfigSpec.setName(this.getTestId());
                     dvsConfigSpec.setNumStandalonePorts(1);
                     dvsConfigSpec.getHost().clear();
                     dvsConfigSpec.getHost().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new DistributedVirtualSwitchHostMemberConfigSpec[] { hostMember }));
                     this.dvsMor = this.iFolder.createDistributedVirtualSwitch(
                              this.iFolder.getNetworkFolder(this.iFolder.getDataCenter()),
                              dvsConfigSpec);
                     if ((this.dvsMor != null)
                              && this.ins.refresh(this.nwSystemMor)
                              && this.iDVSwitch.validateDVSConfigSpec(
                                       this.dvsMor, dvsConfigSpec, null)) {
                        log.info("Successfully created the distributed "
                                 + "virtual switch");
                        /*
                         * Get existing vnics
                         */
                        HostNetworkConfig nwCfg = ins.getNetworkConfig(nwSystemMor);
                        if ((nwCfg != null) && (com.vmware.vcqa.util.TestUtil.vectorToArray(nwCfg.getVnic(), com.vmware.vc.HostVirtualNicConfig.class) != null)
                                 && (com.vmware.vcqa.util.TestUtil.vectorToArray(nwCfg.getVnic(), com.vmware.vc.HostVirtualNicConfig.class).length > 0)
                                 && (com.vmware.vcqa.util.TestUtil.vectorToArray(nwCfg.getVnic(), com.vmware.vc.HostVirtualNicConfig.class)[0] != null)) {
                           HostVirtualNicConfig vnicConfig = com.vmware.vcqa.util.TestUtil.vectorToArray(nwCfg.getVnic(), com.vmware.vc.HostVirtualNicConfig.class)[0];
                           this.origVnicSpec = vnicConfig.getSpec();
                           vNicdevice = vnicConfig.getDevice();
                           log.info("VnicDevice : " + vNicdevice);
                           portCriteria = this.iDVSwitch.getPortCriteria(false,
                                    null, null, null, null, false);
                           portCriteria.setUplinkPort(false);
                           portKeys = this.iDVSwitch.fetchPortKeys(dvsMor,
                                    portCriteria);
                           if ((portKeys != null) && (portKeys.size() > 0)) {
                              this.portKey = portKeys.get(0);
                           }
                           if (storageSystemMor != null) {
                              HostStorageDeviceInfo storageCfg = iscsi.getStorageInfo(storageSystemMor);
                              if (storageCfg != null) {
                                 HostHostBusAdapter[] hbas = com.vmware.vcqa.util.TestUtil.vectorToArray(storageCfg.getHostBusAdapter(), com.vmware.vc.HostHostBusAdapter.class);
                                 if (hbas != null) {
                                    for (int i = 0; i < hbas.length; i++) {
                                       if (hbas[i] instanceof HostInternetScsiHba) {
                                          iscsiHbaId = ((HostInternetScsiHba) hbas[i]).getDevice();
                                          orgAuthenticationProperties = ((HostInternetScsiHba) hbas[i]).getAuthenticationProperties();
                                          log.info("iScsiHba found = "
                                                   + iscsiHbaId);

                                          if (orgAuthenticationProperties.isChapAuthEnabled()) {
                                             log.info("Chap is already enabled no need to do anything is setup");
                                             status = true;
                                          } else {
                                             log.info("Chap is disabled need to do enable the same");

                                             HostInternetScsiHbaAuthenticationProperties tempAuthProperties = new HostInternetScsiHbaAuthenticationProperties();

                                             tempAuthProperties.setChapSecret("xyz");
                                             tempAuthProperties.setChapAuthEnabled(true);
                                             tempAuthProperties.setChapAuthenticationType(StorageConstants.CHAP_AUTH_TYPE_PREFERRED);
                                             tempAuthProperties.setChapName("iqn.1998-01.com.vmware:mckinley135-501a3d96xxxx");
                                             if (iscsi.updateInternetScsiAuthenticationProperties(
                                                      storageSystemMor,
                                                      iscsiHbaId,
                                                      tempAuthProperties)) {
                                                log.info("Chap enabled");
                                                status = true;
                                                isScsiAuthPropertiesChanged = true;
                                             } else {
                                                log.info("Failed to enable chap");
                                             }
                                          }
                                          break;
                                       }
                                    }

                                 } else {
                                    log.error("Unable to find HostHostBusAdapter");
                                 }
                              } else {
                                 log.error("Unable to find storageCfg");
                              }
                           } else {
                              log.error("Unable to find the storage system mor");
                           }

                        } else {
                           log.error("Unable to find valid Vnic");
                        }
                     } else {
                        log.error("Unable to create DistributedVirtualSwitch");
                     }
                  } else {
                     log.error("The network system Mor is null");
                  }
               } else {
                  log.error("Unable to get free pnics");
               }

            } else {
               log.error("Unable to find the host.");
            }
        
      }
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Test.
    * 
    * @param connectAnchor ConnectAnchor.
    */
   @Override
   @Test(description = "Update a existing vnic to connect to an existing"
               + " dvswitch\n"
               + "Update the Authentication properties valid hba id and "
               + "chap Auth disabled and chap name and chap secret are null.")
   public void test()
      throws Exception
   {
      boolean status = false;
      DistributedVirtualSwitchPortConnection portConnection = null;
      HostVirtualNic vNic = null;
      HostVirtualNicSpec updatedVNicSpec = null;
      log.info("test setup Begin:");
     
         DVSConfigInfo info = iDVSwitch.getConfig(dvsMor);
         dvSwitchUuid = info.getUuid();
         portConnection = new DistributedVirtualSwitchPortConnection();
         portConnection.setSwitchUuid(dvSwitchUuid);
         portConnection.setPortKey(this.portKey);
         if (portConnection != null) {
            updatedVNicSpec = (HostVirtualNicSpec) TestUtil.deepCopyObject(this.origVnicSpec);
            updatedVNicSpec.setDistributedVirtualPort(portConnection);
            updatedVNicSpec.setPortgroup(null);
            if (ins.updateVirtualNic(nwSystemMor, vNicdevice, updatedVNicSpec)) {
               log.info("Successfully updated VirtualNic " + vNicdevice);
               HostInternetScsiHbaAuthenticationProperties tempAuthProperties = new HostInternetScsiHbaAuthenticationProperties();

               tempAuthProperties.setChapAuthEnabled(false);
               tempAuthProperties.setMutualChapAuthenticationType("chapProhibited");
               if (iscsi.updateInternetScsiAuthenticationProperties(
                        storageSystemMor, iscsiHbaId, tempAuthProperties)) {
                  log.info("Authentication properties updated successfully");
                  status = true;
                  isScsiAuthPropertiesChanged = false;
               } else {
                  log.info("Failed to update authentication properties");
               }
            } else {
               log.info("Unable to update VirtualNic " + vNicdevice);
               status = false;
            }
         } else {
            status = false;
            log.error("can not get a free port on the dvswitch");
         }
     
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test is started. 1.
    * Migrate the VM back to Source host. 3. Remove the vNic and DVSMor.
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
            if (isScsiAuthPropertiesChanged) {
               if (iscsi.updateInternetScsiAuthenticationProperties(
                        storageSystemMor, iscsiHbaId,
                        orgAuthenticationProperties)) {
                  log.info("Successfully updated to original authentication properties");
               } else {
                  status = false;
                  log.info("Failed to update original authentication properties");
               }
            }
         } catch (Exception ex) {
            TestUtil.handleException(ex);
            status = false;
         }
         try {

            if (this.origVnicSpec != null) {
               if (ins.updateVirtualNic(nwSystemMor, vNicdevice, origVnicSpec)) {
                  log.info("Successfully restored original VirtualNic "
                           + "config: " + vNicdevice);
               } else {
                  log.info("Unable to update VirtualNic " + vNicdevice);
                  status = false;
               }
            }
         } catch (Exception e) {
            status = false;
            TestUtil.handleException(e);
         }

         status &= super.testCleanUp();
     
      assertTrue(status, "Cleanup failed");
      return status;
   }
}
