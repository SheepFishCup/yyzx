<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="名称"
                  v-model="queryParams.username"
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
            <el-table :data="userList" style="width: 100% ;color:black;" stripe>
              <el-table-column
                align="center"
                type="index"
                :index="indexMethod"
                label="序号"
                width="60"
              />
              <el-table-column align="center" prop="id" label="编号" width="120" />
               <el-table-column align="center" prop="createTime" label="创建时间" width="120" />
              <el-table-column align="center" prop="username" label="用户名" width="120" />
              <el-table-column align="center" prop="nickname" label="真实姓名" width="120" />
              <el-table-column align="center" prop="sex" label="性别" width="120">
                <template #default="scope">{{ scope.row.sex==1?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="email" label="邮箱" width="180" />
              <el-table-column align="center" prop="phoneNumber" label="电话号码" width="120" />
              <el-table-column align="center" prop="roleId" label="权限" width="120">
                <template #default="scope">{{ scope.row.roleId==1?'管理员':'护工'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="status" label="状态" width="120">
                <template #default="scope">{{ scope.row.isDeleted==1?'停用':'启用'}}</template>
              </el-table-column>
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
       <!-- <el-form-item label="创建时间：" prop="createTime">
          <el-input v-model="dialog.item.createTime" />
        </el-form-item> -->
          <el-form-item label="用户名：" prop="username">
            <el-input v-model="dialog.item.username" />
          </el-form-item>
          <el-form-item label="真实姓名：" prop="nickname">
            <el-input v-model="dialog.item.nickname" />
          </el-form-item>
          <el-form-item label="性别：" prop="sex">
            <el-select v-model="dialog.item.sex" style="width:320px" placeholder="性别：">
              <el-option label="男" value="1" />
              <el-option label="女" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="邮箱：" prop="email">
            <el-input v-model="dialog.item.email" />
          </el-form-item>
          
          <el-form-item label="电话号码： " prop="phoneNumber">
            <el-input v-model="dialog.item.phoneNumber" />
          </el-form-item>
      
          <el-form-item label="状态：" prop="isDeleted">
            <el-select v-model="dialog.item.isDeleted" style="width:320px" placeholder="状态">
              <el-option
                v-for="item in dialog.statusArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
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
    addUser,
    updateUser,
    delUser,
    findAllUser
  } from "@/api/userApi.js";
  import {getCurDate} from "@/utils/common.js"
  import {getSessionStorage} from '@/utils/common.js'
  import {queryOutwardVo} from "@/api/outwardApi.js";
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
              username: "",
              nickname: "",
              sex: "",
              email: "",
              phoneNumber: "",
              isDeleted: "",//状态 1：启用；2：停用
              createTime:"",
              createBy:"",
              password:"",
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
        //校验规则
        rules: {
          username: [
            { required: true, message: "请输入用户名", trigger: "blur" }
          ], 
          nickname: [
            { required: true, message: "请输入真实姓名", trigger: "blur" }
          ],
          email: [
            { required: true, message: "请输入邮箱", trigger: "blur" }
          ],
          isDeleted: [{ required: true, message: "请选择状态", trigger: "change" }]
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
          status: "1", //查询默认状态1 -启用
          itemName: "",
          pageSize: "1" //默认第一页
        },
        userList: []
      };
    },
    mounted() {
      this.getUserList();
      
    },
    methods: {
      //点击查询
      query() {
        this.queryParams.pageSize = "1"; //回到第一页
        this.getUserList();
      },
      //点击启用
      enable() {
        this.btnFlag = true;
        this.queryParams.status = "1"; //1-启用
        this.queryParams.pageSize = "1"; //回到第一页
        this.getUserList();
      },
      //点击停用
      disable() {
        this.btnFlag = false;
        this.queryParams.pageSize = "1"; //2-停用
        this.queryParams.status = "2"; //0-生效床位信息
        this.getUserList();
      },
      //选中页码
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.queryParams.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getUserList();
      },
      //点击修改
      edit(row) {
        this.dialog.tops = "修改用户";
        this.dialog.dialogVisible = true;
        //初始化模态框数据
        this.$nextTick(() => {
          this.dialog.item.id = row.id;
          this.dialog.item.username = row.username;
          this.dialog.item.nickname = row.nickname;
          this.dialog.item.email = row.email;
          this.dialog.item.sex = row.sex;
          this.dialog.item.isDeleted = row.isDeleted;
          this.dialog.item.phoneNumber = row.phoneNumber;
          
        });
      },
      //点击添加按钮
      addItem() {
        this.dialog.tops = "添加护理项目";
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
              this.dialog.item.createTime = getCurDate();
              this.dialog.item.createBy = getSessionStorage("user").id;
              this.dialog.item.password = "000000";
              this.dialog.item.roleId = 2;
              addUser(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格(回到最初查询状态)
                  this.queryParams.status = "1";
                  this.queryParams.pageSize = "1"; //回到第一页
                  this.queryParams.itemName = "";
                  this.getUserList();
                  this.handleClose(); //关闭模态框
                } else {
                  this.$message.error(res.message);
                }
              });
            } else {
              updateUser(this.dialog.item).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格
                  this.getUserList();
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
            delUser({ id: id }).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                //重载表格
                this.getUserList();
              } else {
                this.$message.error(res.message);
              }
            });
          })
          .catch(() => {});
      },
      //api-查询护理项目(分页)
      getUserList() {
        findAllUser(this.queryParams).then(res => {
          this.userList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
        // queryOutwardVo().then(res=>{
        //     console.log(res);
        // });
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