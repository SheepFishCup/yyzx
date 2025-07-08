<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <div>
          <el-row :gutter="30">
            
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
          <el-tabs type="border-card" @tab-click="handleTabClick" v-model="queryParams.weekDay">
            <el-tab-pane v-for="(week,index) in weekdayList" :label="week" :name="week" :key="index" >
				<div v-if="queryParams.weekDay == week">
					<el-row :gutter="20"  class="row"  v-if="queryParams.weekDay == week">
						<el-col :span="2" class="mealtype">
							<span>{{changeType(1)}}早<br/>餐</span>
						</el-col>
					    <el-col
					      v-for="(meal, index) in breakfastList"
					      :key="meal.id"
					      :span="4"
						  class="food"
					    >
					      <el-card :body-style="{ padding: '15px' }" class="card"  shadow="hover" >
							  <el-tag class="ml-2 taste" v-if="meal.taste=='正常'" type="">{{meal.taste}}</el-tag>
							  <el-tag class="ml-2 taste" v-if="meal.taste=='多糖'" type="warning">{{meal.taste}}</el-tag>
							  <el-tag class="ml-2 taste" v-if="meal.taste=='少糖'" type="success">{{meal.taste}}</el-tag>
							  <el-tag class="ml-2 taste" v-if="meal.taste=='多盐'" type="warning">{{meal.taste}}</el-tag>
							  <el-tag class="ml-2 taste" v-if="meal.taste=='少盐'" type="success">{{meal.taste}}</el-tag>
							  <el-icon class="edit" @click="edit(meal,1)"><EditPen /></el-icon>
							  <el-icon class="close" @click="del(meal.id,1)"><Close /></el-icon>
							<!-- <img src="http://localhost:9999/yyzx/images/congee.jpg"/> -->
							<img
					          :src="foodFullImg(meal.foodImg)"
					          class="image"
					        />
					        <div style="padding: 14px;">
					          <el-row><el-col>类别：{{meal.foodType}}</el-col></el-row>
					          <el-row><el-col>名称：{{meal.foodName}}</el-col></el-row>
					          <el-row><el-col>价格：{{meal.price}}元</el-col></el-row>
					          <el-row><el-col>是否清真：
								<span v-if="meal.isHala">是</span>
								<span v-else>否</span>
							  </el-col></el-row>
					        </div>
							</el-card>
					    </el-col>
					  </el-row>
					  <el-row :gutter="20"  class="row"  v-if="queryParams.weekDay == week">
					  	<el-col :span="2" class="mealtype">
					  		<span>{{changeType(2)}}午<br/>餐</span>
					  	</el-col>
					      <el-col
					        v-for="(meal, index) in lunchList"
					        :key="meal.id"
					        :span="4"
					  	  class="food"
					      >
					        <el-card :body-style="{ padding: '15px' }" class="card"  shadow="hover" >
					  		  <el-tag class="ml-2 taste" v-if="meal.taste=='正常'" type="">{{meal.taste}}</el-tag>
					  		  <el-tag class="ml-2 taste" v-if="meal.taste=='多糖'" type="warning">{{meal.taste}}</el-tag>
					  		  <el-tag class="ml-2 taste" v-if="meal.taste=='少糖'" type="success">{{meal.taste}}</el-tag>
					  		  <el-tag class="ml-2 taste" v-if="meal.taste=='多盐'" type="warning">{{meal.taste}}</el-tag>
					  		  <el-tag class="ml-2 taste" v-if="meal.taste=='少盐'" type="success">{{meal.taste}}</el-tag>
					  		  <el-icon class="edit" @click="edit(meal,2)"><EditPen /></el-icon>
					  		  <el-icon class="close" @click="del(meal.id,2)"><Close /></el-icon>
					  		<!-- <img src="http://localhost:9999/yyzx/images/congee.jpg"/> -->
					  		<img
					            :src="foodFullImg(meal.foodImg)"
					            class="image"
					          />
					          <div style="padding: 14px;">
					            <el-row><el-col>类别：{{meal.foodType}}</el-col></el-row>
					            <el-row><el-col>名称：{{meal.foodName}}</el-col></el-row>
					            <el-row><el-col>价格：{{meal.price}}元</el-col></el-row>
					            <el-row><el-col>是否清真：
					  			<span v-if="meal.isHala">是</span>
					  			<span v-else>否</span>
					  		  </el-col></el-row>
					          </div>
					  		</el-card>
					      </el-col>
					    </el-row>
						<el-row :gutter="20"  class="row"  v-if="queryParams.weekDay == week">
							<el-col :span="2" class="mealtype">
								<span>{{changeType(3)}}晚<br/>餐</span>
							</el-col>
						    <el-col
						      v-for="(meal, index) in dinnerList"
						      :key="meal.id"
						      :span="4"
							  class="food"
						    >
						      <el-card :body-style="{ padding: '15px' }" class="card"  shadow="hover" >
								  <el-tag class="ml-2 taste" v-if="meal.taste=='正常'" type="">{{meal.taste}}</el-tag>
								  <el-tag class="ml-2 taste" v-if="meal.taste=='多糖'" type="warning">{{meal.taste}}</el-tag>
								  <el-tag class="ml-2 taste" v-if="meal.taste=='少糖'" type="success">{{meal.taste}}</el-tag>
								  <el-tag class="ml-2 taste" v-if="meal.taste=='多盐'" type="warning">{{meal.taste}}</el-tag>
								  <el-tag class="ml-2 taste" v-if="meal.taste=='少盐'" type="success">{{meal.taste}}</el-tag>
								  <el-icon class="edit" @click="edit(meal,3)"><EditPen /></el-icon>
								 <el-icon class="close" @click="del(meal.id,3)"><Close /></el-icon>
								<!-- <img src="http://localhost:9999/yyzx/images/congee.jpg"/> -->
								<img
						          :src="foodFullImg(meal.foodImg)"
						          class="image"
						        />
						        <div style="padding: 14px;">
						          <el-row><el-col>类别：{{meal.foodType}}</el-col></el-row>
						          <el-row><el-col>名称：{{meal.foodName}}</el-col></el-row>
						          <el-row><el-col>价格：{{meal.price}}元</el-col></el-row>
						          <el-row><el-col>是否清真：
									<span v-if="meal.isHala">是</span>
									<span v-else>否</span>
								  </el-col></el-row>
						        </div>
								</el-card>
						    </el-col>
						  </el-row>
				</div>
				
			</el-tab-pane>
          </el-tabs>
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
        <el-form-item label="膳食类型：" prop="mealType">
        	<el-select v-model="dialog.item.mealType" style="width:320px" placeholder="餐饮类型：">
        	  <el-option v-for="item in mealtypeList" :label="item.mealtypeName" :value="item.typeId" />
        	</el-select>
        </el-form-item>
		<el-form-item label="食品名称：" prop="foodId">
			<el-select v-model="dialog.item.foodId" style="width:320px" placeholder="食品名称：">
			  <el-option v-for="item in foodList" :label="item.foodName" :value="item.id" />
			</el-select>
		</el-form-item>
        <el-form-item label="星期：" prop="weekDay">
          <!-- <el-input v-model="dialog.item.weekDay" /> -->
		  <el-select v-model="dialog.item.weekDay" style="width:320px" placeholder="星期：">
		    <el-option value="周一" />
			<el-option value="周二" />
			<el-option value="周三" />
			<el-option value="周四" />
			<el-option value="周五" />
			<el-option value="周六" />
			<el-option value="周日" />
		  </el-select>
        </el-form-item>
        <el-form-item label="口味：" prop="taste">
          <!-- <el-input v-model="dialog.item.taste" /> -->
		  <el-select v-model="dialog.item.taste" style="width:320px" placeholder="口味：">
		    <el-option value="正常" />
			<el-option value="多糖" />
			<el-option value="少糖" />
			<el-option value="多盐" />
			<el-option value="少盐" />
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
import request from "@/request/request.js";
import {
 addMeal,
 updateMeal,
 delMeal,
 findMeal
} from "@/api/mealApi.js";
import { findFood } from "@/api/foodApi.js";
export default {
  
  data() {
    return {
		mealtypeList:[{typeId:1,mealtypeName:'早餐'},{typeId:2,mealtypeName:'午餐'},{typeId:3,mealtypeName:'晚餐'}],
		weekdayList:['周一','周二','周三','周四','周五','周六','周日'],
		currentDate:'1997-7-7',
      //模态框数据
      dialog: {
        dialogVisible: false, //模态框状态
        tops: "", //模态框标题,
        item: {
          id: "",
		  foodId:"",
          foodName: "",
          foodType: "",
          price: "",
          isHalal: "",
          weekDay: "",
          mealType: "",
		  taste:"",
		  isDeleted:0
        }
      },
     
      btnFlag: true,
      queryParams: {
		mealType:1,
        weekDay: "周一",
        pageSize: "1" //默认第一页
      },
      breakfastList: [],
	  lunchList: [],
	  dinnerList:[],
	  foodList:[],
	  mealList:[],
	  path:'',
	  hasFood:true
    };
  },
  mounted() {
    this.getFoodList();
	this.getMealList(1);
	this.getMealList(2);
	this.getMealList(3);
  },
  watch: {
  	  'queryParams.weekDay'(newVal, oldVal){
			  console.log(newVal);
  			  this.getMealList(1);
			  this.getMealList(2);
			  this.getMealList(3);
		  // immediate: true,
  	  }
  },
  methods: {
	foodFullImg(foodImg){
			  return 'http://localhost:9999/yyzx/images/'+foodImg;
	},
    //点击查询
    query() {
      // this.getMealList();
    },
    //点击修改
    edit(meal,mealType) {
      this.dialog.tops = "修改膳食日历";
      this.dialog.dialogVisible = true;
      //初始化模态框数据
      this.$nextTick(() => {
        this.dialog.item.id = meal.id;
        this.dialog.item.foodName = meal.foodName;
        this.dialog.item.foodType = meal.foodType;
        this.dialog.item.price = meal.price;
        this.dialog.item.isHalal = meal.isHalal;
        this.dialog.item.weekDay = meal.weekDay;
        this.dialog.item.mealType = mealType;
		this.dialog.item.taste = meal.taste;
      });
    },
    //点击添加按钮
    addItem() {
      this.dialog.tops = "添加膳食管理";
      this.dialog.dialogVisible = true;
	  this.getFoodList();
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
            addMeal(this.dialog.item).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
				console.log(this.dialog.item);
				this.getMealList(this.dialog.item.mealType);
                this.handleClose(); //关闭模态框
              } else {
                this.$message.error(res.message);
              }
            });
          } else {
             updateMeal(this.dialog.item).then(res => {
              if (res.flag) {
                this.$message.success(res.message);
				console.log(this.dialog.item);
                //刷新数据表格
                this.getMealList(this.dialog.item.mealType);
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
   del(id,mealtype) {
     this.$confirm("此操作删除记录, 是否继续?", "提示", {
       confirmButtonText: "确定",
       cancelButtonText: "取消",
       type: "warning"
     })
       .then(() => {
         delMeal({ id: id }).then(res => {
           if (res.flag) {
             this.$message.success(res.message);
             //重载表格
			 console.log("餐饮类型"+mealtype);
			  this.getMealList(mealtype);
           } else {
             this.$message.error(res.message);
           }
         });
       })
       .catch(() => {});
   },
    //api-查询膳食日历
	async getMealList(typeId) {
		this.queryParams.mealType = typeId;
		await findMeal(this.queryParams).then(res => {
		  if(typeId == 1){
		  		  this.breakfastList =  res.data.records;
		  }else if(typeId == 2){
		  		  this.lunchList =  res.data.records;
		  }else{
		  		  this.dinnerList =  res.data.records;
		  }
      });
    },
	//api-查询食品列表-分页
	getFoodList() {
	  findFood().then(res => {
	    this.foodList = res;
	  });
	},
	handleTabClick(tab, event){
		console.log(this.weekdayList[tab.index]);
		this.queryParams.weekDay = this.weekdayList[tab.index];
		// this.getMealList();
		this.breakfastList = null;
		this.lunchList = null;
		this.dinnerList = null;
		
	},
	changeType(typeId){
	  // console.log(typeId);
	  this.queryParams.mealType = typeId;
	}
  }
  
};

</script>

<style scoped >
.activeBtn {
  color: #1890ff;
  background: rgb(232, 244, 255);
}

.time {
  font-size: 12px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button {
  padding: 0;
  min-height: auto;
}
.card{
	height: 80%;
	position: relative;
	cursor: pointer;
	font-size: 14px;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
}
.image {
  width: 100%;
  display: block;
  width:160px;
  height: 160px;
  margin-top: 20px;
}

.edit{
	position: absolute;
	right: 20px;
	top:5px;
	font-size: 20px;
}
.close{
	position: absolute;
	right: 5px;
	top: 5px;
	font-size: 20px;
}

.taste{
	position: absolute;
	left: 5px;
	top: 5px;
}
.row{
	border: 1px solid #eee; 
	margin-top: 20px;
	box-shadow: 5px 5px #eee;
	height: 300px;
}
.mealtype{
	color: #fff;
	background-color: #337ab7;
	display: flex;
	justify-content: center;
	align-items: center;
	height: 300px;
}
.food{
	display: flex;
	justify-content: center; 
	align-items: center;
	margin-left: 15px;
	
}
.nofood{
	width: 400px;
	height: 300px;
	display: flex;
	align-items: center;
	justify-content: center;
}
</style>