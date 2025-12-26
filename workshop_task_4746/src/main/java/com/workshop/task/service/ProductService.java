package com.workshop.task.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.workshop.task.entity.Product4746;
import com.workshop.task.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product4746> {
    // 这个类可以继承 ServiceImpl 获得所有基础操作方法
    // selectList、selectById、save、update、delete 等
}
