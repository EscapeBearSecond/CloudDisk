package com.dyg.controller;

import com.dyg.entity.HdfsFile;
import com.dyg.entity.User;
import com.dyg.service.HdfsService;
import com.dyg.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.extern.log4j.Log4j2;
import org.apache.hadoop.fs.Hdfs;
import org.apache.hadoop.fs.Path;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
public class HdfsController {
    private String rootPath = "hdfs://192.168.59.172:9000/es";
    private String personRootPath = "hdfs://192.168.59.172:9000/es";
    private List<String> originalPath = new ArrayList<>();
    private String normalPath = "hdfs://192.168.59.172:9000/es";
    private String recycleBinPath = "hdfs://192.168.59.172:9000/es";
//    private String prePath = currentPath;
    private List<String> pathList = new ArrayList<>();
    private String Gusername = "";
    @Autowired
    private HdfsService hdfsService;
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String goRegister(){
        return "register";
    }

    @PostMapping("/register")
    public String register(String username,String password,String confirm){
        if (username.length() > 0 && password.equals(confirm)){
            User user = new User(username,password);
            int i = userService.addUser(user);
            hdfsService.createdir(rootPath+"/"+username);
            hdfsService.createdir(rootPath+"/"+username+"/normal");
            hdfsService.createdir(rootPath+"/"+username+"/recycleBin");
            return "redirect:/";
        }else {
            return "/register";
        }
    }

    @RequestMapping({"/login","/"})
    public String goLogin(){
        return "index";
    }
    @PostMapping("/login")
    public String login(String username, String password, ModelMap map, HttpSession session){
        if (userService.login(username,password) != null){
            log.info("登录成功");
            log.info("用户名={},密码等于={}",username,password);
            Gusername = username;
            personRootPath = rootPath+"/"+username;
            normalPath = personRootPath+"/normal";
            recycleBinPath = personRootPath+"/recycleBin";
            pathList.add(normalPath);
            session.setAttribute("loginUsername",username);
            map.put("username",Gusername);
            return "redirect:backHome";
        }else {
            map.put("msg","用户名或密码错误");
            return "index";
        }
    }
    @RequestMapping("/files")
    public String file(ModelMap map){
        String[] ar = new String[]{"doc","docx","xlsx","md","txt","ppt","xls","html","htm","pdf"};
        List<String> all = Arrays.asList(ar);
        //先清除原有的文件
        hdfsService.getList2().clear();
        hdfsService.getAllFiles(normalPath);
        List<HdfsFile> list2 = hdfsService.getList2();
        List<HdfsFile> files = new ArrayList<>();
        for (HdfsFile file:list2){
            String[] split = file.getName().split("\\.");
            if (all.contains(split[1])){
                files.add(file);
            }
        }
        map.put("lists",files);
        map.put("username",Gusername);
        return "files";
    }
    @RequestMapping("/pictures")
    public String pictures(ModelMap map){
        //webp，bmp，jpg，png，tif，gif，apng
        String[] ar = new String[]{"png","jpg","webp","bmp","tif","gif","apng"};
        List<String> all = Arrays.asList(ar);
        //先清除原有的文件
        hdfsService.getList2().clear();
        hdfsService.getAllFiles(normalPath);
        List<HdfsFile> list2 = hdfsService.getList2();
        List<HdfsFile> pictures = new ArrayList<>();
        for (HdfsFile file:list2){
            String[] split = file.getName().split("\\.");
            if (all.contains(split[1])){
                pictures.add(file);
            }
        }
        map.put("lists",pictures);
        map.put("username",Gusername);
        return "pictures";
    }
    @RequestMapping("/videos")
    public String videos(ModelMap map){
        String[] ar = new String[]{"mp4"};
        List<String> all = Arrays.asList(ar);
        //先清除原有的文件
        hdfsService.getList2().clear();
        hdfsService.getAllFiles(normalPath);
        List<HdfsFile> list2 = hdfsService.getList2();
        List<HdfsFile> videos = new ArrayList<>();
        for (HdfsFile file:list2){
            String[] split = file.getName().split("\\.");
            if (all.contains(split[1])){
                videos.add(file);
            }
        }
        map.put("lists",videos);
        map.put("username",Gusername);
        return "videos";
    }
    @RequestMapping("/music")
    public String music(){
        return "music";
    }
    @RequestMapping("/allFiles")
    public String allFiles(ModelMap map, String path, HttpServletRequest request){
        List<HdfsFile> lists = null;
        try {
            if (path!=null){
                pathList.add(path);
            }
            System.out.println(pathList.toString());
            lists = hdfsService.getDirectoryFromHdfs(pathList.get(pathList.size()-1));
        }catch (Exception e){
            e.printStackTrace();
        }
        map.put("lists",lists);
        map.put("path",path);
        map.put("username",Gusername);
        return "allFiles";
    }

    @RequestMapping("/upFile")
    public String upfile(@RequestPart("fileName") MultipartFile fileName){
        String originalFilename = fileName.getOriginalFilename();
        String upFilePath = pathList.get(pathList.size()-1);
        File file = new File("d:/WorkSpace/"+originalFilename);
        try {
            fileName.transferTo(file);
            hdfsService.copyFile(file.getAbsolutePath(),upFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:allFiles";
    }

    @RequestMapping("/download")
    public void download(String fileName, HttpServletResponse response) throws IOException {
        //设置相应类型
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename="+fileName+"");
        InputStream ism = null;
        String downloadPath = pathList.get(pathList.size()-1);
        System.out.println("downloadPaht = "+downloadPath);
        ism = hdfsService.getFileInputStreamForPath(downloadPath+"/"+fileName);
        int len = 0;
        byte[] buf = new byte[1024];
        ServletOutputStream sos = response.getOutputStream();
        while((len=ism.read(buf))!=-1){
            sos.write(buf,0,len);
        }
        System.out.println("下载完成");
        sos.close();
    }
    @RequestMapping("/createDir")
    public String createDir(String dirName){
        hdfsService.createdir(pathList.get(pathList.size()-1)+"/"+dirName);
        return "redirect:allFiles";
    }
    @RequestMapping("/delete")
    public String delete(String fileName){
        String deletePath = pathList.get(pathList.size()-1);
        System.out.println("deletePath = "+deletePath);
        System.out.println("fileName="+fileName);
        originalPath.add(deletePath+"/"+fileName);
        Path fromPath = new Path(  deletePath+"/"+fileName);
        Path toPath = new Path(recycleBinPath+"/"+fileName);
        try {
            hdfsService.renameFile(fromPath,toPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("fromPath = "+fromPath);
        System.out.println("toPath = "+toPath);
        return "redirect:allFiles";
    }
    @RequestMapping("/rename")
    public String  renameFile(String oldName,String newName) throws IOException {
        String renamePath = pathList.get(pathList.size()-1);
        System.out.println("renamePath = "+renamePath);
        Path fromPath = new Path(  renamePath+"/"+oldName);
        Path toPath = new Path(renamePath+"/"+newName);
        System.out.println("fromPath="+fromPath);
        System.out.println("toPath="+toPath);
        hdfsService.renameFile(fromPath,toPath);
        return "redirect:allFiles";
    }
    @RequestMapping("/backHome")
    public String backHome(ModelMap map){
        pathList.clear();
        pathList.add(normalPath);
        List<HdfsFile> lists = null;
        try {
            lists = hdfsService.getDirectoryFromHdfs(normalPath);
        }catch (Exception e){
            e.printStackTrace();
        }
        map.put("username",Gusername);
        map.put("lists",lists);
        return "allFiles";
    }

    @RequestMapping("/delete2")
    public String delete2(String filePath){
        System.out.println("deletePath = "+filePath);
        System.out.println("filePath="+filePath);
        hdfsService.deleteFromHdfs(filePath);
        return "redirect:pictures";
    }
    @RequestMapping("/delete3")
    public String delete3(String filePath){
        System.out.println("deletePath = "+filePath);
        System.out.println("filePath="+filePath);
        hdfsService.deleteFromHdfs(filePath);
        return "redirect:files";
    }
    @RequestMapping("/delete4")
    public String delete4(String filePath){
        System.out.println("deletePath = "+filePath);
        System.out.println("filePath="+filePath);
        hdfsService.deleteFromHdfs(filePath);
        return "redirect:videos";
    }

    @RequestMapping("/download2")
    public void download2(String filePath, HttpServletResponse response) throws IOException {
        //设置相应类型
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename="+filePath+"");
        InputStream ism = null;
        System.out.println("downloadPaht = "+filePath);
        ism = hdfsService.getFileInputStreamForPath(filePath);
        int len = 0;
        byte[] buf = new byte[1024];
        ServletOutputStream sos = response.getOutputStream();
        while((len=ism.read(buf))!=-1){
            sos.write(buf,0,len);
        }
        System.out.println("下载完成");
        sos.close();
    }
    @RequestMapping("/recycleBin")
    public String recycleBin(ModelMap map){
        List<HdfsFile> lists = null;
        try {
            lists = hdfsService.getDirectoryFromHdfs(recycleBinPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.put("lists",lists);
        map.put("username",Gusername);
        return "recycleBin";
    }
    @RequestMapping("/delete5")
    public String delete5(String filePath){
        hdfsService.deleteFromHdfs(filePath);
        return "redirect:recycleBin";
    }

    @RequestMapping("/recover")
    public String  recover(String filePath,String fileName) throws IOException {
        Path fromPath = new Path(filePath);
        if (originalPath.size() > 0){
            System.out.println(originalPath);
            for (String path : originalPath) {
                String[] split = path.split("/");
                if (fileName.equals(split[split.length - 1])) {
                    Path toPath = new Path(path);
                    originalPath.remove(path);
                    hdfsService.renameFile(fromPath,toPath);
                    System.out.println("fromPath="+fromPath);
                    System.out.println("toPath="+toPath);
                    break;
                }
            }
        }
        return "redirect:recycleBin";
    }

    @RequestMapping("/back")
    public String back(){
        //移除当前路径
        if (pathList.size() > 1){
            pathList.remove(pathList.get(pathList.size()-1));
        }
        return "redirect:allFiles";
    }

    @RequestMapping("/exit")
    public String exit(HttpSession session){
        session.removeAttribute("loginUsername");
        return "redirect:/";
    }
}
