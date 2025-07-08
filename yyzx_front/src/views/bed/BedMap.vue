
<template>
  <div class="main">
    <div class="query">
      <table class="query-table">
        <tr>
          <td>
            <div class="query-form">
              <el-form :inline="true">
                <el-form-item label="楼层：">
                  <el-select style="width: 150px;" v-model="form.floor" @change="findCwsyBedVo">
                    <el-option label="一楼" value="一楼" />
                    <el-option label="二楼" value="二楼" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </td>
          <td style="width:120px">
            <div>
              <img src="@/assets/bed/all.png" />
              <span class="pic-text">总床数:</span>{{count.zcw}}
            </div>
          </td>
          <td style="width:120px">
            <div>
              <img src="@/assets/bed/kx.png" />
              <span class="pic-text">空闲:</span>{{count.kx}}
            </div>
          </td>
          <td style="width:120px">
            <div>
              <img src="@/assets/bed/yr.png" />
              <span class="pic-text">有人:</span>{{count.yr}}
            </div>
          </td>
          <td style="width:120px">
            <div>
              <img src="@/assets/bed/wc.png" />
              <span class="pic-text">外出:</span>{{count.wc}}
            </div>
          </td>
        </tr>
      </table>
    </div>
    <div class="map">
      <table border="1px" cellspacing="20px" style=" border-collapse: collapse; width:1200px">
        <tbody>
          <tr class="cwsyt_thead">
            <td
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index <1 "
            >{{item.roomNo}}</td>
            <td class="room_id" rowspan="2" style="background-color:#AEEEEE">电梯厅</td>
            <td class="room_id" rowspan="2" colspan="2" style="background-color:#9AC0CD">洗衣房</td>
            <td class="room_id" rowspan="2" colspan="2" style="background-color:#4EEE94">活动中心</td>
            <td
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index>0 && index<4"
            >{{item.roomNo}}</td>
            <td class="room_id" rowspan="2" style="background-color:#CDAA7D">走道</td>
            <td
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index>=4 && index<6"
            >{{item.roomNo}}</td>
          </tr>
          <tr class="cwsyt_tbody">
            <td  
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index <6 ">
              <a href="javascript:void(0)" v-for="bedItem in item.bedList" :key="bedItem.bedNo">
                <div>
                  <img v-if="bedItem.bedStatus==1" src="@/assets/bed/kx.png" />
                  <img v-if="bedItem.bedStatus==2" src="@/assets/bed/yr.png" />
                  <img v-if="bedItem.bedStatus==3" src="@/assets/bed/wc.png" />
                </div>{{bedItem.bedNo}}
              </a>
            </td>
          </tr>
          <tr></tr>
          <tr class="cwsyt_thead">
            <td
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index>=6"
            >{{item.roomNo}}</td>
          </tr>
          <tr class="cwsyt_tbody">
            <td  
              v-for="(item,index) in listRoom"
              :key="item.roomNo"
              v-show="index >=6 ">
              <a href="javascript:void(0)" v-for="bedItem in item.bedList" :key="bedItem.bedNo">
                <div>
                  <img v-if="bedItem.bedStatus==1" src="@/assets/bed/kx.png" />
                  <img v-if="bedItem.bedStatus==2" src="@/assets/bed/yr.png" />
                  <img v-if="bedItem.bedStatus==3" src="@/assets/bed/wc.png" />
                </div>{{bedItem.bedNo}}
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>


<script>
import {findCwsyBedVo} from '@/api/bedApi.js'
export default {
  data() {
    return {
      //查询条件数据
      form: {
        floor: "一楼" 
      },
      count:{
        zcw:'',
        kx:'',
        yr:'',
        wc:''
      },
      listRoom: []
    };
  },
  mounted() {
    //初始化床位示意图
    this.findCwsyBedVo()
  },
  methods:{
    //查询房间和床位信息列表 、床位统计信息
    findCwsyBedVo(){
      findCwsyBedVo(this.form).then(res=>{
        this.count.zcw=res.data.zcw;
        this.count.kx=res.data.kx;
        this.count.yr=res.data.yr;
        this.count.wc=res.data.wc;
        this.listRoom=res.data.roomList
      })
    }
  }
};
</script>

<style scope>
.main .query {
  height: 60px;
}
.main .query .query-table {
  height: 60px;
}
.main .query .query-table .query-form {
  margin-top: 12px;
}
.main .query .query-table img {
  width: 30px;
  height: 20px;
}
.main .query .query-table .pic-text {
  margin-left: 5px;
  margin-right: 5px;
  font-weight: 700;
}

.main .map {
  margin-top: 3px;
}

.main .map .cwsyt_thead {
  background-color: #0168b7;
  color: white;
  text-align: center;
}
.main .map .cwsyt_tbody {
  height: 140px;
  text-align: center;
}
.main .map .cwsyt_tbody div {
  width: 50px;
  margin-top: 5px;
}
.main .map .cwsyt_tbody div > img {
  width: 100%;
  height: 100%;
  margin-left: 35%;
}
.main .map table > tbody > tr > td {
  border: 1px solid #ddd;
}
a {
  color: #333;
}
</style>