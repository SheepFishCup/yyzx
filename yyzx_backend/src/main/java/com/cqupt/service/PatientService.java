package com.cqupt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqupt.pojo.Patient;
import com.cqupt.utils.ResultVo;

public interface PatientService extends IService<Patient> {
    ResultVo<Patient> login(String code);
}
