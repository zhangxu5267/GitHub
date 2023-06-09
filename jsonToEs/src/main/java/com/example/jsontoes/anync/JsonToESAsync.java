package com.example.jsontoes.anync;

import com.alibaba.fastjson.JSON;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Map;

//@EnableAsync
@Component
public class JsonToESAsync {

    @Autowired
    private InformationDao informationDao;

    public static JsonToESAsync jsonToESAsync;

    @PostConstruct
    public void init() {
        System.out.println("初始化静态方法--------------");
        jsonToESAsync = this;
        jsonToESAsync.informationDao = this.informationDao;
    }

    /* 读文件(json) */
    public Map<String, Object> parseFile(String path) {
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
                return JSON.parseObject(lineTxt);
            }else {
                System.out.println("找不到指定文件");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Model parseJsonFileToBean(String path) {
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
    public static void toes(String path){
//        System.out.println("toes==="+path);
        try {
            if(path.indexOf(".json")>-1){
                Model model = parseJsonFileToBean(path);
                if(model != null){
//                System.out.println(model.toString());
                    jsonToESAsync.informationDao.save(model);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
