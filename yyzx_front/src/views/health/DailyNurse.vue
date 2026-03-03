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
              <el-table-column align="center" prop="customerAge" label="年龄" width="120" />
              <el-table-column align="center" prop="customerSex" label="性别" width="120">
                <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="roomNo" label="房间号" width="120" />
              <el-table-column align="center" prop="bedNo" label="床号" width="120" />
              <el-table-column align="center" prop="buildingNo" label="所属楼房" width="120" />
              <el-table-column align="center" prop="contactTel" label="联系电话" width="150" />
              <el-table-column align="center" prop="levelName" label="护理级别" width="120" />
              <el-table-column align="center" label="操作" width="180">
                <template #default="scope">
                  <el-button
                    type="success"
                    color="#337ab7"
                    icon="Odometer"
                    size="small"
                    @click="opensetting(scope.row)"
                  >日常护理</el-button>
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
            title="客户护理项目"
            :before-close="handleClose"
            v-model="drawer.openFlag"
            direction="rtl"
            size="70%"
          >
            <!-- <el-container>
            <el-header></el-header>-->
            <el-divider style="margin:0"></el-divider>
            <el-main>
              <!-- 表格 -->
              <el-table :data="customerItemList" size="small">
                <el-table-column
                  align="center"
                  :index="indexMethodItem"
                  type="index"
                  label="序号"
                  width="60"
                />
                <el-table-column align="center" prop="customerName" label="客户" width="100" />
                <el-table-column align="center" prop="serialNumber" label="编号" width="100" />
                <el-table-column align="center" prop="nursingName" label="名称" width="100" />
                <el-table-column align="center" prop="servicePrice" label="价格" width="100" />
                <el-table-column align="center" prop="nurseNumber" label="余量" width="100" />
                <el-table-column align="center" prop="maturityTime" label="服务到期日期" width="120" />
                <el-table-column align="center" prop label="状态" width="180">
                  <template #default="scope">
                    <el-tag
                      v-if="0<scope.row.nurseNumber && scope.row.nurseNumber<10"
                      type="warning"
                      disable-transitions
                    >即将用完</el-tag>
                    <el-tag v-if="10<scope.row.nurseNumber" type="success" disable-transitions>数量正常</el-tag>
                    <el-tag v-if="scope.row.nurseNumber<0" type="danger" disable-transitions>已欠费</el-tag>
                    <el-tag
                      v-if="(new Date(scope.row.maturityTime)-new Date())/86400000>10"
                      type="success"
                      disable-transitions
                    >未到期</el-tag>
                    <el-tag
                      v-if="(new Date(scope.row.maturityTime)-new Date())/86400000>0 && (new Date(scope.row.maturityTime)-new Date())/86400000<10"
                      type="warning"
                      disable-transitions
                    >即将到期</el-tag>
                    <el-tag
                      v-if="(new Date(scope.row.maturityTime)-new Date())/86400000<0"
                      type="danger"
                      disable-transitions
                    >已到期</el-tag>
                  </template>
                </el-table-column>
                <el-table-column align="center" label="操作" width="140">
                  <template #default="scope">
                    <el-button type="primary" size="small" round plain @click="nurse(scope.row)">护理</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <!-- 分页插件 -->
              <div style="margin-top:15px" v-if="customerItemList.length>0">
                <el-pagination
                  :page-size="pageItem.pageSize"
                  background
                  :current-page="pageItem.currentPag"
                  layout=" prev, pager, next"
                  :total="pageItem.total"
                  @current-change="handleItemChange"
                />
              </div>
            </el-main>
            <!-- 内嵌抽屉 ：添加护理记录-->
            <div>
              <el-drawer
                v-model="innerDrawer.openFlag"
                title="添加护理记录"
                :append-to-body="true"
                :before-close="handleInnerClose"
                size="35%"
              >
                <el-divider border-style="double" style="margin:0;" />
                <el-form
                  label-position="right"
                  label-width="auto"
                  style="max-width:380px;margin:20px auto"
                  class="demo-form-inline"
                  ref="nurseForm"
                  :model="innerDrawer.nurseForm"
                  :rules="innerDrawer.rules"
                >
                  <el-form-item label="客户姓名：" prop="customerName">
                    <el-input readonly v-model="innerDrawer.nurseForm.customerName" />
                  </el-form-item>
                  <el-form-item label="护理项目编号：" prop="serialNumber">
                    <el-input readonly v-model="innerDrawer.nurseForm.serialNumber" />
                  </el-form-item>
                  <el-form-item label="护理项目：" prop="itemName">
                    <el-input readonly v-model="innerDrawer.nurseForm.itemName" />
                  </el-form-item>
                  <el-form-item label="护理时间：" prop="nursingTime">
                    <el-date-picker
                      style="width:300px"
                      format="YYYY/MM/DD HH:mm:ss"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      v-model="innerDrawer.nurseForm.nursingTime"
                      type="datetime"
                      placeholder="选择护理时间"
                    ></el-date-picker>
                  </el-form-item>
                  <el-form-item label="护理数量：" prop="nursingCount">
                    <el-input v-model="innerDrawer.nurseForm.nursingCount" />
                  </el-form-item>
                  <el-form-item label="护理内容：" prop="nursingContent">
                    <el-input type="textarea" v-model="innerDrawer.nurseForm.nursingContent"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="save('nurseForm')">立即创建</el-button>
                    <el-button @click="cancel">取消</el-button>
                  </el-form-item>
                </el-form>
                <el-divider border-style="double" style="margin:0;" />
              </el-drawer>
            </div>
          </el-drawer>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { getKhxxList } from "@/api/customerApi.js";
  import { listCustomerItem } from "@/api/customerNurseItem.js";
  import { getSessionStorage } from "@/utils/common.js";
  import { addNurseRecord } from "@/api/nurseRecord.js";
  
  export default {
    computed: {
      //分页序号不重修
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      },
      //分页序号不重修 -护理项目
      indexMethodItem() {
        return (
          this.pageItem.currentPag * this.pageItem.pageSize -
          this.pageItem.pageSize +
          1
        );
      }
    },
    data() {
      return {
        user: getSessionStorage("user"),
        //抽屉数据
        drawer: {
          openFlag: false
        },
        //添加护理记录抽屉数据
        innerDrawer: {
          openFlag: false,
          nurseForm: {
            customerId: "",
            itemId: "",
            nursingContent: "",
            nursingCount: 1,
            nursingTime: "",
            userId: getSessionStorage("user").id,
            customerName: "",
            itemName: "",
            serialNumber: ""
          },
          //校验规则
          rules: {
            nursingCount: [
              { required: true, message: "请输入护理数量", trigger: "blur" }
            ],
            nursingTime: [
              {
                type: "date",
                required: true,
                message: "请选择护理时间",
                trigger: "change"
              }
            ]
          }
        },
        //分页属性封装--客户护理项目
        pageItem: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        //查询条件-客户护理项目
        conditionItem: {
          customerId: "", //客户编号
          pageSize: "1" //默认第一页
        },
        //查询条件-客户列表
        condition: {
          userId: getSessionStorage("user").id,
          customerName: "",
          pageSize: "1" //默认第一页
        },
        customerItemList: [], //客户护理项目列表
        khxxList: [],
        //分页属性封装-客户列表
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        }
      };
    },
    methods: {
      //点击查询
      query() {
        this.condition.pageSize = "1"; //回到第一页
        this.getKhxxList();
      },
      //选中页码-客户信息列表
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.condition.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getKhxxList();
      },
      //选中页码-客户护理项目
      handleItemChange(curPage) {
        this.pageItem.currentPag = curPage;
        this.conditionItem.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格:
        this.listCustomerItem();
      },
      //打开抽屉
      opensetting(row) {
        this.drawer.openFlag = true;
        //设置当前用户
        this.conditionItem.customerId = row.id;
        //获取用户的服务项目列表
        this.listCustomerItem();
      },
      //关闭抽屉
      handleClose() {
        this.drawer.openFlag = false;
        //初始化数据
        this.conditionItem.customerId = "";
        this.conditionItem.pageSize = "1";
        this.customerItemList = [];
      },
      //关闭抽屉-内层
      handleInnerClose() {
        this.innerDrawer.openFlag = false;
        //重置参数
        this.resetParams();
      },
      //重置表单和参数
      resetParams() {
        this.innerDrawer.nurseForm.customerId = "";
        this.innerDrawer.nurseForm.itemId = "";
        this.innerDrawer.nurseForm.customerName = "";
        this.innerDrawer.nurseForm.itemName = "";
        this.innerDrawer.nurseForm.nursingContent = "";
        this.innerDrawer.nurseForm.nursingCount = 1;
        this.innerDrawer.nurseForm.nursingTime = "";
        this.innerDrawer.nurseForm.serialNumber = "";
      },
      //点击取消
      cancel() {
        this.handleInnerClose();
      },
      //点击护理 -打开内层抽屉
      nurse(row) {
        console.log('row 数据:', row);
        this.innerDrawer.openFlag = true;
        //构建参数数据
        this.$nextTick(() => {
          this.innerDrawer.nurseForm.customerId = row.customerId;
          this.innerDrawer.nurseForm.itemId = row.itemId;
          this.innerDrawer.nurseForm.customerName = row.customerName;
          this.innerDrawer.nurseForm.itemName = row.nursingName;
          this.innerDrawer.nurseForm.serialNumber = row.serialNumber;
        });
      },
      //api-点击立即创建,生成护理记录
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.$confirm("确认提交?", "提示", {
              confirmButtonText: "确定",
              cancelButtonText: "取消",
              type: "warning"
            })
              .then(() => {
                addNurseRecord(this.innerDrawer.nurseForm).then(res => {
                  if (res.flag) {
                  this.$message({
                    type: "success",
                    message: res.message
                  });
                  //重载客户护理项目列表
                  this.listCustomerItem();
                  //关闭抽屉
                  this.handleInnerClose();
                }else{
                  this.$message.error("添加记录失败")
                }
                });
                
              })
              .catch(() => {});
          } else {
            return false;
          }
        });
      },
      //api-获取用户的服务项目列表
      listCustomerItem() {
        listCustomerItem(this.conditionItem).then(res => {
          this.customerItemList = res.data.records;
          this.pageItem.total = res.data.total; //总记录数
          this.pageItem.pageSize = res.data.size; //每页显示条数
          this.pageItem.currentPag = res.data.current; //当前页码
          this.pageItem.pagCount = res.data.pages; //总页数
        });
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
      }
    },
    mounted() {
      this.getKhxxList();
    }
  };
  </script>
  
  <style scoped >
  </style>