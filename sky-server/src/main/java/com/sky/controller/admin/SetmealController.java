package com.sky.controller.admin;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
}
