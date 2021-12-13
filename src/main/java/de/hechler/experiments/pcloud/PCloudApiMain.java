package de.hechler.experiments.pcloud;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;
import com.pcloud.sdk.UserInfo;

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

	
	private static PCloudConfig config;
	public static PCloudConfig getConfig() {
		if (config == null) {
			config = new PCloudConfig(ENV_FILENAME);
		}
		return config;
	}
	
	private static ApiClient apiClient;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		apiClient = PCloudSdk.newClientBuilder()
				.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
				.apiHost(getConfig().getApiHost())
				// Other configuration...
				.create();
		try {
			RemoteFolder folder = apiClient.listFolder(RemoteFolder.ROOT_FOLDER_ID).execute();
			printFolder(folder);
		} catch (IOException | ApiError e) {
			e.printStackTrace();
		}
		apiClient.shutdown();
	}

	private static void printFolder(RemoteFolder folder) throws IOException, ApiError {
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFile()) {
				RemoteFile remoteFile = entry.asFile();
				System.out.println(remoteFile.name() + "->" + remoteFile.hash());
				UserInfo userInfo = apiClient.getUserInfo().execute();
				System.out.println(userInfo.email());
			}
			printFileAttributes(entry);
		}
	}

	private static void printFileAttributes(RemoteEntry entry) {
		System.out.format("%s | Created:%s | Modified: %s | size:%s\n", entry.name(), entry.created(),
				entry.lastModified(), entry.isFile() ? String.valueOf(entry.asFile().size()) : "-");
	}
}
