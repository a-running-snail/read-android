package com.jingdong.app.reader.entity;

import java.io.File;
import java.util.Date;

import com.jingdong.app.reader.util.FileService.Directory;

public class CacheFile {

	private String name;
	private String firstName;
	private String lastName;
	private Date cleanTime;

	private File file;
	private Directory directory;

	public CacheFile() {
	}

	public CacheFile(File file) {
		setFile(file);
	}

	public CacheFile(String name, long cacheTime) {
		setName(name);
		cleanTime = new Date(new Date().getTime() + cacheTime);
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getCleanTime() {
		return cleanTime;
	}

	public void setCleanTime(Date cleanTime) {
		this.cleanTime = cleanTime;
	}

	public Directory getDirectory() {
		return directory;
	}

	public void setDirectory(Directory directory) {
		this.directory = directory;
	}

	public File getFile() {
		if (null == file && null != getDirectory()) {
			file = new File(getDirectory().getDir(), getName());
		}
		return file;
	}

	public void setFile(File file) {
		setName(file.getName());
		this.file = file;
	}

	public String getName() {
		if (null == name) {
			name = firstName + "." + lastName;
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
		int index = name.lastIndexOf(".");
		firstName = name.substring(0, index);
		lastName = name.substring(index + 1);
	}

}
