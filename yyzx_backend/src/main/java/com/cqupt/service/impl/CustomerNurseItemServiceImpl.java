package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 14:45
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.CustomerNurseItemDTO;
import com.cqupt.mapper.CustomerMapper;
import com.cqupt.mapper.CustomerNurseItemMapper;
import com.cqupt.pojo.Customer;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.service.CustomerNurseItemService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerNurseItemVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CustomerNurseItemServiceImpl extends ServiceImpl<CustomerNurseItemMapper, CustomerNurseItem> implements CustomerNurseItemService {

    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ResultVo<Page<CustomerNurseItemVo>> listCustomerItem(CustomerNurseItemDTO customerNurseItemDTO) throws Exception {
        Integer current = customerNurseItemDTO.getCurrent()!= null ? customerNurseItemDTO.getCurrent() : 1;
        Integer pageSize = customerNurseItemDTO.getPageSize() != null ? customerNurseItemDTO.getPageSize() : 10;
        if (current<1){
            current=1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        Page<CustomerNurseItemVo> page = new Page<>(current, pageSize);
        customerNurseItemMapper.selectCustomerItemVo(page,customerNurseItemDTO.getCustomerId());
        return ResultVo.ok(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addItemToCustomer(List<CustomerNurseItem> customerNurseItems) throws Exception {
        if (customerNurseItems.size() > 0){
            if (customerNurseItems.get(0).getLevelId() != null){
                Customer customer = new Customer();
                customer.setId(customerNurseItems.get(0).getCustomerId());
                customer.setLevelId(customerNurseItems.get(0).getLevelId());
                int row1 = customerMapper.updateById(customer);
                boolean flag = saveBatch(customerNurseItems);
                if (!(flag && row1>0)){
                    throw new Exception("添加护理项目失败");
                }
            }else {
                saveBatch(customerNurseItems);

                for (CustomerNurseItem item : customerNurseItems) {
                    try {
                        String stockKey = "nurse:item:stock:" + item.getCustomerId() + ":" + item.getItemId();
                        Integer initialStock = item.getNurseNumber() != null ? item.getNurseNumber() : 0;
                        redisTemplate.opsForValue().setIfAbsent(stockKey, initialStock);
                    } catch (Exception e) {
                        log.warn("Redis库存初始化失败，itemId: {}", item.getItemId(), e);
                    }
                }
            }
            return ResultVo.ok("添加护理项目成功");
        }
        return ResultVo.fail("请选择要添加的护理项目");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomerLevelAndItem(Integer levelId, Long customerId) throws Exception {
        //更新客户级别为null
        UpdateWrapper uw1=new UpdateWrapper();
        uw1.set("level_id",null);
        uw1.eq("id",customerId);
        int row1=customerMapper.update(null,uw1);

        //删除客户当前级别的所有护理项目
        UpdateWrapper uw2=new UpdateWrapper();
        uw2.set("is_deleted",1);
        uw2.eq("level_id",levelId);
        uw2.eq("customer_id",customerId);
        int row2=customerNurseItemMapper.update(null,uw2);
        if(!(row1>0&&row2>0)){
            throw new Exception("护理项目配置失败");
        }
        return ResultVo.ok("移除成功");
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo enewNurseItem(CustomerNurseItem customerNurseItem) throws Exception {
        CustomerNurseItem existItem = customerNurseItemMapper.selectById(customerNurseItem.getId());
        if (existItem == null) {
            throw new Exception("护理项目不存在");
        }

        int addCount = customerNurseItem.getNurseNumber();
        if (addCount <= 0) {
            throw new Exception("续费数量必须大于0");
        }

        LambdaUpdateWrapper<CustomerNurseItem> luw = new LambdaUpdateWrapper<>();
        luw.eq(CustomerNurseItem::getId, customerNurseItem.getId())
                .setSql("nurse_number = nurse_number + " + addCount);

        if (customerNurseItem.getMaturityTime() != null && existItem.getMaturityTime() != null) {
            long extendedTime = existItem.getMaturityTime().getTime() +
                    (customerNurseItem.getMaturityTime().getTime() - System.currentTimeMillis());
            luw.set(CustomerNurseItem::getMaturityTime, new java.util.Date(extendedTime));
        }

        int row = customerNurseItemMapper.update(null, luw);
        if (row <= 0) {
            throw new Exception("续费失败");
        }

        try {
            String stockKey = "nurse:item:stock:" + existItem.getCustomerId() + ":" + existItem.getItemId();
            redisTemplate.opsForValue().increment(stockKey, addCount);
        } catch (Exception redisEx) {
            log.error("Redis库存同步失败（可能Redis宕机），下次将自动重建，itemId: {}", existItem.getItemId(), redisEx);
            try {
                String stockKey = "nurse:item:stock:" + existItem.getCustomerId() + ":" + existItem.getItemId();
                redisTemplate.delete(stockKey);
            } catch (Exception deleteEx) {
                log.warn("删除Redis key失败（Redis可能宕机），忽略", deleteEx);
            }
        }

        return ResultVo.ok("续费成功");
    }

    @Override
    public ResultVo isIncludesItemCustomer(Integer itemId, Long customerId) throws Exception {
        QueryWrapper<CustomerNurseItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("item_id", itemId);
        queryWrapper.eq("customer_id", customerId);
        queryWrapper.eq("is_deleted", 0);
        int row = customerNurseItemMapper.selectCount(queryWrapper);
        if (row > 0){
            List<CustomerNurseItem> item = customerNurseItemMapper.selectList(queryWrapper);
            return ResultVo.ok("该用户已购买该护理项目", item.get(0));
        }
        return ResultVo.ok("该用户未购买该护理项目");
    }

    //移除
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomerItem(Long id) throws Exception {
        CustomerNurseItem customerNurseItem =new CustomerNurseItem();
        customerNurseItem.setId(id);
        customerNurseItem.setIsDeleted(1);
        int row = customerNurseItemMapper.updateById(customerNurseItem);
        if (row>0){
            return ResultVo.ok("移除成功");
        }
        throw new Exception("移除失败");
    }
}

