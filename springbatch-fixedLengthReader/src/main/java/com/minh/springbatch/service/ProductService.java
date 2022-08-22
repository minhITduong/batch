package com.minh.springbatch.service;

import com.minh.springbatch.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    public List<Product> getProducts(){
        RestTemplate restTemplate = new RestTemplate();
        String url="http://localhost:8080/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);
        List<Product> productList =new ArrayList<>();
        for(Product p: products)
            productList.add(p);
        return productList;
    }
}
