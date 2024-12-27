package com.sky.controller.admin;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> GetOrderDetail(@PathVariable Long id)
    {
        log.info("查询订单详情:{}",id);
        OrderVO orderVO  = orderservice.GetOrderDetail(id);
        return Result.success(orderVO);
    }
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO)
    {
        log.info("接单");
        orderservice.confirm(ordersConfirmDTO.getId());
        return Result.success();
    }
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable("id") Long id)
    {
        log.info("派送订单");
        orderservice.delivery(id);
        return Result.success();
    }
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO)
    {
        log.info("拒单");
        orderservice.rejection(ordersRejectionDTO);
        return Result.success();
    }
}
