package de.hechler.experiments.pcloud;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.pcloud.sdk.*;

/**
 * Test using pCloud API - Java SDK
 * https://github.com/pCloud/pcloud-sdk-java 
 * 
 * SDK docu: https://pcloud.github.io/pcloud-sdk-java/
 * 
 * API docu: https://docs.pcloud.com/
 * 
 * @author feri
 */

public class PCloudApiMain {

	private final static String ENV_FILENAME = ".env";

	private static class Config {
		private String client_id;
		private String client_secret;
	}
	
	private static Config readConfig() throws IOException, FileNotFoundException {
		Properties envProps = new Properties();
		envProps.load(new FileInputStream(ENV_FILENAME));
		Config result = new Config();
		result.client_id = envProps.getProperty("CLIENT_ID");
		result.client_secret = envProps.getProperty("CLIENT_SECRET");
		return result;
	}


	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println(PCloudApiMain.class + " started");
		
		Config config = readConfig();
		
		String oauthAccessToken = config.client_id+":"+config.client_secret;
		ApiClient apiClient = PCloudSdk.newClientBuilder()
                .authenticator(Authenticators.newOAuthAuthenticator(oauthAccessToken))
                // Other configuration...
                .create();		
		Call<RemoteFolder> rootFolderInfo = apiClient.listFolder("/", false);
		System.out.println(rootFolderInfo);
	}

}
