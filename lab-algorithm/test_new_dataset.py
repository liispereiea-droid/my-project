import pandas as pd
import requests
import json

# 1. 配置新数据集的路径和微服务地址
csv_file_path = 'Demand Supply Planning.csv'
api_url = "http://127.0.0.1:8000/predict"

try:
    # 2. 读取新的 S&OP 数据集
    print("⏳ 正在读取庞大的 Demand Supply Planning 数据集...")
    df = pd.read_csv(csv_file_path)

    # 3. 选择我们要测试的具体耗材 (这里选取数据集里的 'SKU0001')
    target_sku_id = 'SKU0001'
    df_target = df[df['SKU_ID'] == target_sku_id].copy()

    if df_target.empty:
        print(f"❌ 数据集中找不到 SKU: {target_sku_id}")
        exit()

    # 获取该 SKU 的具体名称，方便打印展示
    sku_name = df_target['SKU_Description'].iloc[0]

    # 4. 数据清洗与预处理 (架构师级)
    # 转换月份格式并按时间升序排列，保证时序的正确性
    df_target['Month'] = pd.to_datetime(df_target['Month'])
    df_target = df_target.sort_values(by='Month')

    # 提取【实际需求量】(Actual_Demand_units) 作为历史消耗数组
    # 注意：工业数据中可能含有空值或字符串格式的数字，需要强制转换为 float 并且去除非法值
    df_target['Actual_Demand_units'] = pd.to_numeric(df_target['Actual_Demand_units'], errors='coerce').fillna(0)
    historical_data = df_target['Actual_Demand_units'].astype(float).tolist()

    print(f"✅ 成功清洗数据集！")
    print(f"📦 当前分析物料: [{target_sku_id}] {sku_name}")
    print(f"📅 共提取到 {len(historical_data)} 个【月】的历史实际消耗数据。")
    print(f"👉 历史消耗量示例 (前5个月): {historical_data[:5]}")

    # 5. 构造发送给 FastAPI 微服务的 JSON 数据
    payload = {
        "consumable_id": 1,  # 随便写一个模拟的 ID
        "historical_data": historical_data
    }

    print(f"\n🚀 正在将数据发送给算法微服务: {api_url} ...")

    # 6. 发起 HTTP POST 请求调用算法
    response = requests.post(api_url, json=payload)

    # 7. 解析算法返回的结果
    if response.status_code == 200:
        result = response.json()
        print("\n🎉 算法微服务成功返回结果！")
        print("=" * 50)
        print(f"【{sku_name}】  预测报告:")
        print(f"基于过去 {len(historical_data)} 个月的历史实际消耗波动，")
        print(f"算法预测该物料在未来 30 个周期(月)的理论总消耗量为: 【 {result['predicted_30d_usage']} 】")
        print("=" * 50)
    else:
        print(f"❌ 调用失败！HTTP 状态码: {response.status_code}")
        print(f"错误信息: {response.text}")

except FileNotFoundError:
    print(f"❌ 找不到文件 {csv_file_path}，请确保你的 CSV 文件和本脚本放在同一个文件夹下！")
except requests.exceptions.ConnectionError:
    print(f"❌ 无法连接到算法微服务！请确保你已经先运行了 main.py 并且它正监听在 8000 端口。")