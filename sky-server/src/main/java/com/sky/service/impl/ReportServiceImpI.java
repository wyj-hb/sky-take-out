package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;
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

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //1.查询数据库获得营业数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
        //2.查询到的数据写入到excel文件中
        //基于已有文件创建模板文件
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);
            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            for(int i = 0;i<30;i++)
            {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //3.通过输出流将Excel文件下载到客户端
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
