package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insert(ArrayList<OrderDetail> orderDetailList);

    List<OrderDetail> getById(Long id);
}
