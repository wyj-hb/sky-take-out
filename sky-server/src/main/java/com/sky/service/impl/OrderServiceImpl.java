package com.sky.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.WebSocket.WebSocketServer;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.util.Json;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    /*
    * @Description 向订单中插入数据
    * @Param
    * @return
    **/
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理各种异常:地址簿为空,购物车数据为空
        AddressBook address = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(address == null)
        {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long currentId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(currentId).build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list == null || list.size() == 0)
        {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表中插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        //待付款状态
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(address.getPhone());
        orders.setConsignee(address.getConsignee());
        orders.setUserId(currentId);
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : list)
        {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insert(orderDetailList);
        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(currentId);
        //封装返回结果
        OrderSubmitVO orderSubmit = OrderSubmitVO.builder().id(orders.getId()).orderTime(orders.getOrderTime()).orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount()).build();
        return orderSubmit;
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
        //通过websocket向客户端浏览器推送消息
        Map map = new HashMap();
        map.put("type",1);//1表示来单提醒，2表示客户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:" + outTradeNo);
        String  json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }
    @Override
    public PageResult PageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //先查询所有符合条件的订单(pay_status)
        Page<Orders> orderDetails = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> list = new ArrayList();
        //查询出订单明细，并封装入OrderVO进行响应
        if(orderDetails != null && orderDetails.getTotal() > 0)
        {
            for(Orders orders : orderDetails)
            {
                OrderVO ordervo = new OrderVO();
                Long id = orders.getId();
                //根据id查询订单明细
                List<OrderDetail> l = orderDetailMapper.getById(id);
                ordervo.setOrderDetailList(l);
                BeanUtils.copyProperties(orders,ordervo);
                list.add(ordervo);
            }
        }
        PageResult pageResult = new PageResult();
        pageResult.setRecords(list);
        pageResult.setTotal(orderDetails.getTotal());
        return pageResult;
    }

    @Override
    public void insert(Long id) {
        Orders o = new Orders();
        //获取当前用户id
        Long currentId = BaseContext.getCurrentId();
        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        // 将订单详情转为购物车对象
        List<ShoppingCart> shoppingCarts = orderDetailList.stream().map(
                orderDetail -> {
                    ShoppingCart shoppingCart = new ShoppingCart();
                    BeanUtils.copyProperties(orderDetail,shoppingCart);
                    shoppingCart.setUserId(currentId);
                    shoppingCart.setCreateTime(LocalDateTime.now());
                    return shoppingCart;
                }
        ).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCarts);
    }

    //获取订单详情
    @Override
    public OrderVO getOrderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        //根据订单id查询订单信息
        Orders order = orderMapper.getByid(id);
        BeanUtils.copyProperties(order,orderVO);
        //查询订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getById(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    public void cancelByid(Long id) {
        //查询订单状态
        Orders order = orderMapper.getByid(id);
        // 校验订单是否存在
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (order.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(order.getId());
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查询所有符合条件的订单
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(orders);
        return new PageResult(orders.getTotal(),orderVOList);
    }
    public List<OrderVO> getOrderVOList(Page<Orders> page)
    {
        List<OrderVO> list = new ArrayList<>();
        //遍历每一个订单,并将所有的菜品信息获取出来
        List<Orders> result = page.getResult();
        if (!CollectionUtils.isEmpty(result)) {
            for(Orders o : result)
            {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(o,orderVO);
                String str = GetDishByid(o.getId());
                orderVO.setOrderDishes(str);
                List<OrderDetail> orderDetails = orderDetailMapper.getById(o.getId());
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return list;
    }
    public String GetDishByid(Long id)
    {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());
        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    @Override
    public OrderStatisticsVO statics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();

        // 待接单 2 待派送 3 派送中 4
        orderStatisticsVO.setToBeConfirmed(getStatusNum(2));
        orderStatisticsVO.setConfirmed(getStatusNum(3));
        orderStatisticsVO.setDeliveryInProgress(getStatusNum(4));
        return orderStatisticsVO;
    }
    public int getStatusNum(int status)
    {
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        return orders.size();
    }

    @Override
    public OrderVO GetOrderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        //根据id查询order
        Orders order = orderMapper.getByid(id);
        BeanUtils.copyProperties(order,orderVO);
        //根据id查询order_detail
        List<OrderDetail> orderDetails = orderDetailMapper.getById(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    public void confirm(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(3);
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(4);
        orderMapper.update(orders);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getByid(ordersCancelDTO.getId());
        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //查询出订单,如果不是待接单则不可以拒单
        Orders order = orderMapper.getByid(ordersRejectionDTO.getId());
        //不可以拒单
        if(order.getStatus() > 2)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        else
        {
            //更新订单
            Orders orders = new Orders();
            orders.setId(ordersRejectionDTO.getId());
            orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
            orders.setStatus(6);
            orderMapper.update(orders);
        }
    }
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getByid(id);
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    /*
    * @Description 客户催单
    * @Param
    * @return
    **/
    @Override
    public void remind(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getByid(id);
        if(ordersDB == null)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号："+ordersDB.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
