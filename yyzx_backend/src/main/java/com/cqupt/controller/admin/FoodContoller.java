package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/26 09:01
 * @description
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.annotaion.File;
import com.cqupt.constant.FileUploadConstant;
import com.cqupt.exception.BusinessException;
import com.cqupt.mapper.FoodMapper;
import com.cqupt.pojo.Food;
import com.cqupt.service.FoodService;
import com.cqupt.utils.FileUploadUtil;
import com.cqupt.utils.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
//@RequestMapping("/admin/food")
@RequestMapping("/food")
@Api(tags = "食物管理") // swagger分组
@CrossOrigin
public class FoodContoller {
    @Autowired
    private FoodService foodService;

    @Autowired
    private FoodMapper foodMapper;
//    @GetMapping("/listFood")
//    @ApiOperation("食物列表查询")
//    public ResultVo<List<Food>> listFood() {
//        return ResultVo.ok(foodService.list());
//    }
    @GetMapping("/listFood")
    @ApiOperation("食物列表查询")
    public List<Food> listFood() {
        return foodService.list();
    }

    @PostMapping("/upload")
    @ApiOperation("文件上传接口")
    public ResultVo<String> upload(
            @ApiParam("需要上传的文件")
            @File(maxSize = 5, allowFileTypes = {"jpg", "jpeg", "png", "gif"})
            MultipartFile file)
    {
        // 先检查文件是否为空，给出更友好的提示
        if (file == null || file.isEmpty()) {
            log.warn("上传失败：文件为空");
            return ResultVo.error("上传的文件不能为空，请选择文件后再上传");
        }
        try {
            String originalFilename = file.getOriginalFilename();

            // 提取文件名（不含扩展名）和扩展名
            String fileNameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf("."));
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            // 构建要检查的文件名
            String fileNameToCheck = fileNameWithoutExt + "." + extension;

            // 检查数据库中是否已有相同的文件名
            QueryWrapper<Food> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("food_img", fileNameToCheck);
            Food existingFood = getExistingFood(queryWrapper);
            if (existingFood != null){
                log.warn("文件已存在");
                throw new BusinessException("文件已存在");
            }
            String fileName = FileUploadUtil.upload(file);
            String path = FileUploadConstant.FILE_ACCESS_PATH + fileName;
            log.info("文件上传成功，访问路径：{}", path);
            return ResultVo.ok(fileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResultVo.error("文件上传失败：" + e.getMessage());
        } catch (RuntimeException e) {
            log.error("文件验证失败", e);
            return ResultVo.fail(e.getMessage());
        }
    }

    private Food getExistingFood(QueryWrapper<Food> queryWrapper) {
        return foodMapper.selectOne(queryWrapper);
    }

    @PostMapping("/addFood")
    @ApiOperation("添加食物")
    public ResultVo<String> addFood(@RequestBody Food food){
        return foodService.save(food)?ResultVo.ok("添加成功"):ResultVo.fail("添加失败");
    }
}
