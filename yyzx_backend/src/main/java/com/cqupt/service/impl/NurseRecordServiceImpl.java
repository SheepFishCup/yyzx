package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 09:07
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.NurseRecordDTO;
import com.cqupt.mapper.CustomerNurseItemMapper;
import com.cqupt.mapper.NurseRecordMapper;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.pojo.NurseRecord;
import com.cqupt.service.NurseRecordService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.NurseRecordsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NurseRecordServiceImpl extends ServiceImpl<NurseRecordMapper, NurseRecord> implements NurseRecordService {

    @Autowired
    private NurseRecordMapper nurseRecordMapper;
    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addNurseRecord(NurseRecord nurseRecord) throws Exception {
        //生成护理记录
        nurseRecord.setIsDeleted(0);//默认生效
        boolean temp=save(nurseRecord);
        //查询当前用户的护理项目余量
        QueryWrapper qw=new QueryWrapper();
        qw.eq("custormer_id",nurseRecord.getCustomerId());
        qw.eq("item_id",nurseRecord.getItemId());
        qw.eq("is_deleted",0);
        CustomerNurseItem customerNurseItem = customerNurseItemMapper.selectOne(qw);
        // 校验获取到的customerNurseItem是否为空
        if (customerNurseItem == null) {
            throw new Exception("未找到对应的护理项目信息");
        }
        //修改当前用户护理项目余量
        UpdateWrapper uw=new UpdateWrapper();
        //剩余的护理数量 = 当前用户护理项目数量 - 已护理的数量
        uw.set("nurse_number",customerNurseItem.getNurseNumber()-nurseRecord.getNursingCount());
        qw.eq("item_id",nurseRecord.getItemId());
        qw.eq("custormer_id",nurseRecord.getCustomerId());
        qw.eq("is_deleted",0);
        int row = customerNurseItemMapper.update(null,uw);
        if (!(temp&&row>0)){
            throw new Exception("更新用户护理项目失败");
        }
        return ResultVo.ok("护理记录生成成功");
    }

    @Override
    public ResultVo<Page<NurseRecordsVo>> queryNurseRecordsVo(NurseRecordDTO nurseRecordDTO) throws Exception {
        Page<NurseRecordsVo> page = new Page<>(nurseRecordDTO.getPageSize(),6);
        nurseRecordMapper.selectNurseRecordsVo(page,nurseRecordDTO.getCustomerId());
        return ResultVo.ok(page);
    }

    @Override
    public ResultVo removeCustomerRecord(Integer id) throws Exception {
//        UpdateWrapper<NurseRecord> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("id",id);
//        updateWrapper.eq("is_deleted",1);
//        int row = nurseRecordMapper.update(null,updateWrapper);

        NurseRecord nurseRecord = new NurseRecord();
        nurseRecord.setId(id);
        nurseRecord.setIsDeleted(1);

        int row = nurseRecordMapper.updateById(nurseRecord);
        if (row>0){
            return ResultVo.ok("移除成功");
        }
        return ResultVo.fail("移除失败");
    }
}
