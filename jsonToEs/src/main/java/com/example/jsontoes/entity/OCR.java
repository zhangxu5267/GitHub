package com.example.jsontoes.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
// Document设置ES里的索引名
@Document(indexName = "ocr")
public class OCR {
    @Field(type = FieldType.Text)
    private String doc_title;

    @Field(type = FieldType.Float)
    private Float[] title_vec;
}
