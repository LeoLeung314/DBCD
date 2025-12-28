package com.workshop.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workshop.task.entity.vo.TaskProgressVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface TaskProgressMapper extends BaseMapper<TaskProgressVo> {
    // 用于查询视图 v_task_progress_4746 中的数据

    /**
     * 获取所有由任务关联的产品列表（用于下拉选择）
     */
    @Select("SELECT DISTINCT p.product_id, p.product_name " +
            "FROM product_4746 p " +
            "JOIN task_4746 t ON p.product_id = t.product_id")
    List<Map<String, Object>> selectDistinctProducts();

    /**
     * 按时间粒度统计某产品的产量
     * @param productId 产品ID
     * @param dateFormat 时间格式 (openGauss语法: 'YYYY-MM-DD' 或 'YYYY-IW')
     */
    @Select("SELECT to_char(e.exec_time, #{dateFormat}) as time_point, " +
            "       SUM(e.quantity_done) as total_output " +
            "FROM task_exec_4746 e " +
            "LEFT JOIN task_4746 t ON e.task_id = t.task_id " +
            "WHERE t.product_id = #{productId} " +
            "GROUP BY time_point " +
            "ORDER BY time_point ASC")
    List<Map<String, Object>> selectProductOutputTrend(@Param("productId") String productId,
                                                       @Param("dateFormat") String dateFormat);

    /**
     * 调用 DB4AI 预测产品未来批次平均产量（新功能）
     * @param productId 产品ID
     */
    @Select("SELECT * FROM predict_product_output_4746(#{productId})")
    Map<String, Object> predictProductOutput(@Param("productId") String productId);

}
