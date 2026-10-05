import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import AdminView from "../views/AdminView.vue";
import CovidView from "../views/CovidView.vue";
import DashboardView from "../views/DashboardView.vue";
import LoginView from "../views/LoginView.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/login", name: "Login", component: LoginView },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: DashboardView,
    meta: { requiresAuth: true }, // 👈 標記這個頁面需要登入
  },
  {
    path: "/admin/users",
    name: "AdminUsers",
    component: AdminView,
    meta: { requiresAuth: true, requiresAdmin: true }, // 👈 標記需要 Admin 權限
  },
  {
    path: "/covid",
    name: "CovidView",
    component: CovidView,
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 💡 導航守衛 (Navigation Guard)
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const isLoggedIn = !!authStore.token;

  // 1. 如果頁面需要登入，但使用者沒登入 -> 踢去 /login
  if (to.meta.requiresAuth && !isLoggedIn) {
    next("/login");
  }
  // 2. 如果使用者已經登入，還想去 /login -> 導回 /dashboard
  else if (to.path === "/login" && isLoggedIn) {
    next("/dashboard");
  }
  // 3. 如果需要 Admin 權限，但身分不符 -> 擋下並導回儀表板
  else if (to.meta.requiresAdmin && authStore.role !== "ADMIN") {
    alert("權限不足，無法進入管理後台");
    next("/dashboard");
  } else {
    next(); // 放行
  }
});

export default router;
