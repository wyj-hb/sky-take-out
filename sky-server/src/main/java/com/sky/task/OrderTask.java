package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    /*
    * @Description 处理订单超市
    * @Param
    * @return
    **/
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder()
    {
        log.info("定时处理超时订单:{}", LocalDateTime.now());
        //查询待付款状态的订单并且订单时间超过15分钟
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(list != null && list.size() > 0)
        {
            for(Orders o : list)
            {
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason("订单超时自动取消");
                o.setCancelTime(LocalDateTime.now());
                orderMapper.update(o);
            }
        }
    }
    /*
    * @Description 处理未完成订单
    * @Param
    * @return
    **/
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点出发一次
    public void processDeliveryOrder()
    {
        log.info("定时处理派送中的订单:{}",LocalDateTime.now());
        //查询待付款状态的订单并且订单时间超过15分钟
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if(list != null && list.size() > 0)
        {
            for(Orders o : list)
            {
                o.setStatus(Orders.COMPLETED);
                orderMapper.update(o);
            }
        }
    }
}
