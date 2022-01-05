package de.hechler.experiments.pcloud;

import java.io.IOException;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFolder;

import de.hechler.experiments.jfxstarter.persist.FileInfo;
import de.hechler.experiments.jfxstarter.persist.FolderInfo;
import de.hechler.experiments.jfxstarter.persist.VirtualDrive;

public class RemoteFileUpdater {

	private ApiClient apiClient;
	private VirtualDrive vd;
	private long lastProgress;

	public RemoteFileUpdater(ApiClient apiClient, VirtualDrive vd) {
		this.apiClient = apiClient;
		this.vd = vd;
		this.lastProgress = System.currentTimeMillis(); 
	}

	

	public void updateRecursive(String path) {
		try {
			RemoteFolder folder = apiClient.loadFolder(path).execute();
			if (folder != null) {
				updateRecursive(folder.folderId());
			}
		} catch (IOException | ApiError e) {
			throw new RuntimeException("ERROR reading pCloud folder '"+path+"':"+e.toString(), e);
		}
	}

	public void updateRecursive(long folderID) {
		try {
			RemoteFolder folder = apiClient.listFolder(folderID, true).execute();
			FolderInfo vFolder = vd.getFolderByID(folder.folderId());
			if (vFolder == null) {
				vFolder = createRecursiveFolder(folder.folderId());
			}
			updateRecursive(folder, vFolder);
		} catch (ApiError | IOException e) {
			throw new RuntimeException("ERROR reading pCloud folder with id "+folderID+":"+e.toString(), e);
		}
	}

	private void updateRecursive(RemoteFolder folder, FolderInfo vFolder) {
		if (System.currentTimeMillis()-lastProgress > 15000) {
			System.out.println(vd.getCountFiles()+" - "+vFolder.getFullName());
			lastProgress = System.currentTimeMillis();
		}
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFile()) {
				long fileID = id2long(entry.id());
				FileInfo vFile = vd.getFileByID(fileID);
				if (vFile == null) {
					vFile = new FileInfo(fileID, entry.name(), entry.asFile().size(), entry.created(), entry.lastModified(), null, entry.asFile().hash());
					vFolder.addFile(vFile);
					vd.addFile(vFile);
				}
				else {
					vFile.name = entry.name();
					vFile.size = entry.asFile().size();
					vFile.created = entry.created();
					vFile.lastModified = entry.lastModified();
					if (!entry.asFile().hash().equals(vFile.hash)) {
						vFile.sha256 = null;
						vFile.hash = entry.asFile().hash(); 
					}
					if (vFile.parentFolder != vFolder) {
						vFile.parentFolder.removeFile(vFile);
						vFolder.addFile(vFile);
					}
				}
				try {
					if (vFile.sha256 == null) {
						//System.out.println("--> GETTING SHA-256 FOR "+entry.name());
						vFile.sha256 = apiClient.checksumFile(fileID).execute().get("SHA-256");
					}
				} catch (ApiError | IOException e) {
					System.err.println("ERROR: f"+entry.name()+" ("+fileID+"): "+e.toString());
				}
			}
		}
		for (RemoteEntry entry : folder.children()) {
			if (entry.isFolder()) {
				long childFolderID = id2long(entry.id());
				FolderInfo vChildFolder = vd.getFolderByID(childFolderID);
				if (vChildFolder == null) {
					vChildFolder = new FolderInfo(childFolderID, entry.name(), entry.created(), entry.lastModified());
					vFolder.addFolder(vChildFolder);
					vd.addFolder(vChildFolder);
				}
				updateRecursive(entry.asFolder(), vChildFolder);
			}
		}
	}

	private FolderInfo createRecursiveFolder(long folderID) {
		try {
			RemoteFolder folder = apiClient.loadFolder(folderID).execute();
			FolderInfo vFolder = vd.getFolderByID(folder.folderId());
			if (vFolder != null) {
				if (vFolder.id != 0) {
					vFolder.name = folder.name();
				}
				vFolder.created = folder.created();
				vFolder.lastModified = folder.lastModified();
				return vFolder;
			}
			long parentFolderId = folder.parentFolderId();
			if (parentFolderId == folder.folderId()) {
			    vFolder = new FolderInfo(folder.folderId(), "/", folder.created(), folder.lastModified());
				vd.addFolder(vFolder);
				vd.setRootFolder(vFolder);
				return vFolder;
			}
			FolderInfo vParentFolder = createRecursiveFolder(folder.parentFolderId());
			vFolder = new FolderInfo(folder.folderId(), folder.name(), folder.created(), folder.lastModified());
			vParentFolder.addFolder(vFolder);
			vd.addFolder(vFolder);
			return vFolder;
		} catch (IOException | ApiError e) {
			throw new RuntimeException("ERROR reading pCloud folder "+folderID+": "+e.toString(), e);
		}
	}
	
	private static long id2long(String idStr) {
		return Long.parseLong(idStr.replace("f", "").replace("d", ""));
	}

}
