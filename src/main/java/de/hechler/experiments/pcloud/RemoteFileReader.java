package de.hechler.experiments.pcloud;

import java.io.IOException;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.experiments.jfxstarter.persist.FileFolderInfoDAO;
import de.hechler.experiments.jfxstarter.persist.FileFolderInfoStore;

public class RemoteFileReader {

	private ApiClient apiClient;
	private FileFolderInfoStore store;
	private long lastProgress;

	public RemoteFileReader(ApiClient apiClient, FileFolderInfoStore store) {
		this.apiClient = apiClient;
		this.store = store;
		this.lastProgress = System.currentTimeMillis(); 
	}

	public void readRecursive(long folderID) {
		try {
			RemoteFolder folder = apiClient.listFolder(folderID, true).execute();
			readRecursive(folder);
		} catch (ApiError | IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	private void readRecursive(RemoteFolder folder) {
		if (System.currentTimeMillis()-lastProgress > 15000) {
			System.out.println(store.size()+" - "+folder.name());
		}
		FileFolderInfoDAO data = FileFolderInfoDAO.createFolderInfo(
				id2long(folder.id()), folder.parentFolderId(), folder.name(), 
				folder.created(), folder.lastModified()
		);
		store.add(data);
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFile()) {
				long fileID = id2long(entry.id());
				String sha256 = null;
				try {
					sha256 = apiClient.checksumFile(fileID).execute().get("SHA-256");
				} catch (ApiError | IOException e) {
					System.err.println("ERROR: f"+entry.name()+" ("+fileID+"): "+e.toString());
				}
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
				readRecursive(entry.asFolder());
			}
		}
	}

	private static long id2long(String idStr) {
		return Long.parseLong(idStr.replace("f", "").replace("d", ""));
	}
	
}
