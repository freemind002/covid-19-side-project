<template>
  <div class="covid-container">
    <h2>COVID-19 數據儀表板</h2>

    <!-- 1. 搜尋與篩選表單 -->
    <div class="filter-bar">
      <label>
        開始日期:
        <input type="date" v-model="searchParams.startDate" />
      </label>
      <label>
        結束日期:
        <input type="date" v-model="searchParams.endDate" />
      </label>
      <label>
        地區 IDs (逗號分隔):
        <input type="text" v-model="searchParams.geographyIds" placeholder="例: 1,2,3" />
      </label>
      <button @click="handleSearch">查詢</button>
    </div>

    <!-- 錯誤訊息提示框 -->
    <div v-if="errorMessage" class="error-banner">
      {{ errorMessage }}
    </div>

    <!-- 2. 資料表格 -->
    <table class="data-table">
      <thead>
        <tr>
          <th @click="handleSort('updated_on')">
            更新日期 <span v-html="getSortIcon('updated_on')"></span>
          </th>
          <th @click="handleSort('country_region')">國家/地區 <span v-html="getSortIcon('country_region')"></span></th>
          <th @click="handleSort('province_state')">省份/州 <span v-html="getSortIcon('province_state')"></span></th>
          <th @click="handleSort('confirmed_daily')">
            當日確診 <span v-html="getSortIcon('confirmed_daily')"></span>
          </th>
          <th @click="handleSort('confirmed_cumulative')">
            確診累計 <span v-html="getSortIcon('confirmed_cumulative')"></span>
          </th>
          <th @click="handleSort('deaths_daily')">
            當日死亡 <span v-html="getSortIcon('deaths_daily')"></span>
          </th>
          <th @click="handleSort('deaths_cumulative')">
            死亡累計 <span v-html="getSortIcon('deaths_cumulative')"></span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td colspan="7" class="loading">載入中...</td>
        </tr>
        <tr v-else-if="tableData.length === 0">
          <td colspan="7" class="no-data">查無相關數據</td>
        </tr>
        <tr v-for="(item, index) in tableData" :key="index">
          <td>{{ item.updatedOn }}</td>
          <td>{{ item.countryRegion || '-' }}</td>
          <td>{{ item.provinceState || '-' }}</td>
          <td>{{ item.confirmedDaily }}</td>
          <td>{{ item.confirmedCumulative }}</td>
          <td>{{ item.deathsDaily }}</td>
          <td>{{ item.deathsCumulative }}</td>
        </tr>
      </tbody>
    </table>

    <!-- 3. 分頁控制列 -->
    <div class="pagination-bar" v-if="pageInfo.totalPages > 0">
      <button :disabled="pageInfo.first" @click="changePage(pageInfo.number - 1)">上一頁</button>
      <span>
        第 {{ pageInfo.number + 1 }} 頁 / 共 {{ pageInfo.totalPages }} 頁 (總計 {{ pageInfo.totalElements }} 筆)
      </span>
      <button :disabled="pageInfo.last" @click="changePage(pageInfo.number + 1)">下一頁</button>

      <select v-model.number="pageInfo.size" @change="handleSizeChange">
        <option :value="10">每頁 10 筆</option>
        <option :value="20">每頁 20 筆</option>
        <option :value="50">每頁 50 筆</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'
// 點擊查詢按鈕時的處理函式
const handleSearch = () => {
  pageInfo.number = 0 // 💡 每次按查詢，強制回到第一頁！
  fetchData()         // 重新發送 API 請求
}
// 搜尋參數
const searchParams = reactive({
  startDate: '2020-01-01',
  endDate: '2020-03-01',
  geographyIds: ''
})

// 分頁與資料狀態
const tableData = ref([])
const loading = ref(false)
const errorMessage = ref('')

const pageInfo = reactive({
  number: 0,        // 當前頁碼 (對應後端 page=0)
  size: 10,         // 每頁筆數
  totalPages: 0,    // 總頁數
  totalElements: 0, // 總筆數
  first: true,
  last: true
})

// 排序狀態 (支援多重排序或單欄位狀態追蹤)
// 格式: [{ field: 'updated_on', direction: 'desc' }]
const sorts = ref([])

// 取得資料 API 呼叫
const fetchData = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    // 處理 geographyIds 字串轉陣列 (例如 "1, 2, 3" -> [1, 2, 3])
    let geoIdsParam = null
    if (searchParams.geographyIds.trim() !== '') {
      geoIdsParam = searchParams.geographyIds
        .split(',')
        .map(id => id.trim())
        .filter(id => id !== '')
        .join(',') // Axios 陣列傳遞參數格式
    }

    // 組裝 Sort 參數格式 (Spring 接收的格式如: ?sort=updated_on,desc&sort=confirmed_daily,asc)
    const sortParams = sorts.value.map(s => `${s.field},${s.direction}`)

    const response = await axios.get('/api/covid/table', {
      params: {
        startDate: searchParams.startDate,
        endDate: searchParams.endDate,
        geographyIds: geoIdsParam || undefined,
        page: pageInfo.number,
        size: pageInfo.size,
        sort: sortParams.length > 0 ? sortParams : undefined // 👈 如果沒選排序就傳 undefined，交由 Spring Boot 預設處理
      },
      // 確保陣列參數可以用逗號正確序列化
      paramsSerializer: { indexes: null }
    })
    console.log('後端回傳的原始資料:', response.data)

    //對應 Spring Data 的 Page 結構回傳 (若有啟用 VIA_DTO 或預設 Page 結構)
    const data = response.data
    tableData.value = data.content || data.data || []
    // 💡 修正這裡：改從 data.page 裡面抓取分頁數據
    if (data.page) {
      pageInfo.number = data.page.number ?? 0
      pageInfo.size = data.page.size ?? 10
      pageInfo.totalPages = data.page.totalPages ?? 0
      pageInfo.totalElements = data.page.totalElements ?? 0

      // 判斷是否為第一頁或最後一頁，用來控制按鈕能不能點擊
      pageInfo.first = data.page.number === 0
      pageInfo.last = data.page.number >= pageInfo.totalPages - 1
    }

  } catch (error) {
    console.error('API 請求失敗:', error)
    if (error.response && error.response.data) {
      // 抓取後端 GlobalExceptionHandler 回傳的自定義 message
      errorMessage.value = error.response.data.message || '發生未知錯誤'
    } else {
      errorMessage.value = '無法連線至伺服器，請檢查網路或後端狀態'
    }
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 換頁動作
const changePage = (newPage) => {
  if (newPage < 0 || newPage >= pageInfo.totalPages) return
  pageInfo.number = newPage
  fetchData()
}

// 改變每頁筆數
const handleSizeChange = () => {
  pageInfo.number = 0 // 切換筆數時重置回第一頁
  fetchData()
}

// 點擊表頭排序
const handleSort = (field) => {
  const existing = sorts.value.find(s => s.field === field)
  if (existing) {
    if (existing.direction === 'asc') {
      existing.direction = 'desc'
    } else {
      // 如果已經是 desc，則移除排序
      sorts.value = sorts.value.filter(s => s.field !== field)
    }
  } else {
    // 新增排序，預設先 asc
    sorts.value.push({ field, direction: 'asc' })
  }
  fetchData()
}

// 顯示排序圖示
const getSortIcon = (field) => {
  const found = sorts.value.find(s => s.field === field)
  if (!found) return '↕'
  return found.direction === 'asc' ? '▲' : '▼'
}

// 初始化載入
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.covid-container {
  max-width: 1200px;
  margin: 20px auto;
  font-family: Arial, sans-serif;
  padding: 20px;
}

.filter-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.error-banner {
  background-color: #ffe6e6;
  color: #d9534f;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  border: 1px solid #ebccd1;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.data-table th,
.data-table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: center;
}

.data-table th {
  background-color: #f4f4f4;
  cursor: pointer;
  user-select: none;
}

.data-table th:hover {
  background-color: #e2e2e2;
}

.loading,
.no-data {
  color: #777;
  font-style: italic;
}

.pagination-bar {
  display: flex;
  gap: 15px;
  align-items: center;
  justify-content: center;
}

button {
  padding: 6px 12px;
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>