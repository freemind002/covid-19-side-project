import { defineStore } from "pinia";
import { ref } from "vue";

export const useAuthStore = defineStore("auth", () => {
  // 從 localStorage 初始化 Token，重整網頁才不會立刻登出
  const token = ref(localStorage.getItem("jwt_token") || "");
  const role = ref(localStorage.getItem("user_role") || "");

  // 登入成功後呼叫此方法儲存資料
  const setLoginData = (newToken, newRole) => {
    token.value = newToken;
    role.value = newRole;
    localStorage.setItem("jwt_token", newToken);
    localStorage.setItem("user_role", newRole);
  };

  // 登出時清空
  const logout = () => {
    token.value = "";
    role.value = "";
    localStorage.removeItem("jwt_token");
    localStorage.removeItem("user_role");
  };

  return { token, role, setLoginData, logout };
});
