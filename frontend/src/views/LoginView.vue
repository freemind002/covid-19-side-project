<template>
    <div class="login-container">
        <div class="login-box">
            <h2>COVID-19 系統登入</h2>
            <form @submit.prevent="handleLogin">
                <div class="form-group">
                    <label>帳號</label>
                    <input type="text" v-model="form.username" required placeholder="請輸入帳號" />
                </div>
                <div class="form-group">
                    <label>密碼</label>
                    <input type="password" v-model="form.password" required placeholder="請輸入密碼" />
                </div>
                <button type="submit" class="login-btn">登入</button>
                <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
            </form>
        </div>
    </div>
</template>

<script setup>
import axios from 'axios'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
    username: '',
    password: ''
})

const errorMessage = ref('')

const handleLogin = async () => {
    errorMessage.value = ''
    try {
        // 呼叫後端登入 API (假設路徑為 /api/auth/login)
        const response = await axios.post('/api/auth/login', form)

        // 假設後端回傳格式為 { token: "xxx", role: "ADMIN" }
        const { token, role } = response.data

        // 存入 Pinia 與 localStorage
        authStore.setLoginData(token, role)

        // 登入成功，導向儀表板
        router.push('/dashboard')
    } catch (error) {
        console.error('登入失敗:', error)
        errorMessage.value = '帳號或密碼錯誤，請重新輸入'
    }
}
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: #f4f6f8;
}

.login-box {
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    width: 320px;
}

.form-group {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
}

.form-group input {
    padding: 8px;
    margin-top: 5px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.login-btn {
    width: 100%;
    padding: 10px;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.login-btn:hover {
    background: #0056b3;
}

.error {
    color: red;
    font-size: 14px;
    margin-top: 10px;
    text-align: center;
}
</style>