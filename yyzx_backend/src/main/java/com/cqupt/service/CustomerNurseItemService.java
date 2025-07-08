package com.cqupt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.CustomerNurseItemDTO;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerNurseItemVo;

import java.util.List;

public interface CustomerNurseItemService extends IService<CustomerNurseItem> {
    ResultVo<Page<CustomerNurseItemVo>> listCustomerItem(CustomerNurseItemDTO customerNurseItemDTO) throws Exception;

    ResultVo addItemToCustomer(List<CustomerNurseItem> customerNurseItems) throws Exception;

    ResultVo removeCustomerLevelAndItem(Integer levelId, Integer customerId) throws Exception;

    ResultVo enewNurseItem(CustomerNurseItem customerNurseItem) throws Exception;

    ResultVo isIncludesItemCustomer(Integer itemId, Integer customerId) throws Exception;

    ResultVo removeCustomerItem(Integer id) throws Exception;
}
