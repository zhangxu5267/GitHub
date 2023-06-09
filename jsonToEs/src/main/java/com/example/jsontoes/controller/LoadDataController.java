package com.example.jsontoes.controller;

import com.alibaba.fastjson.JSON;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import com.example.jsontoes.service.ModelService;
import com.example.jsontoes.task.JsonToEs;
import com.example.jsontoes.util.Result;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class LoadDataController {

    @Value(value = "${load.json.path}")
    private String loadJsonPath;

    @Resource
    private InformationDao informationDao;

    @Resource
    private JsonToEs jsonToEs;

    @Autowired
    @Lazy
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private ModelService modelService;


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
                return JSON.parseObject(lineTxt, Model.class);
            } else {
                System.out.println("找不到指定文件");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping(value = "/load/json")
    public String loadJson(@RequestParam(value = "path", required = false) String path) {
        String res = "";
        //加载路径
        String loadPath = "";
        try {
            if (StringUtils.isNotEmpty(path)) {
                loadPath = path;
            } else {
                loadPath = loadJsonPath;
            }
            res = loadPath;
            List<Model> list = new ArrayList<>();
            List<String> listPath = jsonToEs.jsonToEs(loadPath);
            for (String pat : listPath) {
                Model model = parseJsonFileToBean(pat);
                if (model != null) {
                    model.setId(model.getDoc_id());
                    list.add(model);
                }
            }

            informationDao.saveAll(list);
            System.out.println(loadPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /***
     * es更新字段
     */
    @GetMapping(value = "/update/json")
    public String updateJson(@RequestParam(value = "path", required = false) String path) {
        String res = "";
            List<UpdateQuery> updateQueryList = new ArrayList<>();

            String loadPath = "";
            try {
                if (StringUtils.isNotEmpty(path)) {
                    loadPath = path;
                } else {
                    return "";
                }
                List<String> listPath = jsonToEs.jsonToEs(loadPath);
                for (String pat : listPath) {
                    Model model = parseJsonFileToBean(pat);
                    if (model != null) {
                        //创建文档对象
                        Document document = Document.create();
                        document.putIfAbsent("main_content", model.getMain_content());//赋值收藏量

                        document.setId(model.getDoc_id());//文档id
                        //创建更新查询
                        UpdateQuery build = UpdateQuery.builder(model.getDoc_id()).withDocument(document).withScriptedUpsert(true).build();
                        updateQueryList.add(build);
                    }
                }

                restTemplate.bulkUpdate(updateQueryList, IndexCoordinates.of("model"));
            } catch (Exception e) {
                res = "fail";
                e.printStackTrace();
            }
            res = "success";

        return res;
    }

    /**
     * 自动化更新
     */
    /***
     * es更新字段
     */
    @GetMapping(value = "/update/jsonauto")
    public Result updateJsonAuto(@RequestParam(value = "path", required = false) String path) {
        String res = "success";

        try {
            modelService.updateJsonDocId(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok();
    }

    @GetMapping(value = "/load/loadVector")
    public Result loadVector(@RequestParam(value = "path", required = false) String path) {
        String res = "success";

        try {
            modelService.loadVector(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok();
    }
}
