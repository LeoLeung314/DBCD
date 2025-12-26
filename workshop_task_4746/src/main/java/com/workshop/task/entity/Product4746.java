package com.workshop.task.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@TableName("product_4746")
public class Product4746 {

    @TableId
    private String productId;      // 产品编号
    private String productName;    // 产品名称
    private String productType;    // 产品类型
    private String unit;           // 单位
    private String remark;         // 备注
    private Date createTime;       // 创建时间

}
