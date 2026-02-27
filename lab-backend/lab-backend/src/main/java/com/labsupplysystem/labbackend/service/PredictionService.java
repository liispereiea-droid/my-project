package com.labsupplysystem.labbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.labsupplysystem.labbackend.entity.Apply;
import com.labsupplysystem.labbackend.entity.Consumable;
import com.labsupplysystem.labbackend.mapper.ApplyMapper;
import com.labsupplysystem.labbackend.mapper.ConsumableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private ConsumableMapper consumableMapper;

    // Python 微服务的地址
    private static final String PYTHON_API_URL = "http://127.0.0.1:8000/predict";

    /**
     * 为指定耗材生成预测并更新库存预警线
     * @param consumableId 耗材ID
     */
    public boolean runPredictionForConsumable(Integer consumableId) {
        // 1. 提取历史数据 (提取过去 90 天的数据用于算法训练)
        int daysToTrace = 90;
        LocalDateTime startDate = LocalDateTime.now().minusDays(daysToTrace);

        // 使用 MyBatis-Plus 查询该耗材过去90天内【已通过(status=2)】的领用记录
        QueryWrapper<Apply> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("consumable_id", consumableId)
                    .eq("status", 2) 
                    .ge("create_time", startDate);
        List<Apply> applyList = applyMapper.selectList(queryWrapper);

        // 2. 数据处理：将零散的领用记录按【天】进行聚合汇总
        Map<String, Integer> dailyUsageMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Apply apply : applyList) {
            // 这里以 create_time 或 approveDate 为准都可以，目前使用 createTime
            String dateStr = apply.getCreateTime().format(formatter);
            dailyUsageMap.put(dateStr, dailyUsageMap.getOrDefault(dateStr, 0) + apply.getApplyCount());
        }

        // 3. 构建连续的时间序列数组 (哪怕某天没有消耗，也要补 0，保证时序连贯性)
        List<Double> historicalData = new ArrayList<>();
        for (int i = daysToTrace; i >= 0; i--) {
            String dateStr = LocalDate.now().minusDays(i).format(formatter);
            historicalData.add(dailyUsageMap.getOrDefault(dateStr, 0).doubleValue());
        }

        // 4. 构造发给 Python 接口的 JSON 数据结构
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("consumable_id", consumableId);
        requestMap.put("historical_data", historicalData);

        try {
            // 5. 调用 Python 微服务 (POST 请求)
            System.out.println(">>> 正在调用算法微服务预测耗材ID: " + consumableId);
            Map<String, Object> response = restTemplate.postForObject(PYTHON_API_URL, requestMap, Map.class);

            if (response != null && response.containsKey("predicted_30d_usage")) {
                // 拿到 Python 计算出的未来 30 天预测消耗量
                Integer predictedUsage30d = (Integer) response.get("predicted_30d_usage");

                // 6. 核心业务逻辑：计算动态阈值 (安全库存预警线)
                // 公式：预测消耗量 * 1.2 (增加 20% 的安全缓冲)
                int dynamicThreshold = (int) Math.ceil(predictedUsage30d * 1.2);
                
                // 兜底逻辑：如果预测消耗极低，设定一个最小警戒底线(如 5)，防止阈值为 0
                dynamicThreshold = Math.max(dynamicThreshold, 5);

                // 7. 更新到 tb_consumable 表
                Consumable consumable = consumableMapper.selectById(consumableId);
                if (consumable != null) {
                    consumable.setPredictedUsage30d(predictedUsage30d);
                    consumable.setDynamicThreshold(dynamicThreshold);
                    consumable.setLastPredictTime(LocalDateTime.now());
                    consumableMapper.updateById(consumable);
                    System.out.println("<<< 预测成功！预测消耗量: " + predictedUsage30d + ", 动态阈值已更新为: " + dynamicThreshold);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("!!! 调用预测微服务失败，请检查 Python 服务是否启动: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}