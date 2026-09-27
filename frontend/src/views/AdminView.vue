<template>
    <div class="admin-container">
        <h2>使用者管理後台 - 建立 / 更新使用者 (支援多重角色)</h2>

        <form @submit.prevent="handleSubmit" class="user-form">
            <div class="form-group">
                <label>使用者帳號 (User Name)</label>
                <input type="text" v-model="form.userName" required placeholder="請輸入帳號" />
            </div>

            <div class="form-group">
                <label>密碼 (Password)</label>
                <input type="password" v-model="form.password" required placeholder="請輸入密碼" />
            </div>

            <div class="form-group">
                <label>電子郵件 (Email)</label>
                <input type="email" v-model="form.email" required placeholder="請輸入 Email" />
            </div>

            <div class="form-group">
                <label>帳號狀態 (Is Active)</label>
                <select v-model="form.isActive" required>
                    <option :value="true">啟用 (Active)</option>
                    <option :value="false">停用 (Inactive)</option>
                </select>
            </div>

            <!-- 💡 改為複選框 (Checkbox Group) -->
            <div class="form-group">
                <label>使用者角色 (可複選)</label>
                <div class="checkbox-group">
                    <div v-for="role in roleList" :key="role.id" class="checkbox-item">
                        <label>
                            <!-- 綁定同一個陣列 form.roleIds，Vue 會自動處理勾選與取消勾選的 id -->
                            <input type="checkbox" :value="role.id" v-model="form.roleIds" />
                            {{ role.role_name }}
                        </label>
                    </div>
                </div>
            </div>

            <button type="submit" class="submit-btn">儲存 / 更新使用者</button>
            <p v-if="message" class="message">{{ message }}</p>
        </form>
    </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'

const form = reactive({
    userName: '',
    password: '',
    email: '',
    isActive: true,
    roleIds: [] // 👈 改為陣列型態來接複選結果
})

const roleList = ref([])
const message = ref('')

// 抓取所有可用角色
const fetchRoles = async () => {
    try {
        const response = await axios.get('/api/roles')
        roleList.value = response.data
    } catch (error) {
        console.error('無法取得角色清單', error)
    }
}

// 提交表單
const handleSubmit = async () => {
    message.value = ''
    try {
        await axios.post('/api/admin/users', form)
        message.value = '儲存成功！'
        // 清空表單
        form.userName = ''
        form.password = ''
        form.email = ''
        form.roleIds = [] // 清空陣列
    } catch (error) {
        console.error('儲存失敗', error)
        message.value = '儲存失敗，請檢查權限或輸入資料'
    }
}

onMounted(() => {
    fetchRoles()
})
</script>

<style scoped>
.admin-container {
    max-width: 500px;
    margin: 40px auto;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.user-form {
    display: flex;
    flex-direction: column;
    gap: 15px;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.form-group input[type="text"],
.form-group input[type="password"],
.form-group input[type="email"],
.form-group select {
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.checkbox-group {
    display: flex;
    gap: 15px;
    margin-top: 5px;
}

.checkbox-item label {
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
}

.submit-btn {
    padding: 10px;
    background: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.submit-btn:hover {
    background: #218838;
}

.message {
    color: green;
    font-weight: bold;
    text-align: center;
}
</style>