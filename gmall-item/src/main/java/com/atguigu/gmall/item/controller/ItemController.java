package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.*;

@Controller
public class ItemController {
    @Autowired
    ItemService itemService;

    @GetMapping("{skuId}.html")
    public String lodeData(@PathVariable Long skuId, Model model){
        ItemVo itemVo=this.itemService.lodeData(skuId);
        model.addAttribute("itemVo",itemVo);

        return "item";
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
        /*CompletableFuture future=CompletableFuture.supplyAsync(()->{
            System.out.println("这是CompletableFuture创造的多线程");
            return "hello CompletableFuture";
        }).whenCompleteAsync((t,u)->{
            System.out.println("================whenCompleteAsync==================");
            System.out.println("上一个任务的返回结果集"+t);
            System.out.println("上一个任务的异常信息"+u);
        }).exceptionally(t->{
           System.out.println("异常信息"+t);
           return null;
        });*/
        //System.out.println(future.get());
        /*CompletableFuture.supplyAsync(()->{
            System.out.println("这是CompletableFuture创造的多线程");
            return "hello CompletableFuture";
        }).thenApplyAsync(t->{
            System.out.println("-------------thenApplyAsync---------------");
            System.out.println("t:"+t);
            return "hello thenApplyAsync";
        }).thenAcceptAsync(t->{
            System.out.println(t);
        }).whenCompleteAsync((t,u)->{
            System.out.println("================whenCompleteAsync==================");
            System.out.println("上一个任务的返回结果集"+t);
            System.out.println("上一个任务的异常信息"+u);
        }).exceptionally(t->{
            System.out.println("异常信息"+t);
            return null;
        });*/

        CompletableFuture future=CompletableFuture.supplyAsync(()->{
            System.out.println("这是CompletableFuture创造的多线程");
            return "hello CompletableFuture";
        });
        future.thenApplyAsync(t->{
            System.out.println("-------------thenApplyAsync---------------");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t:"+t);
            return "hello thenApplyAsync";
        });
        future.thenApplyAsync(t->{
            System.out.println("-------------thenApplyAsync---------------");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t:"+t);
            return "hello thenApplyAsync";
        });
        future.thenApplyAsync(t->{
            System.out.println("-------------thenApplyAsync---------------");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("t:"+t);
            return "hello thenApplyAsync";
        });
        CompletableFuture.runAsync(()->{
            System.out.println("新任务");
        });
        System.out.println("这是main线程  "+Thread.currentThread().getName());
    }
}
