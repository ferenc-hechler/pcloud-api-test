package de.hechler.experiments.pcloud.db;

import java.io.PrintStream;
import java.util.List;

public class FolderInfo {
	
	public List<FolderInfo> childFolders;
	public List<FileInfo> childFiles;
	
	public long folderID;
	public FolderInfo parentFolder;
	public long name;
	
	public void write(PrintStream out) {
		out.println(csvline());
	}

	private String csvline() {
		StringBuffer result = new StringBuffer();
		
		return result.toString();
	}
	
}
