package com.example.jsontoes.listener;

import com.alibaba.fastjson.JSON;
import com.example.jsontoes.anync.JsonToESAsync;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileListener extends FileAlterationListenerAdaptor {

    @Autowired
    private InformationDao informationDao;

//    @Autowired
//    private JsonToESAsync jsonToESAsync;

    @Override
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
         System.out.println("一轮轮询开始，被监视路径：" + observer.getDirectory());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
    }

    @Override
    public void onDirectoryCreate(File directory) {
        System.out.println("创建文件夹：" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryChange(File directory) {
        System.out.println("修改文件夹：" + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File directory) {
        System.out.println("删除文件夹：" + directory.getAbsolutePath());
    }

    public Model parseJsonFileToBean(String path) {
        try {
            File file = new File(path);
            if (file.isFile() && file.exists() && file.canRead()) {
                String encoding = "UTF-8";
                InputStreamReader in;
                in = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(in);
                String lineTxt = "";
                StringBuilder sb = new StringBuilder(lineTxt);
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (!lineTxt.trim().equals("")) {
                        sb.append(lineTxt);
                    }
                }
                lineTxt = sb.toString();
                in.close();
                return JSON.parseObject(lineTxt,Model.class);
            }else {
                System.out.println("找不到指定文件");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //    @Async
    public void toes(String path){
        System.out.println("toes==="+path);
        try {
            Model model = parseJsonFileToBean(path);
            if(model != null){
                System.out.println(model.toString());
                informationDao.save(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFileCreate(File file) {
        String compressedPath = file.getAbsolutePath();
        System.out.println("新建文件：" + compressedPath);

        if (file.canRead()) {
            // TODO 读取或重新加载文件内容
//            System.out.println("文件变更，进行处理");
            try {
                JsonToESAsync.toes(compressedPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileChange(File file) {
        String compressedPath = file.getAbsolutePath();
        System.out.println("修改文件：" + compressedPath);
    }

    @Override
    public void onFileDelete(File file) {
        System.out.println("删除文件：" + file.getAbsolutePath());
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
        // System.out.println("一轮轮询结束，被监视路径：" + fileAlterationObserver.getDirectory());
    }
}
