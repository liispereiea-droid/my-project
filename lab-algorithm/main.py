# main.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List
import pandas as pd
from statsmodels.tsa.holtwinters import SimpleExpSmoothing

# 1. 初始化 FastAPI 应用
app = FastAPI(
    title="实验室耗材智能预测微服务",
    description="基于历史消耗数据，使用指数平滑算法预测未来30天耗材需求",
    version="1.0.0"
)


# 2. 定义 Pydantic 模型，用于自动校验 Spring Boot 传来的 JSON 数据格式
class PredictRequest(BaseModel):
    consumable_id: int  # 耗材ID
    historical_data: List[float]  # 过去N天的每日消耗量数组，按时间升序排列


class PredictResponse(BaseModel):
    consumable_id: int
    predicted_30d_usage: int  # 预测的未来30天总消耗量


# 3. 定义预测路由接口 (POST 方法)
@app.post("/predict", response_model=PredictResponse)
def predict_consumable_usage(request: PredictRequest):
    data = request.historical_data

    # 【算法逻辑1】：数据量校验。如果历史数据少于3天，算法无法有效拟合趋势
    if len(data) < 3:
        # 退化策略：直接使用历史均值乘以30天
        avg = sum(data) / len(data) if data else 0
        return PredictResponse(
            consumable_id=request.consumable_id,
            predicted_30d_usage=int(avg * 30)
        )

    try:
        # 【算法逻辑2】：将原生 List 转换为 Pandas 的 Series 时间序列对象
        ts_data = pd.Series(data)

        # 【算法逻辑3】：简单指数平滑 (Simple Exponential Smoothing)
        # 任务书要求：原理清晰、实现难度适中。SES 正符合此要求。
        # smoothing_level (alpha) = 0.3 意味着模型会给予近期数据30%的权重，远期数据70%的衰减权重，适合短期波动预测。
        model = SimpleExpSmoothing(ts_data, initialization_method="estimated").fit(smoothing_level=0.3)

        # 【算法逻辑4】：预测未来 30 个周期（天）的消耗量
        forecast_30_days = model.forecast(30)

        # 【算法逻辑5】：将未来30天的预测值求和，并向下取整（耗材数量通常为整数）
        total_predicted = int(forecast_30_days.sum())

        # 兜底策略：确保预测值不为负数（消耗量不可能小于0）
        total_predicted = max(0, total_predicted)

        return PredictResponse(
            consumable_id=request.consumable_id,
            predicted_30d_usage=total_predicted
        )
    except Exception as e:
        # 如果算法执行出错，返回 500 状态码及错误信息
        raise HTTPException(status_code=500, detail=f"预测算法执行失败: {str(e)}")


# 4. 开发环境启动配置
if __name__ == "__main__":
    import uvicorn

    # 启动 Uvicorn 服务器，监听本地 8000 端口
    uvicorn.run(app, host="127.0.0.1", port=8000)