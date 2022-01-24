package com.dyg.service;
import com.dyg.entity.HdfsFile;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
public class HdfsService {
    private String hdfsPath = "hdfs://192.168.59.172:9000/es";

//    public static void main(String[] args) throws IOException {
//        HdfsService hd = new HdfsService();
////        hd.getDirectoryFromHdfs();
////        hd.getDirectoryFromHdfs("hdfs://192.168.59.172:9000/es/");
//        hd.copyFile("d:/cisco/localFile.txt");
////        hd.deleteFromHdfs(hd.hdfsPath+"localFile.txt");
////
////        hd.createdir("testfiles");
////        hd.createFile("testfiles.txt");
////
////		InputStream ism = hd.getFileInputStreamForPath("hdfs://192.168.59.173:9000/es/localFile.txt");
////		int len = 0;
////		byte[] buf = new byte[1024];
////		FileOutputStream fos = new FileOutputStream("d://WorkSpace/hdfsDownload.txt");
////		while((len=ism.read(buf))!=-1){
////			fos.write(buf,0,len);
////		}
////        System.out.println("下载完成");
////		fos.close();
//    }

    /**
     * 上传文件
     * @param local 本地路径
     * @throws IOException
     */
    public void copyFile(String local,String hdfs) throws IOException {
        //指定当前的Hadoop的用户
        System.setProperty("HADOOP_USER_NAME", "root");

        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(hdfs),conf);
        //remote---/用户/用户下的文件或文件夹
        fs.copyFromLocalFile(new Path(local), new Path(hdfs));
        System.out.println("copy from: " + local + " to " + hdfs);
        fs.close();
    }

    /**
     * 删除hdfs中的文件
     * @param deletePath  hdfs文件的绝对路径
     */
    public  void deleteFromHdfs(String deletePath)  {
        System.out.println("要删除的文件为："+deletePath);
//        deletePath = hdfsPath + deletePath;
        //指定当前的Hadoop的用户
        System.setProperty("HADOOP_USER_NAME", "root");
        Configuration conf = new Configuration();
        try{

            FileSystem fs = FileSystem.get(URI.create(deletePath), conf);
            fs.deleteOnExit(new Path(deletePath));
            fs.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 创建新目录
     * @param dirpath  要创建的文件夹的名称
     */
    public void createdir(String dirpath){
        try {
            //指定当前的Hadoop的用户
            System.setProperty("HADOOP_USER_NAME", "root");

//            String dirname= hdfsPath+dirpath;

            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(URI.create(dirpath), conf);
            Path f=new Path(dirpath);
            if (!fs.exists(new Path(dirpath))) {

//                创建文件夹
                fs.mkdirs(f);
                System.out.println("创建成功！");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 创建新文件
     * @param filePath  要创建的文件名称
     */
    public void createFile(String filePath){
        try {
            //指定当前的Hadoop的用户
            System.setProperty("HADOOP_USER_NAME", "root");

            String dirname= hdfsPath+filePath;

            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(URI.create(dirname), conf);
            Path f=new Path(dirname);
            if (!fs.exists(new Path(dirname))) {
                //创建文件
                fs.create(f);
                System.out.println("创建成功！");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 获取文件输入流
     * @param strpath 文件的hdfs绝对路径
     * @return
     * @throws IOException
     */
    public InputStream getFileInputStreamForPath(String strpath) throws IOException{
        //指定当前的Hadoop的用户
        System.setProperty("HADOOP_USER_NAME", "root");
        Configuration conf = new Configuration();
        conf.set("fs.default.name", strpath);
        FileSystem fs = FileSystem.get(conf);
        FSDataInputStream open = fs.open(new Path(strpath));
        return open;
    }

    List<HdfsFile> list2=new ArrayList<>();
    /**遍历HDFS上的文件和目录*/
    public void getAllFiles(String path)  {
        //指定当前的Hadoop的用户
        System.setProperty("HADOOP_USER_NAME", "root");
//        String dst = hdfsPath;
        Configuration conf = new Configuration();
        try{
            FileSystem fs = FileSystem.get(URI.create(path), conf);
            FileStatus[] fileStatuses = fs.listStatus(new Path(path));
            if(fileStatuses != null)
                for (FileStatus f : fileStatuses) {
                    if (f.isDirectory()){
                        getAllFiles(f.getPath().toString());
                    }else {
//                        System.out.printf("name: %s, path: %s, folder:%s ,size: %d\n", f.getPath().getName(),f.getPath(), f.isDir(), f.getLen());
                        HdfsFile hf = new HdfsFile(null,f.getPath().getName(),f.isDirectory(),f.getLen(),f.getPath());
                        list2.add(hf);
                    }
                }
            fs.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /**遍历HDFS上的文件和目录*/
    public  List<HdfsFile> getDirectoryFromHdfs(String path) throws FileNotFoundException,IOException {

        //指定当前的Hadoop的用户
        System.setProperty("HADOOP_USER_NAME", "root");
//        FileStatus[] list=null;
        List<HdfsFile> list = new ArrayList<>();
        try{
            Configuration conf = new Configuration();
            String dst = hdfsPath;
            if(path.length()>0){
                dst=path;
            }
            FileSystem fs = FileSystem.get(URI.create(dst), conf);
            FileStatus[] fileStatuses = fs.listStatus(new Path(dst));
            if(fileStatuses != null)
                for (FileStatus f : fileStatuses) {
                    HdfsFile file = new HdfsFile(null,f.getPath().getName(),f.isDirectory(),f.getLen(),f.getPath());
                    list.add(file);
//                    System.out.printf("name: %s, path: %s, folder:%s ,size: %d\n", f.getPath().getName(),f.getPath(), f.isDir(), f.getLen());
                }
            fs.close();

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return  list;
    }
    public void renameFile(Path from,Path to) throws IOException {
        System.setProperty("HADOOP_USER_NAME", "root");
        Configuration conf = new Configuration();
        Path path = new Path(hdfsPath);
        FileSystem fs = path.getFileSystem(conf);
//        FileSystem fs = FileSystem.get(conf);
        fs.rename(from,to);
        System.out.println("重命名成功");
    }
    public boolean searchFile(Path filePath) throws IOException {
        System.setProperty("HADOOP_USER_NAME", "root");
        String dst = "hdfs://Dingyuanguang1:9000/es/";
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(URI.create(dst), conf);
        return fs.exists(filePath);
    }

    public List<HdfsFile> getList2() {
        return list2;
    }
}
