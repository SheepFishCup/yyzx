# API 路由一览

> 颐养中心微服务 — 所有 API 端点与 Gateway 路由映射表

---

## 1. Gateway 路由配置表

| 路由 ID | 路径前缀 | 目标服务 | 负载均衡 | 说明 |
|---------|---------|---------|---------|------|
| auth-admin | `/admin/**` | yyzx-auth | lb://yyzx-auth | 登录、验证码、用户管理、密码重置 |
| auth-role | `/role/**` | yyzx-auth | lb://yyzx-auth | 角色 CRUD |
| auth-menu | `/menu/**` | yyzx-auth | lb://yyzx-auth | 菜单 CRUD |
| auth-rolemenu | `/rolemenu/**` | yyzx-auth | lb://yyzx-auth | 角色-菜单关联 |
| customer-main | `/customer/**` | yyzx-customer | lb://yyzx-customer | 客户 CRUD |
| customer-preference | `/customerpreference/**` | yyzx-customer | lb://yyzx-customer | 客户偏好 |
| customer-nurse-item | `/customernurseitem/**` | yyzx-customer | lb://yyzx-customer | 客户护理项目 |
| customer-patient | `/patient/**` | yyzx-customer | lb://yyzx-customer | 微信小程序端 |
| bed-main | `/bed/**` | yyzx-bed | lb://yyzx-bed | 床位查询 |
| bed-details | `/beddetails/**` | yyzx-bed | lb://yyzx-bed | 床位详情、换床 |
| bed-room | `/room/**` | yyzx-bed | lb://yyzx-bed | 房间管理、床位分布图 |
| nursing-content | `/nursecontent/**` | yyzx-nursing | lb://yyzx-nursing | 护理项目管理 |
| nursing-level | `/nurselevel/**` | yyzx-nursing | lb://yyzx-nursing | 护理等级管理 |
| nursing-level-item | `/nurselevelitem/**` | yyzx-nursing | lb://yyzx-nursing | 等级-项目关联 |
| nursing-record | `/nurserecord/**` | yyzx-nursing | lb://yyzx-nursing | 护理记录 |
| checkinout-backdown | `/backdown/**` | yyzx-checkinout | lb://yyzx-checkinout | 退住申请/审批 |
| checkinout-outward | `/outward/**` | yyzx-checkinout | lb://yyzx-checkinout | 外出申请/审批 |
| meal-food | `/food/**` | yyzx-meal | lb://yyzx-meal | 菜品管理、图片上传 |
| meal-plan | `/meal/**` | yyzx-meal | lb://yyzx-meal | 膳食计划 |
| report | `/report/**` | yyzx-report | lb://yyzx-report | 统计查询、Excel 导出 |

---

## 2. yyzx-auth（认证服务）— `/admin/**`, `/role/**`, `/menu/**`, `/rolemenu/**`

### 2.1 AdminController — `/admin`

| 方法 | 路径 | 功能 | 认证 | 说明 |
|------|------|------|------|------|
| GET | `/admin/generate` | 生成验证码 | 无需 | 返回 base64 图片 + UUID |
| POST | `/admin/loginWithCaptcha` | 验证码登录 | 无需 | 返回 JWT token |
| GET | `/admin/findUserPage` | 分页查用户 | 需要 | 支持昵称/角色筛选 |
| GET | `/admin/findAllUserPage` | 查全部用户 | 需要 | 分页 |
| POST | `/admin/addUser` | 添加用户 | 需要 | BCrypt 密码加密 |
| POST | `/admin/updateUser` | 更新用户 | 需要 | |
| GET | `/admin/delUser` | 删除用户 | 需要 | 软删除 |
| POST | `/admin/updatePassword` | 修改密码 | 需要 | BCrypt 验证旧密码 |
| POST | `/admin/forgotPassword` | 忘记密码 | 无需 | 发送重置邮件 |
| POST | `/admin/resetPassword` | 重置密码 | 无需 | 通过邮件 token 验证 |
| GET | `/admin/verifyResetToken` | 验证重置 token | 无需 | |
| POST | `/admin/unblock` | 解除黑名单 | 需要 | 从布隆过滤器 + Redis Set 移除 |

### 2.2 RoleController — `/role`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/role/listRole` | 角色列表 |
| POST | `/role/addRole` | 添加角色 |
| POST | `/role/updateRole` | 更新角色 |
| GET | `/role/removeRole` | 删除角色 |

### 2.3 MenuController — `/menu`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/menu/listMenu` | 菜单树 |
| POST | `/menu/addMenu` | 添加菜单 |
| POST | `/menu/updateMenu` | 更新菜单 |
| GET | `/menu/removeMenu` | 删除菜单 |

### 2.4 RoleMenuController — `/rolemenu`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/rolemenu/listRoleMenu` | 角色菜单列表 |
| POST | `/rolemenu/addRoleMenu` | 绑定角色菜单 |
| POST | `/rolemenu/updateRoleMenu` | 更新绑定 |
| GET | `/rolemenu/removeRoleMenu` | 删除绑定 |

---

## 3. yyzx-customer（客户服务）— `/customer/**`, `/customerpreference/**`, `/customernurseitem/**`, `/patient/**`

### 3.1 CustomerController — `/customer`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/customer/listKhxxPage` | 分页客户列表 | Redis 缓存 + 分布式锁防击穿 |
| POST | `/customer/addCustomer` | 添加客户 | 创建客户 + BedDetails + 更新床位状态 + RabbitMQ 通知 |
| GET | `/customer/removeCustomer` | 删除客户 | 软删除 + 释放床位 + RabbitMQ 日志 |
| POST | `/customer/editCustomer` | 编辑客户 | 同步更新 BedDetails 到期日 |

### 3.2 CustomerPreferenceController — `/customerpreference`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/customerpreference/listCustomerpreference` | 分页偏好列表 |
| POST | `/customerpreference/addCustomerpreference` | 添加偏好 |
| POST | `/customerpreference/updateCustomerpreference` | 更新偏好 |
| GET | `/customerpreference/removeCustomerpreference` | 删除偏好 |

### 3.3 CustomerNurseItemController — `/customernurseitem`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/customernurseitem/listCustomerItem` | 客户护理项目列表 | 分页 |
| POST | `/customernurseitem/addItemToCustomer` | 批量添加护理项目 | 初始化 Redis 库存 |
| GET | `/customernurseitem/removeCustomerLevelAndItem` | 移除等级+项目 | |
| POST | `/customernurseitem/enewNurseItem` | 续费护理项目 | 原子递增 nurse_number |
| GET | `/customernurseitem/isIncludesItemCustomer` | 检查客户是否有某项目 | |
| GET | `/customernurseitem/removeCustomerItem` | 移除单个项目 | |

### 3.4 PatientController — `/patient`

| 方法 | 路径 | 功能 | 认证 |
|------|------|------|------|
| POST | `/patient/login` | 微信小程序登录 | 无需 | 通过 openid 返回 JWT |

---

## 4. yyzx-bed（床位服务）— `/bed/**`, `/beddetails/**`, `/room/**`

### 4.1 BedController — `/bed`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/bed/findBed` | 按房间/状态查询床位 |

### 4.2 BedDetailsController — `/beddetails`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/beddetails/listBedDetailsVoPage` | 分页床位详情 | |
| PUT | `/beddetails` | 更新床位到期日 | |
| DELETE | `/beddetails` | 删除床位记录 | |
| POST | `/beddetails/exchangeBed` | 床位交换 | 事务性5步操作 |

### 4.3 RoomController — `/room`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/room/findCwsyBedVo` | 按楼层床位分布图 |
| GET | `/room/listRoom` | 所有房间列表 |

---

## 5. yyzx-nursing（护理服务）— `/nursecontent/**`, `/nurselevel/**`, `/nurselevelitem/**`, `/nurserecord/**`

### 5.1 NurseContentController — `/nursecontent`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/nursecontent/findNurseItemPage` | 分页护理项目 |
| POST | `/nursecontent/addNurseItem` | 添加护理项目 |
| POST | `/nursecontent/updateNurseItem` | 更新护理项目 |
| GET | `/nursecontent/delNurseItem` | 删除护理项目（级联校验） |

### 5.2 NurseLevelController — `/nurselevel`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/nurselevel/listNurseLevel` | 护理等级列表（缓存） |
| POST | `/nurselevel/addNurseLevel` | 添加等级 |
| POST | `/nurselevel/updateNurseLevel` | 更新等级 |
| GET | `/nurselevel/removeNurseLevel` | 删除等级 |
| GET | `/nurselevel/listNurseItemByLevel` | 按等级查项目 |
| POST | `/nurselevel/addItemToLevel` | 添加项目到等级 |
| GET | `/nurselevel/removeNurseLevelItem` | 从等级移除项目 |

### 5.3 NurseLevelItemController — `/nurselevelitem`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/nurselevelitem/listNurseLevelItem` | 等级项目列表 |
| POST | `/nurselevelitem/addNurseLevelItem` | 添加关联 |
| POST | `/nurselevelitem/updateNurseLevelItem` | 更新关联 |
| POST | `/nurselevelitem/removeNurseLevelItem` | 删除关联 |

### 5.4 NurseRecordsController — `/nurserecord`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/nurserecord/listNurseRecordsVo` | 分页护理记录 | |
| POST | `/nurserecord/addNurseRecord` | 添加护理记录 | Redis Lua 原子库存扣减 |
| GET | `/nurserecord/removeCustomerRecord` | 删除记录 | |
| GET | `/nurserecord/queryOutwardVo` | 查询外出记录 | （跨表查询） |

---

## 6. yyzx-checkinout（出入管理）— `/backdown/**`, `/outward/**`

### 6.1 BackdownController — `/backdown`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| POST | `/backdown/listBackdownVo` | 分页退住列表 | |
| POST | `/backdown/addBackdown` | 提交退住申请 | |
| POST | `/backdown/examineBackdown` | 审批退住 | 通过后释放床位 |
| DELETE | `/backdown` | 删除退住记录 | 软删除 |

### 6.2 OutwardController — `/outward`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/outward/queryOutwardVo` | 分页外出列表 | |
| POST | `/outward/addOutward` | 提交外出申请 | |
| POST | `/outward/examineOutward` | 审批外出 | 通过后更新床位状态 |
| POST | `/outward/updateOutward` | 更新外出 | |
| GET | `/outward/delOutward` | 删除外出记录 | |
| POST | `/outward/updateBackTime` | 登记返回时间 | |

---

## 7. yyzx-meal（膳食服务）— `/food/**`, `/meal/**`

### 7.1 FoodContoller — `/food`

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/food/listFood` | 菜品列表 | Redis 缓存 |
| POST | `/food/addFood` | 添加菜品 | 清除缓存 |
| POST | `/food/upload` | 上传菜品图片 | |

### 7.2 MealController — `/meal`

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/meal/listMealPage` | 分页膳食计划 |
| POST | `/meal/addMeal` | 添加膳食 |
| POST | `/meal/updateMeal` | 更新膳食 |
| GET | `/meal/removeMeal` | 删除膳食 |

---

## 8. yyzx-report（报表服务）— `/report/**`

### 8.1 ReportController — `/report`（重写版，新增4个端点）

| 方法 | 路径 | 功能 | 说明 |
|------|------|------|------|
| GET | `/report/customerStats` | 客户入住统计 | 参数: `startDate`, `endDate` |
| GET | `/report/financeStats` | 财务统计 | 参数: `startDate`, `endDate` |
| GET | `/report/exportCustomerExcel` | 导出客户统计 Excel | 响应为文件下载 |
| GET | `/report/exportFinanceExcel` | 导出财务 Excel | 响应为文件下载 |

---

## 9. 无需认证的白名单路径

以下路径在 Gateway 层直接放行，不校验 JWT：

| 路径 | 说明 |
|------|------|
| `/admin/generate` | 获取验证码 |
| `/admin/loginWithCaptcha` | 验证码登录 |
| `/admin/login` | 登录 |
| `/admin/forgotPassword` | 忘记密码（发送邮件） |
| `/admin/resetPassword` | 重置密码 |
| `/admin/verifyResetToken` | 验证重置 token |
| `/patient/login` | 微信小程序登录 |
| `/doc.html` | Knife4j 接口文档 |
| `/webjars/**` | Swagger 静态资源 |
| `/v3/api-docs/**` | OpenAPI 规范文档 |
| `/swagger-resources/**` | Swagger 资源 |
| `/swagger-ui.html` | Swagger UI |
| `/swagger-ui/**` | Swagger UI 资源 |
| `/images/**` | 静态图片 |
| `/druid/**` | Druid 监控面板 |

---

## 10. 认证流程中传递的 Header

| Header | 来源 | 说明 |
|--------|------|------|
| `token` | 前端传入 | JWT token，登录后由前端存储并在每个请求中携带 |
| `X-User-Id` | Gateway 注入 | 从 JWT 中解析的 userId，传给下游服务 |
| `X-User-Name` | Gateway 注入 | 从 JWT 中解析的 username，传给下游服务 |

> 下游服务可通过 `request.getHeader("X-User-Id")` 获取当前用户 ID，替代原单体的 `BaseContext.getCurrentId()`。
