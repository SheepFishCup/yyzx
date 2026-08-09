package com.cqupt.customer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.cqupt.context.BaseContext;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.dto.LogMessage;
import com.cqupt.dto.NotifyMessage;
import com.cqupt.feign.BedDetailsFeignClient;
import com.cqupt.feign.BedFeignClient;
import com.cqupt.feign.UserFeignClient;
import com.cqupt.customer.mapper.CustomerMapper;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.BedDetails;
import com.cqupt.pojo.Customer;
import com.cqupt.pojo.User;
import com.cqupt.customer.service.CustomerService;
import com.cqupt.rabbit.RabbitMQProducerService;
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
import java.util.concurrent.TimeUnit;

/**
 * 客户服务实现（Feign 版）
 * <p>跨域访问 bed / user 表全部通过 OpenFeign，不再持有对方 Mapper</p>
 */
@Slf4j
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private BedFeignClient bedFeignClient;
    @Autowired
    private BedDetailsFeignClient bedDetailsFeignClient;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RabbitMQProducerService rabbitMQProducerService;


    @Override
    public ResultVo<Page<KhxxCustomerVo>> KhxxFindCustomer(KhxxDTO khxxDTO) {
        Long currentUserId = BaseContext.getCurrentId();
        ResultVo<User> userResult = userFeignClient.getById(currentUserId);
        User currentUser = userResult != null ? userResult.getData() : null;
        Integer roleId = currentUser != null ? currentUser.getRoleId() : null;

        Integer current = khxxDTO.getCurrent() != null ? khxxDTO.getCurrent() : 1;
        Integer pageSize = khxxDTO.getPageSize() != null ? khxxDTO.getPageSize() : 10;
        if (current < 1) current = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;

        String cacheKey;
        if (roleId != null && roleId == 1) {
            cacheKey = "customer:page:admin:" + current + ":" + pageSize + ":"
                    + (khxxDTO.getManType() != null ? khxxDTO.getManType() : "all") + ":"
                    + (StringUtils.isNotBlank(khxxDTO.getCustomerName()) ? khxxDTO.getCustomerName() : "all");
        } else {
            cacheKey = "customer:page:butler:"
                    + (khxxDTO.getUserId() != null ? khxxDTO.getUserId() : currentUserId) + ":"
                    + current + ":" + pageSize + ":"
                    + (khxxDTO.getManType() != null ? khxxDTO.getManType() : "all") + ":"
                    + (StringUtils.isNotBlank(khxxDTO.getCustomerName()) ? khxxDTO.getCustomerName() : "all");
        }

        @SuppressWarnings("unchecked")
        Page<KhxxCustomerVo> cached = (Page<KhxxCustomerVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) return ResultVo.ok(cached);

        RLock lock = redissonClient.getLock("lock:" + cacheKey);
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    cached = (Page<KhxxCustomerVo>) redisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) return ResultVo.ok(cached);

                    Page<KhxxCustomerVo> page = new Page<>(current, pageSize);
                    customerMapper.selectPageVo(page, khxxDTO.getCustomerName(),
                            khxxDTO.getManType(), khxxDTO.getUserId());

                    long randomExpire = 300 + (long) (Math.random() * 300);
                    redisTemplate.opsForValue().set(cacheKey, page, randomExpire, TimeUnit.SECONDS);
                    return ResultVo.ok(page);
                } finally {
                    lock.unlock();
                }
            }
            Thread.sleep(100);
            return KhxxFindCustomer(khxxDTO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResultVo.fail("查询被中断");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<?> addCustomer(Customer customer) {
        // Feign: 查询床位是否可用
        ResultVo<Bed> bedResult = bedFeignClient.getBedById(customer.getBedId());
        if (bedResult == null || bedResult.getData() == null
                || bedResult.getData().getBedStatus() != 1) {
            return ResultVo.fail("该床位不可用");
        }

        customer.setIsDeleted(0);
        customer.setUserId((long) -1);
        int row1 = customerMapper.insert(customer);

        // Feign: 创建入住详情
        BedDetails bedDetails = new BedDetails();
        bedDetails.setBedId(customer.getBedId());
        bedDetails.setIsDeleted(0);
        bedDetails.setCustomerId(customer.getId());
        bedDetails.setStartDate(customer.getCheckinDate());
        bedDetails.setEndDate(customer.getExpirationDate());
        bedDetails.setBedDetails(customer.getBuildingNo() + "#" + customer.getRoomNo());
        ResultVo<BedDetails> bdResult = bedDetailsFeignClient.createBedDetails(bedDetails);

        // Feign: 修改床位状态为占用
        ResultVo<Void> statusResult = bedFeignClient.updateStatus(customer.getBedId(), 2);

        clearCustomerCache();

        if (row1 > 0 && bdResult != null && bdResult.isFlag()
                && statusResult != null && statusResult.isFlag()) {
            sendAdminNotification(customer);
            sendCreateLog(customer);
            sendWelcomeSms(customer);
            return ResultVo.ok("入住成功");
        }
        throw new RuntimeException("入住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<?> removeCustomer(Long id, Integer bedId) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setIsDeleted(1);
        int row1 = customerMapper.updateById(customer);

        // Feign: 释放床位
        ResultVo<Void> bedResult = bedFeignClient.updateStatus(bedId, 1);

        // Feign: 标记床位详情为已删除
        BedDetails bd = new BedDetails();
        bd.setIsDeleted(1);
        ResultVo<Void> bdResult = bedDetailsFeignClient.updateByCustomer(id, bd);

        clearCustomerCache();

        if (row1 > 0 && bedResult != null && bedResult.isFlag()
                && bdResult != null && bdResult.isFlag()) {
            sendRemoveLog(id, bedId);
            return ResultVo.ok("退住成功");
        }
        throw new RuntimeException("退住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<?> editCustomer(Customer customer) {
        int row1 = customerMapper.updateById(customer);

        if (customer.getExpirationDate() != null) {
            // Feign: 同步更新床位详情到期日
            BedDetails bd = new BedDetails();
            bd.setEndDate(customer.getExpirationDate());
            bedDetailsFeignClient.updateByCustomer(customer.getId(), bd);
        }

        clearCustomerCache();
        sendEditLog(customer);
        return ResultVo.ok("编辑成功");
    }

    private void clearCustomerCache() { /* 依赖自然过期 */ }

    private void sendWelcomeSms(Customer customer) {
        try {
            String phone = customer.getContactTel();
            if (phone != null && !phone.isEmpty()) {
                rabbitMQProducerService.sendNotify(NotifyMessage.builder()
                        .userId(customer.getId()).type("SMS").title("入住欢迎")
                        .content("尊敬的 " + customer.getCustomerName()
                                + "，欢迎入住！房间：" + customer.getRoomNo())
                        .build());
            }
        } catch (Exception e) {
            log.error("发送欢迎短信失败：customerId={}", customer.getId(), e);
        }
    }

    private void sendAdminNotification(Customer customer) {
        try {
            rabbitMQProducerService.sendAdminNotify(NotifyMessage.builder()
                    .userId(null).type("SYSTEM").title("新客户入住通知")
                    .content("新客户 " + customer.getCustomerName()
                            + " 已入住 " + customer.getBuildingNo() + "#" + customer.getRoomNo())
                    .build());
        } catch (Exception e) {
            log.error("发送管理员通知失败：customerId={}", customer.getId(), e);
        }
    }

    private void sendCreateLog(Customer customer) {
        try {
            rabbitMQProducerService.sendLog(LogMessage.builder()
                    .level("INFO").module("CUSTOMER").action("CREATE")
                    .message("创建新客户：" + customer.getCustomerName() + "，房间：" + customer.getRoomNo())
                    .operator("system").timestamp(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.error("发送创建日志失败：customerId={}", customer.getId(), e);
        }
    }

    private void sendRemoveLog(Long customerId, Integer bedId) {
        try {
            rabbitMQProducerService.sendLog(LogMessage.builder()
                    .level("INFO").module("CUSTOMER").action("REMOVE")
                    .message("客户退住：ID=" + customerId + ", 床位 ID=" + bedId)
                    .operator("system").timestamp(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.error("发送删除日志失败：customerId={}", customerId, e);
        }
    }

    private void sendEditLog(Customer customer) {
        try {
            rabbitMQProducerService.sendLog(LogMessage.builder()
                    .level("INFO").module("CUSTOMER").action("EDIT")
                    .message("编辑客户信息：" + customer.getCustomerName() + "，ID=" + customer.getId())
                    .operator("system").timestamp(LocalDateTime.now()).build());
        } catch (Exception e) {
            log.error("发送编辑日志失败：customerId={}", customer.getId(), e);
        }
    }
}
