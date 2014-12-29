package cmpe283.project1;

import java.net.URL;

import com.vmware.vim25.mo.ServiceInstance;

public class DisasterRecovery {

	public static void main(String[] args) throws Exception {		
		ServiceInstance si = new ServiceInstance(new URL(StringConstants.VcenterUrl),
				StringConstants.UserName, StringConstants.Password, true);
		Vcenter vCenter = new Vcenter(si);
						
		ThreadExecutor heartbeat = new ThreadExecutor(vCenter, "Heartbeat");
		heartbeat.start();
		
		ThreadExecutor printStatistics = new ThreadExecutor(vCenter, "PrintSatistics");
		printStatistics.start();
		
		ThreadExecutor snapshot = new ThreadExecutor(vCenter, "Snapshot");
		snapshot.start();
	}
}
