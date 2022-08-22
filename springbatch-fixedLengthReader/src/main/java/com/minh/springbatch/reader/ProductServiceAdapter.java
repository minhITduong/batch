package com.minh.springbatch.reader;

import com.minh.springbatch.model.Product;
import com.minh.springbatch.service.ProductService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ProductServiceAdapter implements InitializingBean {

    @Autowired
    private ProductService service;

    private List<Product> products;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.products = service.getProducts();
    }

    public Product nextProduct(){
        if ( products.size() >0){
            return products.remove(0);
        }else
            return null;

    }

    public ProductService getService() {
        return service;
    }

    public void setService(ProductService service) {
        this.service = service;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
