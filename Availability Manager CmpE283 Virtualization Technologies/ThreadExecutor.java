package cmpe283.project1;

public class ThreadExecutor implements Runnable{

	private Vcenter virCenter;
	private String threadTitle;
	private Thread thr;
	
	public ThreadExecutor(Vcenter vCenter, String name) {
		this.virCenter = vCenter;
		threadTitle = name;
	}
	
	public void run() {
		switch (threadTitle) {
		case "Heartbeat":
			try {
				virCenter.heartbeat();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case "Snapshot":
			try {
				Thread.sleep(10000);
				virCenter.snapshotCreation();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		case "PrintSatistics":
			try {
				virCenter.printDetails();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
			
		default:
			System.out.println("Enter a valid option");
			break;
		}		
	}
	
	public void start ()
	   {
	      System.out.println("Thread----- " + threadTitle + "------Iniated");
	      if (thr == null)
	      {
	         thr = new Thread (this, threadTitle);
	         thr.start ();
	      }
	   }
}
