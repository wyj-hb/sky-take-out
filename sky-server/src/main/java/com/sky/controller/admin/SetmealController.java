package com.sky.controller.admin;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin/setmeal")
@RestController("adminSetmealController")
@Api(tags = "管理端套餐管理")
public class SetmealController{
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @ApiOperation("新建套餐")
    public Result NewSetmeal(@RequestBody SetmealDTO setmealDTO)
    {
        setmealService.save(setmealDTO);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> DishmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        log.info("套餐分页查询:{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.PageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("套餐删除")
    public Result DeleteByids(@RequestParam List<Long> ids)
    {
        log.info("套餐批量删除:{}",ids);
        setmealService.DeleteByids(ids);
        return Result.success();
    }
    @GetMapping("{id}")
    @ApiOperation("根据套餐id查询")
    public Result<SetmealVO> SearchByid(@PathVariable Long id)
    {
        SetmealVO setmealVO = setmealService.SearchByid(id);
        return Result.success(setmealVO);
    }
    @PutMapping
    @ApiOperation("修改套餐")
    public Result ModifySetmeal(@RequestBody SetmealDTO setmealDTO)
    {
        setmealService.SaveSetmeal(setmealDTO);
        return Result.success();
    }

}
