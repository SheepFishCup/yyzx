package com.cqupt.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.vo.CustomerNurseItemVo;
import org.apache.ibatis.annotations.Param;

public interface CustomerNurseItemMapper extends BaseMapper<CustomerNurseItem> {
    Page<CustomerNurseItemVo> selectCustomerItemVo(@Param("page") Page<CustomerNurseItemVo> page,
                                                   @Param("customerId") Long customerId) throws Exception;
}
