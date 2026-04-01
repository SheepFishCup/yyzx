<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="客户姓名"
                  v-model="condition.customerName"
                  clearable
                  @clear="query"
                  size="large"
                >
                  <template #append>
                    <el-button type="info" @click="query" style="color:black">查询</el-button>
                  </template>
                </el-input>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <div>
            <!-- 表格 -->
            <el-table :data="khxxList" style="width: 100% ;color:black;" stripe>
              <el-table-column
                align="center"
                fixed
                type="index"
                :index="indexMethod"
                label="序号"
                width="60"
              />
              <el-table-column align="center" fixed prop="customerName" label="客户姓名" width="120" />
              <el-table-column align="center" prop="customerAge" label="年龄" width="80" />
              <el-table-column align="center" prop="customerSex" label="性别" width="80">
                <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="roomNo" label="房间号" width="100" />
              <el-table-column align="center" prop="bedNo" label="床号" width="100" />
              <el-table-column align="center" prop="buildingNo" label="所属楼房" width="100" />
              <el-table-column align="center" prop="contactTel" label="联系电话" width="150" />
              <el-table-column align="center" prop="levelName" label="护理级别" width="120" />
              <el-table-column align="center" label="操作" width="180">
                <template #default="scope">
                  <el-button
                    type="primary"
                    link
                    size="small"
                    icon="Star"
                    v-if="scope.row.levelId==null"
                    @click="opensetting(scope.row)"
                  >设置护理级别</el-button>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    icon="Delete"
                    v-if="scope.row.levelId!=null"
                    @click="del(scope.row)"
                  >移除护理级别</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <!-- 分页插件 -->
          <div style="margin-top:15px">
            <!-- 
                page-size:每页显示条目个数
                current-page:页码 配合@current-change使用
                disabled:是否禁用分页 
                background:是否为分页按钮添加背景色
                layout:组件布局，子组件名用逗号分隔
                total:总条目数
  
            -->
            <el-pagination
              :page-size="page.pageSize"
              background
              :current-page="page.currentPag"
              layout=" prev, pager, next"
              :total="page.total"
              @current-change="handleCurrentChange"
            />
          </div>
          <!-- 护理级别设置抽屉 -->
          <el-drawer
            title="客户护理设置"
            :before-close="handleClose"
            v-model="drawer.openFlag"
            direction="rtl"
            size="70%"
          >
            <el-container>
              <el-header>
                <div>
                  <el-row>
                    <el-col :span="8">
                      <el-select
                        v-model="drawer.levelId"
                        @change="listNurseItemByLevel"
                        style="width:320px"
                        placeholder="选择护理级别"
                      >
                        <el-option
                          v-for="item in drawer.listLevel"
                          :key="item.id"
                          :label="item.levelName"
                          :value="item.id"
                        />
                      </el-select>
                    </el-col>
                  </el-row>
                </div>
              </el-header>
              <el-divider style="margin:0"></el-divider>
              <el-main>
                <!-- 表格 -->
                <el-table :data="drawer.isInLevelItemList" size="small">
                  <el-table-column align="center" type="index" label="序号" width="60" />
                  <el-table-column align="center" prop="serialNumber" label="编号" width="80" />
                  <el-table-column align="center" prop="nursingName" label="名称" width="100" />
                  <el-table-column align="center" prop="servicePrice" label="价格" width="80" />
                  <el-table-column align="center" prop="executionCycle" label="执行周次" width="80" />
                  <el-table-column align="center" prop="executionTimes" label="执行次数" width="80" />
                  <el-table-column align="center" prop="buyTime" label="服务购买日期" width="150"/>
                  <el-table-column align="center" prop="nurseNumber" label="购买数量" width="100">
                    <template #default="scope">
                      <el-input v-model="scope.row.nurseNumber" placeholder="购买数量"></el-input>
                    </template>
                  </el-table-column>
                  <el-table-column align="center" prop="maturityTime" label="服务到期日期" width="160">
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
                </el-table>
              </el-main>
              <div class="demo-drawer__footer">
                <el-button @click="handleClose">取 消</el-button>
                <el-button @click="isOk" type="primary">确认</el-button>
              </div>
            </el-container>
          </el-drawer>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { getKhxxList } from "@/api/customerApi.js";
  import { listNurseLevel, listNurseItemByLevel } from "@/api/nurseLevelApi.js";
  import { addItemToCustomer,removeCustomerLevelAndItem} from "@/api/customerNurseItem.js";
  
  export default {
    computed: {
      //分页序号不重修
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      }
    },
    data() {
      return {
        //抽屉数据
        drawer: {
          openFlag: false,
          listLevel: [], //级别列表
          levelId: "",
          customerId:"",
          isInLevelItemList: [] //级别的护理项目列表
        },
        // 修改后 ✅
        condition: {
          customerName: "",
          current: 1,      // 当前页
          pageSize: 6      // 每页条数
        },
        khxxList: [],
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        }
      };
    },
    methods: {
      // 修改后 ✅
      query() {
        this.condition.current = 1;
        this.condition.pageSize = 6;
        this.getKhxxList();
      },
      //选中页码
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.condition.current = curPage; //current
        //重新渲染表格
        this.getKhxxList();
      },
      //打开抽屉
      opensetting(row) {
        this.drawer.openFlag = true;
        //设置当前用户
        this.drawer.customerId=row.id
        //获取护理级别列表
        this.listNurseLevel();
      },
      //关闭抽屉
      handleClose() {
        this.drawer.openFlag = false;
        //初始化数据
        this.drawer.isInLevelItemList=[]
        this.drawer.levelId=""
        this.drawer.customerId=""
        this.drawer.listLevel=[]
      },
      //api-点击确认 批量生成客户护理项目信息
      isOk() {
        let customerItemList=this.drawer.isInLevelItemList
        let paramsArr=[];//提交的参数
        if(customerItemList.length==0){
            this.$message.error("请选择护理级别")
            return
        }
        //构建提交的参数列表
        for(let i=0;i<customerItemList.length;i++){
          let obj={} 
          obj.itemId=customerItemList[i].id
          obj.buyTime=customerItemList[i].buyTime
          // obj.custormerId=this.drawer.customerId
          obj.customerId=this.drawer.customerId
          obj.nurseNumber=customerItemList[i].nurseNumber
          obj.isDeleted=0
          obj.maturityTime=customerItemList[i].maturityTime
          obj.levelId=this.drawer.levelId
          paramsArr.push(obj)
          console.log(paramsArr);
        }
        this.$confirm("确定添加此护理级别及其护理项目数据无误?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "error",
          icon:'WarningFilled'
        })
          .then(() => {
            addItemToCustomer(paramsArr).then(res=>{
                if (res.flag) {
                  this.$message.success(res.message);
                  //重载表格:获取用户列表
                  this.getKhxxList();
                  //关闭抽屉
                  this.handleClose();
              } else {
                  this.$message.error(res.message);
              }
            })
           
          })
          .catch(() => {});
        // console.log(paramsArr);
  
      },
      //api-点击移除
      del(row){
        this.$confirm("此操作将会移除当前护理级别正在进行中的护理项目?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "error",
          icon:'WarnTriangleFilled'
        })
          .then(() => {
            removeCustomerLevelAndItem({levelId:row.levelId,customerId:row.id}).then(res=>{
                if (res.flag) {
                  this.$message.success(res.message);
                  //重载表格:获取用户列表
                  this.getKhxxList();
              } else {
                  this.$message.error(res.message);
              }
            })
           
          })
          .catch(() => {});
      },
      //api-查询客户信息列表-分页
      getKhxxList() {
        getKhxxList(this.condition).then(res => {
          this.khxxList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
      },
      //api-查询护理级别列表
      listNurseLevel() {
        listNurseLevel({ levelStatus: 1 }).then(res => {
          this.drawer.listLevel = res.data;
        });
      },
      //api-查询当前级别护理项目
      listNurseItemByLevel() {
        listNurseItemByLevel({ levelId: this.drawer.levelId }).then(res => {
          //this.drawer.isInLevelItemList = res.data;
          //构建表格数据
          let newArr = res.data;
          for (let i = 0; i < newArr.length; i++) {
            newArr[i].nurseNumber = 1;
            newArr[i].buyTime = new Date().getFullYear()+"-"+new Date().getMonth()+"-"+new Date().getDate();
            newArr[i].maturityTime = new Date().getFullYear()+"-"+(new Date().getMonth()+3)+"-"+new Date().getDate(); //默认一年到期
          }
          this.drawer.isInLevelItemList = newArr;
        });
      }
    },
    mounted() {
      this.getKhxxList();
    }
  };
  </script>
  
  <style scoped >
  </style>