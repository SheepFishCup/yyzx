package com.cqupt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.pojo.Customer;
import com.cqupt.vo.KhxxCustomerVo;
import org.apache.ibatis.annotations.Param;

public interface CustomerMapper extends BaseMapper<Customer> {
//    manType 1- 自理老人 2-代理老人 3-无管家
    Page<KhxxCustomerVo> selectPageVo(@Param("page") Page<KhxxCustomerVo> page,
                                      @Param("customerName") String customerName,
                                      @Param("manType") Integer manType,
                                      @Param("userId") Integer userId);
}
