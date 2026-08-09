package com.cqupt.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqupt.pojo.Bed;
import com.cqupt.vo.CwsyBedVo;

public interface BedMapper extends BaseMapper<Bed> {
    CwsyBedVo selectBedCount();
}
