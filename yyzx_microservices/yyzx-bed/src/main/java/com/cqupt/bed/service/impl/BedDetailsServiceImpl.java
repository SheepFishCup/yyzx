package com.cqupt.bed.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/24 16:01
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.BedDetailsDTO;
import com.cqupt.dto.ExchangeDTO;
import com.cqupt.bed.mapper.BedDetailsMapper;
import com.cqupt.bed.mapper.BedMapper;
import com.cqupt.feign.CustomerFeignClient;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.BedDetails;
import com.cqupt.pojo.Customer;
import com.cqupt.bed.service.BedDetailsService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.BedDetailsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class BedDetailsServiceImpl extends ServiceImpl<BedDetailsMapper, BedDetails> implements BedDetailsService {
    @Autowired
    private BedDetailsMapper bedDetailsMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private CustomerFeignClient customerFeignClient;

    @Override
    public ResultVo<Page<BedDetailsVo>> listBedDetailsVoPage(BedDetailsDTO bedDetailsDTO) throws Exception {
        Page<BedDetailsVo> page = new Page<>(bedDetailsDTO.getCurrent(), bedDetailsDTO.getPageSize());
        bedDetailsMapper.selectBedDetails(page, bedDetailsDTO);
        return ResultVo.ok(page);
    }

    //床位调换
    @GlobalTransactional(name = "bed-exchange-bed", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo exchangeBed(ExchangeDTO exchangeDTO) throws Exception {
        // 1、查询床位是否可用
        Bed bed=bedMapper.selectById(exchangeDTO.getNewBedId());
        // status: 房间状态 1:空闲 2:有人 3:外出
        if (bed.getBedStatus() != 1){
            return ResultVo.fail("该床位已使用");
        }
        // 2、修改客户旧床位记录（beddetails)，is_deleted = 1，结束的时间设置为当前时间
        BedDetails bedDetails = bedDetailsMapper.selectById(exchangeDTO.getId());
        bedDetails.setIsDeleted(1);
        bedDetails.setEndDate(new Date());
        int row1=bedDetailsMapper.updateById(bedDetails);
        // 3、添加新的床位记录（beddetails)
        BedDetails newBedDetails = new BedDetails();
        newBedDetails.setIsDeleted(0);
        newBedDetails.setCustomerId(exchangeDTO.getCustomerId());
        newBedDetails.setBedId(exchangeDTO.getNewBedId());

        newBedDetails.setStartDate(new Date());
        // 将新床位的结束时间设置为原来床位的结束时间
        newBedDetails.setEndDate(bedDetails.getEndDate());

        newBedDetails.setBedDetails(exchangeDTO.getBuildingNo()+"#"+bed.getBedNo());
        int row2=bedDetailsMapper.insert(newBedDetails);
        // 4、修床位的状态改委空闲， bed_status = 1
        Bed oldBed =new Bed();
        oldBed.setId(exchangeDTO.getOldBedId());
        oldBed.setBedStatus(1);
        int row3=bedMapper.updateById(oldBed);
        // 5、设置新床位为有人的状态，bed_status =2
        Bed newBed = new Bed();
        newBed.setId(exchangeDTO.getNewBedId());
        newBed.setBedStatus(2);
        int row4=bedMapper.updateById(newBed);
        // 6、修改客户的信息：新的房间号 新床位号 楼号
        Customer customer = new Customer();
        customer.setId(exchangeDTO.getCustomerId());
        customer.setBedId(exchangeDTO.getNewBedId());
        customer.setBuildingNo(exchangeDTO.getBuildingNo());
        customer.setRoomNo(exchangeDTO.getNewRoomNo());

        ResultVo<Void> custResult = customerFeignClient.updateCustomer(
                exchangeDTO.getCustomerId(), customer);
        if (row1>0&&row2>0&&row3>0&&row4>0&&custResult != null && custResult.isFlag()){
            return ResultVo.ok("换房成功");
        }
        throw new Exception("换房失败");
    }
}