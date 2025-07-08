<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="客户姓名"
                  @clear="query"
                  v-model="condition.customerName"
                  clearable
                  size="large"
                >
                  <template #append>
                    <el-button type="info" @click="query" style="color:black">查询</el-button>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="17">
                <el-button type="primary" @click="buyService" color="#337ab7" style="margin-top:4px">
                  <el-icon size="20">
                    <ShoppingCart />
                  </el-icon>
                  <span>购买护理服务</span>
                </el-button>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <el-row :gutter="20">
            <el-col :span="10">
              <div class="table-main">
                <div class="table-main-header">客户信息</div>
                <!-- 表格 -->
                <el-table
                  :data="khxxList"
                  highlight-current-row
                  @current-change="handleChangeCustomer"
                  size="small"
                  style="width: 100% ;color:black;"
                  stripe
                >
                  <el-table-column
                    align="center"
                    fixed
                    type="index"
                    :index="indexMethod"
                    label="序号"
                    width="40"
                  />
                  <el-table-column align="center" prop="customerName" label="姓名" />
                  <el-table-column align="center" prop="customerAge" label="年龄" />
                  <el-table-column align="center" prop="customerSex" label="性别" >
                    <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
                  </el-table-column>
                  <el-table-column align="center" prop="bedNo" label="床号" />
                  <el-table-column align="center" prop="levelName" label="护理级别" />
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
            </el-col>
            <el-col :span="14">
              <div class="table-main">
                <div class="table-main-header">已购护理服务</div>
                <!-- 表格 -->
                <el-table :data="customerItemList" size="small">
                  <el-table-column
                    align="center"
                    :index="indexMethodItem"
                    type="index"
                    label="序号"
                    width="40"
                  />
                  <el-table-column align="center" prop="customerName" label="客户" />
                  <el-table-column align="center" prop="serialNumber" label="编号" />
                  <el-table-column align="center" prop="nursingName" label="名称" />
                  <el-table-column align="center" prop="servicePrice" label="价格"  />
                  <el-table-column align="center" prop="nurseNumber" label="余量"/>
                  <el-table-column align="center" prop="maturityTime" label="服务到期日期" />
                  <el-table-column align="center" prop label="状态">
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
                      <el-button type="primary" size="small" round plain @click="enew(scope.row)">续费</el-button>
                      <el-button type="danger" size="small" round plain @click="del(scope.row.id)">移除</el-button>
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
              </div>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
      <!-- 修改/添加对话框 -->
      <el-dialog
        v-model="dialog.dialogVisible"
        :title="dialog.tops"
        width="40%"
        align-center
        draggable
        :before-close="handleClose"
      >
        <el-divider border-style="double" style="margin:0;" />
        <el-form
          label-position="right"
          label-width="auto"
          style="max-width:380px;margin:20px auto"
          class="demo-form-inline"
          ref="customerItemForm"
          :model="dialog.customerNurseItem"
          :rules="dialog.rules"
        >
          <el-form-item label="客户姓名：" prop="customerName">
            <el-input readonly v-model="dialog.customerNurseItem.customerName" />
          </el-form-item>
          <el-form-item label="护理项目编号：" prop="serialNumber">
            <el-input readonly v-model="dialog.customerNurseItem.serialNumber" />
          </el-form-item>
          <el-form-item label="护理项目名称" prop="nursingName">
            <el-input readonly v-model="dialog.customerNurseItem.nursingName" />
          </el-form-item>
          <el-form-item label="价格：" prop="servicePrice">
            <el-input readonly v-model="dialog.customerNurseItem.servicePrice" />
          </el-form-item>
          <el-form-item label="原有数量：" prop="oldNumber">
            <el-input readonly v-model="dialog.customerNurseItem.oldNumber" />
          </el-form-item>
          <el-form-item label="新增数量：" prop="addNumber">
            <el-input v-model="dialog.customerNurseItem.addNumber" />
          </el-form-item>
          <el-form-item label="总数量：" prop="nurseNumber">
            <el-input readonly v-model="nurseNumber" />
          </el-form-item>
          <el-form-item label="服务到期日期：" prop="maturityTime">
            <el-date-picker
              style="width:300px"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              v-model="dialog.customerNurseItem.maturityTime"
              type="date"
              placeholder="选择日期"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="save('customerItemForm')">保存</el-button>
            <el-button @click="cancel">取消</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { getKhxxList } from "@/api/customerApi.js";
  import {
    listCustomerItem,
    enewNurseItem,
    removeCustomerItem
  } from "@/api/customerNurseItem.js";
  export default {
    computed: {
      //分页序号不重修 -客户
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
      },
      //计算数量
      nurseNumber() {
        if (
          this.dialog.customerNurseItem.addNumber == null ||
          this.dialog.customerNurseItem.addNumber == "" ||
          this.dialog.customerNurseItem.addNumber == "undifine"
        ) {
          return this.dialog.customerNurseItem.oldNumber;
        }
        return (
          parseInt(this.dialog.customerNurseItem.oldNumber) +
          parseInt(this.dialog.customerNurseItem.addNumber)
        );
      }
    },
    data() {
      return {
        //分页属性封装--护理项目
        pageItem: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        //查询条件封装--护理项目
        conditionItem: {
          customerId: "", //客户编号
          pageSize: "1" //默认第一页
        },
        //分页属性封装 --客户
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        customerItemList: [],
        khxxList: [],
        //查询条件封装--客户
        condition: {
          customerName: "",
          pageSize: "1" //默认第一页
        },
        //续费模态框数据
        dialog: {
          dialogVisible: false,
          tops: "护理项目续费",
          //校验规则
          rules: {
            maturityTime: [
              { required: true, message: "请选择日期", trigger: "change" }
            ]
          },
          customerNurseItem: {
            id: "",
            customerName: "",
            maturityTime: "",
            oldNumber: 0,
            newNumber: 0,
            nursingName: "",
            serialNumber: "",
            servicePrice: "",
            addNumber: ""
          }
        }
      };
    },
    methods: {
      //点击查询
      query() {
        //清空护理项目数据表
        this.conditionItem.customerId = "";
        this.customerItemList = [];
  
        this.condition.pageSize = "1"; //回到第一页
        this.getKhxxList();
      },
      //选中页码-客户
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.condition.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getKhxxList();
        //清空护理项目数据表
        this.conditionItem.customerId = "";
        this.customerItemList = [];
      },
      //选中页码-护理项目
      handleItemChange(curPage) {
        this.pageItem.currentPag = curPage;
        this.conditionItem.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格:
        this.listCustomerItem();
      },
      //选中某客户行:获取用户的服务项目列表
      handleChangeCustomer(row) {
        if (row != null) {
          //点击页码会清空row因此做出判断逻辑
          //构建查询条件
          this.conditionItem.customerId = row.id;
          this.listCustomerItem();
        }
      },
      //关闭模态框
      handleClose() {
        this.dialog.dialogVisible = false;
        this.resetForm("customerItemForm"); //重置表单
      },
      //重置表单
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      cancel() {
        this.handleClose();
      },
      //点击续费
      enew(row) {
        this.dialog.dialogVisible = true;
        //初始化模态框数据
        this.$nextTick(() => {
          this.dialog.customerNurseItem.id = row.id;
          this.dialog.customerNurseItem.customerName = row.customerName;
          this.dialog.customerNurseItem.serialNumber = row.serialNumber;
          this.dialog.customerNurseItem.oldNumber = row.nurseNumber;
          this.dialog.customerNurseItem.nursingName = row.nursingName;
          this.dialog.customerNurseItem.servicePrice = row.servicePrice;
          this.dialog.customerNurseItem.maturityTime = row.maturityTime;
        });
      },
      //点击购买护理服务
      buyService() {
        //判断是否选中客户
        if (
          this.conditionItem.customerId != null &&
          this.conditionItem.customerId != ""
        ) {
          this.$router.push({
            path: "/health/serviceBuy",
            query: { customerId: this.conditionItem.customerId }
          });
        } else {
          this.$message.warning("请选中表格中的客户");
        }
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
      //api-选中某客户行:获取用户的服务项目列表
      listCustomerItem() {
        listCustomerItem(this.conditionItem).then(res => {
          this.customerItemList = res.data.records;
          this.pageItem.total = res.data.total; //总记录数
          this.pageItem.pageSize = res.data.size; //每页显示条数
          this.pageItem.currentPag = res.data.current; //当前页码
          this.pageItem.pagCount = res.data.pages; //总页数
        });
      },
      //api-移除客户护理项目
      del(id) {
        this.$confirm("此操作将移除当前护理项目, 请谨慎操作?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
          .then(() => {
            removeCustomerItem({ id: id }).then(res => {
              if (res.flag) {
                this.$message({
                  type: "success",
                  message: res.message
                });
                //重载表格
                this.listCustomerItem();
              }
            });
          })
          .catch(() => {});
      },
      //api-保存
      save(formName) {
        this.$refs[formName].validate(valid => {
          //构建请求参数
          let params = {
            id: this.dialog.customerNurseItem.id,
            maturityTime: this.dialog.customerNurseItem.maturityTime,
            nurseNumber: this.nurseNumber
          };
  
          if (valid) {
            enewNurseItem(params).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                //刷新数据表格
                this.listCustomerItem();
                this.handleClose(); //关闭模态框
              } else {
                this.$message.error(res.message);
              }
            });
          } else {
            return false;
          }
        });
      }
    },
    mounted() {
      this.getKhxxList();
    }
  };
  </script>
  <style scoped>
  .table-main {
    height: 500px;
    border: 1px solid #d4c5c5;
  }
  .table-main .table-main-header {
    height: 50px;
    background-color: #3ca2e0;
    color: #fff;
    font-size: 20px;
    font-family: "Microsoft YaHei";
    line-height: 50px;
  }
  </style>