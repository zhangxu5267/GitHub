package com.example.jsontoes.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author DragonWu
 * @date 2022-09-19 15:53
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
// Document设置ES里的索引名
@Document(indexName = "model")
public class Model {


    @Id
    @Field(type = FieldType.Text)
    private String id;

    //store=true设置存储，index=false表示不进行索引搜索
    @Field(type = FieldType.Text)
    private String doc_id;

    //analyzer设置分词器
    @Field(type = FieldType.Text)
    private String doc_title;

    //analyzer设置分词器
    @Field(type = FieldType.Text)
    private String doc_catalog;

    //analyzer设置分词器
    @Field(type = FieldType.Text)
    private String doc_content;

    @Field(type = FieldType.Text)
    private String main_content;
}
