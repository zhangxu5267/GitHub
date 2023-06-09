package com.example.jsontoes.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import com.example.jsontoes.service.ModelService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class XxlDemoHandler {

    @Value(value = "${load.json.path}")
    private String loadJsonPath;

    @Resource
    private JsonToEs jsonToEs;

    @Resource
    private InformationDao informationDao;

    @Resource
    private ModelService modelService;

    @XxlJob("Demo1")
    public ReturnT<String> demo(){
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("测试开始");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(param);
        System.out.println("测试完成！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
        System.out.println();
        System.out.println();
        System.out.println();
        XxlJobHelper.log("测试开结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 每天定时将json数据导入es
     */
    @XxlJob("JsonToEs")
    public ReturnT<String> jsonToEs(){
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("测试开始");
        String today= DateUtil.today();
        String dirPath = loadJsonPath+ File.separator+"save-"+today;
        List<String> listPath = jsonToEs.jsonToEs(dirPath);
        if(listPath.size()>0){
            System.out.println("处理文件--------------");
            try {
                List<Model> list = new ArrayList<>();
                for (String pat : listPath) {
                    Model model = modelService.parseJsonFileToBean(pat);
                    if (model != null) {
                        model.setId(model.getDoc_id());
                        list.add(model);
                    }
                }
                informationDao.saveAll(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("今日没有数据导入--------------------------");
        }
        XxlJobHelper.log("测试开结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 每天定时创建文件夹
     * @return
     */
    @XxlJob("CreateDir")
    public ReturnT<String> createDir(){
        String msg = "采集成功";
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("开始");
        System.out.println(param);
        //当前日期字符串，格式：yyyy-MM-dd
        String today= DateUtil.today();
        String dirPath = loadJsonPath+ File.separator+"save-"+today;
        boolean exist1 = FileUtil.exist(dirPath);
        if(exist1){
            System.out.println(dirPath+"已存在");
        }else{
            FileUtil.mkdir(dirPath);
            System.out.println("创建"+dirPath+"的文件夹！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
        }

        XxlJobHelper.log("结束");
        return ReturnT.SUCCESS;
    }
}

