package cmpe283.project1;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;

public class Vcenter {
	private Alarm pwrOffAlm;
	private List<Vhost> virH;
	private ServiceInstance serIns;
	
	

	public Vcenter(ServiceInstance si) throws Exception {
		this.serIns = si;
		vhostsManager();
		if (virH.size() == 0 && manageConnectedHosts() == null)
			throw new NullPointerException("Host unavailable. ");
	
		pwrOffAlm = AlarmConfig.createPowerAlarm(si);
	}
	
	public void printDetails() throws Exception {
		while (true) {
			for (Vhost host : virH)
				host.print();

			System.out.println("Print VM details after every 5 minutes");
			System.out.println();
			Thread.sleep(300*1000);
		}
	}

	public void heartbeat() throws Exception {
		while (true) {
			System.out.println();
			for (int i = 0; i < virH.size(); i++) {
				
				if (!checkavailablehosts(virH.get(i))) 
				{
					// solution 1: if vhost is unavailable, recover from snapshot
					if (!virH.get(i).machineRecovery())
					{
						// solution 2: if vhost is unavailable, add new host and failover
						Vhost newhost = manageConnectedHosts();
						if (newhost == null)  break; // no available host
					}
					// restart check each vhost in the list
					i = -1;
				}
				
			} 

			System.out.println("Heartbeat will start in 10 seconds");
			System.out.println();
			Thread.sleep(10000);
		}
	}

	public void snapshotCreation() throws Exception {
		while(true) {
			System.out.println();
			for (Vhost host : virH) {
				HostSystem h=host.getHost();
				host.createSnapshot(h);
			}

			System.out.println("Snapshot would be created every 5 minutes");
			System.out.println();
			Thread.sleep(300*1000);
		}
	}

	public Alarm getPowerOffAlarm() {
		return pwrOffAlm;
	}
	
	private void vhostsManager() throws Exception {
		this.virH = new ArrayList<Vhost>();
		Folder vCRF = serIns.getRootFolder();
		ManagedEntity[] vHs = new InventoryNavigator(vCRF).searchManagedEntities("HostSystem");
		if (vHs.length != 0) {
			for (int i = 0; i < vHs.length; i++) {
				this.virH.add(new Vhost((HostSystem) vHs[i]));
			}
			System.out.println("All the hosts are connected.");
		} else {
			System.out.println("Hosts are not connected.");
		}
	}

	private Vhost manageConnectedHosts() throws Exception {
		String Host = findHost();
		if (Host == null) {
			System.out.println("Hosts cannot be detected.");
			return null;
		}
		return checkHost(Host);
	}
	
	private Vhost checkHost(String hName) throws Exception {
		if (virH != null) {
			for (Vhost host: virH) {
				if (hName.equals(host.getIP())) return host;
			}
		}		return null;
	}

	private String findHost() throws Exception {
		for (String ip : StringConstants.Vhosts.keySet()) {
			if (Ping.pingIP(ip)) {
				System.out.println(ip + " VHost is available.");
				return ip;
			}
		}
		return null;
	}

	private boolean checkavailablehosts(Vhost vh) throws Exception {
		vh.setVMs();
		List<Vmachine> vms = vh.getVMs();
		if (vms == null) return true;
		
		for (int i = 0; i < vms.size(); i++) {
			Vmachine vm = vms.get(i);
			
			if (vm.checkPowerOffAlarm(pwrOffAlm)) {
				System.out.println(vm.getVM().getName() + " VM is powered off manually.");
				continue;
			}
			
			if (vm.ping()) {
				System.out.println(vm.getVM().getName() + " VM is available.");
				continue;
			}

			System.out.println(vm.getVM().getName() + " VM is not reachable.");

			// vm failure, vhost ping success, recover vm to last state
			if (vh.ping()) {
				System.out.println(vh.getIP() + ": Vhost is available.");
				// recover vm by snapshot
				SnapshotsController.revert2PreviousVMSnapshot(vm);
				vm.powerOn();
				while(vm.getIP() == null);
				i--;
			} else {
				System.out.println(vh.getIP() + ": Vhost pinging failed.");
				return false;
			}
		}
		return true;
	}
}
