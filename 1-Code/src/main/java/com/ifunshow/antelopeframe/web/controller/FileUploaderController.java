package com.ifunshow.antelopeframe.web.controller;

import java.io.File;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.ifunshow.antelopeframe.web.base.BaseController;

/**
 * 角色管理
 * @author yyflyons-于亚丰
 */
@Controller
@RequestMapping("/fileupload")
public class FileUploaderController  extends BaseController{
	
	@RequestMapping("/index")
	public String index(){
		return "examples/fileUpload";
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	// 将文件上传请求映射到该方法
	public void handleFormUpload(@RequestParam("file") CommonsMultipartFile mFile) { // 请求参数一定要与form中的参数名对应
		if (!mFile.isEmpty()) {
			//log.info("上传文件的名字：" + mFile.getOriginalFilename());
			// String path = this.servletContext.getRealPath("/tmp/");
			// //获取本地存储路径
			File file = new File("tmp");
			if (!file.exists()) file.mkdirs();
			File file1 = new File("tmp" + File.separator + mFile.getOriginalFilename()); // 新建一个文件
			try {
				mFile.getFileItem().write(file1); // 将上传的文件写入新建的文件中
				
				//log.info("=======文件上传成功====");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
