package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/27 09:22
 * @description 
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.CustomerPreferenceDTO;
import com.cqupt.mapper.CustomerPreferenceMapper;
import com.cqupt.pojo.CustomerPreference;
import com.cqupt.service.CustomerPreferenceService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CustomerPreferenceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerPreferenceServiceImpl extends ServiceImpl<CustomerPreferenceMapper, CustomerPreference> implements CustomerPreferenceService {

    @Autowired
    private CustomerPreferenceMapper customerPreferenceMapper;

    @Override
    public ResultVo<Page<CustomerPreferenceVo>> listCustomerPreferenceVoPage(CustomerPreferenceDTO customerPreferenceDTO) throws Exception {
        // 后端控制分页参数，设置默认值和范围
        Integer current = customerPreferenceDTO.getCurrent() != null ? customerPreferenceDTO.getCurrent() : 1;
        Integer pageSize = customerPreferenceDTO.getPageSize() != null ? customerPreferenceDTO.getPageSize() : 10;

        // 校验参数范围，防止恶意请求
        if (current < 1) {
            current = 1;  // 最小为第 1 页
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;  // 超出范围使用默认值 10
        }

        Page<CustomerPreferenceVo> page = new Page<>(current, pageSize);
        customerPreferenceMapper.selectCustomerPreferenceVo(page, customerPreferenceDTO.getCustomerName());
        return ResultVo.ok(page);

    }

}
