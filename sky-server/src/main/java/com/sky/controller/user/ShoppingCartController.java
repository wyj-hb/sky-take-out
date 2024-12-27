package com.sky.controller.user;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RequestMapping("/user/shoppingCart")
@RestController
@Api(tags = "用户端购物车操作")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("添加购物车，商品信息为:{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @PostMapping("/sub")
    @ApiOperation("删除购物车中的一个商品")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO)
    {
        log.info("删除购物车中的一个物品，商品信息为:{}",shoppingCartDTO);
        shoppingCartService.deleteShoppingCart(shoppingCartDTO);
        return Result.success();
    }
    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list()
    {
        List<ShoppingCart> list =  shoppingCartService.showShoppingCart();
        return Result.success(list);
    }
    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    private Result deleteShoppingCart()
    {
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }
}
