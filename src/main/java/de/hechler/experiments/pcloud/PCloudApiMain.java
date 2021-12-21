package de.hechler.experiments.pcloud;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.experiments.pcloud.db.FileFolderInfoDAO;
import de.hechler.experiments.pcloud.db.FileFolderInfoStore;

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

	private final static String LOGFILE = "logs/pcloud-scan-$NOW.csv";
	
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

	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		long start = System.currentTimeMillis();
		String storeFilename = LOGFILE.replace("$NOW", nowFILE());
//		if (1==1) {
//			store.readFromFile(LOGFILE.replace("-$NOW", ""));
//			store.writeToFile(storeFilename);
//			System.out.println(store.toCSV());
//			return;
//		}
		try {
			apiClient = PCloudSdk.newClientBuilder()
					.authenticator(Authenticators.newOAuthAuthenticator(getConfig().getAccessToken()))
					.apiHost(getConfig().getApiHost())
					// Other configuration...
					.create();
			RemoteFolder folder = apiClient.listFolder(RemoteFolder.ROOT_FOLDER_ID, true).execute();
			printFolder(folder, "");
			store.writeToFile(storeFilename);
			System.out.println("--- DATA ---");
			System.out.println(store.toCSV());
			System.out.println("--- ---- ---");
		} catch (ApiError e) {
			e.printStackTrace();
		}
		long stop = System.currentTimeMillis();
		System.out.println(0.001*(stop-start));
		apiClient.shutdown();
	}

	private static String nowFILE() {
		return sdfFILE.format(new Date());
	}

	private static void printFolder(RemoteFolder folder, String parentPath) throws IOException, ApiError {
		FileFolderInfoDAO data = FileFolderInfoDAO.createFolderInfo(
			id2long(folder.id()), folder.parentFolderId(), folder.name(), 
			folder.created(), folder.lastModified()
		);
		store.add(data);
		String path=parentPath+"/"+folder.name();
		System.out.println("----- FOLDER " + path + "["+folder.id()+"] ("+folder.lastModified()+") -----");
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFile()) {
				long fileID = id2long(entry.id());
				String sha256 = apiClient.checksumFile(fileID).execute().get("SHA-256");
				data = FileFolderInfoDAO.createFileInfo(
					fileID, entry.parentFolderId(), entry.name(), 
					entry.created(), entry.lastModified(), 
					entry.asFile().size(), entry.asFile().hash(), 
					sha256
				);
				store.add(data);
			}
		}
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFolder()) {
//				RemoteFolder childFolder = apiClient.listFolder(id2long(entry.id())).execute();
				printFolder(entry.asFolder(), path);
			}
		}
	}

	private static long id2long(String idStr) {
		return Long.parseLong(idStr.replace("f", "").replace("d", ""));
	}
	
}
