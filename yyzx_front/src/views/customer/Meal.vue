<template>
  <div class="common-layout">
    <el-container>
	<el-header>
	  <div>
	    <el-row :gutter="30">
	      <el-col :span="17">
	        <!-- 添加膳食按钮 -->
	        <el-button type="primary" @click="addItem" color="#337ab7" style="margin-top:4px">
	          <el-icon><Plus /></el-icon>
	          <span>添加膳食</span>
	        </el-button>
        
	        <!-- 添加食品按钮（新增） -->
	        <el-button type="success" @click="addFood" color="#67c23a" style="margin-top:4px;margin-left:10px">
	          <el-icon><Plus /></el-icon>
	          <span>添加食品</span>
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
	<!-- 食品添加/编辑对话框 -->
	<el-dialog
	  v-model="foodDialog.dialogVisible"
	  :title="foodDialog.tops"
	  width="50%"
	  align-center
	  draggable
	  :before-close="handleFoodClose"
	>
	  <el-divider border-style="double" style="margin:0;" />
	  <el-form
	    label-position="right"
	    label-width="100px"
	    style="max-width:500px;margin:20px auto"
	    ref="foodForm"
	    :model="foodDialog.item"
	    :rules="foodRules"
	  >
	    <el-form-item label="食品名称：" prop="foodName">
	      <el-input v-model="foodDialog.item.foodName" placeholder="请输入食品名称" />
	    </el-form-item>
    
	    <el-form-item label="食品类型：" prop="foodType">
	      <el-select v-model="foodDialog.item.foodType" placeholder="请选择食品类型" style="width:100%">
	        <el-option label="主食" value="主食" />
	        <el-option label="大荤" value="大荤" />
	        <el-option label="小荤" value="小荤" />
	        <el-option label="素菜" value="素菜" />
	        <el-option label="汤" value="汤" />
	      </el-select>
	    </el-form-item>
    
	    <el-form-item label="食品价格：" prop="price">
	      <el-input-number v-model="foodDialog.item.price" :min="0" :precision="2" :step="0.5" />
	      <span style="margin-left:10px">元</span>
	    </el-form-item>
    
	    <el-form-item label="是否清真：" prop="isHalal">
	      <el-radio-group v-model="foodDialog.item.isHalal">
	        <el-radio :label="1">是</el-radio>
	        <el-radio :label="0">否</el-radio>
	      </el-radio-group>
	    </el-form-item>
    
	    <el-form-item label="食品图片：" prop="foodImg">
	      <el-upload
		  class="food-uploader"
	  	  action="#"
		  :auto-upload="false"
		  :show-file-list="false"
		  :on-change="handleFileChange"
		  :before-upload="beforeFoodUpload"
		  :accept="'.png,.jpg,.jpeg,.gif'"
	      >
	        <img v-if="foodDialog.item.foodImg" :src="foodDialog.item.foodImg" class="food-preview" />
	        <el-icon v-else class="uploader-icon"><Plus /></el-icon>
	        <div class="el-upload__text">
	          点击上传或拖拽文件到此区域<br/>
	          <span class="upload-tip">仅支持 PNG/JPG/JPEG 格式，大小不超过 2MB,建议200×200或300×300像素</span>
	        </div>
	      </el-upload>
	    </el-form-item>
	  </el-form>
	  <el-divider border-style="double" style="margin:0;" />
	  <template #footer>
	    <span class="dialog-footer">
	      <el-button type="primary" @click="saveFood('foodForm')">保存</el-button>
	      <el-button @click="cancelFood">取消</el-button>
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
import { findFood,uploadFoodImg,addFood } from "@/api/foodApi.js";
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
	  hasFood: true,
	  // 食品对话框数据
		foodDialog: {
			dialogVisible: false,
			tops: '',
			item: {
				id: '',
				foodName: '',
				foodType: '',
				price: 0,
				isHalal: 0,
				foodImg: ''
			}
		},

		// 食品表单验证规则
		// 1. 修改验证规则，移除 foodImg 的 required 校验，改为在 saveFood 中逻辑判断
			foodRules: {
				foodName: [{ required: true, message: '请输入食品名称', trigger: 'blur' }],
				foodType: [{ required: true, message: '请选择食品类型', trigger: 'change' }],
				price: [{ required: true, message: '请输入食品价格', trigger: 'blur' }]
				// foodImg 移除必填，避免上传前校验阻断
			},

		// 临时存储上传的文件
		uploadFile: null
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
		return process.env.VUE_APP_IMG_URL + '/' + foodImg;
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
	},
	 // 点击添加食品按钮
	 addFood() {
      this.foodDialog.tops = '添加食品';
      this.foodDialog.dialogVisible = true;
      this.foodDialog.item = {
        id: '',
        foodName: '',
        foodType: '',
        price: 0,
        isHalal: 0,
        foodImg: ''
      };
      this.uploadFile = null;
    },
    
    // 关闭食品对话框
    handleFoodClose() {
      this.foodDialog.dialogVisible = false;
      this.resetForm('foodForm');
    },
    
    // 取消食品添加
    cancelFood() {
      this.handleFoodClose();
    },
    
    // 图片上传前的验证
    beforeFoodUpload(file) {
      // 验证文件类型
      const validType = ['image/png', 'image/jpeg', 'image/jpg','image/gif'].includes(file.type);
      if (!validType) {
        this.$message.error('仅支持 PNG/JPG/JPEG 格式图片！');
        return false;
      }
      
      // 验证文件大小（2MB）
      const validSize = file.size / 1024 / 1024 < 2;
      if (!validSize) {
        this.$message.error('图片大小不能超过 2MB！');
        return false;
      }
      
      return true;
    },
    
    // 文件变化处理
    handleFileChange(file) {
      this.uploadFile = file.raw;
      // 生成预览图
      const reader = new FileReader();
      reader.readAsDataURL(file.raw);
      reader.onload = (e) => {
        this.foodDialog.item.foodImg = e.target.result;
      };
    },
    
    // 自定义图片上传
    uploadFoodImage(options) {
		return new Promise((resolve, reject) => {
				const formData = new FormData();
				formData.append('file', options.file);
				console.log('上传文件:', options.file);
        		console.log('FormData 内容:', formData.get('file'));

				uploadFoodImg(formData).then(res => {
					if (res.flag || res.code === 200) {
						this.foodDialog.item.foodImg = res.data;
						this.$message.success('图片上传成功');
						resolve(res);
					} else {
						this.$message.error(res.message || res.msg);
						reject(res);
					}
				}).catch(err => {
					console.error('上传错误:', err);
					this.$message.error('图片上传失败');
					reject(err);
				});
			});
    },
    
    // 保存食品
    saveFood(formName) {
		this.$refs[formName].validate(valid => {
    if (valid) {
      // 如果有新选中的文件，先上传
      if (this.uploadFile) {
        const formData = new FormData();
        formData.append('file', this.uploadFile);

        uploadFoodImg(formData).then(uploadRes => {
          if (uploadRes.flag || uploadRes.code === 200) {
            const fileName = uploadRes.data || uploadRes.message;
            if (!fileName) {
              this.$message.error('未获取到图片文件名');
              return;
            }
            this.foodDialog.item.foodImg = fileName;
            this.submitFoodForm();
          } else {
            this.$message.error(uploadRes.message || '图片上传失败');
          }
        }).catch(() => {
          this.$message.error('图片上传失败');
        });
      } else {
        // 如果是编辑且未更换图片，直接提交（保留原图片路径）
        // 如果是新增且没图片，这里可以根据业务需求拦截
        if (!this.foodDialog.item.id && !this.foodDialog.item.foodImg) {
           this.$message.error('请上传食品图片');
           return;
        }
        this.submitFoodForm();
      }
    }
  });
    },
    
    // 提交食品表单
    submitFoodForm() {
		// 确保 foodImg 有值
		if (!this.foodDialog.item.foodImg) {
			this.$message.error('图片尚未上传成功，请等待上传完成后再保存');
			return;
		}
      const api = this.foodDialog.item.id ? updateFood : addFood;
      api(this.foodDialog.item).then(res => {
        if (res.flag) {
          this.$message.success(res.message);
          this.handleFoodClose();
          this.getFoodList(); // 刷新食品列表
        } else {
          this.$message.error(res.message);
        }
      });
    }
  }
  
};

</script>

<style scoped >
/* 图片上传样式 */
.food-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 200px;
}

.food-uploader:hover {
  border-color: #409EFF;
}

.food-preview {
  width: 200px;
  height: 200px;
  object-fit: cover;
  display: block;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

.el-upload__text {
  text-align: center;
  font-size: 12px;
  color: #666;
  padding: 10px;
}

.upload-tip {
  color: #909399;
  font-size: 11px;
  line-height: 1.5;
}

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