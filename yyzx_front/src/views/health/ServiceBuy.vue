<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="护理项目名称"
                  v-model="queryParams.itemName"
                  @clear="query"
                  clearable
                  size="large"
                >
                  <template #append>
                    <el-button type="info" @click="query" style="color:black">查询</el-button>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="7" style="margin-top:5px">
                <el-button type="primary" color="#1d83ec" @click="this.$router.go(-1)" round>返回</el-button>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <div class="table-main">
            <div class="table-main-header">护理项目</div>
            <!-- 表格 -->
            <el-table
              :data="nurseItemList"
              style="width: 100% ;color:black;"
              size="small"
              @row-click="changeRow"
              stripe
            >
              <el-table-column
                align="center"
                type="index"
                :index="indexMethod"
                label="序号"
                width="60"
              />
              <el-table-column align="center" prop="serialNumber" label="编号" width="180" />
              <el-table-column align="center" prop="nursingName" label="名称" width="180" />
              <el-table-column align="center" prop="servicePrice" label="价格" width="180" />
              <el-table-column align="center" prop="executionCycle" label="执行周次" width="180" />
              <el-table-column align="center" prop="executionTimes" label="执行次数" width="180" />
              <el-table-column align="center" prop="message" label="备注" width="180" />
            </el-table>
            <!-- 分页插件 -->
            <div style="margin-top:15px">
              <el-pagination
                :page-size="page.pageSize"
                background
                :current-page="page.currentPag"
                layout=" prev, pager, next"
                :total="page.total"
                @current-change="handleCurrentChange"
              />
            </div>
          </div>
          <el-divider></el-divider>
          <div class="table-main">
            <div class="table-main-header">已选护理项目</div>
            <!-- 表格 -->
            <el-table :data="changeNurseItemList" style="width: 100% ;color:black;" size="small" stripe>
              <el-table-column align="center" type="index" label="序号" width="150" />
              <el-table-column align="center" prop="serialNumber" label="编号" width="150" />
              <el-table-column align="center" prop="nursingName" label="名称" width="150" />
              <el-table-column align="center" prop="buyTime" label="服务购买日期" width="150"/>
                  <el-table-column align="center" prop="nurseNumber" label="购买数量" width="150">
                    <template #default="scope">
                      <el-input v-model="scope.row.nurseNumber" placeholder="购买数量"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" prop="maturityTime" label="服务到期日期" width="150">
                    <template #default="scope">
                      <el-date-picker
                        style="width:130px"
                        format="YYYY/MM/DD"
                        value-format="YYYY-MM-DD"
                        v-model="scope.row.maturityTime"
                        type="date"
                        placeholder="服务到期日期"
                      ></el-date-picker>
                    </template>
                  </el-table-column>
  
              <el-table-column align="center"  label="操作" width="150">
                <template #default="scope">
                  <el-button type="danger" icon="CloseBold" size="small" @click="del(scope.$index)"></el-button>
                </template>
              </el-table-column>
            </el-table>
            
          </div>
          <div class="demo-drawer__footer" style="margin:0 auto;width:200px">
                <el-button @click="clearArr"  type="warning">清空</el-button>
                <el-button @click="isOk" type="success">保存</el-button>
              </div>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { findNurseItem } from "@/api/nurseItemApi.js";
  import { isIncludesItemCustomer ,addItemToCustomer} from "@/api/customerNurseItem.js";
  export default {
    data() {
      return {
        customerId: this.$route.query.customerId,
  
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        queryParams: {
          status: "1", //查询默认状态1 -启用
          itemName: "",
          pageSize: "1" //默认第一页
        },
        changeNurseItemList: [], //已选择护理项目
        nurseItemList: [] //护理项目列表
      };
    },
    mounted() {
      this.getNurseItemList();
    },
    computed: {
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      }
    },
    methods: {
      //点击查询
      query() {
        this.queryParams.pageSize = "1"; //回到第一页
        this.getNurseItemList();
      },
      //选中页码
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.queryParams.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getNurseItemList();
      },
      //鼠标点击某行触发：添加护理项目到已选护理项目列表  api-判断用户是否已经配置了某个指定项目
      changeRow(row) {
          //api-判断用户是否已经配置了某个指定项目
          isIncludesItemCustomer({customerId:this.customerId,itemId:row.id}).then(res=>{
              if(!res.flag){
                  this.$message.error("当前用户已存在该项目,可前往【服务关注】进行续费")
                  return
              }
              //构建已选护理项目列表
              let obj=row;
              obj.buyTime=new Date().getFullYear()+"-"+new Date().getMonth()+"-"+new Date().getDate();
              obj.maturityTime = new Date().getFullYear()+"-"+(new Date().getMonth()+3)+"-"+new Date().getDate(); //默认三个月到期
              obj.nurseNumber=1
              //判断是否已经添加过
              if(JSON.stringify(this.changeNurseItemList).includes(JSON.stringify(obj))===true){
                  this.$message.warning("所选项目已存在列表中")
                  return
              }
              this.changeNurseItemList.push(obj)    
           })
          
      },
      //移除已选护理项目列表项
      del(index){
          this.changeNurseItemList.splice(index,1)
      },
      //清空已选护理项目列表项
      clearArr(){
          this.changeNurseItemList=[]
      },
      //api-查询护理项目(分页)
      getNurseItemList() {
        findNurseItem(this.queryParams).then(res => {
          this.nurseItemList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
      },
      //api-点击确认 批量生成客户护理项目信息
      isOk() {
        let customerItemList=this.changeNurseItemList
        let paramsArr=[];//提交的参数
        if(customerItemList.length==0){
            this.$message.error("请选择需要【购买的】护理项目")
            return
        }
        //构建提交的参数列表
        for(let i=0;i<customerItemList.length;i++){
          let obj={} 
          obj.itemId=customerItemList[i].id
          obj.buyTime=customerItemList[i].buyTime
          obj.customerId=this.customerId
          // obj.custormerId=this.custormerId
          obj.nurseNumber=customerItemList[i].nurseNumber
          obj.isDeleted=0
          obj.maturityTime=customerItemList[i].maturityTime
          paramsArr.push(obj)
        }
        this.$confirm("确定添护理项目数据无误?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "error",
          icon:'WarningFilled'
        })
          .then(() => {
            addItemToCustomer(paramsArr).then(res=>{
                if (res.flag) {
                  this.$message.success(res.message);
                  //路由会上一级
                  this.$router.go(-1)
              } else {
                  this.$message.error(res.message);
              }
            })
           
          })
          .catch(() => {});
        // console.log(paramsArr);
  
      },
    }
  };
  </script>
  <style scoped>
  .table-main {
    height: 300px;
    border: 1px solid #d4c5c5;
    width: 100%;
  }
  .table-main .table-main-header {
    height: 30px;
    background-color: #1d83ec;
    color: #fff;
    font-size: 18px;
    font-family: "Microsoft YaHei";
    line-height: 30px;
  }
  </style>