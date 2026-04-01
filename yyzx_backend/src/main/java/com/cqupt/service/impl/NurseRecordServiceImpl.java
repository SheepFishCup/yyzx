package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/30 09:07
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.constant.MessageConstant;
import com.cqupt.dto.NurseRecordDTO;
import com.cqupt.exception.BusinessException;
import com.cqupt.exception.NurseitemException;
import com.cqupt.mapper.CustomerNurseItemMapper;
import com.cqupt.mapper.NurseRecordMapper;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.pojo.NurseRecord;
import com.cqupt.service.NurseRecordService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.NurseRecordsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    public ResultVo addNurseRecord(NurseRecord nurserecord) throws Exception {
        try {
            // 查询当前用户的护理项目余量
            LambdaQueryWrapper<CustomerNurseItem> lqw = new LambdaQueryWrapper<>();
            lqw.eq(CustomerNurseItem::getCustomerId, nurserecord.getCustomerId())
                    .eq(CustomerNurseItem::getItemId, nurserecord.getItemId());
            CustomerNurseItem customernurseitem = customerNurseItemMapper.selectOne(lqw);

            if (customernurseitem == null) {
                throw new BusinessException(MessageConstant.NURSEITEM_NOT_FOUND);
            }
            if (customernurseitem.getNurseNumber() < nurserecord.getNursingCount()){//剩余数量不足
                throw new NurseitemException(MessageConstant.NUMBER_ERROR);
            }
            // 修改用户护理项目数量
            LambdaUpdateWrapper<CustomerNurseItem> luw = new LambdaUpdateWrapper<>();
            luw.eq(CustomerNurseItem::getCustomerId, nurserecord.getCustomerId())
                    .eq(CustomerNurseItem::getItemId, nurserecord.getItemId())
                    .set(CustomerNurseItem::getNurseNumber, customernurseitem.getNurseNumber() - nurserecord.getNursingCount());
            int count = customerNurseItemMapper.update(null, luw);

            // 生成护理记录
            nurserecord.setIsDeleted(0);
            boolean temp = save(nurserecord);

            if (!(count > 0 && temp)) {
                throw new Exception("护理记录生成失败");
            }

            return ResultVo.ok("护理记录生成成功");
        } catch (Exception e) {
            throw new Exception("操作失败：" + e.getMessage());
        }
    }

    @Override
    public ResultVo<Page<NurseRecordsVo>> queryNurseRecordsVo(NurseRecordDTO nurseRecordDTO) throws Exception {
        Integer current = nurseRecordDTO.getCurrent() != null ? nurseRecordDTO.getCurrent() : 1;
        Integer pageSize = nurseRecordDTO.getPageSize() != null ? nurseRecordDTO.getPageSize() : 10;
        Page<NurseRecordsVo> page = new Page<>(current,pageSize);
        nurseRecordMapper.selectNurseRecordsVo(page,nurseRecordDTO.getCustomerId());
        return ResultVo.ok(page);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomerRecord(Long id) throws Exception {
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
