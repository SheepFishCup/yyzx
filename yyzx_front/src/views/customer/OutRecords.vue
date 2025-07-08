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
                  <span>添加外出申请</span>
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
                <div class="table-main-header">外出登记</div>
                <!-- 表格 -->
                <el-table :data="outwardList" size="small">
                  <el-table-column
                    align="center"
                    :index="indexMethodRecord"
                    type="index"
                    label="序号"
                    width="40"
                  />
                  <el-table-column align="center" prop="customerName" label="客户名称" width="100" />
                  <el-table-column align="center" prop="outgoingReason" label="外出事由" width="100" />
                  <el-table-column align="center" prop="outgoingTime" label="外出时间" width="70" />
                  <el-table-column align="center" prop="expectedReturnTime" label="预计回院时间" width="100" />
                  <el-table-column align="center" prop="actualReturnTime" label="实际回院时间" width="100" />
                  <el-table-column align="center" prop="escorted" label="陪同人" width="100" />
                  <el-table-column align="center" prop="relation" label="与老人关系" width="100" />
                  <el-table-column align="center" prop="escortedTel" label="陪同人电话" width="100" />
                  <!-- <el-table-column align="center" prop="status" label="审批状态" width="100" /> -->
                  <el-table-column align="center" prop="auditStatus" label="审批状态" width="100">
                      <template #default="{row}">
                          {{row.auditstatus===0?'已提交':row.auditStatus===1?'同意':'拒绝'}}
                      </template>
                  </el-table-column>
                  <el-table-column align="center" label="操作" width="100">
                    <template #default="scope">
                      <el-button v-if="scope.row.auditStatus === 1 && roleId === 2" :disabled="scope.row.actualReturnTime" type="success" size="small" round plain @click="updateTime(scope.row.id)">登记回院时间</el-button>
                      <el-button  v-if="scope.row.auditStatus === 0 && roleId === 2" type="danger" size="small" round plain @click="del(scope.row.id)">撤销申请</el-button>
                      <el-button  v-if="scope.row.auditStatus === 0 && roleId !== 2" type="primary" size="small" round plain @click="examine(scope.row.id)">审批</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <!-- 分页插件 -->
                <div style="margin-top:15px" v-if="outwardList.length>0">
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
        v-model="dialog.dialogTimeVisible"
        :title="dialog.tops"
        width="40%"
        align-center
        draggable
        :before-close="handleTimeClose"
      >
        <el-divider border-style="double" style="margin:0;" />
        <el-form
          label-position="right"
          label-width="auto"
          style="max-width:380px;margin:20px auto"
          class="demo-form-inline"
          ref="itemTimeForm"
          :model="dialog.item"
          :rules="rules"
        >
       
          <el-form-item label="实际回院时间： " prop="actualReturnTime">
              <el-date-picker
                      style="width:320px" 
                      v-model="dialog.item.actualReturnTime"
                      type="date"
                      placeholder="实际回院时间:"
                      value-format="YYYY-MM-DD"
                    />
          </el-form-item>
          
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="updateBackTime('itemTimeForm')">保存</el-button>
            <el-button @click="cancelTime">取消</el-button>
          </span>
        </template>
      </el-dialog>
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
            <el-button type="primary" @click="examineOutward('itemExamineForm')">保存</el-button>
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
       <!-- <el-form-item label="创建时间：" prop="createTime">
          <el-input v-model="dialog.item.createTime" />
        </el-form-item> -->
          <el-form-item label="客户姓名：" prop="customerId">
              <el-select v-model="dialog.item.customerId" style="width:320px" placeholder="客户姓名：">
                <el-option v-for="item in customerList" :label="item.customerName" :value="item.id" />
              </el-select>
          </el-form-item>
          <el-form-item label="外出事由：" prop="outgoingReason">
            <el-input v-model="dialog.item.outgoingReason" />
          </el-form-item>
         <!-- <el-form-item label="性别：" prop="sex">
            <el-select v-model="dialog.item.sex" style="width:320px" placeholder="性别：">
              <el-option label="男" value="1" />
              <el-option label="女" value="2" />
            </el-select>
          </el-form-item> -->
          <el-form-item label="外出时间：" prop="outgoingTime">
              <el-date-picker
                      style="width:320px" 
                      v-model="dialog.item.outgoingTime"
                      type="date"
                      placeholder="外出时间:"
                      value-format="YYYY-MM-DD"
                    />
          </el-form-item>
          
          <el-form-item label="预计回院时间： " prop="expectedReturnTime">
              <el-date-picker
                      style="width:320px" 
                      v-model="dialog.item.expectedReturnTime"
                      type="date"
                      placeholder="外出时间:"
                      value-format="YYYY-MM-DD"
                    />
          </el-form-item>
          
          <el-form-item label="陪同人： " prop="escorted">
            <el-input v-model="dialog.item.escorted" />
          </el-form-item>
          
          
          <el-form-item label="陪同人电话： " prop="escortedTel">
            <el-input v-model="dialog.item.escortedTel" />
          </el-form-item>
      
          <!-- <el-form-item label="状态：" prop="isDeleted">
            <el-select v-model="dialog.item.isDeleted" style="width:320px" placeholder="状态">
              <el-option
                v-for="item in dialog.statusArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item> -->
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
  import {queryOutwardVo,addOutward,examineOutward, delOutward,updateBackTime} from "@/api/outwardApi.js";
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
          nickname: "",
          sex: "",
          email: "",
          phoneNumber: "",
          isDeleted: "",//状态 1：启用；2：停用
          createTime:"",
          createBy:"",
          password:"",
          roleId:"",
          auditStatus:0,
          actualReturnTime:"",
          backTimeId:"",
          auditPerson:"",
          auditTime:""
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
          //分页属性封装 --外出记录
          pageRecord: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        outwardList:[],
        khxxList: [],
        customerList:[],
        //查询条件封装--客户
        condition: {
          customerName: "",
          userId:"",
          pageSize: "1" //默认第一页
        },
        //查询条件封装--外出记录
        conditionRecord: {
          userId:"",
          pageSize: "1" //默认第一页
        },
        
      };
    },
    methods: {
      //点击查询
      query() {
        //清空护理记录数据表
        this.outwardList=[]
        // this.conditionRecord.userId = "";
  
        this.condition.pageSize = "1"; //回到第一页
        this.getKhxxList();
        this.queryOutwardVo();
      },
      //选中页码-客户
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.condition.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getKhxxList();
        //清空护理记录数据表
        this.outwardList=[]
        this.conditionRecord.customerId = "";
      },
     
      //选中某客户行:获取用户的服务项目列表
      handleChangeCustomer(row) {
        if (row != null) {
          //点击页码会清空row因此做出判断逻辑
          //构建查询条件
          this.conditionRecord.userId = row.userId;
          //只有userid无法准确筛选，添加customerId准确筛选
          this.conditionRecord.customerId = row.id;
          this.queryOutwardVo();
        }
      },
      //选中页码-外出记录
      handleRecordChange(curPage) {
        this.pageRecord.currentPag = curPage;
        this.conditionRecord.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格:
        this.listCustomerItem();
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
      queryOutwardVo(){
        queryOutwardVo(this.conditionRecord).then(res=>{
          // 假设 conditionRecord.customerId 是当前选中客户的 ID
          const customerId = this.conditionRecord.customerId;
          this.outwardList = res.data.records;
          // 再次过滤，只保留属于当前客户的记录
          if (customerId) {
            this.outwardList = this.outwardList.filter(item => item.customerId === customerId);
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
      updateTime(id){
          this.dialog.tops = "修改实际回院时间";
          this.dialog.dialogTimeVisible = true;
          this.dialog.item.backTimeId = id;
      },
      handleTimeClose() {
        this.dialog.dialogTimeVisible = false;
        this.resetForm("itemTimeForm"); //重置表单
      },
      cancelTime() {
        this.handleTimeClose();
      },
      //点击添加按钮
      addItem() {
        this.dialog.tops = "添加外出申请";
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
      examineOutward(formName){
          this.$refs[formName].validate(valid => {
            if (valid) {
              this.dialog.item.auditPerson = getSessionStorage('user').id;
              this.dialog.item.auditTime = getCurDate();
              examineOutward(this.dialog.item).then(res=>{
                  if (res.flag) {
                    this.$message.success(res.message);
                    //刷新数据表格(回到最初查询状态)
                    this.queryOutwardVo();
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
      updateBackTime(formName){
          this.$refs[formName].validate(valid => {
            if (valid) {
              this.dialog.item.id = this.dialog.item.backTimeId;
              updateBackTime(this.dialog.item).then(res=>{
                  if (res.flag) {
                    this.$message.success(res.message);
                    //刷新数据表格(回到最初查询状态)
                    this.queryOutwardVo();
                    this.handleTimeClose(); //关闭模态框
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
              addOutward(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格(回到最初查询状态)
                  this.queryOutwardVo();
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
            delOutward({ id: id }).then(res => {
              if (res.flag) {
                this.$message({
                  type: "success",
                  message: res.message
                });
                //重载表格
                this.queryOutwardVo();
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
      this.queryOutwardVo();
      
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