package com.cqupt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.pojo.Customer;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.KhxxCustomerVo;

public interface CustomerService extends IService<Customer> {
    // 分页查询客户信息
    ResultVo<Page<KhxxCustomerVo>> KhxxFindCustomer(KhxxDTO khxxDTO) throws Exception;
    // 客户信息添加
    ResultVo addCustomer(Customer customer) throws Exception;
    // 客户信息删除及床位信息
    ResultVo removeCustomer(Integer id,Integer bedId) throws Exception;
    // 客户信息修改
    ResultVo editCustomer(Customer customer) throws Exception;
}
