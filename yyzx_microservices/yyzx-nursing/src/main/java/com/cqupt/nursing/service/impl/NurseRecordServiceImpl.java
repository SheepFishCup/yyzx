package com.cqupt.nursing.service.impl;
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
import com.cqupt.nursing.mapper.CustomerNurseItemMapper;
import com.cqupt.nursing.mapper.NurseRecordMapper;
import com.cqupt.pojo.CustomerNurseItem;
import com.cqupt.pojo.NurseRecord;
import com.cqupt.nursing.service.NurseRecordService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.NurseRecordsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Slf4j
@Service
public class NurseRecordServiceImpl extends ServiceImpl<NurseRecordMapper, NurseRecord> implements NurseRecordService {

    @Autowired
    private NurseRecordMapper nurseRecordMapper;
    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addNurseRecord(NurseRecord nurserecord) throws Exception {
        String stockKey = "nurse:item:stock:" + nurserecord.getCustomerId() + ":" + nurserecord.getItemId();
        Integer nursingCount = nurserecord.getNursingCount();
        Long result = null;

        try {
            LambdaQueryWrapper<CustomerNurseItem> lqw = new LambdaQueryWrapper<>();
            lqw.eq(CustomerNurseItem::getCustomerId, nurserecord.getCustomerId())
                    .eq(CustomerNurseItem::getItemId, nurserecord.getItemId());
            CustomerNurseItem customernurseitem = customerNurseItemMapper.selectOne(lqw);

            if (customernurseitem == null) {
                throw new BusinessException(MessageConstant.NURSEITEM_NOT_FOUND);
            }

            try {
                stringRedisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(customernurseitem.getNurseNumber()));

                String luaScript =
                        "local stock = redis.call('GET', KEYS[1]) " +
                                "if stock == false then " +
                                "    return -1 " +
                                "end " +
                                "local stockNum = tonumber(stock) " +
                                "local needNum = tonumber(ARGV[1]) " +
                                "if stockNum < needNum then " +
                                "    return 0 " +
                                "end " +
                                "redis.call('DECRBY', KEYS[1], needNum) " +
                                "return 1";

                result = stringRedisTemplate.execute(
                        new DefaultRedisScript<>(luaScript, Long.class),
                        Collections.singletonList(stockKey),
                        String.valueOf(nursingCount)
                );
            } catch (Exception redisEx) {
                log.warn("Redis不可用，降级为数据库乐观锁，itemId: {}", nurserecord.getItemId(), redisEx);
                result = 1L;
            }

            if (result == null || result == -1) {
                throw new BusinessException("护理项目库存未配置，请联系管理员");
            }

            if (result == 0) {
                throw new NurseitemException(MessageConstant.NUMBER_ERROR);
            }

            LambdaUpdateWrapper<CustomerNurseItem> luw = new LambdaUpdateWrapper<>();
            luw.eq(CustomerNurseItem::getCustomerId, nurserecord.getCustomerId())
                    .eq(CustomerNurseItem::getItemId, nurserecord.getItemId())
                    .ge(CustomerNurseItem::getNurseNumber, nursingCount)
                    .setSql("nurse_number = nurse_number - " + nursingCount);

            int count = customerNurseItemMapper.update(null, luw);
            if (count == 0) {
                throw new NurseitemException(MessageConstant.NUMBER_ERROR);
            }
            nurserecord.setIsDeleted(0);
            save(nurserecord);

            return ResultVo.ok("护理记录生成成功");
        } catch (Exception e) {
            if (result != null && result == 1) {
                try {
                    stringRedisTemplate.opsForValue().increment(stockKey, nursingCount);
                } catch (Exception rollbackEx) {
                    log.error("Redis库存回滚失败（可能Redis宕机），key: {}, count: {}", stockKey, nursingCount, rollbackEx);
                }
            }
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
