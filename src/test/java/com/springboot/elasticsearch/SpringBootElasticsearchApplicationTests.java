package com.springboot.elasticsearch;

import com.springboot.elasticsearch.bean.Product;
import com.springboot.elasticsearch.respository.ProductESResitory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class SpringBootElasticsearchApplicationTests {
    @Autowired
    private ProductESResitory resitory;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void testcreateOrUpdate() {
        List<Product> list = new ArrayList<>();
        Product p1 = new Product(1L,"huawei shou huan B5",999,"gaoqingcaiping lanyaerji xilvjiance laidianxiaoxi","huawei");
        resitory.save(p1);
        Product p2 = new Product(2L,"huawei shou ji Math40",4999,"gaoqingcaiping lanyaerji laidianxiaoxi tongxin","huawei");
        resitory.save(p2);
        Product p3 = new Product(3L,"pingguo shou ji ApplePro 13",9999,"gaoqingcaiping lanyaerji laidianxiaoxi tongxin","Apple");
        resitory.save(p3);
        Product p4 = new Product(4L,"xiaomi shou huan XSS",499,"gaoqingcaiping lanyaerji xilvjiance laidianxiaoxi","xiaomi");
        resitory.save(p4);
        Product p5 = new Product(5L,"xiaomi shou ji 11",3999,"gaoqingcaiping lanyaerji tongxin laidianxiaoxi","xiaomi");
        resitory.save(p5);
        Product p6 = new Product(6L,"yijia shou ji 6",4000,"gaoqingcaiping lanyaerji tongxin laidianxiaoxi","yijia");
        resitory.save(p6);
        Product p7 = new Product(7L,"yijia shou ji 9Pro",5000,"gaoqingcaiping lanyaerji yijia laidianxiaoxi","yijia");
        resitory.save(p7);
        Product p8 = new Product(8L,"suoni shou huan XXX",999,"gaoqingcaiping lanyaerji xilvjiance laidianxiaoxi","suoni");
        resitory.save(p8);
    }

    // 查询商品标题或简介中符合"蓝牙 指纹 双卡"的字样的商品，并且高亮显示
    @Test
    void testQuery11(){
        List<SearchHit<Product>> byTitleOrIntro = resitory.findByTitleOrIntroContaining("yijia", "yijia");
        for (SearchHit<Product> searchHit:byTitleOrIntro) {
            System.out.println("-->before:"+searchHit.getContent());
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(highlightFields.get("title")==null?searchHit.getContent().getTitle():highlightFields.get("title").get(0));
            searchHit.getContent().setIntro(highlightFields.get("intro")==null?searchHit.getContent().getIntro():highlightFields.get("intro").get(0));
            System.out.println("-->after:"+searchHit.getContent());
        }
    }
    // 查询商品标题或简介中符合"蓝牙 指纹 双卡"的字样的商品，并且高亮显示
    @Test
    void testQuery10(){
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("yijia", "title", "intro"))
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<span style='color:red'>").postTags("</span>"),
                        new HighlightBuilder.Field("intro").preTags("<span style='color:red'>").postTags("</span>"));
        SearchHits<Product> search = elasticsearchRestTemplate.search(builder.build(), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println("-->before:"+searchHit.getContent());
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            searchHit.getContent().setTitle(highlightFields.get("title")==null?searchHit.getContent().getTitle():highlightFields.get("title").get(0));
            searchHit.getContent().setIntro(highlightFields.get("intro")==null?searchHit.getContent().getIntro():highlightFields.get("intro").get(0));
            System.out.println("-->after:"+searchHit.getContent());
        }
    }

    // 按照品牌分组，统计各品牌的价格数据
    @Test
    void testQuery9(){
        TermsAggregationBuilder aggs = AggregationBuilders.terms("groupByBrand").field("brand");
        StatsAggregationBuilder field = AggregationBuilders.stats("statsPrice").field("price");
        aggs.subAggregation(field);
        NativeSearchQuery build = new NativeSearchQueryBuilder().addAggregation(aggs).build();
        SearchHits<Product> search = elasticsearchRestTemplate.search(build,Product.class);
        List<Aggregation> aggregations = search.getAggregations().asList();
        for (Aggregation aggregation:aggregations) {
            System.out.println("aggregation"+aggregation.getClass());
            Terms terms = (Terms) aggregation;
            for (Terms.Bucket bucket:terms.getBuckets()) {
                System.out.println("bucket.getKey()"+bucket.getKey());
                System.out.println("bucket.getDocCount()"+bucket.getDocCount());
                Aggregation statsPrice = bucket.getAggregations().asMap().get("statsPrice");
                System.out.println("statsPrice:"+statsPrice.getClass());
                ParsedStats stats = (ParsedStats)statsPrice;
                System.out.println("AvgAsString--->"+stats.getAvgAsString());
                System.out.println("MaxAsString===>"+stats.getMaxAsString());
                System.out.println("MinAsString--->"+stats.getMinAsString());
                System.out.println("SumAsString--->"+stats.getSumAsString());
            }
        }
    }

    // 按照品牌分组，统计各品牌的平均价格
    @Test
    void testQuery8(){
        TermsAggregationBuilder aggs = AggregationBuilders.terms("groupByBrand").field("brand");
        AvgAggregationBuilder field = AggregationBuilders.avg("AvgPrice").field("price");
        aggs.subAggregation(field);
        NativeSearchQuery build = new NativeSearchQueryBuilder().addAggregation(aggs).build();
        SearchHits<Product> search = elasticsearchRestTemplate.search(build,Product.class);
        List<Aggregation> aggregations = search.getAggregations().asList();
        for (Aggregation aggregation:aggregations) {
            System.out.println("aggregation"+aggregation.getClass());
            Terms terms = (Terms) aggregation;
            for (Terms.Bucket bucket:terms.getBuckets()) {
                System.out.println("bucket.getKey()"+bucket.getKey());
                System.out.println("bucket.getDocCount()"+bucket.getDocCount());
                Aggregation avgPrice = bucket.getAggregations().asMap().get("AvgPrice");
                System.out.println("avgPrice:"+avgPrice.getClass());
                ParsedAvg avg = (ParsedAvg)avgPrice;
                System.out.println("--->"+avg.getType());
                System.out.println("===>"+avg.getValue());
            }
        }
    }
    // 按照品牌分组，统计各品牌的数量
    @Test
    void testQuery7(){
        TermsAggregationBuilder aggs = AggregationBuilders.terms("groupByBrand").field("brand");
        NativeSearchQuery build = new NativeSearchQueryBuilder().addAggregation(aggs).build();
        SearchHits<Product> search = elasticsearchRestTemplate.search(build,Product.class);
        Aggregation groupByBrand = search.getAggregations().get("groupByBrand");
        System.out.println(groupByBrand.getClass());
        if(groupByBrand instanceof ParsedStringTerms){
            List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) groupByBrand).getBuckets();
            for (Terms.Bucket bucket:buckets) {
                System.out.println(bucket.getKey());
                System.out.println(bucket.getDocCount());
            }
        }
    }
    // 查询商品标题中符合"pro"的字样或者价格在1000~3000的商品
    @Test
    void testQuery6(){
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(
                QueryBuilders.boolQuery().
                        should(QueryBuilders.matchQuery("title","Pro")).
                        filter(QueryBuilders.rangeQuery("price").gte(1000).lte(5000))), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }
    // 查询商品标题中符合"i7"的字样并且价格大于7000的商品
    @Test
    void testQuery5(){
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(
                QueryBuilders.boolQuery().
                        must(QueryBuilders.matchQuery("title","13")).
                        filter(QueryBuilders.rangeQuery("price").gt(7000))), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    // 查询商品标题或简介中符合"蓝牙 指纹 双卡"的字样的商品
    @Test
    void testQuery4(){
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(QueryBuilders.multiMatchQuery("tongxin huan", "title", "intro")), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    // 查询商品价格在5000~9000之间商品，按照价格升序排列
    @Test
    void testQuery3(){
        SortBuilder<?> builder = SortBuilders.fieldSort("price").order(SortOrder.ASC);
        List<SortBuilder<?>> list = new ArrayList<>();
        list.add(builder);
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(QueryBuilders.rangeQuery("price").gte(3999).lte(9000),null,list), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    // 查询商品价格等于5000的商品
    @Test
    void testQuery2(){
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(QueryBuilders.matchQuery("price", 5000)), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    // 查询商品标题中符合"游戏 手机"的字样的商品
    @Test
    void testQuery1(){
        SearchHits<Product> search = elasticsearchRestTemplate.search(new NativeSearchQuery(QueryBuilders.matchQuery("title", "youxi ji")), Product.class);
        List<SearchHit<Product>> searchHits = search.getSearchHits();
        for (SearchHit<Product> searchHit:searchHits) {
            System.out.println(searchHit.getContent());
        }
    }

    //分页查询文档按照价格降序排列，显示第2页，每页显示3个
    @Test
    void testPageQuery(){
        Page<Product> page = resitory.findAll(PageRequest.of(1, 3, Sort.by(Sort.Order.desc("price"))));
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        page.forEach(System.out::println);
    }

    @Test
    void testGet(){
        Optional<Product> byId = resitory.findById(7L);
        if(byId.isPresent()){
            System.out.println(byId.get());
        }
    }

    @Test
    void testList(){
        Iterable<Product> all = resitory.findAll();
        for (Product product:all) {
            System.out.println(product);
        }
    }

    @Test
    void testDelete(){
        resitory.deleteById(1l);
    }



}
