package com.cqupt.checkinout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqupt.dto.OutwardDTO;
import com.cqupt.pojo.Outward;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.OutwardVo;
import org.apache.ibatis.annotations.Param;

public interface OutwardMapper extends BaseMapper<Outward> {
    Page<OutwardVo> selectOutwardVo(@Param("page") Page<OutwardVo> page,
                                    @Param("userId") Long userId,
                                    @Param("customerId") Long customerId
                                    ) throws Exception;

}
