package com.minh.springbatch.mapper;

import com.minh.springbatch.model.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<Product> {
        public static final String ID_COLUMN = "prod_id";
        public static final String NAME_COLUMN = "prod_name";
        public static final String PRICE_COLUMN = "price";
        public static final String UNIT_COLUMN = "unit";
        public static final String DESC_COLUMN = "prod_desc";


        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            Product product = new Product();

            product.setProductID(rs.getInt(ID_COLUMN));
            product.setProdName(rs.getString(NAME_COLUMN));
            product.setProductDesc(rs.getString(DESC_COLUMN));
            product.setPrice(rs.getBigDecimal(PRICE_COLUMN));
            product.setUnit(rs.getInt(UNIT_COLUMN));
            return product;
        }
}
