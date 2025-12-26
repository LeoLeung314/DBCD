package com.workshop.task.controller;

import com.workshop.task.entity.Product4746;
import com.workshop.task.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    @Autowired
    private ProductService productService;

    /**
     * 获取所有产品列表
     */
    @GetMapping("/list")
    public Map<String, Object> getProductList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Product4746> productList = productService.list();
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", productList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 新增产品
     */
    @PostMapping("/add")
    public Map<String, Object> addProduct(@RequestBody Product4746 product) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = productService.save(product);
            if (success) {
                result.put("code", 200);
                result.put("message", "产品新增成功");
            } else {
                result.put("code", 500);
                result.put("message", "产品新增失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "新增失败: " + e.getMessage());
        }
        return result;
    }

}