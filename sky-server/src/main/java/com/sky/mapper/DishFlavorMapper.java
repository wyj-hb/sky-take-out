package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //批量插入口味数据
    void insertBatch(List<DishFlavor> flavors);
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleByDishId(Long dishId);
    void deleByDishIds(List<Long> dishIds);
    //根据菜品id查询对应的口味数据
    @Select("select * from dish_flavor where dish_id = #{iid}")
    List<DishFlavor> getByDishId(Long dishId);
}
