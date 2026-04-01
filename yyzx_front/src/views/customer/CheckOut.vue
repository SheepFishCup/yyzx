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
              <el-col :span="17" v-if="roleId===2">
                <el-button type="primary" @click="addItem" color="#337ab7" style="margin-top:4px">
                  <el-icon>
                    <Plus />
                  </el-icon>
                  <span>添加退住申请</span>
                </el-button>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <el-row :gutter="20">
            <el-col :span="9">
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
                  <el-table-column align="center" prop="customerName" label="姓名"/>
                  <el-table-column align="center" prop="customerAge" label="年龄"/>
                  <el-table-column align="center" prop="customerSex" label="性别">
                    <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
                  </el-table-column>
                  <el-table-column align="center" prop="bedNo" label="床号"/>
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
            <el-col :span="15">
              <div class="table-main">
                <div class="table-main-header">退住登记</div>
                <!-- 表格 -->
                <el-table :data="backdownList" size="small">
                  <el-table-column
                    align="center"
                    :index="indexMethodRecord"
                    type="index"
                    label="序号"
                    width="40"
                  />
                  <el-table-column align="center" prop="customerName" label="客户名称" width="100" />
                  <el-table-column align="center" prop="checkinDate" label="入住时间" width="100" />
                  <el-table-column align="center" prop="retreatTime" label="退住时间" width="70" />
                  <el-table-column align="center" prop="retreatType" label="退住类型" width="100" />
                  <el-table-column align="center" prop="retreatReason" label="退住原因" width="100" />
                  <el-table-column align="center" prop="auditTime" label="审批时间" width="100" />
                  <!-- <el-table-column align="center" prop="status" label="审批状态" width="100" /> -->
                  <el-table-column align="center" prop="auditStatus" label="审批状态" width="100">
                      <template #default="{row}">
                          {{row.auditStatus===0?'已提交':row.auditStatus===1?'同意':'拒绝'}}
                      </template>
                  </el-table-column>
                  <el-table-column align="center" prop="bedId" label="床位" width="100" />
                  <el-table-column align="center" label="操作" width="100">
                    <template #default="scope">
                      <!-- <el-button v-if="scope.row.auditstatus === 1 && dialog.item.roleId === 2" :disabled="scope.row.actualreturntime" type="success" size="small" round plain @click="updateTime(scope.row.id)">登记回院时间</el-button> -->
                      <el-button  v-if="scope.row.auditStatus === 0 && roleId === 2" type="danger" size="small" round plain @click="del(scope.row.id)">撤销申请</el-button>
                      <el-button  v-if="scope.row.auditStatus === 0 && roleId !== 2" type="primary" size="small" round plain @click="examine(scope.row.id)">审批</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <!-- 分页插件 -->
                <div style="margin-top:15px" v-if="backdownList.length>0">
                  <el-pagination
                    :page-size="pageRecord.pageSize"
                    background
                    :current-page="pageRecord.currentPag"
                    layout=" prev, pager, next"
                    :total="pageRecord.total"
                    @current-change="handleRecordChange"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
      
      <!-- 修改/添加对话框 -->
      <el-dialog
        v-model="dialog.dialogExamineVisible"
        :title="dialog.tops"
        width="40%"
        align-center
        draggable
        :before-close="handleExamineClose"
      >
        <el-divider border-style="double" style="margin:0;" />
        <el-form
          label-position="right"
          label-width="auto"
          style="max-width:380px;margin:20px auto"
          class="demo-form-inline"
          ref="itemExamineForm"
          :model="dialog.item"
          :rules="rules"
        >
       
          <el-form-item label="审批：" prop="auditStatus">
              <el-radio-group v-model="dialog.item.auditStatus" class="ml-4">
                    <el-radio label="1">同意</el-radio>
                    <el-radio label="2">拒绝</el-radio>
              </el-radio-group>
          </el-form-item>
          
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="examineBackdown('itemExamineForm')">保存</el-button>
            <el-button @click="cancelExamine">取消</el-button>
          </span>
        </template>
      </el-dialog>
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
          ref="itemForm"
          :model="dialog.item"
          :rules="rules"
        >
          <el-form-item label="客户姓名：" prop="customerId">
              <el-select v-model="dialog.item.customerId" style="width:320px" placeholder="客户姓名：">
                <el-option v-for="item in customerList" :label="item.customerName" :value="item.id" />
              </el-select>
          </el-form-item>
          <el-form-item label="退住时间：" prop="retreatTime">
              <el-date-picker
                      style="width:320px" 
                      v-model="dialog.item.retreatTime"
                      type="date"
                      placeholder="退住时间:"
                      value-format="YYYY-MM-DD"
                    />
          </el-form-item>
         <el-form-item label="退住类型：" prop="retreatType">
            <el-select v-model="dialog.item.retreatType" style="width:320px" placeholder="退住类型：">
              <el-option label="正常退住" value="0" />
              <el-option label="死亡退住" value="1" />
              <el-option label="保留床位" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="退住原因： " prop="retreatReason">
            <el-input v-model="dialog.item.retreatReason" />
          </el-form-item>
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="save('itemForm')">保存</el-button>
            <el-button @click="cancel">取消</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import { getKhxxList } from "@/api/customerApi.js";
  import {addBackdown,examineBackdown,delBackdown,queryBackdownVo} from "@/api/backdownApi.js";
  import {getSessionStorage,getCurDate} from '@/utils/common.js'
  
  export default {
      
    computed: {
      //分页序号不重修 -客户
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      },
        //分页序号不重修 -外出记录
        indexMethodRecord () {
        return (
          this.pageRecord.currentPag * this.pageRecord.pageSize -
          this.pageRecord.pageSize +1
        );
      },
    },
    data() {
      return {
      //模态框数据
      dialog: {
        dialogVisible: false, //模态框状态
        dialogTimeVisible: false, //模态框状态
        dialogExamineVisible:false,
        tops: "", //模态框标题,
        item: {
          id: "",
          username: "",
          customerName: "",
          checkinDate: "",
          retreatTime: "",
          retreatType: "",
          retreatReason: "",
          auditTime:"",
          auditStatus:0,
          bedId:"",
          roleId:""
        },
        statusArr: [
          {
            value: 1,
            label: "停用"
          },
          {
            value: 2,
            label: "启用"
          }
        ]
      },
        roleId:'',
        //分页属性封装 --客户
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
          //分页属性封装 --退住记录
          pageRecord: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        backdownList:[],
        khxxList: [],
        customerList:[],
        // 查询条件封装--客户
        condition: {
          customerName: "",
          userId: "",
          current: 1,      // 改为 current，表示当前页
          pageSize: 6      // 新增 pageSize，表示每页条数
        },
        // 查询条件封装--退住记录
        conditionRecord: {
          userId: "",
          current: 1,      // 改为 current，表示当前页
          pageSize: 6      // 新增 pageSize，表示每页条数
        },
        
      };
    },
    methods: {
      //点击查询
      query() {
        this.backdownList = []
        this.condition.current = 1;  // 改为 current
        this.getKhxxList();
        this.queryBackdownVo();
      },
      //选中页码-客户
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.condition.current = curPage;  // 改为 current
        this.getKhxxList();
        this.backdownList = []
      },
     
      //选中某客户行:获取用户的服务项目列表
      handleChangeCustomer(row) {
        if (row != null) {
          // 重置分页页码
          this.pageRecord.currentPag = 1;
          this.conditionRecord.current = 1;
          // 构建查询条件
          this.conditionRecord.customerId = row.id;
          this.conditionRecord.userId = row.userId;
          this.queryBackdownVo();
        }
      },
      //选中页码-护理记录
      handleRecordChange(curPage) {
        this.pageRecord.currentPag = curPage;
        this.conditionRecord.current = curPage;  // 改为 current
        this.queryBackdownVo();  // 注意：原代码调用的是 listCustomerItem()，但该方法不存在
      },
      
      //api-查询客户信息列表-分页
      getKhxxList() {
        getKhxxList(this.condition).then(res => {
          this.khxxList = res.data.records;
          this.customerList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
      },
      //api-查询外出申请记录
      queryBackdownVo(){
        queryBackdownVo(this.conditionRecord).then(res=>{
          // 假设 conditionRecord.customerId 是当前选中客户的 ID
          const customerId = this.conditionRecord.customerId;
          // 先赋值所有数据
          this.backdownList = res.data.records;
          // 再次过滤，只保留属于当前客户的记录
          if (customerId) {
            this.backdownList = this.backdownList.filter(item => item.customerId === customerId);
          }
          this.pageRecord.total = res.data.total; //总记录数
          this.pageRecord.pageSize = res.data.size; //每页显示条数
          this.pageRecord.currentPag = res.data.current; //当前页码
          this.pageRecord.pagCount = res.data.pages; //总页数
          
        })
      },
      examine(id){
          this.dialog.tops = "审批";
          this.dialog.dialogExamineVisible = true;
          this.dialog.item.id = id;
      },
      handleExamineClose() {
        this.dialog.dialogExamineVisible = false;
        this.resetForm("itemExamineForm"); //重置表单
      },
      cancelExamine() {
        this.handleExamineClose();
      },
      
      //点击添加按钮
      addItem() {
        this.dialog.tops = "添加退住申请";
        this.dialog.dialogVisible = true;
      },
      handleClose() {
        this.dialog.dialogVisible = false;
        this.resetForm("itemForm"); //重置表单
      },
      cancel() {
        this.handleClose();
      },
      //重置表单
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      examineBackdown(formName){
          this.$refs[formName].validate(valid => {
            if (valid) {
              this.dialog.item.auditTime = getCurDate();
              this.dialog.item.auditPerson = getSessionStorage('user').id;
              examineBackdown(this.dialog.item).then(res=>{
                  if (res.flag) {
                    this.$message.success(res.message);
                    //刷新数据表格(回到最初查询状态)
                    this.queryBackdownVo();
                    this.handleExamineClose(); //关闭模态框
                  } else {
                    this.$message.error(res.message);
                  }
              });
            } else {
              return false;
            }
            
          });
      },
      
      //api-保存(新增/编辑)
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            //通过id判断是添加还是编辑
            if (this.dialog.item.id == null || this.dialog.item.id == "") {
              this.dialog.item.createTime = getCurDate();
              this.dialog.item.createBy = getSessionStorage("user").id;
              this.dialog.item.password = "000000";
              this.dialog.item.roleId = 2;
              this.dialog.item.isDeleted = 0;
              addBackdown(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格(回到最初查询状态)
                  this.queryBackdownVo();
                  this.handleClose(); //关闭模态框
                } else {
                  this.$message.error(res.message);
                }
              });
            } 
          } else {
            return false;
          }
        });
      },
      del(id) {
        this.$confirm("确定删除?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
          .then(() => {
            delBackdown({ id: id }).then(res => {
              if (res.flag) {
                this.$message({
                  type: "success",
                  message: res.message
                });
                //重载表格
                this.queryBackdownVo();
              }
            });
          })
          .catch(() => {});
      },
    },
    mounted() {
      debugger
      this.roleId = getSessionStorage('user').roleId;
      this.conditionRecord.userId = this.roleId === 1?'':getSessionStorage('user').id;
      this.condition.userId = this.roleId === 1?'':getSessionStorage('user').id;
      this.getKhxxList();
      this.queryBackdownVo();
      
    }
  };
  </script>
  
  <style scoped>
  .table-main {
    height: 600px;
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