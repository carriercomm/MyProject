package dvs.reconfiguredvs.portmirror;

import static com.vmware.vcqa.TestConstants.*;
import static com.vmware.vcqa.util.Assert.*;
import static com.vmware.vcqa.vim.MessageConstants.*;

import java.util.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.vmware.vc.VMwareDVSVspanConfigSpec;
import com.vmware.vc.VMwareVspanSession;
import com.vmware.vcqa.TestConstants;
import com.vmware.vcqa.execution.TestExecutionUtils;
import com.vmware.vcqa.util.MultiMap;
import com.vmware.vcqa.vim.dvs.VspanHelper;

public class Pos005 extends PortMirrorTestBase {
	VMwareVspanSession[] existingSessions;
	final String sessionTpye = "remoteMirrorDest";

	@Factory
	@Parameters( { "dataFile" })
	public Object[] getTests(@Optional("") final String dataFile)
			throws Exception {
		return TestExecutionUtils.getTests(this.getClass().getName(), dataFile);
	}

	@BeforeMethod(alwaysRun = true)
	@Override
	public boolean testSetUp() throws Exception {
		initialize();
		getProperties();
		hostMor = hs.getConnectedHost(null);
		assertNotNull(hostMor, HOST_GET_FAIL);
		dvsMor = createDVSWithNic(dvsName, hostMor);
		setupPortgroups(dvsMor); // "portGroups" variant is ready.
		setupVMs(dvsMor);
		return true;
	}

	@Test(description = "Test session type is \"remoteMirrorDest\"")
	@Override
	public void test() throws Exception {
		String[] tmps = srcRxVlan.split("-");
		int len = tmps.length;
		Integer srcRxvlans[] = new Integer[len];
		for (int i = 0; i < len; i++) {
			srcRxvlans[i] = Integer.parseInt(tmps[i]);
		}
		VMwareDVSVspanConfigSpec[] vspanConfigSpecs = new VMwareDVSVspanConfigSpec[1];
		VMwareDVSVspanConfigSpec m_newSpecs = VspanHelper
				.getRawVMwareDVSVspanConfigSpec(sessionTpye);
		assertTrue(this.setVspanPorts(m_newSpecs, portGroups),
				"error occured when setup mirror ports");
		m_newSpecs.getVspanSession().setName(this.getTestId());
		m_newSpecs.getVspanSession().setEnabled(this.enabled);
		m_newSpecs.getVspanSession().setNormalTrafficAllowed(
				this.normalTrafficAllowed);
		m_newSpecs.getVspanSession().setMirroredPacketLength(
				this.mirrorPacketLength);
		m_newSpecs.getVspanSession().setSamplingRate(this.samplingRate);
		m_newSpecs.getVspanSession().setSessionType(sessionTpye);
		m_newSpecs.getVspanSession().getSourcePortReceived().getVlans().clear();
      m_newSpecs.getVspanSession().getSourcePortReceived().getVlans().addAll(
				com.vmware.vcqa.util.TestUtil.arrayToVector(srcRxvlans));
		m_newSpecs.setOperation(TestConstants.CONFIG_SPEC_ADD);
		vspanConfigSpecs[0] = m_newSpecs;
		assertTrue(this.reconfigureVspan(dvsMor, vspanConfigSpecs, true),
				"Failed to ADD Vspan sessions");
	}

	@AfterMethod(alwaysRun = true)
	@Override
	public boolean testCleanUp() throws Exception {
		boolean done = true;
		done &= cleanupVMs();
		log.info("Destroying the DVS: {} ", dvsName);
		done &= this.destroy(dvsMor);
		return done;
	}

	protected void getProperties() {
		normalTrafficAllowed = data.getBoolean(NORMAL_TRAFFIC_ALLOWED);
		enabled = data.getBoolean(ENABLED);
		mirrorPacketLength = data.getInt(MIRROR_PKT_LENGTH);
		samplingRate = data.getInt(SAMPLINGRATE);
		srcRxVlan = data.getString(SRCRX_VLANS);// src rx ports vlans
		log.info(">>> Finish the data read >>> ");
	}

	/**
	 * set the portkeys.
	 */
	protected boolean setVspanPorts(VMwareDVSVspanConfigSpec m_cfg,
			final MultiMap<String, String> pg) {
		final Collection<String> keys = pg.keySet();
		if (keys.size() < 3) {
			log
					.error("Don't have enough ports to do the dvPortMirror session type testing");
			return false;
		}
		Iterator<String> m_i;
		m_i = keys.iterator();
		String pgName = m_i.next();
		/* only rx vlan take effects */
		Integer[] vlans = new Integer[2];
		vlans[0] = 0;
		vlans[1] = 4094;
		m_cfg.getVspanSession().getSourcePortReceived().getVlans().clear();
      m_cfg.getVspanSession().getSourcePortReceived().getVlans().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(vlans));
		/* set the dst ports */
		pgName = m_i.next();
		List<String> l = pg.get(pgName);
		usedPortKeys.addAll(l);
		String[] dstports = new String[l.size()];
		l.toArray(dstports);
		m_cfg.getVspanSession().getDestinationPort().getPortKey().clear();
      m_cfg.getVspanSession().getDestinationPort().getPortKey().addAll(com.vmware.vcqa.util.TestUtil.arrayToVector(dstports));
		return true;
	}
}
