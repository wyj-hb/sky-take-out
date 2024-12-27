package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderservice;
    @GetMapping("/conditionSearch")
    @ApiOperation("订单条件搜索")
    public Result<PageResult> ConditionSearch(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("订单条件搜索:{}",ordersPageQueryDTO);
        PageResult pageResult = orderservice.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> OrdersStatics()
    {
        log.info("统计各个状态的订单数量");
        OrderStatisticsVO orderStatisticsVO = orderservice.statics();
        return Result.success(orderStatisticsVO);
    }
}
