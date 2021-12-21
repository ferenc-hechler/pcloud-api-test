package de.hechler.experiments.pcloud.db;

import java.util.Date;

public class FileInfo {
	
	public long fileID;
	public FolderInfo parentFolder;
	public String name;
	public String hash;
	public String sha256;
	public long filesize;
	public Date created;
	public Date lastModified;
	
}
