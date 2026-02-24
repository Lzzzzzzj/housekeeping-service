# 家政平台 API 接口测试用例

本文档基于《概要设计.md》与当前后端 Controller 实现整理，包含所有接口的测试用例、输入示例与预期输出。  
**认证说明**：除用户注册、用户登录外，其余接口均需在请求头中携带 `Authorization: Bearer <token>`（或项目实际使用的 Header 名称）。

**统一响应结构**：
```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```
- `code`: 200 成功，401 未登录/无权限，500 业务错误
- `msg`: 提示信息
- `data`: 业务数据，无数据时为 `null`

---

## 一、用户端接口 (User App API)

**基础路径**：`/api/v1/user`  
**说明**：订单、类目、服务、申请等接口需用户登录（userType=1）后携带 token。

### 1.1 用户认证（无需 token）

#### 1.1.1 用户注册

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/register` |
| **请求体** | JSON |

**输入示例（正常）**：
```json
{
  "username": "zhangsan",
  "password": "123456",
  "phone": "13800138000",
  "nickname": "张三"
}
```

**预期输出（成功）**：
- HTTP 200
- `code`: 200，`msg`: "success"
- `data`: LoginVO
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGc...",
    "userId": 1,
    "memberId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13800138000",
    "avatar": null,
    "userType": 1
  }
}
```

**异常用例**：

| 输入 | 预期输出 |
|------|----------|
| 缺少 `username` 或为空 | code 500，msg 含「用户名不能为空」或校验错误 |
| `password` 长度 &lt; 6 或 &gt; 20 | code 500，msg 含「密码长度必须在6-20位之间」 |
| `phone` 非 1 开头的 11 位手机号 | code 500，msg 含「手机号格式不正确」 |
| `username` 已存在 | code 500，msg 含重复/已注册等提示 |

---

#### 1.1.2 用户登录

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/login` |
| **请求体** | JSON |

**输入示例（正常）**：
```json
{
  "usernameOrPhone": "zhangsan",
  "password": "123456"
}
```
或使用手机号：`"usernameOrPhone": "13800138000"`

**预期输出（成功）**：
- HTTP 200
- `code`: 200，`data`: LoginVO（结构同注册返回）

**异常用例**：

| 输入 | 预期输出 |
|------|----------|
| `usernameOrPhone` 为空 | code 500，msg 含「用户名或手机号不能为空」 |
| `password` 为空 | code 500，msg 含「密码不能为空」 |
| 账号或密码错误 | code 500，msg 含错误提示 |

---

### 1.2 类目

#### 1.2.1 获取全类目列表（树形）

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/category/list` |
| **认证** | 可选（按项目配置） |

**输入**：无请求体，无必填参数。

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: CategoryVO 树形数组
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "name": "日常保洁",
      "icon": "https://...",
      "formConfig": "{...}",
      "sort": 0,
      "children": [
        { "id": 2, "parentId": 1, "name": "深度保洁", "children": [] }
      ]
    }
  ]
}
```

#### 1.2.2 （管理员说明）pms_category 增删改查见「三、管理员端接口 - 类目管理」

---

### 1.3 服务（用户端仅查）

#### 1.3.1 分页查询服务列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/service/page` |
| **Query** | categoryId(可选), title(可选), page(默认1), pageSize(默认20) |

**输入示例**：
- `GET /api/v1/user/service/page`
- `GET /api/v1/user/service/page?categoryId=1&page=1&pageSize=10`
- `GET /api/v1/user/service/page?title=保洁`

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: PmsService 数组
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "categoryId": 1,
      "title": "日常保洁",
      "basePrice": 80.00,
      "unit": "小时",
      "depositType": 1,
      "description": "..."
    }
  ]
}
```

---

### 1.4 订单（需用户登录）

#### 1.4.1 用户提交订单

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/create` |
| **请求体** | OrderCreateDTO |

**输入示例（正常，未使用优惠券）**：
```json
{
  "serviceId": 1,
  "appointmentTime": "2025-03-01T14:00:00",
  "addressInfo": {
    "name": "张三",
    "phone": "13800138000",
    "lng": 113.123,
    "lat": 23.456,
    "address": "广东省广州市天河区某某路1号"
  },
  "extInfo": {
    "rooms": 3,
    "halls": 2,
    "hasElevator": true
  },
  "userCouponId": null
}
```

**输入示例（正常，使用优惠券）**：
```json
{
  "serviceId": 1,
  "appointmentTime": "2025-03-01T14:00:00",
  "addressInfo": {
    "name": "张三",
    "phone": "13800138000",
    "lng": 113.123,
    "lat": 23.456,
    "address": "广东省广州市天河区某某路1号"
  },
  "extInfo": {
    "rooms": 3,
    "halls": 2,
    "hasElevator": true
  },
  "userCouponId": 1001
}
```

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: OmsOrder（含 id、orderSn、status=10 等）
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "orderSn": "ORD202502240001",
    "memberId": 1,
    "staffId": null,
    "serviceId": 1,
    "totalAmount": 80.00,
      "payAmount": 70.00,
    "status": 10,
    "appointmentTime": "2025-03-01T14:00:00",
      "addressInfo": "{...}",
      "extInfo": "{...}",
      "couponId": 1,
      "userCouponId": 1001,
      "couponAmount": 10.00,
    "createTime": "2025-02-24T10:00:00",
    "serviceTitle": "日常保洁"
  }
}
```

**异常用例**：

| 输入 | 预期输出 |
|------|----------|
| 未登录 | code 401 或 500，msg 含「请登录」 |
| `serviceId` 为空 | code 500，msg 含「服务ID不能为空」 |
| `appointmentTime` 为过去时间 | code 500，msg 含「预约时间必须为未来时间」 |
| `addressInfo` 为空 | code 500，msg 含「地址信息不能为空」 |
| `userCouponId` 不属于当前用户 / 已使用 / 已过期 | code 500，msg 含「优惠券不可用」或类似提示 |

---

### 1.6 优惠券（需用户登录）

> 说明：优惠券由管理员在后台上架，用户可以通过余额购买或兑换码领取。

#### 1.6.1 我的优惠券列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/coupon/my` |
| **Query** | status(可选)：0-未使用，1-已使用，2-已过期，默认返回全部 |

**输入示例**：
- `GET /api/v1/user/coupon/my`
- `GET /api/v1/user/coupon/my?status=0`

**预期输出（成功）**：
- HTTP 200，`code`: 200  
- `data`: UserCouponVO 数组
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1001,
      "couponId": 1,
      "memberId": 1,
      "status": 0,
      "obtainType": 1,
      "obtainTime": "2025-02-24T10:00:00",
      "useTime": null,
      "expireTime": "2025-03-01T23:59:59",
      "couponName": "新用户立减10元",
      "couponType": 1,
      "amount": 10.00,
      "minAmount": 0.00
    }
  ]
}
```

**异常用例**：未登录返回 401 或 code 500，提示「请登录」。

---

#### 1.6.2 优惠券列表（优惠券中心，可购买/可兑换）

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/coupon/center` |
| **Query** | 无必填参数，可根据需要扩展分页参数 |

**输入示例**：
- `GET /api/v1/user/coupon/center`

**预期输出（成功）**：
- HTTP 200，`code`: 200  
- `data`: CouponVO 数组（只返回当前时间在领取时间窗口内且已上架的优惠券）
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "name": "新用户立减10元",
      "couponType": 1,
      "amount": 10.00,
      "minAmount": 0.00,
      "price": 1.00,
      "totalCount": 1000,
      "receiveLimit": 1,
      "obtainChannel": 0,
      "status": 1,
      "receiveStartTime": "2025-02-20T00:00:00",
      "receiveEndTime": "2025-03-01T23:59:59"
    }
  ]
}
```

---

#### 1.6.3 余额购买优惠券

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/coupon/buy` |
| **请求体** | CouponBuyDTO |

**输入示例（正常）**：
```json
{
  "couponId": 1
}
```

业务规则（用于测试断言）：
- 校验优惠券存在且 `status=1`、当前时间在 `receiveStartTime`~`receiveEndTime` 之间；
- 校验优惠券 `price > 0` 且 `obtainChannel` 允许余额购买；
- 校验用户余额是否足够；
- 校验用户已领取数量未超过 `receiveLimit`；
- 扣减用户余额，写入 `mkt_user_coupon` 一条记录，返回新的用户优惠券 ID。

**预期输出（成功）**：
- HTTP 200，`code`: 200  
- `data`: UserCouponVO（或至少返回 `userCouponId`）
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1001,
    "couponId": 1,
    "memberId": 1,
    "status": 0,
    "obtainType": 1,
    "obtainTime": "2025-02-24T10:00:00",
    "expireTime": "2025-03-01T23:59:59"
  }
}
```

**异常用例**：

| 场景 | 预期输出 |
|------|----------|
| 未登录 | code 401 或 500，提示「请登录」 |
| 优惠券不存在或已下架 | code 500，msg 含「优惠券不存在或已下架」 |
| 不在领取时间范围内 | code 500，msg 含「当前不在领取时间内」 |
| 优惠券配置为仅兑换码 (`obtainChannel=2`) | code 500，msg 含「不可通过余额购买」 |
| 用户余额不足 | code 500，msg 含「余额不足」 |
| 超过单用户领取上限 | code 500，msg 含「领取数量已达上限」 |

---

#### 1.6.4 兑换码领取优惠券

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/coupon/redeem` |
| **请求体** | CouponRedeemDTO |

**输入示例（正常）**：
```json
{
  "code": "ABC123XYZ"
}
```

业务规则（用于测试断言）：
- 校验兑换码存在且未过期、未使用；
- 关联的优惠券存在且为上架状态，且 `obtainChannel` 允许兑换码领取；
- 同一用户重复使用同一兑换码（或同模板多次）按业务要求限制；
- 将兑换码标记为已使用，绑定当前用户，在 `mkt_user_coupon` 中插入记录。

**预期输出（成功）**：
- HTTP 200，`code`: 200  
- `data`: UserCouponVO
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1002,
    "couponId": 2,
    "memberId": 1,
    "status": 0,
    "obtainType": 2,
    "obtainTime": "2025-02-24T10:05:00",
    "expireTime": "2025-03-15T23:59:59"
  }
}
```

**异常用例**：

| 场景 | 预期输出 |
|------|----------|
| 未登录 | code 401 或 500，提示「请登录」 |
| 兑换码不存在 | code 500，msg 含「兑换码无效」 |
| 兑换码已使用 | code 500，msg 含「兑换码已被使用」 |
| 兑换码已过期 | code 500，msg 含「兑换码已过期」 |
| 对应优惠券已下架或不支持兑换码领取 | code 500，msg 含「优惠券不可用」 |

---

#### 1.4.2 发起支付（预支付单）

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/pay` |
| **请求体** | OrderPayDTO |

**输入示例**：
```json
{
  "orderSn": "ORD202502240001"
}
```

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: OrderPayVO（微信支付预支付参数，具体以对接为准）
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "orderSn": "ORD202502240001",
    "prepayId": "...",
    "paySign": "...",
    "timeStamp": "...",
    "nonceStr": "..."
  }
}
```

**异常用例**：`orderSn` 为空则 code 500；订单非当前用户或状态不允许支付则业务错误。

---

#### 1.4.3 支付成功确认

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/pay-success` |
| **请求体** | OrderPayDTO |

**输入示例**：
```json
{
  "orderSn": "ORD202502240001"
}
```

**预期输出（成功）**：
- HTTP 200，`code`: 200，`data`: null

**异常用例**：订单号不属于当前用户或已处理过，则业务错误。

---

#### 1.4.4 个人订单列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/order/list` |
| **Query** | status(可选)：按订单状态过滤，如 10/20/30/40/50/60/70 |

**输入示例**：
- `GET /api/v1/user/order/list`
- `GET /api/v1/user/order/list?status=20`

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: OmsOrder 数组（仅当前用户的订单）

---

#### 1.4.5 取消订单

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/cancel` |
| **请求体** | OrderCancelDTO |

**输入示例**：
```json
{
  "orderId": 1
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：订单不属于当前用户、或状态不允许取消，则业务错误。

---

#### 1.4.6 用户确认订单完成

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/confirm` |
| **请求体** | OrderConfirmDTO |

**输入示例**：
```json
{
  "orderId": 1
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：订单非当前用户、或状态不是「待结算」等可确认状态，则业务错误。

---

#### 1.4.7 订单测试接口（预留）

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/order/test` |

**输入**：无请求体。  
**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

### 1.5 服务人员申请（需用户登录）

#### 1.5.1 申请成为服务人员

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/user/staff/apply` |
| **请求体** | StaffApplyDTO |

**输入示例（正常）**：
```json
{
  "realName": "李师傅",
  "idCard": "440101199001011234",
  "phone": "13900139000",
  "healthCertUrl": "https://xxx/health.jpg",
  "skillCertUrls": "[\"https://xxx/skill1.jpg\"]",
  "applyReason": "从事家政多年"
}
```

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: UmsStaffApply（含 id、status=0 待审核等）

**异常用例**：`realName` 为空、`idCard`/`phone` 格式不符等，返回校验错误。

---

#### 1.5.2 查询我的申请状态

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/user/staff/apply/status` |

**输入**：无请求体，无必填参数（依赖登录用户）。

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: StaffApplyVO（单条或 null）
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "realName": "李师傅",
    "status": 0,
    "rejectReason": null,
    "auditTime": null,
    "createTime": "2025-02-24T10:00:00"
  }
}
```

---

## 二、师傅端接口 (Staff App API)

**基础路径**：`/api/v1/staff/order`  
**认证**：需服务人员登录（userType=2），请求头携带 token。

**说明**：概要设计中师傅端「上报实时经纬度」接口 `POST /api/v1/staff/location/update` 当前代码中未实现，未列入下表。

### 2.1 抢单池/待接订单列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/staff/order/pool` |

**输入**：无请求体、无必填参数（根据当前师傅位置/技能等由后端筛选）。

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: OmsOrder 数组（可抢或待办的订单列表）

**异常用例**：未登录或非师傅账号，返回「需要服务人员权限」。

---

### 2.2 抢单

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/staff/order/grab/{orderId}` |
| **路径参数** | orderId：订单ID |

**输入示例**：
- `POST /api/v1/staff/order/grab/1`

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：

| 场景 | 预期输出 |
|------|----------|
| 非师傅账号 | code 500，msg「需要服务人员权限」 |
| 订单已被抢或状态不可抢 | code 500，msg 含「抢单失败」等 |
| 分布式锁竞争失败 | code 500，抢单失败提示 |

---

### 2.3 服务动作（出发/到达/开始/完成）

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/staff/order/action` |
| **请求体** | OrderActionDTO |

**输入示例**：
```json
{
  "orderId": 1,
  "action": "DEPART",
  "lng": 113.123,
  "lat": 23.456,
  "photos": []
}
```
action 枚举：`DEPART`(出发)、`ARRIVE`(到达)、`START`(开始)、`FINISH`(完成)。START/FINISH 可带 `photos` 数组。

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：`orderId`/`action` 为空、订单不属于当前师傅、状态不允许该动作，则 code 500。

---

### 2.4 发起现场加价/增项

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/staff/order/extra-fee` |
| **请求体** | OrderExtraFeeDTO |

**输入示例**：
```json
{
  "orderId": 1,
  "title": "增加一台空调清洗",
  "amount": 50.00,
  "photos": ["https://xxx/photo1.jpg"]
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：`orderId`/`title`/`amount` 不合法、金额≤0、订单非当前师傅，则 code 500。

---

### 2.5 开启/关闭自动接单

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/staff/order/setting/auto-accept` |
| **Query** | enable：true / false |

**输入示例**：
- `PUT /api/v1/staff/order/setting/auto-accept?enable=true`
- `PUT /api/v1/staff/order/setting/auto-accept?enable=false`

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

## 三、管理员端接口 (Admin Web API)

**认证**：需管理员登录（userType=3），请求头携带 token。

### 3.0 管理员登录

> 依据《概要设计.md》中统一用户表 `sys_user` 与管理员扩展表 `sys_admin` 的设计，管理员登录时除了发放 token 外，需要返回管理员扩展信息。

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/login` |
| **请求体** | JSON |
| **说明** | 仅 `sys_user.user_type = 3` 且在 `sys_admin` 中存在对应记录的账号可登录 |

**请求体示例（正常）**：

```json
{
  "usernameOrPhone": "admin",
  "password": "123456"
}
```

> `usernameOrPhone` 可为管理员用户名或绑定手机号。

**预期输出（成功）**：

- HTTP 200
- `code`: 200，`msg`: "success"
- `data`: AdminLoginVO（包含 token、基础用户信息与 `sys_admin` 信息）

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "token": "eyJhbGc...",
    "userId": 100,
    "username": "admin",
    "nickname": "超级管理员",
    "phone": "13700137000",
    "avatar": null,
    "userType": 3,
    "sysAdmin": {
      "id": 1,
      "userId": 100,
      "adminType": 1,
      "balance": 1000.00,
      "createdBy": null
    }
  }
}
```

字段说明（建议实现时遵循）：

- **token**：JWT 或其他形式的认证令牌，后续接口通过 `Authorization: Bearer <token>` 鉴权。
- **userType**：固定为 3，表示管理员（含超级/普通）。
- **sysAdmin.id**：`sys_admin` 表主键。
- **sysAdmin.adminType**：0-普通管理员，1-超级管理员。
- **sysAdmin.balance**：管理员账户余额（普通管理员用于活动经费等，超级管理员可为 0）。
- **sysAdmin.createdBy**：创建者 `user_id`，超级管理员初始化时可为 `null`。

**异常用例**：

| 场景 | 预期输出 |
|------|----------|
| `usernameOrPhone` 为空 | code 500，msg 含「用户名或手机号不能为空」 |
| `password` 为空 | code 500，msg 含「密码不能为空」 |
| 账号不存在 / 密码错误 | code 500，msg 含「账号或密码错误」 |
| 账号存在但 `user_type != 3` | code 500，msg 含「需要管理员账号」或类似提示 |
| `sys_admin` 中未找到对应记录 | code 500，msg 含「管理员信息缺失」或类似提示 |

### 3.1 管理员账号（超级管理员）

#### 3.1.1 创建普通管理员

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/admin/create` |
| **请求体** | AdminCreateDTO |

**输入示例**：
```json
{
  "username": "admin2",
  "password": "123456",
  "phone": "13700137000",
  "nickname": "运营管理员",
  "balance": 1000.00
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：非超级管理员调用则无权限；用户名/手机号重复、格式校验失败则 code 500。

---

### 3.2 服务管理（管理员）

**基础路径**：`/api/v1/admin/service`

#### 3.2.1 创建服务类型

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/service/create` |
| **请求体** | ServiceSaveDTO |

**输入示例**：
```json
{
  "categoryId": 1,
  "title": "日常保洁",
  "basePrice": 80.00,
  "unit": "小时",
  "depositType": 1,
  "description": "日常保洁服务"
}
```
depositType：0-全额支付，1-只付定金，2-线下报价。

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.2.2 修改服务类型

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/admin/service/{id}` |
| **路径参数** | id：服务ID |
| **请求体** | ServiceSaveDTO（同创建） |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.2.3 删除服务类型

| 项目 | 说明 |
|------|------|
| **路径** | `DELETE /api/v1/admin/service/{id}` |

**输入示例**：`DELETE /api/v1/admin/service/1`

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.2.4 获取单个服务详情

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/service/{id}` |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: PmsService

---

#### 3.2.5 分页查询服务列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/service/page` |
| **Query** | categoryId(可选), title(可选), page(默认1), pageSize(默认20) |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: PmsService 数组

---

### 3.6 类目管理（管理员）

**基础路径**：`/api/v1/admin/category`

#### 3.6.1 创建类目

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/category/create` |
| **请求体** | CategorySaveDTO |

**输入示例**：
```json
{
  "parentId": 0,
  "name": "日常保洁",
  "icon": "https://xxx/icon-clean.png",
  "formConfig": "{\"fields\":[{\"name\":\"rooms\",\"type\":\"number\"}]}",
  "sort": 0,
  "isShow": 1
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.6.2 修改类目

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/admin/category/{id}` |
| **路径参数** | id：类目ID |
| **请求体** | CategorySaveDTO（同创建） |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.6.3 删除类目

| 项目 | 说明 |
|------|------|
| **路径** | `DELETE /api/v1/admin/category/{id}` |

**输入示例**：`DELETE /api/v1/admin/category/1`

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.6.4 获取单个类目详情

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/category/{id}` |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: PmsCategory

---

#### 3.6.5 分页查询类目列表（按名称模糊）

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/category/page` |
| **Query** | name(可选，按名称模糊), page(默认1), pageSize(默认20) |

**输入示例**：
- `GET /api/v1/admin/category/page`
- `GET /api/v1/admin/category/page?name=保洁&page=1&pageSize=10`

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: PmsCategory 数组

---

### 3.3 优惠券管理（管理员）

**基础路径**：`/api/v1/admin/coupon`

> 说明：仅管理员可调用，用于在后台上架/下架优惠券模板，配置立减/满减规则和价格。

#### 3.3.1 创建优惠券

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/coupon/create` |
| **请求体** | CouponSaveDTO |

**输入示例（立减券，可余额购买也可兑换码领取）**：
```json
{
  "name": "新用户立减10元",
  "couponType": 1,
  "amount": 10.00,
  "minAmount": 0.00,
  "price": 1.00,
  "totalCount": 1000,
  "receiveLimit": 1,
  "obtainChannel": 0,
  "receiveStartTime": "2025-02-20T00:00:00",
  "receiveEndTime": "2025-03-01T23:59:59",
  "useStartTime": "2025-02-20T00:00:00",
  "useEndTime": "2025-03-10T23:59:59",
  "remark": "新用户专享"
}
```

**输入示例（满减券，仅兑换码领取）**：
```json
{
  "name": "满100减20",
  "couponType": 2,
  "amount": 20.00,
  "minAmount": 100.00,
  "price": 0.00,
  "totalCount": 0,
  "receiveLimit": 0,
  "obtainChannel": 2,
  "receiveStartTime": "2025-02-20T00:00:00",
  "receiveEndTime": "2025-03-31T23:59:59",
  "useStartTime": "2025-02-20T00:00:00",
  "useEndTime": "2025-04-30T23:59:59"
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：非管理员调用、字段缺失或金额配置非法（如满减券 `minAmount` 小于 `amount`）等返回 code 500。

---

#### 3.3.2 修改优惠券

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/admin/coupon/{id}` |
| **路径参数** | id：优惠券ID |
| **请求体** | CouponSaveDTO（同创建） |

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.3.3 上架/下架优惠券

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/admin/coupon/{id}/status` |
| **请求体** | CouponStatusDTO |

**输入示例**：
```json
{
  "status": 1
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

#### 3.3.4 分页查询优惠券列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/coupon/page` |
| **Query** | status(可选), name(可选), page(默认1), pageSize(默认20) |

**输入示例**：
- `GET /api/v1/admin/coupon/page`
- `GET /api/v1/admin/coupon/page?status=1&page=1&pageSize=10`

**预期输出（成功）**：
- HTTP 200，`code`: 200  
- `data`: CouponVO 数组

### 3.3 服务人员申请审核（管理员）

#### 3.3.1 申请列表

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/staff/apply/list` |
| **Query** | status(可选 0/1/2), page(默认1), pageSize(默认20) |

**输入示例**：
- `GET /api/v1/admin/staff/apply/list`
- `GET /api/v1/admin/staff/apply/list?status=0&page=1&pageSize=10`

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: StaffApplyVO 数组（含 applicantNickname、applicantUsername 等）

---

#### 3.3.2 审核申请（通过/驳回）

| 项目 | 说明 |
|------|------|
| **路径** | `PUT /api/v1/admin/staff/apply/audit` |
| **请求体** | StaffApplyAuditDTO |

**输入示例（通过）**：
```json
{
  "applyId": 1,
  "approved": true,
  "rejectReason": null
}
```

**输入示例（驳回）**：
```json
{
  "applyId": 1,
  "approved": false,
  "rejectReason": "健康证已过期"
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

**异常用例**：驳回时未填 `rejectReason` 若业务要求必填则 code 500。

---

### 3.4 结算配置（管理员）

**基础路径**：`/api/v1/admin/settle`

#### 3.4.1 获取当前结算配置

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/settle/config` |

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: SettleConfigDTO
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "mode": "percent",
    "fixedAmount": 10.00,
    "percent": 0.20
  }
}
```

---

#### 3.4.2 保存结算配置

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/settle/config` |
| **请求体** | SettleConfigDTO |

**输入示例（按比例抽成）**：
```json
{
  "mode": "percent",
  "fixedAmount": 0,
  "percent": 0.20
}
```

**输入示例（固定金额）**：
```json
{
  "mode": "fixed",
  "fixedAmount": 10.00,
  "percent": 0
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

### 3.5 派单配置（管理员）

**基础路径**：`/api/v1/admin/dispatch`

#### 3.5.1 获取派单配置

| 项目 | 说明 |
|------|------|
| **路径** | `GET /api/v1/admin/dispatch/config` |

**预期输出（成功）**：
- HTTP 200，`code`: 200
- `data`: DispatchConfigDTO（enableAuto、weightScore、weightDistance、weightPunctual、radiusKm、maxConcurrentOrders 等）

---

#### 3.5.2 保存派单配置

| 项目 | 说明 |
|------|------|
| **路径** | `POST /api/v1/admin/dispatch/config` |
| **请求体** | DispatchConfigDTO |

**输入示例**：
```json
{
  "enableAuto": true,
  "weightScore": 0.5,
  "weightDistance": 0.3,
  "weightPunctual": 0.2,
  "radiusKm": 5,
  "maxConcurrentOrders": 3
}
```

**预期输出（成功）**：HTTP 200，`code`: 200，`data`: null

---

## 四、订单状态码速查

| 状态码 | 含义     |
|--------|----------|
| 10     | 待支付   |
| 20     | 待接单   |
| 30     | 待服务   |
| 31     | 上门中   |
| 40     | 待报价/补差价 |
| 50     | 服务中   |
| 60     | 待结算   |
| 70     | 已完成   |
| 80     | 已取消/退款 |

---

## 五、接口清单汇总

| 模块     | 方法 | 路径 | 说明 |
|----------|------|------|------|
| 用户认证 | POST | /api/v1/user/register | 用户注册 |
| 用户认证 | POST | /api/v1/user/login | 用户登录 |
| 用户-类目 | GET | /api/v1/user/category/list | 类目树 |
| 用户-服务 | GET | /api/v1/user/service/page | 服务分页 |
| 用户-订单 | POST | /api/v1/user/order/create | 创建订单 |
| 用户-订单 | POST | /api/v1/user/order/pay | 发起支付 |
| 用户-订单 | POST | /api/v1/user/order/pay-success | 支付成功确认 |
| 用户-订单 | GET | /api/v1/user/order/list | 订单列表 |
| 用户-订单 | POST | /api/v1/user/order/cancel | 取消订单 |
| 用户-订单 | POST | /api/v1/user/order/confirm | 确认完成 |
| 用户-订单 | POST | /api/v1/user/order/test | 测试接口 |
| 用户-申请 | POST | /api/v1/user/staff/apply | 申请成为师傅 |
| 用户-申请 | GET | /api/v1/user/staff/apply/status | 我的申请状态 |
| 师傅-订单 | GET | /api/v1/staff/order/pool | 抢单池 |
| 师傅-订单 | POST | /api/v1/staff/order/grab/{orderId} | 抢单 |
| 师傅-订单 | PUT | /api/v1/staff/order/action | 服务动作 |
| 师傅-订单 | POST | /api/v1/staff/order/extra-fee | 现场加价 |
| 师傅-订单 | PUT | /api/v1/staff/order/setting/auto-accept | 自动接单开关 |
| 管理员-认证 | POST | /api/v1/admin/login | 管理员登录 |
| 管理员-账号 | POST | /api/v1/admin/admin/create | 创建普通管理员 |
| 管理员-服务 | POST | /api/v1/admin/service/create | 创建服务 |
| 管理员-服务 | PUT | /api/v1/admin/service/{id} | 更新服务 |
| 管理员-服务 | DELETE | /api/v1/admin/service/{id} | 删除服务 |
| 管理员-服务 | GET | /api/v1/admin/service/{id} | 服务详情 |
| 管理员-服务 | GET | /api/v1/admin/service/page | 服务分页 |
| 管理员-类目 | POST | /api/v1/admin/category/create | 创建类目 |
| 管理员-类目 | PUT | /api/v1/admin/category/{id} | 更新类目 |
| 管理员-类目 | DELETE | /api/v1/admin/category/{id} | 删除类目 |
| 管理员-类目 | GET | /api/v1/admin/category/{id} | 类目详情 |
| 管理员-类目 | GET | /api/v1/admin/category/page | 类目分页 |
| 管理员-优惠券 | POST | /api/v1/admin/coupon/create | 创建优惠券 |
| 管理员-优惠券 | PUT | /api/v1/admin/coupon/{id} | 更新优惠券 |
| 管理员-优惠券 | PUT | /api/v1/admin/coupon/{id}/status | 上架/下架优惠券 |
| 管理员-优惠券 | GET | /api/v1/admin/coupon/page | 优惠券分页 |
| 用户-优惠券 | GET | /api/v1/user/coupon/my | 我的优惠券列表 |
| 用户-优惠券 | GET | /api/v1/user/coupon/center | 优惠券中心列表 |
| 用户-优惠券 | POST | /api/v1/user/coupon/buy | 余额购买优惠券 |
| 用户-优惠券 | POST | /api/v1/user/coupon/redeem | 兑换码领取优惠券 |
| 管理员-申请 | GET | /api/v1/admin/staff/apply/list | 申请列表 |
| 管理员-申请 | PUT | /api/v1/admin/staff/apply/audit | 审核申请 |
| 管理员-结算 | GET | /api/v1/admin/settle/config | 结算配置查询 |
| 管理员-结算 | POST | /api/v1/admin/settle/config | 结算配置保存 |
| 管理员-派单 | GET | /api/v1/admin/dispatch/config | 派单配置查询 |
| 管理员-派单 | POST | /api/v1/admin/dispatch/config | 派单配置保存 |

以上为当前项目已实现接口的完整测试用例与输入输出说明，可直接用于 Postman/Apifox/自动化测试脚本编写。
