package dvs.nrp.updatenetiorm;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.vc.DVSNetworkResourcePool;
import com.vmware.vc.DVSNetworkResourcePoolConfigSpec;
import com.vmware.vc.InvalidArgument;
import com.vmware.vc.ManagedObjectReference;
import com.vmware.vc.MethodFault;
import com.vmware.vc.SharesLevel;
import com.vmware.vcqa.TestBase;
import com.vmware.vcqa.util.Assert;
import com.vmware.vcqa.vim.DistributedVirtualSwitch;
import com.vmware.vcqa.vim.Folder;
import com.vmware.vcqa.vim.dvs.DVSTestConstants;
import com.vmware.vcqa.vim.dvs.DVSUtil;
import com.vmware.vcqa.vim.host.NetworkResourcePoolHelper;

/**
 * Configure a NRP for vm traffic . Specify the shares as a high value
 */
public class Neg003 extends TestBase
{
   private DistributedVirtualSwitch idvs;
   private Folder ifolder;
   private ManagedObjectReference dvsMor;
   private DVSNetworkResourcePool nrp;
   private DVSNetworkResourcePoolConfigSpec nrpConfigSpec;

   /**
    * Test method Enable Netiorm on the dvs
    */
   @Override
   @Test(description = "Configure a NRP for vm traffic . Specify the shares as a high value")
   public void test()
      throws Exception
   {
      try {
         // update nrp
         idvs.updateNetworkResourcePool(dvsMor,
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
   @Override
   @AfterMethod(alwaysRun=true)
   public boolean testCleanUp()
      throws Exception
   {
      // delete the dvs
      Assert.assertTrue(idvs.destroy(dvsMor), "DVS destroyed",
               "Unable to destroy DVS");
      return true;
   }

   /**
    * Setup method Setup the dvs and attach a host to it.
    */
   @Override
   @BeforeMethod(alwaysRun=true)
   public boolean testSetUp()
      throws Exception
   {
      ifolder = new Folder(connectAnchor);
      idvs = new DistributedVirtualSwitch(connectAnchor);

      // create the dvs
      dvsMor = ifolder.createDistributedVirtualSwitch("dvs",
               DVSUtil.getvDsVersion());

      // enable netiorm
      Assert.assertTrue(idvs.enableNetworkResourceManagement(dvsMor, true),
               "Netiorm not enabled");

      Assert.assertTrue(NetworkResourcePoolHelper.isNrpEnabled(connectAnchor,
               dvsMor), "Nrp is not enabled");

      // Extract the network resource pool related to the vm from the dvs
      nrp = idvs.extractNetworkResourcePool(dvsMor, DVSTestConstants.NRP_VM);

      // set the values in the config spec
      setNrpConfigSpec();

      return true;
   }

   private void setNrpConfigSpec()
   {
      nrpConfigSpec = new DVSNetworkResourcePoolConfigSpec();
      nrpConfigSpec.setKey(nrp.getKey());
      nrpConfigSpec.setName(nrp.getName());
      nrpConfigSpec.setAllocationInfo(nrp.getAllocationInfo());
      nrpConfigSpec.getAllocationInfo().setLimit(new Long(10));
      nrpConfigSpec.getAllocationInfo().getShares().setLevel(SharesLevel.CUSTOM);
      nrpConfigSpec.getAllocationInfo().getShares().setShares(50000);
   }

   /**
    * Test Description
    */
   public void setTestDescription()
   {
      setTestDescription("Configure a NRP for vm traffic . Specify the shares as a high value");
   }

   /**
    * Gets the expected method fault
    */
   @Override
   public MethodFault getExpectedMethodFault()
   {
      return new InvalidArgument();
   }

}
