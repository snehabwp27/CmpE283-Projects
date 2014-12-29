package cmpe283.project1;

import java.util.HashMap;

public class StringConstants {
	public final static String UserName = "administrator";
	public final static String Password = "12!@qwQW";
	public final static String VcenterUrl = "https://130.65.132.116/sdk";
	public final static String AdminUrl = "https://130.65.132.14/sdk";
	

	public final static HashMap<String, String> VMs = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			
			put("130.65.133.41", "T16-vHost-130.65.133.41");
			put("130.65.133.42", "T16-vHost-130.65.133.42");
			put("130.65.133.43", "T16-vHost-130.65.133.43");	}
	};
	
}