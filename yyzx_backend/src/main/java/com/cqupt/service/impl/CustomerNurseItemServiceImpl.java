package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 14:45
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerNurseItemServiceImpl extends ServiceImpl<CustomerNurseItemMapper, CustomerNurseItem> implements CustomerNurseItemService {

    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;

    @Autowired
    private CustomerMapper customerMapper;

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
            //判断选择的是级别中的护理项目 还是单独购买的护理项目
            if (customerNurseItems.get(0).getLevelId() != null){
                //购买的级别中护理项目
                Customer customer = new Customer();
                customer.setId(customerNurseItems.get(0).getCustomerId());
                customer.setLevelId(customerNurseItems.get(0).getLevelId());
                //update  customer set level_id =? where id = ?
                int row1 = customerMapper.updateById(customer);
                boolean flag = saveBatch(customerNurseItems);
                if (!(flag && row1>0)){
                    throw new Exception("添加护理项目失败");
                }
            }else {
                //单独购买的护理项目
                saveBatch(customerNurseItems);
            }
            return ResultVo.ok("添加护理项目成功");
        }
        return ResultVo.fail("请选择要添加的护理项目");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomerLevelAndItem(Integer levelId, Long customerId) throws Exception {
//        Customer customer = new Customer();
//        customer.setId(customerId);
//        customer.setLevelId(null);
//        //update  customer set level_id =? where id = customerId
//        customerMapper.updateById(customer);

//        UpdateWrapper updateWrapper = new UpdateWrapper();
//        Customer customer = new Customer();
//        customer.setLevelId(null);
//        updateWrapper.eq("id",customerId);
//        // update customer set level_id = null where id = customerId
//        customerMapper.update(customer,updateWrapper);

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
//        Customernurseitem customernurseitem=new Customernurseitem();
//        customernurseitem.setIsDeleted(1);
        int row2=customerNurseItemMapper.update(null,uw2);
        if(!(row1>0&&row2>0)){
            throw new Exception("护理项目配置失败");
        }
        return ResultVo.ok("移除成功");
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo enewNurseItem(CustomerNurseItem customerNurseItem) throws Exception {
//        //判断用户是否配置了某个护理项目
//        QueryWrapper queryWrapper = new QueryWrapper();
//        queryWrapper.eq("id", customerNurseItem.getId());
//        CustomerNurseItem customerNurseItem1 = customerNurseItemService.getOne(queryWrapper);
//        if (customerNurseItem1 == null){
//            return ResultVo.fail("用户未配置该护理项目");
//        }
//        //修改:maturityTime,nurseNumber
//        customerNurseItem1.setMaturityTime(customerNurseItem.getMaturityTime());
//        customerNurseItem1.setNurseNumber(customerNurseItem.getNurseNumber());
//        //更新
//        boolean row2 = customerNurseItemService.updateById(customerNurseItem1);
//        //判断更新是否成功
//        if (row2){
//            return ResultVo.ok("续费成功");
//        }
//        throw new Exception("续费失败");
        int row = customerNurseItemMapper.updateById(customerNurseItem);
        if (row <= 0){
            throw new Exception("续费失败");
        }
        return ResultVo.ok("续费成功");
    }

    @Override
    public ResultVo isIncludesItemCustomer(Integer itemId, Long customerId) throws Exception {
//        QueryWrapper<CustomerNurseItem> queryWrapper = new QueryWrapper();
//        queryWrapper.eq("item_id", itemId);
//        //原数据库字段为custormer_id
//        queryWrapper.eq("customer_id", customerId);
//        queryWrapper.eq("is_deleted", 0);
//        int row = customerNurseItemService.count(queryWrapper);
//        if (row > 0){
//            CustomerNurseItem item = customerNurseItemService.getOne(queryWrapper);
//            //显示该项
//            return ResultVo.ok("用户已配置了该护理项目", item);
//        }
//        return ResultVo.fail("用户未配置该护理项目");
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

