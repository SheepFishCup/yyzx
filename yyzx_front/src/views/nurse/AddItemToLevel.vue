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
                <el-button type="primary" @click="this.$router.go(-1)" round>返回</el-button>
              </el-col>
            </el-row>
          </div>
        </el-header>
        <el-divider style="margin:0"></el-divider>
        <el-main>
          <el-row :gutter="20">
            <el-col :span="11">
              <div class="table-main">
                <div class="table-main-header">护理项目</div>
                <!-- 表格 -->
                <el-table
                  :data="nurseItemList"
                  highlight-current-row
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
                  <el-table-column align="center" prop="serialNumber" label="编号" width="80" />
                  <el-table-column align="center" prop="nursingName" label="名称" width="100" />
                  <el-table-column align="center" prop="servicePrice" label="价格" width="80" />
                  <el-table-column align="center" prop="executionCycle" label="执行周次" width="80" />
                  <el-table-column align="center" prop="executionTimes" label="执行次数" width="80" />
                </el-table>
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
              </div>
            </el-col>
            <el-col :span="13">
              <div class="table-main">
                <div class="table-main-header">护理项目({{levelName}})</div>
                <!-- 表格 -->
                <el-table
                  :data="isInLevelItemList"
                  style="width: 100% ;color:black;"
                  size="small"
                  stripe
                >
                  <el-table-column align="center" type="index" label="序号" width="60" />
                  <el-table-column align="center" prop="serialNumber" label="编号" width="80" />
                  <el-table-column align="center" prop="nursingName" label="名称" width="100" />
                  <el-table-column align="center" prop="servicePrice" label="价格" width="80" />
                  <el-table-column align="center" prop="executionCycle" label="执行周次" width="80" />
                  <el-table-column align="center" prop="executionTimes" label="执行次数" width="80" />
                  <el-table-column align="center" fixed="right" label="操作" width="100">
                    <template #default="scope">
                      <el-button
                        type="danger"
                        icon="CloseBold"
                        size="small"
                        @click="del(scope.row.id)"
                      ></el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { findNurseItem } from "@/api/nurseItemApi.js";
  import {
    addItemToLevel,
    listNurseItemByLevel,
    removeNurseLevelItem
  } from "@/api/nurseLevelApi.js";
  export default {
    data() {
      return {
        levelId: this.$route.query.levelId,
        levelName: this.$route.query.levelName,
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
        isInLevelItemList: [], //在当前级别中的护理项目
        nurseItemList: [] //护理项目列表
      };
    },
    mounted() {
      this.getNurseItemList();
      this.listNurseItemByLevel();
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
      //api-鼠标点击某行触发：添加护理项目到当前护理级别中
      changeRow(row) {
        addItemToLevel({ levelId: this.levelId, itemId: row.id }).then(res => {
          if (res.flag) {
            //刷新表格
            this.listNurseItemByLevel();
          } else {
            this.$message.error(res.message);
          }
        });
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
      //api-查询当前级别护理项目
      listNurseItemByLevel() {
        listNurseItemByLevel({ levelId: this.levelId }).then(res => {
          this.isInLevelItemList = res.data;
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
            removeNurseLevelItem({ levelId: this.levelId, itemId: id }).then(
              res => {
                if (res.flag) {
                  this.$message.success(res.message);
                  //刷新表格
                  this.listNurseItemByLevel();
                } else {
                  this.$message.error(res.message);
                }
              }
            );
          })
          .catch(() => {});
      }
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