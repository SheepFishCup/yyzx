package com.cqupt.customer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.CustomerPreferenceDTO;
import com.cqupt.pojo.CustomerPreference;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerPreferenceVo;

public interface CustomerPreferenceService extends IService<CustomerPreference> {
    ResultVo<Page<CustomerPreferenceVo>> listCustomerPreferenceVoPage(CustomerPreferenceDTO customerPreferenceDTO) throws Exception;
}
