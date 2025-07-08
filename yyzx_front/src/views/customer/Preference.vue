<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="客户名称"
                  v-model="queryParams.customerName"
                  @clear="query"
                  clearable
                  size="large"
                >
                  <template #append>
                    <el-button type="info" @click="query" style="color:black">查询</el-button>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="17">
                <el-button type="primary" @click="addItem" color="#337ab7" style="margin-top:4px">
                  <el-icon>
                    <Plus />
                  </el-icon>
                  <span>添加</span>
                </el-button>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <div>
            <!-- 表格 -->
            <el-table :data="preferenceList" style="width: 100% ;color:black;" stripe>
              <el-table-column
                align="center"
                type="index"
                :index="indexMethod"
                label="序号"
                width="60"
              />
              <el-table-column align="center" prop="customerName" label="客户名称" width="120" />
              <el-table-column align="center"  prop="customerSex" label="性别" width="60" >
              <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="customerAge" label="年龄" width="120" />
              <el-table-column align="center" prop="preferences" label="喜好" width="300" />
              <el-table-column align="center" prop="attention" label="注意事项" width="200" />
              <el-table-column align="center" prop="remark" label="备注" width="200" />
             
              <el-table-column align="center" fixed="right" label="操作" width="220">
                <template #default="scope">
                  <el-button type="primary" icon="Edit" link @click="edit(scope.row)">修改</el-button>
                  <el-button type="danger" icon="Delete" link @click="del(scope.row.id)">删除</el-button>
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
          ref="itemForm"
          :model="dialog.item"
          :rules="rules"
        >
          <el-form-item label="客户姓名：" prop="customerId">
              <el-select v-model="dialog.item.customerId" style="width:320px" placeholder="客户姓名：">
                <el-option v-for="item in customerList" :label="item.customerName" :value="item.id" />
              </el-select>
          </el-form-item>
          <el-form-item label="喜好：" prop="preferences">
            <el-input v-model="dialog.item.preferences" />
          </el-form-item>
          <el-form-item label="提示：" prop="attention">
            <el-input v-model="dialog.item.attention" />
          </el-form-item>
      
          <el-form-item label="备注：" prop="remark">
            <el-input v-model="dialog.item.remark" />
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
  import {
    addPreference,
    findPreference,
    updatePreference,
    delPreference
  } from "@/api/preference.js";
  import { getKhxxList } from "@/api/customerApi.js";
  export default {
    computed: {
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      }
    },
    data() {
      return {
        //模态框数据
        dialog: {
          dialogVisible: false, //模态框状态
          tops: "", //模态框标题,
          item: {
            id: "",
            customerId:"",
            customerName: "",
            customerSex: "",
            customerAge: "",
            preferences: "",
            attention: "",
            remark: "",
            isDeleted:0
          }
        },
       
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        btnFlag: true,
        queryParams: {
          customerName: "",
          pageSize: "1" //默认第一页
        },
        preferenceList: [],
        customerList:[],
         //查询条件封装--客户
        condition: {
          customerName: "",
          pageSize: "1" //默认第一页
        },
      };
    },
    mounted() {
        this.getKhxxList();
      this.getPreferenceList();
    },
    methods: {
      //点击查询
      query() {
        this.queryParams.pageSize = "1"; //回到第一页
        this.getPreferenceList();
        this.getKhxxList();
      },
      //点击修改
      edit(row) {
        this.dialog.tops = "修改护理项目";
        this.dialog.dialogVisible = true;
        //初始化模态框数据
        this.$nextTick(() => {
          this.dialog.item.id = row.id;
          this.dialog.item.customerName = row.customerName;
          this.dialog.item.customerSex = row.customerSex;
          this.dialog.item.customerAge = row.customerAge;
          this.dialog.item.preferences = row.preferences;
          this.dialog.item.attention = row.attention;
          this.dialog.item.remark = row.remark;
        });
      },
      //点击添加按钮
      addItem() {
        this.dialog.tops = "添加膳食管理";
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
      //api-保存(新增/编辑)
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            //通过id判断是添加还是编辑
            if (this.dialog.item.id == null || this.dialog.item.id == "") {
              addPreference(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格(回到最初查询状态)
                  this.queryParams.status = "1";
                  this.queryParams.pageSize = "1"; //回到第一页
                  this.queryParams.customerName = "";
                  this.getPreferenceList();
                  this.getKhxxList();
                  this.handleClose(); //关闭模态框
                } else {
                  this.$message.error(res.message);
                }
              });
            } else {
              updatePreference(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格
                  this.getPreferenceList();
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
      //api-删除
      del(id) {
        this.$confirm("此操作删除记录, 是否继续?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
          .then(() => {
            delPreference({ id: id }).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                //重载表格
                this.getPreferenceList();
              } else {
                this.$message.error(res.message);
              }
            });
          })
          .catch(() => {});
      },
      //api-查询护理项目(分页)
      getPreferenceList() {
        findPreference(this.queryParams).then(res => {
          this.preferenceList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
      },
      //api-查询客户信息列表-分页
      getKhxxList() {
        getKhxxList(this.condition).then(res => {
          this.customerList = res.data.records;
        });
      }
    }
  };
  </script>
  
  <style scoped >
  .activeBtn {
    color: #1890ff;
    background: rgb(232, 244, 255);
  }
  </style>