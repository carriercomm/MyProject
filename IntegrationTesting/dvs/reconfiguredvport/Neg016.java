/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.reconfiguredvport;

import static com.vmware.vcqa.util.Assert.*;

import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.ConfigSpecOperation;
import com.vmware.vc.DVPortConfigInfo;
import com.vmware.vc.DVPortConfigSpec;
import com.vmware.vc.DVSConfigSpec;
import com.vmware.vc.DVSFailureCriteria;
import com.vmware.vc.DistributedVirtualPort;
import com.vmware.vc.DistributedVirtualSwitchPortCriteria;
import com.vmware.vc.InvalidArgument;
import com.vmware.vc.MethodFault;
import com.vmware.vc.VMwareDVSPortSetting;
import com.vmware.vc.VmwareUplinkPortTeamingPolicy;
import com.vmware.vcqa.util.TestUtil;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.dvs.DVSUtil;

import dvs.CreateDVSTestBase;

/**
 * Reconfigure DVPort. See setTestDescription for detailed description
 */
public class Neg016 extends CreateDVSTestBase
{
   /*
    * private data variables
    */
   private DistributedVirtualSwitch iDVS = null;
   private DVPortConfigSpec[] portConfigSpecs = null;
   private final int DVS_PORT_NUM = 1;
   private final String INVALID_CHECKSPEED_STR = "invalid";

   /**
    * Sets the test description.
    *
    * @param testDescription the testDescription to set
    */
   @Override
   public void setTestDescription()
   {
      super.setTestDescription("Reconfigure DVPort with invalid failure "
               + "criteria for uplinkTeamingPolicy");
   }

   /**
    * Method to setup the environment for the test.
    *
    * @param connectAnchor ConnectAnchor object
    * @return boolean true if successful, false otherwise.
    */
   @Override
   @BeforeMethod(alwaysRun = true)
   public boolean testSetUp()
      throws Exception
   {
      boolean status = false;
      log.info("Test setup Begin:");
      if (super.testSetUp()) {
         networkFolderMor = iFolder.getNetworkFolder(dcMor);
         if (networkFolderMor != null) {
            iDVS = new DistributedVirtualSwitch(connectAnchor);
            configSpec = createVmwareDVSConfigSpec();
            dvsMOR = iFolder.createDistributedVirtualSwitch(
                     networkFolderMor, configSpec);
            if (dvsMOR != null) {
               log.info("Successfully created the DVSwitch");
               final List<String> portKeyList = iDVS.fetchPortKeys(dvsMOR, null);
               if (portKeyList != null && portKeyList.size() == DVS_PORT_NUM) {
                  portConfigSpecs = new DVPortConfigSpec[DVS_PORT_NUM];
                  portConfigSpecs[0] = createPortConfigSpec(portKeyList.get(0));
                  status = true;
               } else {
                  log.error("Can't get correct port keys");
               }
            } else {
               log.error("Cannot create the distributed virtual "
                        + "switch with the config spec passed");
            }
         } else {
            log.error("Failed to create the network folder");
         }
      }
      assertTrue(status, "Setup failed");
      return status;
   }

   /**
    * Method that creates the DVS.
    *
    * @param connectAnchor ConnectAnchor object
    */
   @Override
   @Test(description = "Reconfigure DVPort with invalid failure "
            + "criteria for uplinkTeamingPolicy")
   public void test()
      throws Exception
   {
      log.info("Test Begin:");
      boolean status = false;
      try {
         iDVS.reconfigurePort(dvsMOR, portConfigSpecs);
         log.error("Exception should thrown in this test case");
         status = false;
      } catch (final Exception actualMethodFaultExcep) {
         MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil.getFault(actualMethodFaultExcep);
         final InvalidArgument expectedMethodFault = new InvalidArgument();
         status = TestUtil.checkMethodFault(actualMethodFault,
                  expectedMethodFault);
      }
      assertTrue(status, "Test Failed");
   }

   /**
    * Method to restore the state as it was before the test is started.
    *
    * @param connectAnchor ConnectAnchor object
    */
   @Override
   @AfterMethod(alwaysRun = true)
   public boolean testCleanUp()
      throws Exception
   {
      boolean status = true;
      status &= super.testCleanUp();
      assertTrue(status, "Cleanup failed");
      return status;
   }

   /**
    *
    */
   private DVSConfigSpec createVmwareDVSConfigSpec()
   {
      final DVSConfigSpec configSpec = new DVSConfigSpec();
      configSpec.setName(this.getClass().getName());
      configSpec.setNumStandalonePorts(DVS_PORT_NUM);
      return configSpec;
   }

   /**
    * create DVPort ConfigSpec with invalid failure criteria
    */
   private DVPortConfigSpec createPortConfigSpec(final String key)
   {
      DistributedVirtualSwitchPortCriteria portCriteria = null;
      DVPortConfigSpec configSpec = new DVPortConfigSpec();
      List<DistributedVirtualPort> dvPorts = null;
      VMwareDVSPortSetting setting = null;
      DVPortConfigInfo dvPortConfigInfo = null;
      DVSFailureCriteria failureCriteria = null;
      VmwareUplinkPortTeamingPolicy uplinkTeamingPolicy = null;
      try {
         portCriteria = new DistributedVirtualSwitchPortCriteria();
         portCriteria.getPortKey().clear();
         portCriteria.getPortKey().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(new String[] { key }));
         dvPorts = iDVS.fetchPorts(dvsMOR, portCriteria);
         if (dvPorts != null && dvPorts.get(0) != null) {
            log.info("Successfully obtained the port");
            dvPortConfigInfo = dvPorts.get(0).getConfig();
            if (dvPortConfigInfo != null) {
               configSpec.setKey(key);
               configSpec.setOperation(ConfigSpecOperation.EDIT.value());
               setting = (VMwareDVSPortSetting) dvPortConfigInfo.getSetting();
               if (setting == null) {
                  setting = DVSUtil.getDefaultVMwareDVSPortSetting(null);
               }
               uplinkTeamingPolicy = setting.getUplinkTeamingPolicy();
               if (uplinkTeamingPolicy == null) {
                  failureCriteria = DVSUtil.getFailureCriteria(false,
                           INVALID_CHECKSPEED_STR, null, null, null, null,
                           null, null);
                  uplinkTeamingPolicy = DVSUtil.getUplinkPortTeamingPolicy(
                           false, null, null, null, null, failureCriteria, null);
               } else {
                  failureCriteria = uplinkTeamingPolicy.getFailureCriteria();
                  failureCriteria.setInherited(false);
                  failureCriteria.setCheckSpeed(DVSUtil.getStringPolicy(false,
                           INVALID_CHECKSPEED_STR));
                  uplinkTeamingPolicy.setInherited(false);
               }
               uplinkTeamingPolicy.setFailureCriteria(failureCriteria);
               setting.setBlocked(DVSUtil.getBoolPolicy(false, new Boolean(
                        false)));
               configSpec.setSetting(setting);
            } else {
               log.error("Failed to obtain the DVPortConfigInfo");
               configSpec = null;
            }
         } else {
            log.error("Failed to obtain the port " + "config spec");
            configSpec = null;
         }
      } catch (final Exception e) {
         TestUtil.handleException(e);
      }
      return configSpec;
   }
}