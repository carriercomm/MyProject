/*
 * ************************************************************************
 *
 * Copyright 2008 VMware, Inc.  All rights reserved. -- VMware Confidential
 *
 * ************************************************************************
 */
package dvs.functional.vmsnapshot;

import static com.vmware.vcqa.vim.dvs.DVSTestConstants.DVPORTGROUP_TYPE_EARLY_BINDING;

import org.testng.annotations.BeforeMethod;

import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.util.TestUtil;

/**
 * Revert a powered off VM to a snapshot when it's VM ethernet adapter was
 * connected to a port on an earlybinding portgroup on the DVS and the port is
 * occupied, and there is a free port in the porgroup for the VM ethernet
 * adapter to connect while reverting the snapshot.
 */
public class Pos005 extends VMSFunctionalTestBase
{
   /**
    * Sets the test description.
    * 
    * @param testDescription the testDescription to set
    */
   public void setTestDescription()
   {
      setTestDescription("Revert a powered off VM to a snapshot when it's VM"
               + " ethernet adapter was connected to a port on an "
               + "earlybinding portgroup on the DVS and the port is"
               + " occupied, and there is a free port in the porgroup"
               + " for the VM ethernet adapter to connect while "
               + "reverting the snapshot.");
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
      boolean setUpDone = false;
      this.portgroupType = DVPORTGROUP_TYPE_EARLY_BINDING;
      this.reusePorts = true;
      this.leaveFreePorts = true;
     
         setUpDone = super.testSetUp();
     
      Assert.assertTrue(setUpDone, "Setup failed");
      return setUpDone;
   }

}