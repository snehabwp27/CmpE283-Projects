package cmpe283.project1;

public class Ping {

	public static boolean pingIP(String ip) throws Exception {
		String inst = "";
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			// For Windows
			inst = "ping -n 3 " + ip;
		} else {
			// For Linux and OSX
			inst = "ping -c 3 " + ip;
		}
		
		System.out.println("Ping "+ ip + "......");
		Process process = Runtime.getRuntime().exec(inst);
		process.waitFor();		
		return process.exitValue() == 0;
	}

}
