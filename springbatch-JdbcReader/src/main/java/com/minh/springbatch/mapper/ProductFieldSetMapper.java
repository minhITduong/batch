package com.minh.springbatch.mapper;

import com.minh.springbatch.model.Product;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class ProductFieldSetMapper implements FieldSetMapper<Product> {
    @Override
    public Product mapFieldSet(FieldSet fieldSet) throws BindException {
        Product product = new Product();
        product.setProductID(fieldSet.readInt(0));
        product.setProdName(fieldSet.readString(1));
        product.setProductDesc(fieldSet.readString(2));
        product.setPrice(fieldSet.readBigDecimal(3));
        product.setUnit(fieldSet.readInt(4));
        return product;
    }
}
