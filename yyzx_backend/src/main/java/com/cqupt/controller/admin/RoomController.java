package com.cqupt.controller.admin;
/*
 * Project: yyzx_backend
 * @author yyr
 * @date 2025/06/25 14:36
 * @description
 */


import com.cqupt.pojo.Room;
import com.cqupt.service.RoomService;
import com.cqupt.utils.ResultVo;
import com.cqupt.vo.CwsyBedVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
//@RequestMapping("/admin/room")
@RequestMapping("/room")
@Api(tags = "房间管理") // swagger分组
@CrossOrigin
public class RoomController {
    @Autowired
    private RoomService roomService;

    @GetMapping("/findCwsyBedVo")
    @ApiOperation("查询床位示意图")
    @ApiImplicitParams({
            @ApiImplicitParam(dataType="String", name="floor", value="楼层",required = true)
    })
    public ResultVo<CwsyBedVo> findCwsyBedVo(String floor) throws Exception {
        log.info("查询床位示意图");
        return roomService.findCswyBedVo(floor);
    }

    @GetMapping("/listRoom")
    @ApiOperation("查询床位列表")
    public ResultVo<List<Room>> listRoom(){
        return ResultVo.ok(roomService.list());
    }

}
