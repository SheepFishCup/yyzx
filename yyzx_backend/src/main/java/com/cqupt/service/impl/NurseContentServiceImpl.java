package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/28 10:18
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.NurseItemDTO;
import com.cqupt.exception.BusinessException;
import com.cqupt.exception.DeletionNotAllowedException;
import com.cqupt.mapper.CustomerNurseItemMapper;
import com.cqupt.mapper.NurseContentMapper;
import com.cqupt.mapper.NurseLevelItemMapper;
import com.cqupt.pojo.NurseContent;
import com.cqupt.service.NurseContentService;
import com.cqupt.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NurseContentServiceImpl extends ServiceImpl<NurseContentMapper, NurseContent> implements NurseContentService {

    @Autowired
    private NurseLevelItemMapper nurseLevelItemMapper;
    @Autowired
    private NurseContentMapper nurseContentMapper;
    @Autowired
    private CustomerNurseItemMapper customerNurseItemMapper;

    @Override
    public ResultVo<List<NurseContent>> listNurseItemByLevel(Integer levelId) throws Exception {
        //先查询级别的项目配置-只查询item_id
        QueryWrapper qw=new QueryWrapper();
        qw.eq("level_id",levelId);
        qw.select("item_id");
        List<Integer> itemIds =nurseLevelItemMapper.selectObjs(qw);
        List<NurseContent> nurseContents = new ArrayList<>();
        //判断是否有记录
        if (itemIds.size()>0){
            //查询护理项目信息
            nurseContents = nurseContentMapper.selectBatchIds(itemIds);
        }
        return ResultVo.ok(nurseContents);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo updateNurseItem(NurseContent nursCcontent) throws Exception {
        // 如果nursCcontent.getStatus()为null，默认设为启用状态
        if (nursCcontent.getStatus() == null) {
            nursCcontent.setStatus(1); // 默认启用状态
        }
        //如果修改状态为--停用，需要直接剔除护理级别护理项目列表中的记录，保证列表中的项目都是可用的
        if (nursCcontent.getStatus() == 2){
            QueryWrapper qwCount=new QueryWrapper();
            qwCount.eq("level_id",nursCcontent.getId());
            Long count = nurseLevelItemMapper.selectCount(qwCount);
            if (count > 0){
                QueryWrapper qw=new QueryWrapper();
                qw.eq("level_id",nursCcontent.getId());
                int row = nurseLevelItemMapper.delete(qw);
                //更新护理项目
                boolean temp = updateById(nursCcontent);
                if (!(temp && row>0)){
                    throw new Exception("更新护理项目失败");
                }
                return ResultVo.ok("更新护理项目成功");
            }
        }
        updateById(nursCcontent);
        return ResultVo.ok("更新护理项目成功");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo delNurseItem(Integer id) throws Exception {
        NurseContent nurseContent = new NurseContent();
        nurseContent.setIsDeleted(1);
        nurseContent.setId(id);
        QueryWrapper qw=new QueryWrapper();
        qw.eq("item_id",id);
        boolean updateSuccess = updateById(nurseContent);
        Long customerCount = customerNurseItemMapper.selectCount(qw);
        if (customerCount > 0){
            throw new DeletionNotAllowedException("当前护理项目正在使用中，请先删除该护理项目在客户护理项目列表中的记录");
        }
        //查询当前的护理项目是否在护理级别中，如果在列表中，则删除
        Long count = nurseLevelItemMapper.selectCount(qw);
        if (count > 0){
            int row = nurseLevelItemMapper.delete(qw);
            boolean temp = updateById(nurseContent);
            if (!(temp && row>0)){
                throw new DeletionNotAllowedException("删除护理项目失败");
            }
            return ResultVo.ok("删除护理项目成功");
        }else if (!updateSuccess){
            throw new BusinessException("删除护理项目失败");
        }
        return ResultVo.ok("删除护理项目成功");
    }




}
