package de.hechler.experiments.pcloud;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteFolder;

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

	
	private final static String ENVIRONMENT = "dev";
	
	private final static String CONFIG_FILENAME = ".env-"+ENVIRONMENT;

	private final static String OUT_FILE = "logs/pcloud-"+ENVIRONMENT+"-scan-$NOW.csv";
	
	private static PCloudConfig config;
	public static PCloudConfig getConfig() {
		if (config == null) {
			config = new PCloudConfig(CONFIG_FILENAME);
		}
		return config;
	}
	
	public static void main(String[] args)  {
		StopWatch watch = new StopWatch();
		String inCsv = "testdata/pCloud.csv";
		String outCsv = "testdata/pCloud.csv";
		String remotePath = "/";
		
		ApiClient apiClient = PCloudSdk.newClientBuilder()
				.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
				.apiHost(getConfig().getApiHost())
				// Other configuration...
				.create();
		System.out.println("start reding at "+watch.getSeconds()+"s");
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

	
}
