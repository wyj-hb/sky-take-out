package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
