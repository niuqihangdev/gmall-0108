package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.*;

@Controller
public class ItemController {
    @Autowired
    ItemService itemService;

    @GetMapping("{skuId}.html")
    @ResponseBody
    public ResponseVo<ItemVo> lodeData(@PathVariable Long skuId){
        ItemVo itemVo=this.itemService.lodeData(skuId);
        return ResponseVo.ok(itemVo);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /*new Thread(new FutureTask<>(new Callable<String>() {

            @Override
            public String call() throws Exception {
                System.out.println("这是Callable接口实现多线程程序");
                return "。。。。";
            }
        })).start();

        new Thread(new FutureTask<String>(()->{
            System.out.println("这是lamda表达式");
            return "....";
        })).start();*/
        /*ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,5,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));
        threadPoolExecutor.execute(()->{
            System.out.println("通过线程池");
        });*/
        CompletableFuture future=CompletableFuture.supplyAsync(()->{
            System.out.println("这是CompletableFuture创造的多线程");
            return "hello CompletableFuture";
        });
        System.out.println(future.get());

        System.out.println("这是main线程  "+Thread.currentThread().getName());
    }
}
