<template>
    <div class="common-layout">
      <el-container>
        <el-header>
          <div>
            <el-row :gutter="30">
              <el-col :span="5">
                <el-form-item label="姓名：">
                  <el-input clearable v-model="queryParams.customerName" @clear="query" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="入住日期：">
                  <el-date-picker
                    value-format="YYYY-MM-DD"
                    format="YYYY/MM/DD"
                    v-model="dateChange"
                    type="daterange"
                    unlink-panels
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-button type="primary" @click="query" color="#337ab7" icon="Search">查询</el-button>
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
                @click="doing"
              >正在使用</el-button>
              <el-button
                :class="[{activeBtn: !btnFlag}]"
                style="border: 1px white solid;margin-left: 0px"
                @click="history"
              >使用历史</el-button>
            </el-row>
          </div>
          <div>
            <!-- 表格 -->
            <el-table :data="bedDetailsList" style="width: 100% ;color:black;" stripe>
              <el-table-column
                align="center"
                type="index"
                :index="indexMethod"
                label="序号"
                width="60"
              />
              <el-table-column align="center" prop="customerName" label="客户姓名" width="120" />
              <el-table-column align="center" prop="customerSex" label="性别" width="100">
                <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
              </el-table-column>
              <el-table-column align="center" prop="bedDetails" label="床位详情" width="120" />
              <!-- <el-table-column align="center" prop="roomNo" label="房间号" width="120" /> -->
              <el-table-column align="center" prop="startDate" label="床位使用起始日期" width="150" />
              <el-table-column align="center" prop="endDate" label="床位使用结束如期" width="150" />
              <el-table-column align="center" fixed="right" label="操作" width="220">
                <template #default="scope">
                  <el-button
                    type="warning"
                    link
                    icon="Switch"
                    v-if="btnFlag"
                    size="small"
                    @click="exchange(scope.row)"
                  >床位调换</el-button>
                  <el-button
                    type="primary"
                    icon="Edit"
                    link
                    size="small"
                    v-if="btnFlag"
                    @click="editMessage(scope.row)"
                  >修改</el-button>
                  <el-button
                    type="danger"
                    icon="Delete"
                    size="small"
                    v-if="!btnFlag"
                    @click="del(scope.row.id)"
                  >删除</el-button>
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
      <!-- 修改对话框 -->
      <el-dialog v-model="edit.dialogVisible" :before-close="editClose" title="信息修改" width="60%" align-center draggable>
        <el-divider border-style="double" style="margin:0;" />
        <el-form
          label-position="right"
          label-width="auto"
          style="max-width:800px;margin:20px auto"
          class="demo-form-inline"
          :inline="true"
          :model="edit.bedDetalsForm"
          :rules="rules"
          ref="editForm"
        >
          <el-form-item label="客户姓名：" prop="customerName">
            <el-input readonly v-model="edit.bedDetalsForm.customerName" />
          </el-form-item>
          <el-form-item label="性别：" prop="customerSex">
            <el-input
              readonly
              v-model="edit.bedDetalsForm.customerSex"
              :value="edit.bedDetalsForm.customerSex==0?'男':'女'"
            />
          </el-form-item>
          <!-- <el-form-item label="房间号：" prop="roomNo">
            <el-input readonly v-model="edit.bedDetalsForm.roomNo" />
          </el-form-item> -->
  
          <el-form-item label="床位详情： " prop="bedDetails">
            <el-input readonly v-model="edit.bedDetalsForm.bedDetails" />
          </el-form-item>
  
          <el-form-item label="床位使用起始日期：" prop="startDate">
            <el-input readonly v-model="edit.bedDetalsForm.startDate" />
          </el-form-item>
  
          <el-form-item label="床位使用结束日期： " prop="endDate">
            <el-date-picker
              v-model="edit.bedDetalsForm.endDate"
              style="width:200px"
              format="YYYY/MM/DD"
              value-format="YYYY-MM-DD"
              type="date"
              placeholder="选择日期"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="editSave('editForm')">保存</el-button>
            <el-button @click="editCancel">取消</el-button>
          </span>
        </template>
      </el-dialog>
      <!-- 床位调换对话框 -->
      <el-dialog v-model="exchangeData.dialogVisible" title="床位调换" width="60%"  :before-close="exchangeClose" align-center draggable>
        <el-divider border-style="double" style="margin:0;" />
        <el-form
          label-position="right"
          label-width="auto"
          style="max-width:800px;margin:20px auto"
          class="demo-form-inline"
          :inline="true"
          :model="exchangeData.exchangeForm"
          ref="exchangeForm"
        >
          <el-form-item label="客户姓名：" prop="customerName">
            <el-input readonly v-model="exchangeData.exchangeForm.customerName" />
          </el-form-item>
          <el-form-item label="旧床位详情" prop="bedDetails">
            <el-input readonly v-model="exchangeData.exchangeForm.bedDetails" />
          </el-form-item>
  
          <el-form-item label="性别：" prop="customerSex">
            <el-input
              readonly
              v-model="exchangeData.exchangeForm.customerSex"
              :value="exchangeData.exchangeForm.customerSex==0?'男':'女'"
            />
          </el-form-item>
          <el-form-item label="新楼号" prop="buildingNo">
            <el-input readonly v-model="exchangeData.exchangeForm.buildingNo" />
          </el-form-item>
  
          <!-- <el-form-item label="旧房间号" prop="oldRoomNo">
            <el-input readonly v-model="exchangeData.exchangeForm.oldRoomNo" />
          </el-form-item> -->
          <el-form-item label="新房号" prop="newRoomNo">
            <el-select
              v-model="exchangeData.exchangeForm.newRoomNo"
              style="width:200px"
              placeholder="请选择房间号"
              @change="getBed"
            >
              <el-option-group
                v-for="group in exchangeData.roomList"
                :key="group.label"
                :label="group.label"
              >
                <el-option
                  v-for="item in group.options"
                  :key="item.roomNo"
                  :label="item.roomNo"
                  :value="item.roomNo"
                ></el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
  
          <el-form-item label="当前床位使用起始日期：" prop="startDate">
            <el-input readonly v-model="exchangeData.exchangeForm.startDate" />
          </el-form-item>
          <el-form-item label="新床号" prop="newBedId">
            <el-select
              style="width:200px"
              v-model="exchangeData.exchangeForm.newBedId"
              placeholder="请选择床位"
            >
              <el-option ref="bedlist"
                v-for="item in exchangeData.bedList"
                :key="item.bedNo"
                :label="item.bedNo"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="当前床位使用结束时间：" prop="endDate">
            <el-input readonly v-model="exchangeData.exchangeForm.endDate" />
          </el-form-item>
        </el-form>
        <el-divider border-style="double" style="margin:0;" />
        <template #footer>
          <span class="dialog-footer" style="padding-top: 0px">
            <el-button type="primary" @click="exchangeSave">保存</el-button>
            <el-button @click="exchangeCancel">取消</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import {
    findBedDetailsList,
    updateDetailsStartDate,
    findBedByRoom,
    exchangeBed,
    delBedDetails
  } from "@/api/bedApi.js";
  import { listRoom } from "@/api/roomApi.js";
  export default {
    computed: {
      indexMethod() {
        return this.page.currentPag * this.page.pageSize - this.page.pageSize + 1;
      }
    },
    mounted() {
      //获取表格数据
      this.getBedDetailsList();
    },
    data() {
      return {
        queryParams: {
          customerName: "",
          startDate: "",
          endDate: "",
          pageSize: "1", //默认第一页
          isDeleted: "0" //显示标志  0-生效床位信息 1-失效床位信息(历史记录)
        },
        dateChange: [], //日期区间
        bedDetailsList: [], //信息列表
        //分页属性封装
        page: {
          total: 0,
          pageSize: 6,
          currentPag: 1,
          pagCount: 0
        },
        //编辑模态框model
        edit: {
          dialogVisible: false,
          bedDetalsForm: {
            id: "",
            customerName: "",
            customerSex: "",
            bedDetails: "",
            roomNo: "",
            startDate: "",
            endDate: ""
          }
        },
        //床位调换模态框model
        exchangeData: {
          dialogVisible: false,
          exchangeForm: {
            //展示属性
            customerName: "",
            customerSex: "",
            bedDetails: "",
            startDate: "",
            oldRoomNo: "",
            //提交参数属性
            id: "",
            customerId: "",
            buildingNo: "606",
            newRoomNo: "",
            newBedId: "",
            oldBedId: "",
            endDate: ""
          },
          roomList: [], //房间列表
          bedList: [] //床位列表
        },
        rules: {
          endDate: [
            { required: true, message: "选择床位使用结束时间", trigger: "change" }
          ]
        },
        btnFlag: true
      };
    },
    methods: {
      // //初始化查询条件
      // rest() {
      //   this.queryParams.startDate = "";
      //   this.queryParams.endDate = "";
      //   this.queryParams.customerName = "";
      //   this.queryParams.pageSize = "1";
      //   this.queryParams.isDeleted = "0";
      // },
      //点击查询
      query() {
        if (this.dateChange != null) {
          //如果输入了日期
          this.queryParams.startDate = this.dateChange[0];
          this.queryParams.endDate = this.dateChange[1];
        } else {
          //清除日期后要初始化日期条件
          this.queryParams.startDate = "";
          this.queryParams.endDate = "";
        }
        this.queryParams.pageSize = "1"; //回到第一页
        //重载表格
        this.getBedDetailsList();
      },
      //选中页码
      handleCurrentChange(curPage) {
        this.page.currentPag = curPage;
        this.queryParams.pageSize = curPage; //参数pageSize是服务端接收页码参数名
        //重新渲染表格
        this.getBedDetailsList();
      },
      //正在使用
      doing() {
        this.btnFlag = true;
        this.queryParams.isDeleted = "0"; //0-生效床位信息
        this.queryParams.pageSize = "1"; //回到第一页
        this.getBedDetailsList();
      },
      // 使用历史
      history() {
        this.btnFlag = false;
        this.queryParams.isDeleted = "1"; //1-失效床位信息(历史记录)
        this.queryParams.pageSize = "1"; //回到第一页
        this.getBedDetailsList();
      },
      //点击编辑
      editMessage(row) {
        //显示模态框
        this.edit.dialogVisible = true;
        //初始化模态框数据
        this.$nextTick(() => {
          this.edit.bedDetalsForm.id = row.id;
          this.edit.bedDetalsForm.customerName = row.customerName;
          this.edit.bedDetalsForm.customerSex = row.customerSex;
          this.edit.bedDetalsForm.bedDetails = row.bedDetails;
          this.edit.bedDetalsForm.roomNo = row.roomNo;
          this.edit.bedDetalsForm.startDate = row.startDate;
          this.edit.bedDetalsForm.endDate = row.endDate;
        });
     
      },
      //重置表单
      resetForm(formName) {
        this.$refs[formName].resetFields();
      },
      //编辑模态框关闭
      editClose() {
        this.edit.dialogVisible = false;
        this.resetForm("editForm"); //重置表单
      },
      //编辑取消
      editCancel() {
        this.editClose();
      },
      //床位调换模态框关闭
      exchangeClose() {
        this.exchangeData.dialogVisible = false;
        this.resetForm("exchangeForm"); //重置表单
      },
      //点击床位调换
      exchange(row) {
        this.exchangeData.dialogVisible = true;
        // 初始化表单
        this.$nextTick(() => {
          this.exchangeData.exchangeForm.id = row.id;
          this.exchangeData.exchangeForm.customerName = row.customerName;
          this.exchangeData.exchangeForm.customerSex = row.customerSex;
          this.exchangeData.exchangeForm.bedDetails = row.bedDetails;
          this.exchangeData.exchangeForm.roomNo = row.roomNo;
          this.exchangeData.exchangeForm.startDate = row.startDate;
          this.exchangeData.exchangeForm.endDate = row.endDate;
          this.exchangeData.exchangeForm.oldRoomNo = row.roomNo;
          this.exchangeData.exchangeForm.customerId = row.customerId;
          this.exchangeData.exchangeForm.newRoomNo = "";
          this.exchangeData.exchangeForm.oldBedId = row.bedId;
          this.exchangeData.exchangeForm.newBedId = "";
        });
        //获取房间列表
        this.getRoomList();
      },
      //取消床位调换
      exchangeCancel() {
        this.exchangeClose();
      },
      //api-删除历史记录
      del(id) {
        this.$confirm('此操作删除记录, 是否继续?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(() => {
            delBedDetails({id:id}).then(res=>{
              if(res.flag){
                this.$message.success(res.message)
                //重载表格
                this.getBedDetailsList();
              }else{
                this.$message.error(res.message)
              }
            })
          }).catch(() => {
          });
      },
      //api-点击床位调换保存
      exchangeSave() {
        if (
          this.exchangeData.exchangeForm.newRoomNo != null &&
          this.exchangeData.exchangeForm.newRoomNo != "" &&
          this.exchangeData.exchangeForm.newBedId != null &&
          this.exchangeData.exchangeForm.newBedId != ""
        ) {
          exchangeBed(this.exchangeData.exchangeForm).then(res => {
            if (res.flag) {
              this.$message.success(res.message);
              this.exchangeClose(); //关闭模态框
              //重载表格
              this.getBedDetailsList();
            } else {
              this.$message.error(res.message);
            }
          });
        } else {
          this.$message.error("请选择新的房间和床位");
        }
      },
      //api-查询房间列表
      getRoomList() {
        listRoom().then(res => {
          //构建房间列表数据(按楼层分组显示)
          let floor1 = res.data.filter((item, i, arr) => {
            return item.roomFloor == "一楼";
          });
          let floor2 = res.data.filter((item, i, arr) => {
            return item.roomFloor == "二楼";
          });
          this.exchangeData.roomList = [
            {
              label: "一楼",
              options: floor1
            },
            {
              label: "二楼",
              options: floor2
            }
          ];
        });
      },
      //api-根据选择的房间查询床位
      getBed() {
        //清空床位下拉列表
        this.exchangeData.bedList=[]
        this.exchangeData.exchangeForm.newBedId=""
        findBedByRoom({
          bedStatus: 1,
          roomNo: this.exchangeData.exchangeForm.newRoomNo
        }).then(res => {
          this.exchangeData.bedList =res.data; 
          //this.exchangeData.bedList.
        });
      },
      //api-编辑保存
      editSave(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            updateDetailsStartDate(this.edit.bedDetalsForm).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                this.editClose(); //关闭模态框
                //重载表格
                this.getBedDetailsList();
              } else {
                this.$message.error(res.message);
              }
            });
          } else {
            return false;
          }
        });
      },
      //api-查询床位详情信息(分页)
      getBedDetailsList() {
        findBedDetailsList(this.queryParams).then(res => {
          this.bedDetailsList = res.data.records;
          this.page.total = res.data.total; //总记录数
          this.page.pageSize = res.data.size; //每页显示条数
          this.page.currentPag = res.data.current; //当前页码
          this.page.pagCount = res.data.pages; //总页数
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