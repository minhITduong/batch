package com.minh.springbatch.mapper;

import com.minh.springbatch.model.Product;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class ProductFieldSetMapperByName implements FieldSetMapper<Product> {
    @Override
    public Product mapFieldSet(FieldSet fieldSet) {

        if (fieldSet == null) {
            return null;
        }

        Product product = new Product();
        product.setProductID(fieldSet.readInt("productID"));
        product.setProdName(fieldSet.readString("productName"));
        product.setProductDesc(fieldSet.readString("ProductDesc"));
        product.setPrice(fieldSet.readBigDecimal("price"));
        product.setUnit(fieldSet.readInt("unit"));

        return product;
    }
}
