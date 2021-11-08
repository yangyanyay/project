package com.springboot.elasticsearch.bean;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products",shards = 5)
public class Product {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String title;
    private Integer price;
    @Field(type = FieldType.Text)
    private String intro;
    @Field(type = FieldType.Keyword)
    private String brand;

    public Product() {
    }

    public Product(Long id, String title, Integer price, String intro, String brand) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.intro = intro;
        this.brand = brand;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", intro='" + intro + '\'' +
                ", brand='" + brand + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
