package cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

public class SnapshotsController {

	public static void createVMSnapshot(Vmachine vm) throws Exception {
		if (!Ping.pingIP(vm.getIP())) {
			System.out.println("Not able to ping " + vm.getName() + " VM. Hence Snapshot creation failed.");
			return;
		}
		Task task1 = vm.getVM().removeAllSnapshots_Task();
		if (task1.waitForTask() == Task.SUCCESS) {
			System.out.println("Removed all snapshots for " + vm.getName());
		}
		System.out.println("Ping is successfull. Creating snapshot for " + vm.getName() + " VM");
		String snapshotname = vm.getName() + " VM-SnapShot";
		String desc = "A snapshot of " + vm.getName() + " VM";
		Task task = vm.getVM().createSnapshot_Task(snapshotname, desc, false, false);
		if (task.waitForTask() == Task.SUCCESS)
			System.out.println(snapshotname + " created.");
		else
			System.out.println(snapshotname + " creation failed.");
	}

	public static void createVHSnapshot(HostSystem host) throws Exception {
		ServiceInstance siAdminVCenter = new ServiceInstance(new URL(StringConstants.AdminUrl),
				StringConstants.UserName, StringConstants.Password, true);
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(siAdminVCenter.getRootFolder())
		.searchManagedEntity("VirtualMachine", StringConstants.VMs.get(host.getName()));
		if (Ping.pingIP(host.getName())) {
			Vmachine vm1=new Vmachine(vm);
			/*Task task1 = vm1.getVM().removeAllSnapshots_Task();
			if (task1.waitForTask() == Task.SUCCESS) {
				System.out.println("Removed all snapshots for " + vm1.getName());
			}*/
			System.out.println("Successfully pinged. Creating snapshot for " + vm.getName() + " VHost");
			String snapshotname = vm.getName() + " VHost-SnapShot";
			String desc = "A snapshot of " + vm.getName();

			Task task = vm.createSnapshot_Task(snapshotname, desc, false, false);
			if (task.waitForTask() == Task.SUCCESS)
				System.out.println(snapshotname + " created.");
			else
				System.out.println(snapshotname + " creation failed.");
		} else {
			System.out.println("Not able to ping " + vm.getName() + " VHost. Hence Snapshot creation failed.");
		}
		
		siAdminVCenter.getServerConnection().logout();
	}
	
	public static boolean revert2PreviousVMSnapshot(Vmachine vm) throws Exception {
		Task task = vm.getVM().revertToCurrentSnapshot_Task(null);
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println(vm.getName() + " recovered from last snapshot.");
			return true;
		} else {
			System.out.println(vm.getName() + " failed recovery.");
			return false;
		}
	}
	
	public static boolean revert2PreviousVHSnapshot(Vhost vhost) throws Exception {
		ServiceInstance superVCenter = new ServiceInstance(new URL(StringConstants.AdminUrl),
				StringConstants.UserName, StringConstants.Password, true);
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(superVCenter.getRootFolder())
		.searchManagedEntity("VirtualMachine", StringConstants.VMs.get(vhost.getIP()));
		Vmachine v = new Vmachine(vm); 
		boolean res = revert2PreviousVMSnapshot(v);
		v.powerOn();
		
		superVCenter.getServerConnection().logout();
		return res;
	}
	
	public static void revertSnapshot(Vmachine vm, String snapshotname) throws Exception {
		VirtualMachineSnapshot vmsnap = getSnapshotInTree(vm, snapshotname);
		if (vmsnap != null) {
			Task task = vmsnap.revertToSnapshot_Task(null);
			if (task.waitForTask() == Task.SUCCESS) {
				System.out.println(vm.getName() + " reverted to snapshot:"
						+ snapshotname);
			}
		}
	}

	private static VirtualMachineSnapshot getSnapshotInTree(Vmachine vm,
			String snapName) {
		if (vm == null || snapName == null) {
			return null;
		}

		VirtualMachineSnapshotTree[] snapTree = vm.getVM().getSnapshot()
				.getRootSnapshotList();
		if (snapTree != null) {
			ManagedObjectReference mor = findSnapshotInTree(snapTree, snapName);
			if (mor != null) {
				return new VirtualMachineSnapshot(vm.getVM().getServerConnection(), mor);
			}
		}
		return null;
	}

	private static ManagedObjectReference findSnapshotInTree(
			VirtualMachineSnapshotTree[] snapTree, String snapName) {
		for (int i = 0; i < snapTree.length; i++) {
			VirtualMachineSnapshotTree node = snapTree[i];
			if (snapName.equals(node.getName())) {
				return node.getSnapshot();
			} else {
				VirtualMachineSnapshotTree[] childTree = node
						.getChildSnapshotList();
				if (childTree != null) {
					ManagedObjectReference mor = findSnapshotInTree(childTree,
							snapName);
					if (mor != null) {
						return mor;
					}
				}
			}
		}
		return null;
	}
}
