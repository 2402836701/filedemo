package com.filestream.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RestController
@RequestMapping(value="/c/filestream")
public class FilestreamAction {
	
	private static final Logger log=LoggerFactory.getLogger(FilestreamAction.class);
	private static final String filePath="D://file//";	
	
	@PostMapping(value="upload")
	@ResponseBody
	public String upload(@RequestParam("file") MultipartFile file) {
		try {
			if(file.isEmpty()) {
				return "文件为空";
			}
			String fileName = file.getOriginalFilename();//获取上传的文件名称
			log.info("文件名称："+fileName);
			String suffixName = fileName.substring(fileName.lastIndexOf("."));//通过文件名截取后缀名
			log.info("文件后缀："+suffixName);
			String path = filePath + fileName;
			File dest = new File(path);
			//检测是否存在目录结构
			if(!dest.getParentFile().exists()) {
				dest.getParentFile().mkdirs();		//新建文件夹
			}
			file.transferTo(dest);					//文件写入
			return "上传成功";
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
        return "上传失败";
	}
	
	@PostMapping(value="batch")
	@ResponseBody
	public String batch(HttpServletRequest request) {
		List<MultipartFile> files=((MultipartHttpServletRequest)request).getFiles("file");
		MultipartFile file = null;
	    BufferedOutputStream stream = null;
		for(MultipartFile multipart : files) {
			file = multipart;
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    stream = new BufferedOutputStream(new FileOutputStream(
                    		new File(filePath + file.getOriginalFilename())));	//设置文件路径及名字
                    stream.write(bytes);	// 写入
                    stream.close();
                } catch (Exception e) {
                    stream = null;
                    return file.getOriginalFilename() + " 文件上传失败 ==> "+ e.getMessage();
                }
            } else {
                return file.getOriginalFilename()+" 文件上传失败因为文件为空";
            }
		}
		return "上传成功";
	}
	
	/**
	 * 文件下载
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/download")
	@ResponseBody
    public String downloadFile(HttpServletRequest request, HttpServletResponse response) {
        String fileName = "wenjian.pub";		// 需要下载的文件名
        if (fileName != null) {
            File file = new File(filePath , fileName);
            if (file.exists()) {
                response.setCharacterEncoding("UTF-8");					//设置编码格式
                response.setContentType("application/force-download");	// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);	// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);				//读
                    bis = new BufferedInputStream(fis);				//写
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }
                    return "下载成功";
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return "下载失败";
    }
}
