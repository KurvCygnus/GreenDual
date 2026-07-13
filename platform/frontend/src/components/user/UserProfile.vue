<template>
  <el-dialog v-model="visible" title="个人中心" width="600px" :close-on-click-modal="false">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="个人信息" name="info">
        <el-descriptions :column="1" border class="profile-desc">
          <el-descriptions-item label="用户名">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-avatar :size="32" :icon="UserFilled" />
              <span>{{ profile.username }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="roleTagType">{{ profile.roleName }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ profile.userId || "--" }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ profile.createTime || "--" }}
          </el-descriptions-item>
          <el-descriptions-item label="最后登录">
            {{ profile.lastLoginTime || "--" }}
          </el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-form
          :model="passwordForm"
          :rules="passwordRules"
          ref="passwordFormRef"
          label-width="100px"
          style="max-width: 450px; margin: 20px auto"
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请确认新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading">
              确认修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import { UserFilled } from "@element-plus/icons-vue";
import { resetPassword } from "@/api/auth";

const props = defineProps({
  modelValue: Boolean,
});
const emit = defineEmits(["update:modelValue"]);

const visible = ref(false);
const activeTab = ref("info");
const passwordFormRef = ref(null);
const passwordLoading = ref(false);

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val;
    if (val) {
      loadProfile();
    }
  }
);

watch(visible, (val) => {
  emit("update:modelValue", val);
  if (!val) {
    activeTab.value = "info";
    resetPasswordForm();
  }
});

const profile = reactive({
  username: "",
  userId: null,
  roleName: "",
  roleLevel: 1,
  createTime: "",
  lastLoginTime: "",
});

const roleTagType = (() => {
  const map = { 1: "info", 2: "warning", 3: "danger" };
  return (level) => map[level] || "info";
})();

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 3, max: 20, message: "密码长度在 3 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

const roleNameMap = { 1: "观察员", 2: "运维工程师", 3: "系统管理员" };

const loadProfile = () => {
  profile.username = localStorage.getItem("username") || "未知";
  profile.userId = localStorage.getItem("userId") || "--";
  const roleLevel = parseInt(localStorage.getItem("roleId")) || 1;
  profile.roleLevel = roleLevel;
  profile.roleName = roleNameMap[roleLevel] || "未知";
};

const resetPasswordForm = () => {
  passwordForm.oldPassword = "";
  passwordForm.newPassword = "";
  passwordForm.confirmPassword = "";
};

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return;
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true;
      try {
        const userId = localStorage.getItem("userId");
        const response = await resetPassword(userId, passwordForm.newPassword);
        if (response.data.code === 1) {
          ElMessage.success("密码修改成功");
          resetPasswordForm();
          activeTab.value = "info";
        } else {
          ElMessage.error(response.data.msg || "密码修改失败");
        }
      } catch (error) {
        ElMessage.error("密码修改失败");
        console.error(error);
      } finally {
        passwordLoading.value = false;
      }
    }
  });
};
</script>

<style scoped>
.profile-desc {
  margin: 10px 0;
}
</style>
