package com.ifunshow.antelopeframe.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.ifunshow.antelopeframe.util.compress.ZipCompressorByAnt;
import com.ifunshow.antelopeframe.util.file.FileManager;
import com.ifunshow.antelopeframe.web.base.BaseController;
import com.ifunshow.antelopeframe.web.service.HdfsClientService;

/**
 * hdfs欢迎页面
 * @author yyflyons-于亚丰
 */
@Controller
@RequestMapping("/hdfs")
public class HdfsFileController extends BaseController{
	
	@Autowired
	private HdfsClientService hdfsClientService;
	
	@RequestMapping(value="/index")
	public String index(){
		return "hdfs/index";
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	// 将文件上传请求映射到该方法 // 请求参数一定要与form中的参数名对应
	public String handleFormUpload(Model model,@RequestParam("uploadfile") CommonsMultipartFile mFile,@RequestParam("dstpath") String dst) {
		String errMsg = null;
		if (!mFile.isEmpty()) {
			if(StringUtils.isBlank(dst) || "home".equalsIgnoreCase(dst)){
				dst = "/";
			}else{
				dst = dst.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
			}
			File file = new File("tmp");
			if (!file.exists()) file.mkdirs();
			file = new File("tmp" + File.separator + mFile.getOriginalFilename());
			try {
				mFile.getFileItem().write(file);
				hdfsClientService.upload2Hdfs(file.getPath(),dst);
				file.delete();
			} catch (IOException e) {
				errMsg = e.getMessage();
				if(errMsg.startsWith("Permission denied:")){
					model.addAttribute("errMsg", "error:'权限不足！'");
				}else{
					model.addAttribute("errMsg", "error:'"+e.getMessage()+"'");
				}
				file.delete();
			} catch (Exception e) {
				errMsg = e.getMessage();
				file.delete();
			}
		}
		return "1";
	}
	
	@RequestMapping(value = "/list/{path}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model listFiles(Model model, @PathVariable String path) {
		model.addAttribute("parentPath", path);
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		List<Map<String, String>> list = null;
		try {
			list = hdfsClientService.ls(path);
			model.addAttribute("files", list);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if(errMsg.startsWith("Permission denied:")){
				model.addAttribute("errMsg", "权限不足！");
			}else{
				model.addAttribute("errMsg", e.getMessage());
			}
		}
		
		return model;
	}
	
	@RequestMapping(value = "/listdir/{path}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model listDirs(Model model, @PathVariable String path) {
		model.addAttribute("parentPath", path);
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		List<Map<String, String>> list = null;
		try {
			list = hdfsClientService.lsSubdir(path);
			model.addAttribute("dirs", list);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			if(errMsg.startsWith("Permission denied:")){
				model.addAttribute("errMsg", "权限不足！");
			}else{
				model.addAttribute("errMsg", e.getMessage());
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/delete/{path}/{files}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model deleteFiles(Model model, @PathVariable String path,@PathVariable String files) {
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isNotBlank(files)){
			String[] file = files.split(":");
			if(null != file && file.length > 0){
				for (int i = 0; i < file.length; i++) {
					file[i] = file[i].replaceAll("`", ".");
				}
				try {
					hdfsClientService.deleteFromHdfs(path,file);
				} catch (IOException e) {
					String errMsg = e.getMessage();
					if(errMsg.startsWith("Permission denied:")){
						model.addAttribute("errMsg", "权限不足！");
					}else{
						model.addAttribute("errMsg", e.getMessage());
					}
				}
			}
		}
		return model;
	}
	
	
	@RequestMapping(value = "/addFolder/{path}/{folder}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model createFolder(Model model, @PathVariable String path,@PathVariable String folder) {
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isNotBlank(folder)){
			try {
				hdfsClientService.addFolderOnHdfs(path,folder);
			} catch (IOException e) {
				String errMsg = e.getMessage();
				if(errMsg.startsWith("Permission denied:")){
					model.addAttribute("errMsg", "权限不足！");
				}else{
					model.addAttribute("errMsg", e.getMessage());
				}
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/moveFile/{folder}/{path}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model moveFile(Model model, @PathVariable String folder,@PathVariable String path) {
		if(StringUtils.isBlank(folder) || "home".equalsIgnoreCase(folder)){
			folder = "/";
		}else{
			folder = folder.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isNotBlank(folder)){
			try {
				hdfsClientService.moveFile(path,folder);
			} catch (IOException e) {
				String errMsg = e.getMessage();
				if(errMsg.startsWith("Permission denied:")){
					model.addAttribute("errMsg", "权限不足！");
				}else{
					model.addAttribute("errMsg", e.getMessage());
				}
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/renameFile/{path}/{oldFileName}/{fileName}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model renameFile(Model model, @PathVariable String path,@PathVariable String oldFileName,@PathVariable String fileName) {
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "/";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isNotBlank(oldFileName) && StringUtils.isNotBlank(fileName) && !oldFileName.equals(fileName)){
			try {
				hdfsClientService.renameFileOnHdfs(path,oldFileName.replaceAll("`", "."),fileName.replaceAll("`", "."));
			} catch (IOException e) {
				String errMsg = e.getMessage();
				if(errMsg.startsWith("Permission denied:")){
					model.addAttribute("errMsg", "权限不足！");
				}else{
					model.addAttribute("errMsg", e.getMessage());
				}
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/setPermision/{path}/{files}/{permission}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model setFilePermision(Model model, @PathVariable String path,@PathVariable String files,@PathVariable String permission) {
		if(StringUtils.isBlank(path) || "home".equalsIgnoreCase(path)){
			path = "";
		}else{
			path = path.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
		}
		if(StringUtils.isNotBlank(files)){
			String[] file = files.split(":");
			if(null != file && file.length > 0){
				String errMsg = "";
				for (int i = 0; i < file.length; i++) {
					file[i] = file[i].replaceAll("`", ".");
					try {
						hdfsClientService.setHdfsFilePermission(path+"/"+file[i], permission);
					} catch (IOException e) {
						errMsg += e.getMessage()+"\n";
					}
				}
				model.addAttribute("errMsg",errMsg);
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/dowloadHdfsFile/{filepath}",method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public Model dowloadFileFromHdfs(Model model, @PathVariable String filepath) {
		String fileName = "";
		if(StringUtils.isBlank(filepath) || "home".equalsIgnoreCase(filepath)){
			model.addAttribute("errMsg", "不能下载根目录，请选择一个目录或文件！");
		}else{
			fileName = filepath.substring(filepath.lastIndexOf(":")+1);
			filepath = filepath.replaceAll(":", "/").replaceAll("`", ".").replaceFirst("home", "");
			if(StringUtils.isNotBlank(filepath)){
				try {
					File file = new File("tmp/"+UUID.randomUUID()+"/");
					file.mkdirs();
					hdfsClientService.copyFile2Local(filepath, file.getPath());
					new ZipCompressorByAnt("tmp/"+fileName+".zip").compress(file.getPath());
					FileManager.deleteFile(file);
					model.addAttribute("file", ""+fileName);
				} catch (Exception e) {
					String errMsg = "";
					if(errMsg.startsWith("Permission denied:")){
						model.addAttribute("errMsg", "权限不足！");
					}else{
						model.addAttribute("errMsg", e.getMessage());
					}
				}
			}
		}
		return model;
	}
	
	@RequestMapping(value = "/dowloadFile/{fileName}",method = {RequestMethod.POST,RequestMethod.GET})
	public void dowloadFile(Model model, @PathVariable String fileName,HttpServletResponse res) throws IOException {
		File fileZip = new File("tmp/"+fileName+".zip");
        OutputStream os = res.getOutputStream();
        try {
            res.reset();
            res.setHeader("Content-Disposition", "attachment; filename="+fileName+".zip");
            res.setContentType("application/octet-stream; charset=utf-8");
            os.write(FileUtils.readFileToByteArray(fileZip));
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }
		fileZip.delete();
	}
	
}
