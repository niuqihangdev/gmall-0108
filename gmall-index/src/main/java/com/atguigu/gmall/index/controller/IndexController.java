package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    IndexService indexService;

    @GetMapping
    public String toIndex(Model model){
        List<CategoryEntity> categories= this.indexService.queryLvl1Categories();
        model.addAttribute("categories",categories);
        return "index";
    }

    @GetMapping("/index/cates/{pid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> queryLvl2Categories(@PathVariable Long pid){
        List<CategoryEntity> categoryEntities=this.indexService.queryLvl2Categories(pid);
        return ResponseVo.ok(categoryEntities);
    }

    @GetMapping("/index/lock")
    @ResponseBody
    public ResponseVo testLock(){
        this.indexService.testLock();
        return ResponseVo.ok();
    }

    @GetMapping("/index/lock2")
    @ResponseBody
    public ResponseVo testLock2(){
        this.indexService.testLock2();
        return ResponseVo.ok();
    }

    @GetMapping("/index/lock3")
    @ResponseBody
    public ResponseVo testLock3(){
        this.indexService.testLock3();
        return ResponseVo.ok();
    }

    /**
     * 测试读写锁
     * @return
     */
    @GetMapping("/index/read")
    @ResponseBody
    public ResponseVo testRead(){
        this.indexService.testRead();
        return ResponseVo.ok();
    }

    @GetMapping("/index/write")
    @ResponseBody
    public ResponseVo testWrite(){
        this.indexService.testWrite();
        return ResponseVo.ok();
    }

    /**
     * 测试读闭锁
     * @return
     */
    @GetMapping("/index/latch")
    @ResponseBody
    public ResponseVo testLatch(){
        this.indexService.testLatch();
        return ResponseVo.ok("班长锁门");
    }

    @GetMapping("/index/countDown")
    @ResponseBody
    public ResponseVo testCountDown(){
        this.indexService.testCountDown();
        return ResponseVo.ok("出来一个同学");
    }
}

