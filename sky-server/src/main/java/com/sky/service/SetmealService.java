package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface SetmealService {

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    List<DishItemVO> getDishItemById(Long id);
    //保存新套餐
    void save(SetmealDTO setmealDTO);
    //分页套餐查询
    PageResult PageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void DeleteByids(List<Long> ids);
}
