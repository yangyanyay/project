package com.springboot.elasticsearch.respository;

import com.springboot.elasticsearch.bean.Product;
import com.sun.org.glassfish.gmbal.ParameterNames;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.List;

@Repository
public interface ProductESResitory extends ElasticsearchRepository<Product,Long> {

    @Highlight(fields={
            @HighlightField(name = "title"),
            @HighlightField(name = "intro")
     },parameters = @HighlightParameters(
            preTags = "<span style='color:red'>",
            postTags = "</span>"
    ))
    // 查询商品标题或简介中符合"蓝牙 指纹 双卡"的字样的商品，并且高亮显示
    List<SearchHit<Product>> findByTitleOrIntroContaining(String title, String intro);
}
