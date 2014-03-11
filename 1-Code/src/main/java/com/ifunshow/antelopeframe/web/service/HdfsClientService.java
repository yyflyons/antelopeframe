package com.ifunshow.antelopeframe.web.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HdfsClientService {
	@Autowired
	private FileSystem hdfsFileSystem;
	
	
	public List<Map<String,String>> ls() throws Exception{
		return ls("/");
	}
	
	public List<Map<String,String>> ls(String dir,String user) throws Exception{
		return ls("/user/"+user+"/"+dir);
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public List<Map<String,String>> ls(String dir) throws Exception{
		List<Map<String,String>> list = null;
		FileStatus[] fs = hdfsFileSystem.listStatus(new Path(dir));
		if(null != fs && fs.length > 0){
			list = new ArrayList<Map<String,String>>();
			for(FileStatus f: fs){
				Map<String,String> map = new HashMap<String,String>();
				map.put("path", f.getPath().getParent().toString());
				map.put("fileName", f.getPath().getName().replaceAll("\\.", "`"));
				map.put("isFile", f.isFile()?"1":"0");
				map.put("isDirectory", f.isDirectory()?"1":"0");
				map.put("owner", f.getOwner());
				map.put("group", f.getGroup());
				if(f.isDirectory()){
					map.put("len", (f.getLen()==0?"-":f.getLen()+""));
				}else{
					map.put("len", FormetFileSize(f.getLen()));
				}
				
				
				map.put("permission", f.getPermission().toString());
				map.put("modificationTime",sdf.format(new Date(f.getModificationTime())));
				list.add(map);
			}
		}
		return list;
	}
	
	public void upload2Hdfs(String srcPath,String dstPath) throws IOException{
		hdfsFileSystem.copyFromLocalFile(new Path(srcPath), new Path(dstPath));
	}
	
	public void deleteFromHdfs(String dstPath,String[] files) throws IOException{
		for (String file:files) {
			hdfsFileSystem.delete(new Path(dstPath+"/"+file),true);
		}
	}
	
	public void addFolderOnHdfs(String path, String folder) throws IOException {
		hdfsFileSystem.mkdirs(new Path(path+"/"+folder));
	}
	
	
	public void setHdfsFilePermission(String path, String permission) throws IOException {//012345678
		hdfsFileSystem.setPermission(new Path(path), new FsPermission(getFsAction(permission.substring(0,3)),getFsAction(permission.substring(3,6)),getFsAction(permission.substring(6))));
	}
	
	
	public FsAction getFsAction(String permision){
		if("--x".equalsIgnoreCase(permision)){
			return  FsAction.EXECUTE;
		}else if("-w-".equalsIgnoreCase(permision)){
			return  FsAction.WRITE;
		}else if("-wx".equalsIgnoreCase(permision)){
			return  FsAction.WRITE_EXECUTE;
		}else if("r--".equalsIgnoreCase(permision)){
			return  FsAction.READ;
		}else if("r-x".equalsIgnoreCase(permision)){
			return  FsAction.READ_EXECUTE;
		}else if("rw-".equalsIgnoreCase(permision)){
			return  FsAction.READ_WRITE;
		}else if("rwx".equalsIgnoreCase(permision)){
			return  FsAction.ALL;
		}else{//---
			return  FsAction.NONE;
		}
	}
	
	
	public String FormetFileSize(long fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else if (fileS < 1099511627776L){
			fileSizeString = df.format((double) fileS / 1073741824) + "GB";
		}else{
			fileSizeString = df.format((double) fileS / 1099511627776L) + "TB";
		}
		return fileSizeString;
	}

	
}
