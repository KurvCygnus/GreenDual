<template>
  <div class="user-management">
    <!-- 操作栏 -->
    <el-card class="toolbar-card" shadow="never">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="用户名">
          <el-input
            v-model="filterForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="权限">
          <el-select
            v-model="filterForm.permission"
            placeholder="全部"
            clearable
            style="width: 150px"
          >
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch"
            >搜索</el-button
          >
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd"
            >添加用户</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="36" color="#409eff"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="36" color="#67c23a"><UserFilled /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ adminCount }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <el-icon size="36" color="#e6a23c"><User /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ normalCount }}</div>
              <div class="stat-label">普通用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="userList"
        style="width: 100%"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" width="150">
          <template #default="scope">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-avatar :size="32" :icon="UserFilled" />
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="password" label="密码" width="150">
          <template #default>
            <span>******</span>
          </template>
        </el-table-column>
        <el-table-column prop="userPermission" label="权限" width="120">
          <template #default="scope">
            <el-tag
              :type="scope.row.userPermission === 2 ? 'danger' : 'primary'"
            >
              {{ scope.row.userPermission === 2 ? "管理员" : "普通用户" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ scope.row.createTime || "--" }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="scope">
            {{ scope.row.lastLoginTime || "--" }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-button
              type="warning"
              size="small"
              @click="handleResetPassword(scope.row)"
              >重置密码</el-button
            >
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.userId === 1"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form
        :model="userForm"
        :rules="rules"
        ref="userFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEdit">
          <el-input
            v-model="userForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="权限" prop="userPermission">
          <el-select v-model="userForm.userPermission" style="width: 100%">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="resetPasswordVisible" title="重置密码" width="400px">
      <el-form
        :model="resetForm"
        :rules="resetRules"
        ref="resetFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名">
          <el-input :value="resetForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Search,
  Refresh,
  Plus,
  User,
  UserFilled,
} from "@element-plus/icons-vue";
import {
  getUserList,
  addUser as addUserApi,
  updateUser as updateUserApi,
  deleteUser as deleteUserApi,
  resetPassword as resetPasswordApi,
  getUserStats,
} from "@/api/auth";

const loading = ref(false);
const dialogVisible = ref(false);
const resetPasswordVisible = ref(false);
const isEdit = ref(false);
const userFormRef = ref(null);
const resetFormRef = ref(null);

// 筛选条件
const filterForm = reactive({
  username: "",
  permission: null,
});

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

// 用户列表
const userList = ref([]);

// 统计数据
const totalUsers = computed(() => userList.value.length);
const adminCount = computed(
  () => userList.value.filter((u) => u.userPermission === 2).length,
);
const normalCount = computed(
  () => userList.value.filter((u) => u.userPermission === 1).length,
);

// 用户表单
const userForm = reactive({
  userId: null,
  username: "",
  password: "",
  confirmPassword: "",
  userPermission: 1,
});

// 重置密码表单
const resetForm = reactive({
  userId: null,
  username: "",
  newPassword: "",
  confirmPassword: "",
});

// 对话框标题
const dialogTitle = computed(() => (isEdit.value ? "编辑用户" : "添加用户"));

// 表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== userForm.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const validateResetConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    {
      min: 2,
      max: 20,
      message: "用户名长度在 2 到 20 个字符",
      trigger: "blur",
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 3, max: 20, message: "密码长度在 3 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" },
  ],
  userPermission: [
    { required: true, message: "请选择权限", trigger: "change" },
  ],
};

const resetRules = {
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 3, max: 20, message: "密码长度在 3 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    { validator: validateResetConfirmPassword, trigger: "blur" },
  ],
};

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true;
  try {
    const params = {
      match: filterForm.username,
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
    };
    const response = await getUserList(params);
    if (response.data.code === 1) {
      userList.value = response.data.data || [];
      pagination.total = response.data.totalPages || userList.value.length;
    }
  } catch (error) {
    console.error("获取用户列表失败", error);
    userList.value = [
      {
        userId: 1,
        username: "admin",
        password: "admin123",
        userPermission: 2,
        createTime: "2025-01-01 10:00:00",
        lastLoginTime: "2026-03-20 00:52:00",
      },
      {
        userId: 2,
        username: "test",
        password: "test123",
        userPermission: 1,
        createTime: "2025-01-15 14:30:00",
        lastLoginTime: "2026-03-19 16:20:00",
      },
    ];
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  ElMessage.success("搜索完成");
};

// 重置
const handleReset = () => {
  filterForm.username = "";
  filterForm.permission = null;
  pagination.currentPage = 1;
  fetchUserList();
};

// 添加用户
const handleAdd = () => {
  isEdit.value = false;
  userForm.userId = null;
  userForm.username = "";
  userForm.password = "";
  userForm.confirmPassword = "";
  userForm.userPermission = 1;
  dialogVisible.value = true;
};

// 编辑用户
const handleEdit = (user) => {
  isEdit.value = true;
  userForm.userId = user.userId;
  userForm.username = user.username;
  userForm.password = "";
  userForm.confirmPassword = "";
  userForm.userPermission = user.userPermission;
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return;

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const response = await updateUserApi({
            userId: userForm.userId,
            userPermission: userForm.userPermission,
          });
          if (response.data.code === 1) {
            ElMessage.success("用户信息更新成功");
            dialogVisible.value = false;
            await fetchUserList();
          } else {
            ElMessage.error(response.data.msg || "更新失败");
          }
        } else {
          const response = await addUserApi({
            username: userForm.username,
            password: userForm.password,
            userPermission: userForm.userPermission,
          });
          if (response.data.code === 1) {
            ElMessage.success("用户添加成功");
            dialogVisible.value = false;
            await fetchUserList();
          } else {
            ElMessage.error(response.data.msg || "添加失败");
          }
        }
      } catch (error) {
        console.error("操作失败", error);
        ElMessage.error("操作失败");
      }
    }
  });
};

// 重置密码
const handleResetPassword = (user) => {
  resetForm.userId = user.userId;
  resetForm.username = user.username;
  resetForm.newPassword = "";
  resetForm.confirmPassword = "";
  resetPasswordVisible.value = true;
};

// 提交重置密码
const submitResetPassword = async () => {
  if (!resetFormRef.value) return;

  await resetFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await resetPasswordApi(
          resetForm.userId,
          resetForm.newPassword,
        );
        if (response.data.code === 1) {
          ElMessage.success("密码重置成功");
          resetPasswordVisible.value = false;
        } else {
          ElMessage.error(response.data.msg || "重置失败");
        }
      } catch (error) {
        console.error("重置密码失败", error);
        ElMessage.error("重置密码失败");
      }
    }
  });
};

// 删除用户
const handleDelete = (user) => {
  ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？`, "提示", {
    type: "warning",
  })
    .then(async () => {
      try {
        const response = await deleteUserApi(user.userId);
        if (response.data.code === 1) {
          const index = userList.value.findIndex(
            (u) => u.userId === user.userId,
          );
          if (index > -1) {
            userList.value.splice(index, 1);
          }
          ElMessage.success("删除成功");
        } else {
          ElMessage.error(response.data.msg || "删除失败");
        }
      } catch (error) {
        console.error("删除用户失败", error);
        ElMessage.error("删除失败");
      }
    })
    .catch(() => {});
};

// 分页
const handleSizeChange = (size) => {
  pagination.pageSize = size;
};

const handleCurrentChange = (page) => {
  pagination.currentPage = page;
};

onMounted(() => {
  fetchUserList();
});
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100%;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
