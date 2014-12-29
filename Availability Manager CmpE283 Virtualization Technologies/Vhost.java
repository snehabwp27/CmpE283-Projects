package cmpe283.project1;

import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class Vhost {

	private HostSystem host;
	private List<Vmachine> vms;

	public Vhost(HostSystem host) throws Exception {
		this.host = host;
		setVMs();
	}

	public boolean machineRecovery() throws Exception {
		if (SnapshotsController.revert2PreviousVHSnapshot(this)) {
			System.out.println("Host is recovered from snapshot. ");			
			return reconnect();
		}
		return false;
	}

	public void createSnapshot(HostSystem host) throws Exception {
		if (vms == null)
			return;
		SnapshotsController.createVHSnapshot(host);
		for (int i = 0; i < vms.size(); i++)			
			SnapshotsController.createVMSnapshot(vms.get(i));
		
	}

	// reconnect host
		public boolean reconnect() throws Exception {
			int n = 0;
			//while (n < StringConstants.HostConnectRetries)
			while (n < 4){
				n++;
				Task task = host.reconnectHost_Task(null);
				System.out.println("Reconnecting host " + host.getName() + "......");
				Thread.sleep(1000);
				
				if (task.waitForTask() == Task.SUCCESS) {
					System.out.println("Reconnecting host succeeded");
					for (Vmachine vm : vms) {
						vm.powerOn();
						while(vm.getIP() == null);
					}
					return true;
				} else {
					System.out.println(task.getTaskInfo().getError()
							.getLocalizedMessage());
				}
			}
			return false;
		}
		
	// disconnect host
	public void disconnect() throws Exception {
		Task task = host.disconnectHost();
		System.out.println("Disconnecting host " + host.getName() + "......");

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Disconnecting host succeeded");
		} else {
			System.out.println(task.getTaskInfo().getError()
					.getLocalizedMessage());
		}
	}

	// remove host
	public void remove() throws Exception {
		System.out.println("Removing host " + host.getName() + "......");
		
		ComputeResource cr = (ComputeResource) host.getParent();
		Task task = cr.destroy_Task();

		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Removing host success");
		} else {
			System.out.println(task.getTaskInfo().getError()
					.getLocalizedMessage());
		}
	}

	public boolean ping() throws Exception {
		int time = 0;
		while (!Ping.pingIP(getIP())) {						
			time++;
		//	if (time >= StringConstants.PingRetries)
			if (time >= 4)
				return false;
			else 
//				Thread.sleep(StringConstants.PingInterval * 1000);
				Thread.sleep(3000);
		}
		return true;
	}

	public HostSystem getHost() {
		return host;
	}

	public void setVMs() throws Exception {
		vms = new ArrayList<Vmachine>();
		
		ManagedEntity[] mes = new InventoryNavigator(host).searchManagedEntities("VirtualMachine");
		if (mes == null) return;	
		
		for (int i = 0; i < mes.length; i++) {
			vms.add(new Vmachine((VirtualMachine) mes[i]));
		}
	}

	public List<Vmachine> getVMs() {
		return vms;
	}

	public String getIP() {
		return host.getConfig().getNetwork().getVnic()[0].getSpec().getIp()
				.getIpAddress();
	}

	public void print() throws Exception {
		System.out.println();
		System.out.println("vHost: " + getIP());
		System.out.println("--VMs--");

		if (vms == null)
			return;
		for (int i = 0; i < vms.size(); i++)
			vms.get(i).print();
		;
		System.out.println("------");
	}
}
