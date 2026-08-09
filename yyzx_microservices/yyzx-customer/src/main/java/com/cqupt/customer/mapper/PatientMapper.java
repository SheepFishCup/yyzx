package com.cqupt.customer.mapper;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2026/03/08 19:20
 * @description
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqupt.pojo.Patient;
import org.apache.ibatis.annotations.Select;

public interface PatientMapper extends BaseMapper<Patient> {
    @Select("select * from patient where openid = #{openid}")
    Patient getPatientByOpenid(String openid);
}
