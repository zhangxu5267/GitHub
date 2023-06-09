package com.example.jsontoes.dao;


import com.example.jsontoes.entity.Model;
import org.elasticsearch.common.text.Text;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author DragonWu
 * @date 2022-09-19 16:04
 **/
public interface InformationDao extends ElasticsearchRepository<Model, Text> {

}
