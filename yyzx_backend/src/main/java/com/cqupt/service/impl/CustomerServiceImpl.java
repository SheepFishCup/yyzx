package com.cqupt.service.impl;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 10:30
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqupt.dto.KhxxDTO;
import com.cqupt.mapper.BedDetailsMapper;
import com.cqupt.mapper.BedMapper;
import com.cqupt.mapper.CustomerMapper;
import com.cqupt.pojo.Bed;
import com.cqupt.pojo.BedDetails;
import com.cqupt.pojo.Customer;
import com.cqupt.service.CustomerService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.KhxxCustomerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private BedMapper bedMapper;
    @Autowired
    private BedDetailsMapper bedDetailsMapper;

    @Override
    public ResultVo<Page<KhxxCustomerVo>> KhxxFindCustomer(KhxxDTO khxxDTO) throws Exception {
        Page<KhxxCustomerVo> page=new Page<>(khxxDTO.getPageSize(),6);//第一参数为当前页，第二个参数为页包含多少数据
        customerMapper.selectPageVo(page,khxxDTO.getCustomerName(),khxxDTO.getManType(),khxxDTO.getUserId());
        return ResultVo.ok(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addCustomer(Customer customer) throws Exception {
        //查询床位是否可用
        Bed bed = bedMapper.selectById(customer.getBedId());
        if (bed.getBedStatus() != 1){
            return ResultVo.fail("该床位已使用");
        }
        //生成客户信息
        customer.setIsDeleted(0);
        customer.setUserId(-1);//默认无管家
        int row1=customerMapper.insert(customer);
        //生成入住详细信息
        BedDetails bedDetails = new BedDetails();
        bedDetails.setBedId(customer.getBedId());//设置床位id
        bedDetails.setIsDeleted(0);//设置显示
        bedDetails.setCustomerId(customer.getId());//设置客户id
        bedDetails.setStartDate(customer.getCheckinDate());//设置入住时间
        bedDetails.setEndDate(customer.getExpirationDate());//设置到期时间
        bedDetails.setBedDetails(customer.getBuildingNo()+"#"+customer.getRoomNo());//设置床位详情
        int row2=bedDetailsMapper.insert(bedDetails);
        //修改床位状态
        Bed bed1 = new Bed();
        bed1.setId(customer.getBedId());
        bed1.setBedStatus(2);
        int row3=bedMapper.updateById(bed1);
        //判断是否入住成功
        if (row1>0&&row2>0&&row3>0){
            return ResultVo.ok("入住成功");
        }
        throw new Exception("入住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo removeCustomer(Integer id, Integer bedId) throws Exception {
        //修改用户is_delete=1 不显示
        Customer customer = new Customer();
//        Customer customer = customerMapper.selectById(id);
//        可以减少设置的客户信息，只设置id和is_deleted
        customer.setId(id);
        customer.setIsDeleted(1);
        int row1=customerMapper.updateById(customer);
        //修改床位状态为空闲 -1
        Bed bed = new Bed();
        bed.setId(bedId);
        bed.setBedStatus(1);
        int row2=bedMapper.updateById(bed);
        //将床位信息is_delete=1
        BedDetails bedDetails = new BedDetails();
        bedDetails.setIsDeleted(1);
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("customer_id",id);
        updateWrapper.eq("bed_id",bedId);
        updateWrapper.eq("is_deleted",0);
        int row3=bedDetailsMapper.update(bedDetails,updateWrapper);
        if (row1>0&&row2>0&&row3>0){
            return ResultVo.ok("退住成功");
        }
        throw new Exception("退住失败");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo editCustomer(Customer customer) throws Exception {
        //1.修改用户信息
        int row1=customerMapper.updateById(customer);

        //2.如果合同到期时间发生改变，更新当前生效床位信息的退住时间为合同到期时间
        if (customer.getExpirationDate()!=null){
            UpdateWrapper uw = new UpdateWrapper();
            uw.eq("customer_id",customer.getId());
            uw.eq("is_deleted",0);
            BedDetails bedDetails = new BedDetails();
            bedDetails.setEndDate(customer.getExpirationDate());
            int row2=bedDetailsMapper.update(bedDetails,uw);
            if (!(row1>0&&row2>0)){
                throw new Exception("编辑失败");
            }
        }
        return ResultVo.ok("编辑成功");
    }
}
