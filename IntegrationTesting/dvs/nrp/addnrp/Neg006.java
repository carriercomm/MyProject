/*
 * ************************************************************************
 *
 * Copyright 2010 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.nrp.addnrp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.vmware.vc.DVSNetworkResourcePoolConfigSpec;
import com.vmware.vc.InvalidArgument;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vcqa.IDataDrivenTest;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.NetworkResourcePoolHelper;

/**
 * DESCRIPTION:Add a nrp with invalid pTag values<BR>
 * TARGET:VC<BR>
 * SETUP:<BR>
 * 1.create the dvs<BR>
 * 2.enable netiorm<BR>
 * 3.set the values in the config spec<BR>
 * TEST:<BR>
 * 4.add nrp<BR>
 * CLEANUP:<BR>
 * 5.Destroy dvs<BR>
 */
public class Neg006 extends TestBase implements IDataDrivenTest
{
   private DistributedVirtualSwitch dvs;
   private ManagedObjectReference dvsMor;

   @Factory
   @Parameters( { "dataFile" })
   public Object[] getTests(@Optional("") String dataFile)
      throws Exception
   {
      return TestExecutionUtils.getTests(this.getClass().getName(), dataFile);
   }

   public String getTestName()
   {
      return getTestId();
   }

   /**
    * Setup method Setup the dvs and attach a host to it.
    */
   @BeforeMethod(alwaysRun = true)
   public boolean testSetUp()
      throws Exception
   {
      Folder folder = new Folder(connectAnchor);
      dvs = new DistributedVirtualSwitch(connectAnchor);

      // create the dvs
      dvsMor = folder.createDistributedVirtualSwitch(getTestId(),
               DVSUtil.getvDsVersion());

      // enable netiorm
      Assert.assertTrue(dvs.enableNetworkResourceManagement(dvsMor, true),
               "Netiorm not enabled");

      Assert.assertTrue(NetworkResourcePoolHelper.isNrpEnabled(connectAnchor,
               dvsMor), "NRP not enabled");

      return true;
   }

   /**
    * Test method Enable Netiorm on the dvs
    */
   @Test(description = "Add a nrp with invalid pTag values")
   public void test()
      throws Exception
   {
      try {
         DVSNetworkResourcePoolConfigSpec nrpConfigSpec = NetworkResourcePoolHelper.createDefaultNrpSpec();
         Integer ptag = data.getInt(DVSTestConstants.NRP_PTAG);
         log.info("Ptag::" + ptag);
         nrpConfigSpec.getAllocationInfo().setPriorityTag(ptag);
         // add nrp
         dvs.addNetworkResourcePool(dvsMor,
                  new DVSNetworkResourcePoolConfigSpec[] { nrpConfigSpec });
         com.vmware.vcqa.util.Assert.assertTrue(false, "No Exception Thrown!");
      } catch (Exception excep) {
         com.vmware.vc.MethodFault actualMethodFault = com.vmware.vcqa.util.TestUtil.getFault(excep);
         com.vmware.vc.MethodFault expectedMethodFault = new InvalidArgument();
         com.vmware.vcqa.util.Assert.assertTrue(
                  com.vmware.vcqa.util.TestUtil.checkMethodFault(
                           actualMethodFault, expectedMethodFault),
                  "MethodFault mismatch!");
      }
   }

   /**
    * Cleanup method Destroy the dvs
    */
   @AfterMethod(alwaysRun = true)
   public boolean testCleanUp()
      throws Exception
   {
      // delete the dvs
      Assert.assertTrue(dvs.destroy(dvsMor), "DVS destroyed",
               "Unable to destroy DVS");

      return true;
   }

   /**
    * Test Description
    */
   public void setTestDescription()
   {
      setTestDescription("Add a nrp with invalid pTag values");
   }
}
