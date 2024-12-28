package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpI implements ReportService
{
    /*
    * @Description 统计指定时间内的营业额数据
    * @Param
    * @return
    **/
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //查询起始终止日期
        List<LocalDate> l1 = new ArrayList<>();
        LocalDate tmp = begin;
        while(!tmp.equals(end))
        {
            //日期计算
            l1.add(tmp);
            tmp = tmp.plusDays(1);
        }
        String datalist = StringUtils.join(l1, ",");
        //获取金额
        ArrayList<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : l1)
        {
            //查询date日期对应的营业额数据，营业额是指:状态未"已完成"的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//起始
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//终止
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover =  orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String turnOverList = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder().dateList(datalist).turnoverList(turnOverList).build();
    }
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //查询起始终止日期
        List<LocalDate> l1 = new ArrayList<>();
        LocalDate tmp = begin;
        while(!tmp.equals(end))
        {
            //日期计算
            l1.add(tmp);
            tmp = tmp.plusDays(1);
        }
        String datalist = StringUtils.join(l1, ",");
        //查询用户总量
        List<Integer> totalUserList = new ArrayList<>();
        //查询新增用户
        List<Integer> newUserList = new ArrayList<>();
        for(LocalDate date : l1)
        {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//起始
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//终止
            Map map = new HashMap();
            //总用户数量
            map.put("end",endTime);
            Integer count = userMapper.countBymap(map);
            //新增用户数量
            map.put("begin",beginTime);
            count = userMapper.countBymap(map);
            newUserList.add(count);
        }
        return UserReportVO.builder().dateList(datalist).totalUserList(StringUtils.join(totalUserList,",")).newUserList(StringUtils.join(newUserList,",")).build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //查询起始终止日期
        List<LocalDate> l1 = new ArrayList<>();
        LocalDate tmp = begin;
        while(!tmp.equals(end))
        {
            //日期计算
            l1.add(tmp);
            tmp = tmp.plusDays(1);
        }
        String datalist = StringUtils.join(l1, ",");
        //每天订单总数
        List<Integer> totalOrderList = new ArrayList<>();
        //每天有效订单总数
        List<Integer> ValidOrderList = new ArrayList<>();
        for(LocalDate date : l1)
        {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);//起始
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);//终止
            //查询每天订单总数
            Integer sum = getOrderCount(beginTime, endTime, null);
            //查询有效订单数
            Integer valid = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            totalOrderList.add(sum);
            ValidOrderList.add(valid);
        }
        Integer total = totalOrderList.stream().reduce(Integer::sum).get();
        Integer valid = ValidOrderList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = 0.0;
        if(total != 0)
        {
            orderCompletionRate = valid.doubleValue() / total;
        }
        return OrderReportVO.builder()
                .dateList(datalist)
                .orderCountList(StringUtils.join(totalOrderList,","))
                .validOrderCountList(StringUtils.join(ValidOrderList,","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(total)
                .validOrderCount(valid)
                .build();
    }
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status)
    {
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status", status);
        return orderMapper.countByMap(map);
    }

    @Override
    public SalesTop10ReportVO getDishTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);//起始
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);//终止
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(beginTime, endTime);
        //获取菜品列表
        List<String> names = salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String namelist = StringUtils.join(names, ",");
        //获取销量列表
        List<Integer> numbers = salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String nums = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder().nameList(namelist).numberList(nums).build();
    }
}
