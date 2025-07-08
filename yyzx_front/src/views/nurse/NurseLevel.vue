<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="17">
                <el-button type="primary" @click="addLevel" color="#337ab7" style="margin-top:4px">
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
            <el-row class="mb-4">
              <el-button
                :class="[{activeBtn: btnFlag}]"
                style="border: 1px white solid;margin-left: 0px"
                @click="enable"
              >启用</el-button>
              <el-button
                :class="[{activeBtn: !btnFlag}]"
                style="border: 1px white solid;margin-left: 0px"
                @click="disable"
              >停用</el-button>
            </el-row>
          </div>
          <div>
            <!-- 表格 -->
            <el-table :data="listLevel" style="width: 100% ;color:black;" stripe>
              <el-table-column align="center" type="index" label="序号" width="60" />
              <el-table-column align="center" prop="levelName" label="护理级别" width="200" />
              <el-table-column align="center" prop="levelStatus" label="状态" width="200">
                <template #default="scope">{{ scope.row.levelStatus==1?'启用':'停用'}}</template>
              </el-table-column>
              <el-table-column align="center" fixed="right" label="操作" width="400">
                <template #default="scope">
                  <el-button type="primary" icon="Edit" size="small" @click="edit(scope.row)">修改</el-button>
                  <!-- <el-button type="danger" icon="Delete" size="small" @click="del(scope.row.id)">删除</el-button> -->
                  <el-button type="success" icon="Star" v-if="scope.row.levelStatus==1" size="small" @click="itemToLevel(scope.row)">护理项目配置</el-button>
                </template>
              </el-table-column>
            </el-table>
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
          ref="levelForm"
          :model="dialog.level"
          :rules="dialog.rules"
        >
          <el-form-item label="护理级别：" prop="levelName">
            <el-input v-model="dialog.level.levelName" />
          </el-form-item>
  
          <el-form-item label="状态：" prop="levelStatus">
            <el-select v-model="dialog.level.levelStatus" style="width:320px" placeholder="状态">
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
            <el-button type="primary" @click="save('levelForm')">保存</el-button>
            <el-button @click="cancel">取消</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import {
    addNurseLevel,
    updateNurseLevel,
    removeNurseLevel,
    listNurseLevel
  } from "@/api/nurseLevelApi.js"
  
  import AddItemToLevel from '@/views/nurse/AddItemToLevel'

  export default {
    data() {
      return {
        btnFlag: true,
        queryParams: {
          levelStatus: "1" //查询默认状态1 -启用
        },
        dialog: {
          dialogVisible: false,
          tops: "",
          level: {
            id: "",
            levelName: "",
            levelStatus: ""
          },
          statusArr: [
            {
              value: 1,
              label: "启用"
            },
            {
              value: 2,
              label: "停用"
            }
          ],
          //校验规则
          rules: {
            levelName: [
              { required: true, message: "请输入级别", trigger: "blur" }
            ],
            levelStatus: [
              { required: true, message: "请选择状态", trigger: "change" }
            ]
          }
        },
        listLevel: []
      };
    },
    mounted() {
      this.listNurseLevel();
    },
    methods: {
      //点击启用
      enable() {
        this.btnFlag = true;
        this.queryParams.levelStatus = "1"; //1-启用
        this.listNurseLevel();
      },
      //点击停用
      disable() {
        this.btnFlag = false;
        this.queryParams.levelStatus = "2"; //2-停用
        this.listNurseLevel();
      },
      //关闭模态框
      handleClose() {
        this.dialog.dialogVisible = false;
        this.resetForm("levelForm"); //重置表单
      },
      //重置表单
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      cancel() {
        this.handleClose();
      },
      //点击添加
      addLevel() {
        this.dialog.tops = "添加护理级别";
        this.dialog.dialogVisible = true;
      },
      //点击编辑
      edit(row) {
        this.dialog.tops = "信息编辑";
        this.dialog.dialogVisible = true;
        //初始化模态框数据
        this.$nextTick(() => {
          this.dialog.level.id = row.id;
          this.dialog.level.levelName = row.levelName;
          this.dialog.level.levelStatus = row.levelStatus;
        });
      },
      //点击护理项目配置
      itemToLevel(row){
        //动态添加新路由：护理项目配置
        this.$router.addRoute('layout',{path:"/nurse/addItemToLevel",component:AddItemToLevel})
        //路由到护理项目配置
        this.$router.push({path:"/nurse/addItemToLevel",query:{levelId:row.id,levelName:row.levelName}})
      },
      //api-保存(新增/编辑)
      save(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            //通过id判断是添加还是编辑
            if (this.dialog.level.id == null || this.dialog.level.id == "") {
              addNurseLevel(this.dialog.level).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格(回到最初查询状态)
                  this.queryParams.levelStatus = "1";
                  this.listNurseLevel();
                  this.handleClose(); //关闭模态框
                } else {
                  this.$message.error(res.message);
                }
              });
            } else {
              updateNurseLevel(this.dialog.level).then(res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新数据表格
                  this.listNurseLevel();
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
      // del(id) {
      //   this.$confirm("此操作删除记录, 是否继续?", "提示", {
      //     confirmButtonText: "确定",
      //     cancelButtonText: "取消",
      //     type: "warning"
      //   })
      //     .then(() => {
      //       removeNurseLevel({ id: id }).then(res => {
      //         if (res.flag) {
      //           this.$message.success(res.message);
      //           //重载表格
      //           this.listNurseLevel();
      //         } else {
      //           this.$message.success(res.message);
      //         }
      //       });
      //     })
      //     .catch(() => {});
      // },
      //api-查询护理级别列表
      listNurseLevel() {
        listNurseLevel(this.queryParams).then(res => {
          this.listLevel = res.data;
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