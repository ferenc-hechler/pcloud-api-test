package de.hechler.experiments.pcloud;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;

import de.hechler.experiments.jfxstarter.persist.VirtualDrive;
import de.hechler.experiments.jfxstarter.tools.StopWatch;

/**
 * Test using pCloud API - Java SDK https://github.com/pCloud/pcloud-sdk-java
 * 
 * SDK docu: https://pcloud.github.io/pcloud-sdk-java/
 * 
 * API docu: https://docs.pcloud.com/
 * 
 * @author feri
 */

public class UpdatePCloudCSV {

	
	private final static String ENVIRONMENT = "prod";
	
	private final static String CONFIG_FILENAME = ".env-"+ENVIRONMENT;

	private final static String IN_FILE  = "testdata/pcloud-"+ENVIRONMENT+".csv";
	private final static String OUT_FILE = "testdata/pcloud-"+ENVIRONMENT+".csv";
//	private final static String REMOTE_PATH = "/My Pictures/2017/2017_02_10";
	private final static String REMOTE_PATH = "/";
	
	private static PCloudConfig config;
	public static PCloudConfig getConfig() {
		if (config == null) {
			config = new PCloudConfig(CONFIG_FILENAME);
		}
		return config;
	}
	
	public static void main(String[] args)  {
		StopWatch watch = new StopWatch();
		String inCsv = IN_FILE;
		String outCsv = OUT_FILE;
		String remotePath = REMOTE_PATH;
		if (args.length == 3) {
			inCsv = args[0];
			outCsv = args[1];
			remotePath = args[2];
		}
		else if (args.length != 0) {
			usage();
		}
		System.out.println("input CSV:   " + inCsv);
		System.out.println("output CSV:  " + outCsv);
		System.out.println("remote path: " + remotePath);
		
		System.out.println("initializing pcloud api client for environment "+ENVIRONMENT+" at "+watch.getSeconds()+"s");
		ApiClient apiClient = PCloudSdk.newClientBuilder()
				.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
				.apiHost(getConfig().getApiHost())
				// Other configuration...
				.create();
		System.out.println("start reading at "+watch.getSeconds()+"s");
		VirtualDrive vd = new VirtualDrive();
		vd.readFromFile(inCsv);
		System.out.println("start update at "+watch.getSeconds()+"s");
		RemoteFileUpdater updater = new RemoteFileUpdater(apiClient, vd);
		updater.updateRecursive(remotePath);
		System.out.println("write result at "+watch.getSeconds()+"s");
		vd.exportToStore().writeToFile(outCsv);
		System.out.println("finished after "+watch.getSeconds()+"s");
		apiClient.shutdown();
	}

	private static void usage() {
		System.err.println("USAGE: updatePCloudCSV <in-CSV> <out-CSV> <remote-path>");
		System.exit(9);
	}

	
}
