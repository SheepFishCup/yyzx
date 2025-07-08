<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="客户姓名"
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
                <div class="table-main-header">无管家客户信息</div>
                <!-- 表格 -->
                <el-table
                  :data="notManagerKhxxList"
                  @row-click="changeRow"
                  style="width: 100% ;color:black;"
                  stripe
                >
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
                  <el-table-column align="center" prop="roomNo" label="房间号" width="80" />
                  <el-table-column align="center" prop="bedNo" label="床号" width="80" />
                  <el-table-column align="center" prop="levelName" label="护理级别" width="80" />
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
            <el-col :span="13">
              <div class="table-main">
                <div class="table-main-header">{{nickName}}--服务的客户</div>
                <!-- 表格 -->
                <el-table
                  :data="managerKhxxList"
                  style="width: 100% ;color:black;"
                  size="small"
                  stripe
                >
                <el-table-column
                    align="center"
                    fixed
                    type="index"
                    :index="managerKhxxIndexMethod"
                    label="序号"
                    width="60"
                  />
                  <el-table-column align="center" fixed prop="customerName" label="客户姓名" width="120" />
                  <el-table-column align="center" prop="customerAge" label="年龄" width="80" />
                  <el-table-column align="center" prop="customerSex" label="性别" width="80">
                    <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
                  </el-table-column>
                  <el-table-column align="center" prop="roomNo" label="房间号" width="80" />
                  <el-table-column align="center" prop="bedNo" label="床号" width="80" />
                  <el-table-column align="center" prop="levelName" label="护理级别" width="80" />
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
                <div style="margin-top:15px">
                  <el-pagination
                    :page-size="manager.page.pageSize"
                    background
                    :current-page="manager.page.currentPag"
                    layout=" prev, pager, next"
                    :total="manager.page.total"
                    @current-change="handleCurrentChangeManager"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { getKhxxList, editCustomer } from "@/api/customerApi.js";
  export default {
    data() {
      return {
        nickName: this.$route.query.nickName,
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        queryParams: {
          customerName: "",
          pageSize: "1", //默认第一页
          manType: "3" //无管家客户
        },
        //当前管家客户信息模型数据
        manager: {
          queryParams: {
            pageSize: "1", //默认第一页
            userId: this.$route.query.userId
          },
          page: {
            total: 0,
            pageSize: 6,
            currentPag: 1,
            pagCount: 0
          }
        },
        notManagerKhxxList: [], //无管家客户信息列表
        managerKhxxList: [] //当前管家的客户信息列表
      };
    },
    mounted() {
      this.getNotManagerKhxxList();
      this.getManagerKhxxList();
    },
    computed: {
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      },
      managerKhxxIndexMethod(){
        return this.manager.page.currentPag * this.manager.page.pageSize - this.manager.page.pageSize + 1;
      }
    },
    methods: {
      //点击查询
      query() {
        this.queryParams.pageSize = "1"; //回到第一页
        this.getNotManagerKhxxList();
      },
      //选中页码-无管家
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.queryParams.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getNotManagerKhxxList();
      },
      //选中页码-当前管家
      handleCurrentChangeManager(curPage) {
        this.manager.page.currentPag = curPage;
        this.manager.queryParams.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getManagerKhxxList();
      },
      //api-鼠标点击某行触发：设置未当前管家的服务对象
      changeRow(row) {
        editCustomer({
          id: row.id,
          userId: this.manager.queryParams.userId
        }).then(res => {
          if (res.flag) {
            //刷新表格
            this.getManagerKhxxList();
            this.getNotManagerKhxxList();
          } else {
            this.$message.error(res.message);
          }
        });
      },
  
      //api-查询无管家客户信息列表
      getNotManagerKhxxList() {
        getKhxxList(this.queryParams).then(res => {
          this.notManagerKhxxList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
        });
      },
  
      //api-查询当前管家护理客户
      getManagerKhxxList() {
        getKhxxList(this.manager.queryParams).then(res => {
          this.managerKhxxList = res.data.records;
          this.manager.page.total = res.data.total; //总记录数
          this.manager.page.pageSize = res.data.size; //每页显示条数
          this.manager.page.currentPag = res.data.current; //当前页码
          this.manager.page.pagCount = res.data.pages; //总页数
        });
      },
  
      //api-移除当前管家服务的客户
      del(id) {
        this.$confirm("确定移除?", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
          .then(() => {
            editCustomer({ id: id, userId: -1 }).then(res => {
              if (res.flag) {
                //刷新表格
                this.getManagerKhxxList();
                this.getNotManagerKhxxList();
              } else {
                this.$message.error(res.message);
              }
            });
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