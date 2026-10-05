<template>
    <div class="covid-dashboard">
        <h2>COVID-19 疫情數據查詢系統</h2>

        <!-- 查詢控制面板 -->
        <div class="filter-bar">
            <!-- 1. 國家/地區下拉選單 -->
            <div class="input-group">
                <label>選擇地區：</label>
                <select v-model="selectedGeoId">
                    <option disabled value="">請選擇國家或省份</option>
                    <option v-for="geo in geographyList" :key="geo.id" :value="geo.id">
                        {{ geo.countryRegion }} {{ geo.provinceState ? ' (' + geo.provinceState + ')' : '' }}
                    </option>
                </select>
            </div>

            <!-- 2. 開始日期 -->
            <div class="input-group">
                <label>開始日期：</label>
                <input type="date" v-model="startDate" />
            </div>

            <!-- 3. 結束日期 -->
            <div class="input-group">
                <label>結束日期：</label>
                <input type="date" v-model="endDate" />
            </div>

            <!-- 4. 查詢按鈕 -->
            <button @click="handleSearch" :disabled="!startDate || !endDate">
                查詢數據
            </button>
        </div>

        <!-- 查詢結果呈現表格 -->
        <div v-if="records.length > 0" class="table-container">
            <h3>查詢結果 (共 {{ totalElements }} 筆)</h3>
            <table class="result-table">
                <thead>
                    <tr>
                        <th>日期</th>
                        <th>地區</th>
                        <th>確診新增</th>
                        <th>確診累積</th>
                        <th>死亡新增</th>
                        <th>死亡累積</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in records" :key="index">
                        <td>{{ item.updatedOn }}</td>
                        <td>{{ item.countryRegion }}{{ item.provinceState ? ' - ' + item.provinceState : '' }}</td>
                        <td>{{ item.confirmedDaily?.toLocaleString() ?? 0 }}</td>
                        <td>{{ item.confirmedCumulative?.toLocaleString() ?? 0 }}</td>
                        <td>{{ item.deathsDaily?.toLocaleString() ?? 0 }}</td>
                        <td>{{ item.deathsCumulative?.toLocaleString() ?? 0 }}</td>
                    </tr>
                </tbody>
            </table>

            <!-- 分頁控制列 -->
            <div class="pagination-bar">
                <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0">上一頁</button>
                <span>第 {{ currentPage + 1 }} 頁 / 共 {{ totalPages || 1 }} 頁</span>
                <button @click="changePage(currentPage + 1)" :disabled="currentPage + 1 >= totalPages">下一頁</button>
            </div>
        </div>

        <!-- 查無資料或未查詢的提示 -->
        <p v-else class="no-data">請選擇日期區間並點擊查詢，或該區間無數據。</p>
    </div>
</template>

<script setup>
import axios from 'axios';
import { onMounted, ref } from 'vue';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore()

// 狀態宣告
const geographyList = ref([]) // 儲存國家清單
const selectedGeoId = ref('') // 使用者選中的地理 ID（可為空代表全部）
const startDate = ref('')     // 開始日期
const endDate = ref('')       // 結束日期

// 分頁與表格資料狀態
const records = ref([])       // 當前頁面的表格資料
const currentPage = ref(0)    // 當前頁碼 (從 0 開始)
const pageSize = ref(15)      // 每頁筆數
const totalPages = ref(0)     // 總頁數
const totalElements = ref(0)  // 總筆數

// 1. 畫面載入時（onMounted），自動去後端撈取國家清單
onMounted(async () => {
    try {
        const response = await axios.get('/api/covid/operator/geographies', {
            headers: {
                Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
            }
        })
        geographyList.value = response.data
    } catch (error) {
        console.error('無法取得國家/地區清單：', error)
        alert('載入地區清單失敗，請確認後端伺服器或登入狀態是否正常。')
    }
})

// 2. 點擊查詢按鈕時觸發（重設為第 0 頁並載入資料）
const handleSearch = () => {
    currentPage.value = 0
    fetchTableData()
}

// 3. 呼叫後端新的分頁表格 API
const fetchTableData = async () => {
    try {
        const params = {
            startDate: startDate.value,
            endDate: endDate.value,
            page: currentPage.value,
            size: pageSize.value
        }

        if (selectedGeoId.value !== '') {
            params.geographyIds = [Number(selectedGeoId.value)]
        }

        const response = await axios.get('/api/covid/operator/table-page', {
            headers: {
                Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
            },
            params
        })

        console.log("API 回傳結果：", response.data)

        // 修正這裡：根據你的截圖結構，資料在 content，分頁資訊在 page 物件裡
        records.value = response.data.content || []

        // 判斷 page 物件是否存在，避免報錯
        if (response.data.page) {
            totalPages.value = response.data.page.totalPages || 0
            totalElements.value = response.data.page.totalElements || 0
        } else {
            // 以防萬一後端有時候直接回傳根目錄
            totalPages.value = response.data.totalPages || 0
            totalElements.value = response.data.totalElements || 0
        }

    } catch (error) {
        console.error('查詢分頁疫情數據失敗：', error)
        alert('查詢失敗，請檢查日期格式或參數。')
    }
}

// 4. 切換頁碼函式
const changePage = (newPage) => {
    if (newPage >= 0 && newPage < totalPages.value) {
        currentPage.value = newPage
        fetchTableData()
    }
}
</script>

<style scoped>
.covid-dashboard {
    max-width: 900px;
    margin: 40px auto;
    font-family: sans-serif;
    padding: 20px;
    background: #f9f9f9;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-bar {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    align-items: flex-end;
    margin-bottom: 25px;
    background: #fff;
    padding: 15px;
    border-radius: 6px;
}

.input-group {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.input-group select,
.input-group input {
    padding: 8px;
    font-size: 14px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

button {
    padding: 9px 20px;
    background-color: #2b6cb0;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    height: 38px;
}

button:disabled {
    background-color: #cbd5e0;
    cursor: not-allowed;
}

button:hover:not(:disabled) {
    background-color: #2c5282;
}

.table-container {
    background: #fff;
    padding: 15px;
    border-radius: 6px;
}

.result-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 10px;
}

.result-table th,
.result-table td {
    border: 1px solid #e2e8f0;
    padding: 10px;
    text-align: center;
}

.result-table th {
    background-color: #edf2f7;
}

.pagination-bar {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    margin-top: 20px;
}

.no-data {
    text-align: center;
    color: #718096;
    margin-top: 20px;
}
</style>