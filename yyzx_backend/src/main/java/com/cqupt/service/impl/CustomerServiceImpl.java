package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 10:30
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.config.RabbitMQConfig;
import com.cqupt.context.BaseContext;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.dto.LogMessage;
import com.cqupt.dto.MailMessage;
import com.cqupt.dto.NotifyMessage;
import com.cqupt.mapper.BedDetailsMapper;
import com.cqupt.mapper.BedMapper;
import com.cqupt.mapper.CustomerMapper;
import com.cqupt.mapper.UserMapper;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.BedDetails;
import com.cqupt.pojo.Customer;
import com.cqupt.pojo.User;
import com.cqupt.service.CustomerService;
import com.cqupt.service.RabbitMQProducerService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.KhxxCustomerVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private BedDetailsMapper bedDetailsMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitMQProducerService rabbitMQProducerService;


    @Override
    public ResultVo<Page<KhxxCustomerVo>> KhxxFindCustomer(KhxxDTO khxxDTO) throws Exception {
        // 获取当前登录用户 ID（用于缓存隔离）
        Long currentUserId = BaseContext.getCurrentId();
        // 获取当前登录用户的角色（通过查询数据库）
        User currentUser = userMapper.selectById(currentUserId);
        Integer roleId = currentUser != null ? currentUser.getRoleId() : null;

        // 设置默认分页参数，避免 null
        Integer current = khxxDTO.getCurrent() != null ? khxxDTO.getCurrent() : 1;
        Integer pageSize = khxxDTO.getPageSize() != null ? khxxDTO.getPageSize() : 10;

        // 参数范围校验，防止恶意请求
        if (current < 1) {
            current = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 构造缓存 key（管理员共享，管家隔离）
        String cacheKey;
        if (roleId != null && roleId == 1) { // 假设 1 是管理员
            // 管理员查询所有客户，缓存共享
            cacheKey = "customer:page:admin:" +
                    current + ":" +
                    pageSize + ":" +
                    (khxxDTO.getManType() != null ? khxxDTO.getManType() : "all") + ":" +
                    (StringUtils.isNotBlank(khxxDTO.getCustomerName()) ? khxxDTO.getCustomerName() : "all");

        } else {
            // 管家查询自己负责的客户，缓存隔离
            // 使用 userId 参数（管家 ID）来隔离
            cacheKey = "customer:page:butler:" +
                    (khxxDTO.getUserId() != null ? khxxDTO.getUserId() : currentUserId) + ":" +
                    current + ":" +
                    pageSize + ":" +
                    (khxxDTO.getManType() != null ? khxxDTO.getManType() : "all") + ":" +
                    (StringUtils.isNotBlank(khxxDTO.getCustomerName()) ? khxxDTO.getCustomerName() : "all");
        }
        // 1. 先查缓存
        Page<KhxxCustomerVo> cached = (Page<KhxxCustomerVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return ResultVo.ok(cached);
        }

        // 2. 缓存未命中，加分布式锁（防止缓存击穿）
        RLock lock = redissonClient.getLock("lock:" + cacheKey);
        if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
            try {
                // 双重检查（其他线程可能已经查好了）
                cached = (Page<KhxxCustomerVo>) redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return ResultVo.ok(cached);
                }

                // 3. 查数据库
                Page<KhxxCustomerVo> page = new Page<>(current, pageSize);
                customerMapper.selectPageVo(page, khxxDTO.getCustomerName(), khxxDTO.getManType(), khxxDTO.getUserId());

                // 4. 写缓存（随机过期时间，防止雪崩）
                long randomExpireTime = 300 + (long)(Math.random() * 300); // 5-10 分钟随机
                redisTemplate.opsForValue().set(cacheKey, page, randomExpireTime, TimeUnit.SECONDS);

                return ResultVo.ok(page);
            } finally {
                lock.unlock();
            }
        } else {
            // 获取锁失败，等待后重试（防止死等）
            Thread.sleep(100);
            return KhxxFindCustomer(khxxDTO); // 递归重试
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addCustomer(Customer customer) throws Exception {
        //查询床位是否可用
        Bed bed = bedMapper.selectById(customer.getBedId());
        if (bed.getBedStatus() != 1){
            return ResultVo.fail("该床位已使用");
        }
        //生成客户信息
        customer.setIsDeleted(0);
        customer.setUserId((long) -1);//默认无管家
        int row1=customerMapper.insert(customer);
        //生成入住详细信息
        BedDetails bedDetails = new BedDetails();
        bedDetails.setBedId(customer.getBedId());//设置床位id
        bedDetails.setIsDeleted(0);//设置显示
        bedDetails.setCustomerId(customer.getId());//设置客户id
        bedDetails.setStartDate(customer.getCheckinDate());//设置入住时间
        bedDetails.setEndDate(customer.getExpirationDate());//设置到期时间
        bedDetails.setBedDetails(customer.getBuildingNo()+"#"+customer.getRoomNo());//设置床位详情
        int row2=bedDetailsMapper.insert(bedDetails);
        //修改床位状态
        Bed bed1 = new Bed();
        bed1.setId(customer.getBedId());
        bed1.setBedStatus(2);
        int row3=bedMapper.updateById(bed1);
        // 新增：添加客户后清除缓存
        clearCustomerCache();
        //判断是否入住成功
        if (row1>0&&row2>0&&row3>0){
            // ✅ 使用 RabbitMQ 异步发送系统通知给管理员
            sendAdminNotification(customer);

            // ✅ 使用 RabbitMQ 异步记录操作日志
            sendCreateLog(customer);
            return ResultVo.ok("入住成功");
        }
        throw new Exception("入住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomer(Long id, Integer bedId) throws Exception {
        //修改用户is_delete=1 不显示
        Customer customer = new Customer();
//        Customer customer = customerMapper.selectById(id);
//        可以减少设置的客户信息，只设置id和is_deleted
        customer.setId(id);
        customer.setIsDeleted(1);
        int row1=customerMapper.updateById(customer);
        //修改床位状态为空闲-1
        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setBedStatus(1);
        int row2=bedMapper.updateById(bed);
        //将床位信息is_delete=1
        BedDetails bedDetails = new BedDetails();
        bedDetails.setIsDeleted(1);
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("customer_id",id);
        updateWrapper.eq("bed_id",bedId);
        updateWrapper.eq("is_deleted",0);
        int row3=bedDetailsMapper.update(bedDetails,updateWrapper);

        // 新增：删除客户后清除缓存
        clearCustomerCache();

        if (row1>0&&row2>0&&row3>0){
            // ✅ 使用 RabbitMQ 异步记录退住日志
            sendRemoveLog(id, bedId);
            return ResultVo.ok("退住成功");
        }
        throw new Exception("退住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo editCustomer(Customer customer) throws Exception {
        //1.修改用户信息
        int row1=customerMapper.updateById(customer);

        //2.如果合同到期时间发生改变，更新当前生效床位信息的退住时间为合同到期时间
        if (customer.getExpirationDate()!=null){
            UpdateWrapper uw = new UpdateWrapper();
            uw.eq("customer_id",customer.getId());
            uw.eq("is_deleted",0);
            BedDetails bedDetails = new BedDetails();
            bedDetails.setEndDate(customer.getExpirationDate());
            int row2=bedDetailsMapper.update(bedDetails,uw);
            if (!(row1>0&&row2>0)){
                throw new Exception("编辑失败");
            }
        }
        // 新增：修改客户后清除缓存
        clearCustomerCache();

        // ✅ 使用 RabbitMQ 异步记录编辑日志
        sendEditLog(customer);

        return ResultVo.ok("编辑成功");
    }
    /**
     * 清除客户列表缓存（保证数据一致性）
     */
    private void clearCustomerCache() {
        // 简单做法：不清除具体 key，等它自动过期（5-10 分钟）
        // 如果需要立即清除，可以使用 Redis 的 pattern 删除
        // 注意：生产环境慎用 keys 命令，会影响性能
        // redisTemplate.delete(redisTemplate.keys("customer:page:*"));
    }

    /**
     * 发送欢迎短信（替代邮件）
     */
    private void sendWelcomeSms(Customer customer) {
        try {
            // 假设客户有手机号字段
            String phone = customer.getContactTel(); // 需要有 phone 字段
            if (phone != null && !phone.isEmpty()) {
                NotifyMessage notifyMessage = NotifyMessage.builder()
                        .userId(customer.getId())
                        .type("SMS")
                        .title("入住欢迎")
                        .content("尊敬的 " + customer.getCustomerName() +
                                "，欢迎入住养老护理中心！您的房间号：" +
                                customer.getRoomNo() + "。祝您生活愉快！")
                        .build();
                rabbitMQProducerService.sendNotify(notifyMessage);
            }
        } catch (Exception e) {
            log.error("发送欢迎短信失败：customerId={}", customer.getId(), e);
        }
    }
    /**
     * 发送管理员通知
     */
    private void sendAdminNotification(Customer customer) {
        try {
            // 发送到管理员群组通知队列
            NotifyMessage notifyMessage = NotifyMessage.builder()
                    .userId(null) // null 表示群组通知
                    .type("SYSTEM")
                    .title("新客户入住通知")
                    .content("新客户 " + customer.getCustomerName() +
                            " 已入住 " + customer.getBuildingNo() + "#" + customer.getRoomNo())
                    .build();

            rabbitMQProducerService.sendAdminNotify(notifyMessage);

            log.info("发送管理员群组通知：customerId={}", customer.getId());
        } catch (Exception e) {
            log.error("发送管理员通知失败：customerId={}", customer.getId(), e);
        }
    }

    /**
     * 发送创建客户日志
     */
    private void sendCreateLog(Customer customer) {
        try {
            LogMessage logMessage = LogMessage.builder()
                    .level("INFO")
                    .module("CUSTOMER")
                    .action("CREATE")
                    .message("创建新客户：" + customer.getCustomerName() +
                            "，房间：" + customer.getRoomNo())
                    .operator("system")
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitMQProducerService.sendLog(logMessage);
        } catch (Exception e) {
            log.error("发送创建日志失败：customerId={}", customer.getId(), e);
        }
    }

    /**
     * 发送删除客户日志
     */
    private void sendRemoveLog(Long customerId, Integer bedId) {
        try {
            LogMessage logMessage = LogMessage.builder()
                    .level("INFO")
                    .module("CUSTOMER")
                    .action("REMOVE")
                    .message("客户退住：ID=" + customerId + ", 床位 ID=" + bedId)
                    .operator("system")
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitMQProducerService.sendLog(logMessage);
        } catch (Exception e) {
            log.error("发送删除日志失败：customerId={}", customerId, e);
        }
    }

    /**
     * 发送编辑客户日志
     */
    private void sendEditLog(Customer customer) {
        try {
            LogMessage logMessage = LogMessage.builder()
                    .level("INFO")
                    .module("CUSTOMER")
                    .action("EDIT")
                    .message("编辑客户信息：" + customer.getCustomerName() +
                            "，ID=" + customer.getId())
                    .operator("system")
                    .timestamp(LocalDateTime.now())
                    .build();
            rabbitMQProducerService.sendLog(logMessage);
        } catch (Exception e) {
            log.error("发送编辑日志失败：customerId={}", customer.getId(), e);
        }
    }
}
