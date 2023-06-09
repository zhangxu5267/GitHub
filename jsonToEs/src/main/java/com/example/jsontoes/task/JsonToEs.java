package com.example.jsontoes.task;

import com.alibaba.fastjson.JSON;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import com.example.jsontoes.listener.FileListener;
import com.example.jsontoes.listener.FileMonitor;
import io.netty.util.internal.StringUtil;
import lombok.val;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.apache.http.HttpHost;

import javax.annotation.PostConstruct;
import javax.jws.WebParam;
import java.io.*;
import java.util.*;

@Component
//@EnableScheduling
public class JsonToEs {


    @Autowired
    private InformationDao informationDao;



//    @PostConstruct
    public void start_task(){
        System.out.println("======================");
        // 监控间隔
        try {
            /*FileMonitor fileMonitor = new FileMonitor(300_000L);
            fileMonitor.monitor("/python/txt", new FileListener());
            fileMonitor.start();*/
        } catch (Exception e) {
            e.printStackTrace();
        }



    }



    /**
     * 获取指定文件夹下所有文件，不含文件夹里的文件
     *
     * @param dirFilePath 文件夹路径
     * @return
     */
    public List<String> getAllinge(String dirFilePath) {
        List<String> list = new ArrayList<>();
        if (StringUtil.isNullOrEmpty(dirFilePath))
            return list;
         return getAllFile(new File(dirFilePath));
    }


    /**
     * 获取指定文件夹下所有文件，不含文件夹里的文件
     *
     * @param dirFile 文件夹
     * @return
     */
    public List<String> getAllFile(File dirFile) {
        List<String> list = new ArrayList<>();
        // 如果文件夹不存在或着不是文件夹，则返回 null
        if (Objects.isNull(dirFile) || !dirFile.exists() || dirFile.isFile())
            return list;

        File[] childrenFiles = dirFile.listFiles();
        if (Objects.isNull(childrenFiles) || childrenFiles.length == 0)
            return list;

        List<File> files = new ArrayList<>();
        for (File childFile : childrenFiles) {
            // 如果是文件，直接添加到结果集合
            if (childFile.isFile()) {
                files.add(childFile);

                if(childFile.getName().indexOf(".json")>-1){
//                    System.out.println(childFile.getAbsolutePath());
                    list.add(childFile.getAbsolutePath());
                }
            }
            //以下几行代码取消注释后可以将所有子文件夹里的文件也获取到列表里
//            else {
//                // 如果是文件夹，则将其内部文件添加进结果集合
//                List<File> cFiles = getAllFile(childFile);
//                if (Objects.isNull(cFiles) || cFiles.isEmpty()) continue;
//                files.addAll(cFiles);
//            }
        }
        return list;
    }


    public List<String> jsonToEs(String path){
        //读取文件的路径
        List<String> list = new ArrayList<>();
        try {
            list =  getAllinge(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

//    @Scheduled(cron = "0/30 * * * * ?")
//    public void mytask() {
//        jsonToEs();
//    }

}
