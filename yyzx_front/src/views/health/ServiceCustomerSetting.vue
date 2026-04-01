<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="7">
                <el-input
                  placeholder="管家名称"
                  v-model="queryParams.nickname"
                  @clear="query"
                  clearable
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
            <el-table :data="userList" style="width: 100% ;color:black;" stripe>
              <el-table-column
                align="center"
                type="index"
                :index="indexMethod"
                label="序号"
                width="200"
              />
              <el-table-column align="center" prop="nickname" label="姓名" width="200" />
              <el-table-column align="center" prop="phoneNumber" label="电话" width="200" />
              <el-table-column align="center" prop="sex" label="性别" width="200">
                <template #default="scope">{{ scope.row.sex==0?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="email" label="邮箱" width="200" />
              <el-table-column align="center" fixed="right" label="操作" width="220">
                <template #default="scope">
                  <el-button
                    type="success"
                    color="#337ab7"
                    icon="Star"
                    size="small"
                    @click="userToCustomerService(scope.row)"
                  >设置服务对象</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
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
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  import { getUserList } from "@/api/userApi.js";
  export default {
    computed: {
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      }
    },
    data() {
      return {
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        queryParams: {
          roleId: "2",
          nickname: "",
          current: 1,
          pageSize: 6
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
      this.queryParams.current = 1;
      this.queryParams.pageSize = 6;
      this.getUserList();
    },
    handleCurrentChange(curPage) {
      this.page.currentPag = curPage;
      this.queryParams.current = curPage;
      this.getUserList();
    },
    userToCustomerService(row) {
      this.$router.push({
        path: "/health/userToCustomerService",
        query: { userId: row.id, nickName: row.nickname }
      });
    },
  
      // //api-删除
      // del(id) {
      //   this.$confirm("此操作删除记录, 是否继续?", "提示", {
      //     confirmButtonText: "确定",
      //     cancelButtonText: "取消",
      //     type: "warning"
      //   })
      //     .then(() => {
      //       delNurseItem({ id: id }).then(res => {
      //         if (res.flag) {
      //           this.$message.success(res.message);
      //           //重载表格
      //           this.getNurseItemList();
      //         } else {
      //           this.$message.error(res.message);
      //         }
      //       });
      //     })
      //     .catch(() => {});
      // },
      //api-查询护理项目(分页)
      getUserList() {
      console.log('发送的 queryParams:', this.queryParams);
      getUserList(this.queryParams).then(res => {
        this.userList = res.data.records;
        this.page.total = res.data.total;
        this.page.pageSize = res.data.size;
        this.page.currentPag = res.data.current;
        this.page.pagCount = res.data.pages;
      }).catch(err => {
        console.error('请求失败:', err);
      });
    }
    }
  };
  </script>
  
  <style scoped >
  </style>