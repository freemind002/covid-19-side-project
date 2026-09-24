import axios from "axios";
import { createPinia } from "pinia"; // 👈 1. 一定要引入 createPinia
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import { useAuthStore } from "./stores/auth";

const app = createApp(App);
const pinia = createPinia(); // 👈 2. 建立 Pinia 實例

// 務必在挂載或使用 Store 之前，先註冊 pinia 和 router
app.use(pinia); // 👈 3. 註冊 Pinia
app.use(router); // 👈 4. 註冊 Router

// 請求攔截器
axios.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    if (authStore.token) {
      // 自動將 Token 塞入 Authorization Header
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

app.mount("#app");
