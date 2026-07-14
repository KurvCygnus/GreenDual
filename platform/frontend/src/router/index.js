import { createRouter, createWebHistory } from "vue-router";
import { ElMessage } from "element-plus";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: "/login",
    },
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },
    {
      path: "/home",
      name: "home",
      component: () => import("../views/HomeView.vue"),
    },
    {
      path: "/test",
      name: "test",
      component: () => import("../views/TestView.vue"),
    },
  ],
});

// 全局前置守卫 — 未登录用户重定向到 /login
const WHITE_LIST = ["/login"];
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");

  if (WHITE_LIST.includes(to.path)) {
    // 已登录用户访问登录页 → 跳首页
    if (token && to.path === "/login") {
      return next("/home");
    }
    return next();
  }

  // 访问受保护页面但没有 token → 跳登录
  if (!token) {
    ElMessage.warning("请先登录");
    return next("/login");
  }

  next();
});

// 全局后置守卫 — 路由加载错误处理
router.onError((error) => {
  console.error("[路由错误]", error);
  ElMessage.error({
    message: "页面加载失败，请刷新后重试",
    duration: 5000,
  });
});

export default router;
