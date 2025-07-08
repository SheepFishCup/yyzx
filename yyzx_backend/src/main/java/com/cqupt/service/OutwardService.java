package com.cqupt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.Outward;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import org.springframework.transaction.annotation.Transactional;

public interface OutwardService extends IService<Outward> {
    ResultVo examineOutward(Outward  outward) throws Exception;
    ResultVo<Page<OutwardVo>> queryOutwardVo(OutwardDTO outwardDTO) throws Exception;
    ResultVo delOutward(Integer id) throws Exception;
    ResultVo updateOutward(Outward outward) throws Exception;
    ResultVo addOutward(Outward outward) throws Exception;
}
