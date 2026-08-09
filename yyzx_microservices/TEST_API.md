# 接口测试命令

> 颐养中心微服务 — 各模块核心接口 curl 测试命令与预期返回

---

## 前置准备

```bash
# 设置环境变量（根据实际修改）
GW="http://localhost:8080"
TOKEN=""  # 登录后获取

# 各服务直连地址（绕过网关测试用）
AUTH="http://localhost:8081/yyzx"
CUST="http://localhost:8082/yyzx"
BED="http://localhost:8083/yyzx"
NURS="http://localhost:8084/yyzx"
CIO="http://localhost:8085/yyzx"
MEAL="http://localhost:8086/yyzx"
NOTF="http://localhost:8087/yyzx"
RPT="http://localhost:8088/yyzx"
```

---

## 1. yyzx-auth — 认证服务

### 1.1 获取验证码

```bash
curl -s $GW/admin/generate | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "message": "操作成功",
    "data": {
        "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "base64": "data:image/png;base64,iVBORw0KGgo..."
    }
}
```

### 1.2 验证码登录

```bash
curl -s -X POST $GW/admin/loginWithCaptcha \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123456",
    "captcha": "1234",
    "uuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }' | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "message": "操作成功",
    "data": {
        "user": {
            "id": 1,
            "username": "admin",
            "nickname": "管理员",
            "roleId": 1
        },
        "token": "eyJhbGciOiJIUzUxMiJ9..."
    }
}
```

> 将返回的 token 保存为环境变量：`TOKEN="eyJhbGciOiJIUzUxMiJ9..."`

### 1.3 分页查询用户

```bash
curl -s $GW/admin/findUserPage?current=1\&pageSize=10 \
  -H "token: $TOKEN" | python -m json.tool
```

### 1.4 修改密码

```bash
curl -s -X POST $GW/admin/updatePassword \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{
    "oldPassword": "123456",
    "newPassword": "654321",
    "confirmPassword": "654321"
  }' | python -m json.tool
```

---

## 2. yyzx-customer — 客户服务

### 2.1 分页查询客户列表

```bash
curl -s "$GW/customer/listKhxxPage?current=1&pageSize=10" \
  -H "token: $TOKEN" | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "message": "操作成功",
    "data": {
        "records": [
            {
                "id": 1,
                "customerName": "张三",
                "customerAge": 78,
                "customerSex": "男",
                "roomNo": "101",
                "bedNo": "1",
                "butlerName": "李管家"
            }
        ],
        "total": 23,
        "current": 1,
        "size": 10
    }
}
```

### 2.2 添加客户

```bash
curl -s -X POST $GW/customer/addCustomer \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{
    "customerName": "测试客户",
    "customerAge": 75,
    "customerSex": "男",
    "idcard": "320102193001011234",
    "roomNo": "102",
    "buildingNo": "A栋",
    "bedId": 3,
    "contactTel": "13800138000",
    "checkinDate": "2026-07-21",
    "expirationDate": "2027-07-21",
    "psychosomaticState": "健康",
    "userId": 1
  }' | python -m json.tool
```

### 2.3 查询客户偏好

```bash
curl -s "$GW/customerpreference/listCustomerpreference?current=1&pageSize=10" \
  -H "token: $TOKEN" | python -m json.tool
```

---

## 3. yyzx-bed — 床位服务

### 3.1 查询床位（按房间）

```bash
curl -s "$GW/bed/findBed?roomNo=101" \
  -H "token: $TOKEN" | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "data": [
        {
            "id": 1,
            "roomNo": "101",
            "bedNo": "1",
            "bedStatus": 2
        }
    ]
}
```

### 3.2 房间楼层分布图

```bash
curl -s $GW/room/findCwsyBedVo \
  -H "token: $TOKEN" | python -m json.tool
```

### 3.3 床位交换

```bash
curl -s -X POST $GW/beddetails/exchangeBed \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{
    "customerId": 1,
    "newBedId": 5,
    "endDate": "2027-07-21"
  }' | python -m json.tool
```

---

## 4. yyzx-nursing — 护理服务

### 4.1 分页查询护理项目

```bash
curl -s "$GW/nursecontent/findNurseItemPage?current=1&pageSize=10" \
  -H "token: $TOKEN" | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "data": {
        "records": [
            {
                "id": 1,
                "serialNumber": "HL-001",
                "nursingName": "翻身护理",
                "servicePrice": 50.00,
                "status": 1
            }
        ],
        "total": 15
    }
}
```

### 4.2 查询护理等级列表

```bash
curl -s $GW/nurselevel/listNurseLevel \
  -H "token: $TOKEN" | python -m json.tool
```

### 4.3 添加护理记录（含库存扣减）

```bash
curl -s -X POST $GW/nurserecord/addNurseRecord \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{
    "customerId": 1,
    "itemId": 1,
    "nursingTime": "2026-07-21 10:00:00",
    "nursingContent": "翻身护理 - 上午",
    "nursingCount": 1,
    "userId": 1
  }' | python -m json.tool
```

---

## 5. yyzx-checkinout — 出入管理

### 5.1 提交退住申请

```bash
curl -s -X POST $GW/backdown/addBackdown \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{
    "customerId": 1,
    "retreatTime": "2026-08-01 09:00:00",
    "retreatType": 0,
    "retreatReason": "家属接回"
  }' | python -m json.tool
```

### 5.2 查询退住列表

```bash
curl -s -X POST $GW/backdown/listBackdownVo \
  -H "Content-Type: application/json" \
  -H "token: $TOKEN" \
  -d '{"current":1,"pageSize":10}' | python -m json.tool
```

### 5.3 外出查询

```bash
curl -s "$GW/outward/queryOutwardVo?current=1&pageSize=10" \
  -H "token: $TOKEN" | python -m json.tool
```

---

## 6. yyzx-meal — 膳食服务

### 6.1 查询菜品列表

```bash
curl -s $GW/food/listFood \
  -H "token: $TOKEN" | python -m json.tool
```

### 6.2 查询每周膳食计划

```bash
curl -s "$GW/meal/listMealPage?weekDay=1&mealType=1&current=1&pageSize=10" \
  -H "token: $TOKEN" | python -m json.tool
```

---

## 7. yyzx-report — 报表服务

### 7.1 客户入住统计

```bash
curl -s "$GW/report/customerStats?startDate=2026-06-21&endDate=2026-07-21" \
  -H "token: $TOKEN" | python -m json.tool
```

**预期返回：**
```json
{
    "flag": true,
    "data": {
        "totalBeds": 50,
        "occupiedBeds": 23,
        "availableBeds": 27,
        "occupancyRate": 46.00,
        "newCustomers": 5,
        "leftCustomers": 2,
        "levelDistribution": {
            "levelOneCare": 3,
            "levelTwoCare": 5,
            "levelThreeCare": 4,
            "levelFourCare": 3,
            "levelFiveCare": 2,
            "levelSixCare": 1,
            "levelSevenCare": 1,
            "levelEightCare": 0,
            "selfCare": 4
        }
    }
}
```

### 7.2 导出客户统计 Excel

```bash
curl -s -o customer_stats.xlsx \
  "$GW/report/exportCustomerExcel?startDate=2026-06-21&endDate=2026-07-21" \
  -H "token: $TOKEN"
# 检查文件大小 > 0
ls -lh customer_stats.xlsx
```

---

## 8. yyzx-notification — 通知服务

> 此服务主要作为 RabbitMQ 消费者运行，无 REST 端点。
> 通过其他服务的操作触发消息消费来验证。

### 8.1 验证队列已创建

```bash
# 访问 RabbitMQ 管理 API
curl -s -u guest:guest http://localhost:15672/api/queues | python -m json.tool | grep yyzx
```

**预期输出：**
```
"name": "yyzx.mail.queue"
"name": "yyzx.notify.queue"
"name": "yyzx.log.queue"
"name": "yyzx.admin.notify.queue"
"name": "yyzx.mail.dlx.queue"
...
```

### 8.2 WebSocket 连接测试

```javascript
// 浏览器控制台执行
const ws = new WebSocket('ws://localhost:8087/yyzx/ws/1');
ws.onopen = () => console.log('WebSocket 连接成功');
ws.onmessage = (e) => console.log('收到消息:', e.data);
```

---

## 9. yyzx-task — 定时任务

> 此服务运行 Quartz 定时任务，无 REST 端点。
> 通过日志和数据库记录验证。

### 9.1 验证 Quartz 已启动

```bash
# 查看启动日志
docker logs yyzx-task 2>&1 | grep -i quartz
# 预期: "Scheduler class: 'org.quartz.core.QuartzScheduler'"
```

### 9.2 手动触发退住审批测试

```bash
# 1. 创建一个待审批的退住申请（超过 24 小时前创建）
# 2. 等待下个整点，查看日志：
docker logs yyzx-task 2>&1 | grep "自动审批"
# 预期: "退住申请 ID=xx 自动审批通过"
```

---

## 10. Gateway 整体测试

### 10.1 验证所有路由可达

```bash
#!/bin/bash
# 批量健康检查脚本
ENDPOINTS=(
  "/admin/generate"
  "/customer/listKhxxPage?current=1&pageSize=1"
  "/bed/findBed"
  "/nursecontent/findNurseItemPage?current=1&pageSize=1"
  "/food/listFood"
)

for ep in "${ENDPOINTS[@]}"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" -H "token: $TOKEN" "$GW$ep")
  echo "$code  $ep"
done
```

**预期输出：**
```
200  /admin/generate
200  /customer/listKhxxPage?current=1&pageSize=1
200  /bed/findBed
200  /nursecontent/findNurseItemPage?current=1&pageSize=1
200  /food/listFood
```

### 10.2 验证白名单无需认证

```bash
# 获取验证码（无 token）
curl -s -o /dev/null -w "%{http_code}" $GW/admin/generate
# 预期: 200

# 登录（无 token）
curl -s -o /dev/null -w "%{http_code}" -X POST $GW/admin/loginWithCaptcha \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456","captcha":"","uuid":""}'
# 预期: 400（参数不完整），但非 401（说明没被拦截）
```

### 10.3 验证认证拦截生效

```bash
# 不带 token 访问受保护接口
curl -s $GW/customer/listKhxxPage | python -m json.tool
```

**预期返回：**
```json
{
    "flag": false,
    "message": "未提供认证 token",
    "data": null
}
```
HTTP Status: 401
