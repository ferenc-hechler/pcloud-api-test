package de.hechler.experiments.pcloud;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.experiments.jfxstarter.persist.FileFolderInfoStore;
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

public class PCloudApiMain {

	private final static String ENV_FILENAME = ".env";

	private final static String OUT_FILE = "logs/pcloud-scan-$NOW.csv";
	
	private static PCloudConfig config;
	public static PCloudConfig getConfig() {
		if (config == null) {
			config = new PCloudConfig(ENV_FILENAME);
		}
		return config;
	}
	
	private static ApiClient apiClient;

	private static SimpleDateFormat sdfFILE = new SimpleDateFormat("yyyyMMdd_HHmmss");

	
	public static FileFolderInfoStore store = new FileFolderInfoStore();

	
	public static void main(String[] args)  {
		StopWatch watch = new StopWatch();
		String storeFilename = OUT_FILE.replace("$NOW", nowFILE());
		apiClient = PCloudSdk.newClientBuilder()
				.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
				.apiHost(getConfig().getApiHost())
				// Other configuration...
				.create();
		RemoteFileReader rfr = new RemoteFileReader(apiClient, store);
		rfr.readRecursive(RemoteFolder.ROOT_FOLDER_ID);
		System.out.println("Store: "+store.size()+", Time: "+watch.getSecondsAndReset()+"s");
		store.writeToFile(storeFilename);
		apiClient.shutdown();
	}

	private static String nowFILE() {
		return sdfFILE.format(new Date());
	}

	
}
