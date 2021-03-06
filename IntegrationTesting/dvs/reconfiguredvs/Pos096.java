/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfiguredvs;

import static com.vmware.vcqa.util.Assert.assertTrue;

import java.util.Vector;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSConfigInfo;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DVSFailureCriteria;
import com.vmware.vc.VMwareDVSPortSetting;
import com.vmware.vc.VMwareUplinkPortOrderPolicy;
import com.vmware.vc.VmwareUplinkPortTeamingPolicy;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;

import dvs.CreateDVSTestBase;

/**
 * Reconfigure an existing DVSwitch.Set the teaming policy as LBT (Dynamic Load
 * Balancer in NIC Teaming)
 */
public class Pos096 extends CreateDVSTestBase
{

   /*
    * private data variables
    */
   private DistributedVirtualSwitch iDistributedVirtualSwitch = null;
   private DVSConfigSpec deltaConfigSpec = null;

   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      super.setTestDescription("Reconfigure an existing DVSwitch."
               + "Set the teaming policy as LBT(Dynamic Load Balancer in NIC"
               + " Teaming");
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
      DVSFailureCriteria failureCriteria = null;
      VmwareUplinkPortTeamingPolicy uplinkTeamingPolicy = null;
      VMwareUplinkPortOrderPolicy portOrderPolicy = null;
      String[] activePorts = new String[] { "uplink1", "uplink2" };
      String[] standByPorts = new String[] { "uplink3", "uplink4" };
      log.info("Test setup Begin:");
      DVSConfigInfo configInfo = null;
      status = super.testSetUp();
      this.networkFolderMor = this.iFolder.getNetworkFolder(this.dcMor);
      if (this.networkFolderMor != null) {
         this.iDistributedVirtualSwitch = new DistributedVirtualSwitch(
                  connectAnchor);
         this.configSpec = new DVSConfigSpec();
         this.configSpec.setName(this.getClass().getName());
         this.dvsMOR = this.iFolder.createDistributedVirtualSwitch(
                  this.networkFolderMor, this.configSpec);
         if (this.dvsMOR != null) {
            log.info("Successfully created the DVSwitch");
            this.deltaConfigSpec = new DVSConfigSpec();
            configInfo = this.iDistributedVirtualSwitch.getConfig(this.dvsMOR);
            if (configInfo.getDefaultPortConfig() instanceof VMwareDVSPortSetting) {
               VMwareDVSPortSetting portSetting = (VMwareDVSPortSetting) configInfo.getDefaultPortConfig();
               uplinkTeamingPolicy = portSetting.getUplinkTeamingPolicy();
               if (uplinkTeamingPolicy == null) {
                  failureCriteria = DVSUtil.getFailureCriteria(false, "exact",
                           new Integer(50), true, true, true, new Integer(10),
                           false);
                  portOrderPolicy = DVSUtil.getPortOrderPolicy(false,
                           activePorts, standByPorts);
                  uplinkTeamingPolicy = DVSUtil.getUplinkPortTeamingPolicy(
                           false, "loadbalance_loadbased", true, true, true,
                           failureCriteria, portOrderPolicy);
               } else {
                  failureCriteria = uplinkTeamingPolicy.getFailureCriteria();
                  failureCriteria.setInherited(false);
                  failureCriteria.setCheckSpeed(DVSUtil.getStringPolicy(false,
                           "exact"));
                  failureCriteria.setCheckBeacon(DVSUtil.getBoolPolicy(false,
                           true));
                  failureCriteria.setCheckDuplex(DVSUtil.getBoolPolicy(false,
                           true));
                  failureCriteria.setCheckErrorPercent(DVSUtil.getBoolPolicy(
                           false, true));
                  failureCriteria.setCheckSpeed(DVSUtil.getStringPolicy(false,
                           "exact"));
                  failureCriteria.setFullDuplex(DVSUtil.getBoolPolicy(false,
                           true));
                  failureCriteria.setPercentage(DVSUtil.getIntPolicy(false, 10));
                  failureCriteria.setSpeed((DVSUtil.getIntPolicy(false, 50)));
                  portOrderPolicy = uplinkTeamingPolicy.getUplinkPortOrder();
                  portOrderPolicy.setInherited(false);
                  portOrderPolicy.getActiveUplinkPort().clear();
                  portOrderPolicy.getActiveUplinkPort().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(activePorts));
                  portOrderPolicy.getStandbyUplinkPort().clear();
                  portOrderPolicy.getStandbyUplinkPort().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(standByPorts));
                  uplinkTeamingPolicy.setFailureCriteria(failureCriteria);
                  uplinkTeamingPolicy.setNotifySwitches(DVSUtil.getBoolPolicy(
                           false, true));
                  uplinkTeamingPolicy.setPolicy(DVSUtil.getStringPolicy(false,
                           "loadbalance_loadbased"));
                  uplinkTeamingPolicy.setReversePolicy(DVSUtil.getBoolPolicy(
                           false, true));
                  uplinkTeamingPolicy.setRollingOrder(DVSUtil.getBoolPolicy(
                           false, true));
                  uplinkTeamingPolicy.setUplinkPortOrder(portOrderPolicy);

               }
               String validConfigVersion = configInfo.getConfigVersion();
               this.deltaConfigSpec.setConfigVersion(validConfigVersion);
               portSetting.setBlocked(DVSUtil.getBoolPolicy(false, false));
               portSetting.setUplinkTeamingPolicy(uplinkTeamingPolicy);
               this.deltaConfigSpec.setDefaultPortConfig(portSetting);
               status = true;
            }
         } else {
            log.error("Cannot create the distributed virtual "
                     + "switch with the config spec passed");
         }
      } else {
         log.error("Failed to create the network folder");
      }

      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that creates the DVS.
    * 
    * @param connectAnchor ConnectAnchor object
    */
   @Test(description = "Reconfigure an existing DVSwitch."
               + "Set the teaming policy as LBT(Dynamic Load Balancer in NIC"
               + " Teaming")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      assertTrue(this.iDistributedVirtualSwitch.reconfigure(this.dvsMOR,
               this.deltaConfigSpec), "Successfully reconfigured the vds",
               "Failed to reoconfigure the vds");
      Vector<String> ignoredProperties = TestUtil.getIgnorePropertyList(
               this.deltaConfigSpec, false);
      if (!ignoredProperties.contains(DVSTestConstants.DVS_CONFIGVERSION)) {
         ignoredProperties.add(DVSTestConstants.DVS_CONFIGVERSION);
      }
      assertTrue(TestUtil.compareObject(
               iDistributedVirtualSwitch.getConfigSpec(dvsMOR),
               this.deltaConfigSpec, ignoredProperties), "The teaming "
               + "policy has been reconfigured on the vds",
               "The teaming policy " + "has not been set on the vds");
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
      return super.testCleanUp();
   }
}