package com.ifunshow.antelopeframe.util.file;

import java.io.File;

public class FileManager {
	public static void deleteFile(File file){
		if(file.isDirectory()){
			File[] lists = file.listFiles();
			if(null != lists && lists.length > 0){
				for (File _file : lists) {
					deleteFile(_file);
				}
			}
		}
		file.delete();
	}
}
