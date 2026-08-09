package com.cqupt.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.pojo.Backdown;
import com.cqupt.vo.BackdownVo;
import org.apache.ibatis.annotations.Param;

public interface BackdownMapper extends BaseMapper<Backdown> {
    Page<BackdownVo> selectBackdownVo(@Param("page") Page<BackdownVo> page,
                                      @Param("userId") Long userId,
                                      @Param("customerId") Long customerId
                                    ) throws Exception;
}
