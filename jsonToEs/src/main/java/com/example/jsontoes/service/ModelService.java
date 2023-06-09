package com.example.jsontoes.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.jsontoes.dao.InformationDao;
import com.example.jsontoes.entity.Model;
import com.example.jsontoes.entity.OCR;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class ModelService {

    @Value(value = "${http.file.url}")
    private String http_url;

    @Resource
    private ElasticsearchRestTemplate restTemplate;

    //定义线程池
    private static final String POOL_NAME_PREFIX = "create-job";
    private static final Long KEEP_ALIVE_TIME = 60L;
    private static final int APS = Runtime.getRuntime().availableProcessors();
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            APS * 2,
            APS * 4,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(256),
            new ThreadFactoryBuilder().setNamePrefix(POOL_NAME_PREFIX + "-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 将json文件转换成实体
     * @param path
     * @return
     */
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

    /**
     * 加载向量vector
     */
    public String loadVector(String path){
        String res = "";
        List<OCR> ocrs = new ArrayList<>();
        List<File> fileList = cn.hutool.core.io.FileUtil.loopFiles(new File(path), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".json")) {
                    return true;
                }
                return false;
            }
        });
        int cnt = 1;
        for(File infile : fileList){
            //将数据转成需要的
            try {
                cn.hutool.json.JSONObject json = JSONUtil.readJSONObject(new File(infile.getAbsolutePath()), Charset.forName("utf-8"));
                String doc_vector = json.get("title_vec") + "";
                String[] strings = StrUtil.splitToArray(doc_vector, ",");
                Float[] floats = Convert.toFloatArray(strings);
                OCR ocr = new OCR();
                ocr.setDoc_title(json.get("doc_title")+"");
                ocr.setTitle_vec(floats);
                ocrs.add(ocr);
            } catch (IORuntimeException e) {
                e.printStackTrace();
            }
            cnt++;
        }
        List<IndexQuery> indexQueryList = transToIndexQuery(ocrs);
//分批次插入  这个量可以更大 视服务器性能而定（带宽/数据量大小/服务器性能）
        int last;
        int batchSize = 500;
        for (int i = 0; i < indexQueryList.size()/batchSize + 1; i++) {
            last  = Math.min((i + 1) * batchSize, indexQueryList.size());
            restTemplate.bulkIndex(indexQueryList.subList(i*batchSize, last), IndexCoordinates.of("ocr"));
        }
        return res;
    }

    public List<IndexQuery> transToIndexQuery(List<OCR> list){
        List<IndexQuery> indexQueryList = new ArrayList<>();
        try {
            list.forEach(e->{
                IndexQuery query = new IndexQuery();
                query.setObject(e);
                indexQueryList.add(query);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indexQueryList;
    }

    /**
     * 更新json文件的doc_id
     */
    public String updateJsonDocId(String path){
        String res = "";
        int cnt = 1;
        List<Model> modelList = new ArrayList<>();
        List<File> fileList = cn.hutool.core.io.FileUtil.loopFiles(new File(path), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".json")) {
                    return true;
                }
                return false;
            }
        });
        try {
            //线程计数器
            final CountDownLatch latch = new CountDownLatch(fileList.size());
            for(File infile : fileList){
                //由线程池执行
                THREAD_POOL_EXECUTOR.submit(() -> {
                    try {
                        String url_param = http_url+"?fileName="+ StrUtil.removeSuffixIgnoreCase(infile.getName(),".json")+"*";
                        String result= HttpUtil.createGet(url_param).execute().body();
                        JSONObject fileJsonObject = JSON.parseObject(result);
                        JSONArray fileArray = JSON.parseArray(fileJsonObject.getJSONObject("result").get("records") + "");
                        if(fileArray.size()>0){
                            cn.hutool.json.JSONObject json = JSONUtil.readJSONObject(new File(infile.getAbsolutePath()), Charset.forName("utf-8"));
                            System.out.println(Thread.currentThread().getName()+"执行"+latch.getCount()+"="+StrUtil.removeSuffixIgnoreCase(infile.getName(),".json"));
                            modelList.add(JSON.parseObject(json.toString(),Model.class));
                        }
                    } finally {
                        if (latch != null) {
                            latch.countDown();
                        }
                    }
                });
                cnt++;
            }
            latch.await();
            System.out.println("------"+modelList.size());
        } catch (Exception ex) {

        }
        return res;
    }

    /**
     * 重新生成json文件
     */
    public void recreateJson(cn.hutool.json.JSONObject jsonObject,File infile) throws IOException {
        BufferedWriter bw = null;
        if(jsonObject != null){
            try {
                bw = new BufferedWriter(new FileWriter("D:\\youxi\\upFiles\\deploy\\afterTransJson\\json-2023-04-16\\"+infile.getName()));
                bw.write(jsonObject.toString());//转化成字符串再写
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                bw.close();
            }
        }
    }
}
