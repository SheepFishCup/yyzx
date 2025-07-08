<template>
  <div class="common-layout">
    <el-container>
  
      <el-header>
      <div>
        <el-row :gutter="30">
          <el-col :span="7">
            <el-input placeholder="客户姓名" v-model="condition.customerName" clearable @clear="query" size="large">
              <template #append>
                <el-button type="info" @click="query"  style="color:black">查询</el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="17">
            <el-button type="primary" @click="dj" color="#337ab7" style="margin-top:4px">
              <el-icon>
                <Plus />
              </el-icon>
              <span>登记</span>
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
            :class="[{activeBtn: btnFlag1}]"
            style="border: 1px white solid;margin-left: 0px"
            @click="selfCareMan"
          >自理老人</el-button>
          <el-button
            :class="[{activeBtn: btnFlag2}]"
            style="border: 1px white solid;margin-left: 0px"
            @click="careMan"
          >护理老人</el-button>
          <!-- <el-button
            :class="[{activeBtn: btnFlag3}]"
            style="border: 1px white solid;margin-left: 0px"
            @click="historyMan"
          >历史老人</el-button> -->
        </el-row>
      </div>

      <div>
        <!-- 表格 -->
        <el-table :data="khxxList" style="width: 100% ;color:black;"  stripe >
           <el-table-column align="center" fixed type="index" :index="indexMethod" label="序号" width="60" />
          <el-table-column align="center" fixed  prop="customerName" label="客户姓名" width="100" />
          <el-table-column align="center"  prop="bloodType" label="血型" width="60" />
          <el-table-column align="center"  prop="contactTel" label="联系电话" width="150" />
          <el-table-column align="center"  prop="customerAge" label="年龄" width="60" />
          <el-table-column align="center"  prop="customerSex" label="性别" width="60" >
          <template #default="scope">{{ scope.row.customerSex==0?'男':'女'}}</template>
          </el-table-column>
          <el-table-column align="center"  prop="familyMember" label="家属" width="100" />
          <el-table-column align="center"  prop="idcard" label="身份证号" width="180" />
          <el-table-column align="center"  prop="buildingNo" label="所属楼房" width="80" />
          <el-table-column align="center"  prop="roomNo" label="房间号" width="80" />
          <el-table-column align="center"  prop="bedNo" label="床号" width="80" />
          <el-table-column align="center"  prop="birthday" label="出生日期" width="150" />
          <el-table-column align="center"  prop="checkinDate" label="入住时间" width="150"/>
          <el-table-column align="center"  prop="expirationDate" label="合同到期时间" width="150" />
          <el-table-column align="center"  prop="levelName" label="护理级别" width="80" />
          <el-table-column align="center"  prop="nickName" label="健康管家(护工)" width="120" />
          <el-table-column align="center"  prop="psychosomaticState" label="身心状况" width="200" />
          <el-table-column align="center" fixed="right"   label="操作" width="180">
            <template #default="scope">
              <el-button type="danger" link size="small" v-if="scope.row.isDeleted==1" >逻辑上已删除,只查询不操作</el-button>
              <el-button type="danger" link v-if="scope.row.isDeleted==0"  @click="del(scope.row)">删除</el-button>
              <el-button type="primary" link v-if="scope.row.isDeleted==0"  @click="edit(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页插件 -->
      <div  style="margin-top:15px">
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
      
    <!-- 模态框 :入住登记-->
    <el-dialog
      v-model="djDialog.dialogVisible"
      :title="djDialog.tops"
      width="60%"
      align-center
      draggable
      :before-close="djhandleClose"
      >
      <el-divider border-style="double" style="margin:0;" />
      <el-form
        label-position="right"
        label-width="auto"
        style="max-width:800px;margin:20px auto"
        class="demo-form-inline"
        ref="customerForm"
        :inline="true"
        :model="djDialog.customerForm"
        :rules="rules"
      >
        <el-form-item label="客户姓名：" prop="customerName">
          <el-input placeholder="输入[客户姓名]" v-model="djDialog.customerForm.customerName" />
        </el-form-item>
        <el-form-item label="楼栋： " prop="buildingNo">
          <el-input readonly v-model="djDialog.customerForm.buildingNo" />
        </el-form-item>
        <el-form-item label="年龄：" prop="customerAge">
          <el-input v-model="djDialog.customerForm.customerAge" />
        </el-form-item>

        <el-form-item label="房间号： " prop="roomNo">
          <el-input readonly v-if="isShow" v-model="djDialog.customerForm.roomNo" />
          <el-select v-if="!isShow"
            v-model="djDialog.customerForm.roomNo"
            style="width:200px"
            @change="getBed"
            placeholder="请选择房间号"
          >
            <el-option-group
              v-for="group in djDialog.roomList"
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

        <el-form-item label="性别：" prop="customerSex">
          <el-select
            v-model="djDialog.customerForm.customerSex"
            style="width:200px"
            placeholder="性别"
          >
            <el-option
              v-for="item in djDialog.sex"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="床位号： " prop="bedId">
          <el-input readonly v-if="isShow" v-model="djDialog.customerForm.bedId" />
          <el-select v-if="!isShow"
            style="width:200px"
            v-model="djDialog.customerForm.bedId"
            placeholder="请选择床位"
          >
            <el-option
              v-for="item in djDialog.bedList"
              :key="item.bedNo"
              :label="item.bedNo"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期： " prop="birthday">
          <el-date-picker
            @change="getAge"
            style="width:200px"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            v-model="djDialog.customerForm.birthday"
            type="date"
            placeholder="选择日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="入住时间： "  prop="checkinDate">
          <el-date-picker 
            style="width:200px"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            v-model="djDialog.customerForm.checkinDate"
            type="date"
            :disabled-date="disabledDate"
            placeholder="选择日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="身份证号：" prop="idcard">
          <el-input v-model="djDialog.customerForm.idcard" />
        </el-form-item>
        <el-form-item label="合同到期时间： " prop="expirationDate">
          <el-date-picker
            style="width:200px"
            v-model="djDialog.customerForm.expirationDate"
            type="date"
            format="YYYY/MM/DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate0"
            placeholder="选择日期"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="血型：" prop="bloodType">
          <el-input v-model="djDialog.customerForm.bloodType" />
        </el-form-item>
        <el-form-item label="联系电话：" prop="contactTel">
          <el-input v-model="djDialog.customerForm.contactTel" />
        </el-form-item>
        <el-form-item label="家属：" prop="familyMember">
          <el-input v-model="djDialog.customerForm.familyMember" />
        </el-form-item>
        <el-form-item label="身心状况： " prop="psychosomaticState">
          <el-input v-model="djDialog.customerForm.psychosomaticState" />
        </el-form-item>
      </el-form>
      <el-divider border-style="double" style="margin:0;" />
      <template #footer>
        <span class="dialog-footer" style="padding-top: 0px">
          <el-button type="primary" @click="djsave('customerForm')">保存</el-button>
          <el-button v-if="!djDialog.customerForm.id" type="primary" @click="resetForm('customerForm')">重置</el-button>
          <el-button @click="djcancel">取消</el-button>
        </span>
      </template>
    </el-dialog>

    </el-container>
  </div>
</template>

<script>
import { listRoom } from "@/api/roomApi.js";
import { findBedByRoom } from "@/api/bedApi.js";
import { rzdjCustomer, getKhxxList ,delCustomer,editCustomer} from "@/api/customerApi.js";

export default {
computed:{
  //分页序号不重修
  indexMethod(){
    return this.page.currentPag* this.page.pageSize-this.page.pageSize+1
  }
},
data() {
  return {
    isShow:false,// 编辑和新增使用同一个模态框，isShow用来判别展示不同的标签
    //模态框数据
    djDialog: {
      dialogVisible: false, //模态框状态
      tops: "", //模态框标题,
      customerForm: {
        id:"",
        bedId: "",
        bloodType: "O",
        buildingNo: "606", //楼号固定606
        contactTel: "",
        customerAge: "",
        customerName: "",
        customerSex: "0",
        roomNo: "",
        idcard: "",
        psychosomaticState: "",
        familyMember: "", //家属
        filepath: "@/assets/tou.png", //头像路径固定
        birthday: "",
        checkinDate: "",
        expirationDate: ""
      },
      roomList: [],
      bedList: [],
      sex: [
        {
          value: "0",
          label: "男"
        },
        {
          value: "1",
          label: "女"
        }
      ]
    },
    //查询条件
    condition:{
      customerName:"",
      pageSize:"1",//默认第一页
      manType:"1" //默认查询 1-自理老人 2-护理老人 3-历史老人
    },
    //分页属性封装
    page:{
      total:0,
      pageSize:6,
      currentPag:1,
      pagCount:0
    },
    //客户信息列表
    khxxList: [],
    //校验规则
    rules: {
      bedId: [{ required: true, message: "选择床位", trigger: "change" }],
      bloodType: [{ required: true, message: "请输入血型", trigger: "blur" }],
      contactTel: [
        { required: true, message: "请输入手机号", trigger: "blur" }
      ],
      customerAge: [
        { required: true, message: "请输入年龄", trigger: "blur" }
      ],
      customerName: [
        { required: true, message: "请输入客户姓名", trigger: "blur" }
      ],
      roomNo: [
        { required: true, message: "请选择房间号", trigger: "change" }
      ],
      idcard: [
        { required: true, message: "请输入身份证号", trigger: "blur" }
      ],
      familyMember: [
        { required: true, message: "请输入家属", trigger: "blur" }
      ],
      checkinDate: [
        {
          type: "date",
          required: true,
          message: "请选择时间",
          trigger: "change"
        }
      ],
      expirationDate: [
        {
          type: "date",
          required: true,
          message: "请选择时间",
          trigger: "change"
        }
      ]
    },
    //加状态，点击每个按钮改一下状态
    btnFlag1: true,
    btnFlag2: false,
    btnFlag3:false,
  };
},
//监听开始时间
watch: {
  "djDialog.customerForm": {
    handler: function(newvalue, oldvalue) {
      //当入住时间清空或者重新选择的入住时间当过当前结束时间时，清空结束时间
      if (
        newvalue.checkinDate == null ||
        newvalue.checkinDate == "" ||
        newvalue.checkinDate > this.djDialog.customerForm.expirationDate
      ) {
        this.djDialog.customerForm.expirationDate = "";
      }
    },
    deep: true
  }
},
methods: {
  //时间限制（设置时间控件的隐藏日期）
  disabledDate(time) {
    //入住时间只能是今日往后(包括今日)
    return time.getTime() < Date.now() - 1000 * 60 * 60 * 24;
  },
  disabledDate0(time) {  
    // 获取入住时间
    let start = this.djDialog.customerForm.checkinDate;
    //elementplus格式化后需要转日期格式
    start=new Date(start)
    if (start != "" && start != "undifine" && start != null) {
      return time.getTime() < start- 1000 * 60 * 60 * 24;
    }
    return time.getTime() < Date.now() - 1000 * 60 * 60 * 24;
  },
  //登记：点击"登记"
  dj() {
    this.isShow=false
    // //查询房间列表
    this.getRoomList();
    //打开登记模态框
    this.djDialog.dialogVisible = true;
    this.djDialog.tops = "入住登记";
  },
  //客户列表：点击编辑
  edit(row){
    this.isShow=true
    //打开登记模态框
    this.djDialog.dialogVisible = true;
    this.djDialog.tops = "信息编辑";
    // 这里用到this.$nextTick() 是为了保证赋值是发生在弹窗打开后，所以弹窗打开的那一刻，
    // 表单使用的还是最初data中的formData ，并将其作为表单初始值。
    this.$nextTick(()=>{
      //初始化编辑数据
      this.djDialog.customerForm.id=row.id
      this.djDialog.customerForm.bedId=row.bedId
      this.djDialog.customerForm.birthday=row.birthday
      this.djDialog.customerForm.bloodType=row.bloodType
      this.djDialog.customerForm.buildingNo=row.buildingNo
      this.djDialog.customerForm.contactTel=row.contactTel
      this.djDialog.customerForm.customerAge=row.customerAge
      this.djDialog.customerForm.customerName=row.customerName
      this.djDialog.customerForm.customerSex=row.customerSex+""  //表格中获取的性别为0或1 -number类型，导致下来列表显示不正确
      this.djDialog.customerForm.roomNo=row.roomNo
      this.djDialog.customerForm.idcard=row.idcard
      this.djDialog.customerForm.psychosomaticState=row.psychosomaticState
      this.djDialog.customerForm.familyMember=row.familyMember
      this.djDialog.customerForm.filepath=row.filepath
      this.djDialog.customerForm.checkinDate=row.checkinDate
      this.djDialog.customerForm.expirationDate=row.expirationDate
    })
  },
  //登记：点击"x" 关闭模态框触发
  djhandleClose() {
    this.djcancel();
  },
  //登记：关闭模态框
  djcancel() {
    this.djDialog.dialogVisible = false;
    //重置表单
    this.resetForm("customerForm");
    //将id初始为""
    this.djDialog.customerForm.id=""
  },
  //登记/编辑：保存
  djsave(formName) {
    this.$refs[formName].validate(valid => {
      if (valid) {
        //通过id判断是添加还是编辑
        if(this.djDialog.customerForm.id==null || this.djDialog.customerForm.id==""){
           //校验通过 api-入住登记
            rzdjCustomer(this.djDialog.customerForm).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                //1、刷新表格数据
                //刷新数据表格(回到最初查询状态)
                this.condition.manType="1";
                this.condition.pageSize="1"; //回到第一页
                this.condition.customerName=""
                this.getKhxxList()
                //2、关闭弹出层 
                this.djcancel();
              } else {
                this.$message.error(res.message);
              }
            });
        }else{
          //校验通过 api-编辑
          editCustomer(this.djDialog.customerForm).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
                //1、刷新表格数据
                //刷新数据表格(保留查询状态)
                this.getKhxxList()
                //2、关闭弹出层 
                this.djcancel();
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
  
  //重置表单
  resetForm(formName) {
    this.$refs[formName].resetFields();
  },
  //自理老人
  selfCareMan() {
    this.btnFlag1 = true;
    this.btnFlag2 = false;
    this.btnFlag3=false;
    this.condition.manType="1";
    this.condition.pageSize="1"; //回到第一页
    this.getKhxxList()
  },
  //护理老人
  careMan() {
    this.btnFlag1 = false; 
    this.btnFlag2 = true;
    this.btnFlag3 = false;
    this.condition.manType="2";
    this.condition.pageSize="1"; //回到第一页
    this.getKhxxList()
  },
  //选择出生日期
  getAge(){
    let bir=this.djDialog.customerForm.birthday
    if(bir!=null && bir!='' && bir!='undifine'){
      this.djDialog.customerForm.customerAge=Math.round((new Date()-new Date(bir))/(1000*60*60*24*365))
    }
  },
   //历史老人
   historyMan() {
    this.btnFlag1 = false;
    this.btnFlag2 = false;
    this.btnFlag3 = true;
    this.condition.manType="3";
    this.condition.pageSize="1"; //回到第一页
    this.getKhxxList()
  },
  //点击查询
  query(){
    this.condition.pageSize="1"; //回到第一页
    this.getKhxxList();
  },
  //点击删除 api-删除客户信息
  del(row){
    this.$confirm('此操作删除客户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        delCustomer({id:row.id,bedId:row.bedId}).then(res=>{
          if(res.flag){
            this.$message({
             type: 'success',
             message: '删除成功!'
            });
            //重载表格
            this.getKhxxList();
          }
        })
      }).catch(() => {
      });
  },
  //选中页码
  handleCurrentChange(curPage){
    this.page.currentPag=curPage
    this.condition.pageSize= curPage //参数pageSize是服务端接收页码参数名
    //重新渲染表格
    this.getKhxxList();
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
      this.djDialog.roomList = [
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
    this.djDialog.bedList = [];
    this.djDialog.customerForm.bedId=""
    findBedByRoom({
      bedStatus: 1,
      roomNo: this.djDialog.customerForm.roomNo
    }).then(res => {
      this.djDialog.bedList = res.data;
    });
  },
  //api-查询客户信息列表-分页
  getKhxxList(){
    getKhxxList(this.condition).then(res=>{
      this.khxxList = res.data.records
      this.page.total=res.data.total  //总记录数
      this.page.pageSize=res.data.size //每页显示条数
      this.page.currentPag=res.data.current //当前页码
      this.page.pagCount=res.data.pages //总页数
    })
  }
},
mounted() {
  //获取khxx
  this.getKhxxList();
},
};
</script>

<style scoped >
.activeBtn {
color: #1890ff;
background: rgb(232, 244, 255);
}
</style>