<template>
  <div class="covid-container">
    <h2>COVID-19 數據儀表板</h2>

    <!-- 0. 頂部總覽卡片(Summary 和 Daily) -->
    <div class="summary-container">
      <div class="summary-card">
        <h4>確診累計總數 (Summary)</h4>
        <p class="summary-value">{{ summaryData.totalConfirmed.toLocaleString() }}</p>
      </div>
      <div class="summary-card">
        <h4>最近日期當日確診 (Daily)</h4>
        <p class="summary-value">{{ summaryData.latestConfirmedDaily.toLocaleString() }}</p>
      </div>
      <div class="summary-card">
        <h4>死亡累計總數 (Summary)</h4>
        <p class="summary-value">{{ summaryData.totalDeaths.toLocaleString() }}</p>
      </div>
      <div class="summary-card">
        <h4>最近日期當日死亡 (Daily)</h4>
        <p class="summary-value">{{ summaryData.latestDeathsDaily.toLocaleString() }}</p>
      </div>
    </div>

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

    <!-- 2. 資料表格 (分頁與排序) -->
    <table class="data-table">
      <thead>
        <tr>
          <th @click="handleSort('updatedOn')">
            更新日期 <span v-html="getSortIcon('updatedOn')"></span>
          </th>
          <th @click="handleSort('countryRegion')">
            國家/地區 <span v-html="getSortIcon('countryRegion')"></span>
          </th>
          <th @click="handleSort('provinceState')">
            省份/州 <span v-html="getSortIcon('provinceState')"></span>
          </th>
          <th @click="handleSort('confirmedDaily')">
            當日確診 <span v-html="getSortIcon('confirmedDaily')"></span>
          </th>
          <th @click="handleSort('confirmedCumulative')">
            確診累計 <span v-html="getSortIcon('confirmedCumulative')"></span>
          </th>
          <th @click="handleSort('deathsDaily')">
            當日死亡 <span v-html="getSortIcon('deathsDaily')"></span>
          </th>
          <th @click="handleSort('deathsCumulative')">
            死亡累計 <span v-html="getSortIcon('deathsCumulative')"></span>
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

// 搜尋參數
const searchParams = reactive({
  startDate: '2020-01-01',
  endDate: '2020-03-01',
  geographyIds: ''
})

// 頂部總覽卡片 狀態資料
const summaryData = reactive({
  totalConfirmed: 0,
  latestConfirmedDaily: 0,
  totalDeaths: 0,
  latestDeathsDaily: 0
})

// 分頁與表格資料狀態
const tableData = ref([])
const loading = ref(false)
const errorMessage = ref('')

const pageInfo = reactive({
  number: 0,    // 當前頁碼 (對應後端 page=0)
  size: 10,     // 每頁筆數
  totalPages: 0,  // 總頁數
  totalElements: 0, // 總筆數
  first: true,
  last: true
})

// 排序狀態追蹤
const sorts = ref([])

// 💡 呼叫專屬 API 取得頂部 頂部總覽卡片 數據 (整合 Summary 與 Daily API)
const fetchSummaryData = async () => {
  try {
    let geoIdsParam = searchParams.geographyIds.trim() !== '' ? searchParams.geographyIds : undefined
    const targetDate = searchParams.endDate || '2020-03-01' // 預設以結束日期作為單日查詢基準

    // 同時發送 4 個專屬 API 請求
    const [confSumRes, deathSumRes, confDailyRes, deathDailyRes] = await Promise.all([
      axios.get('/api/confirmed/summary', {
        params: { startDate: searchParams.startDate, endDate: searchParams.endDate, geographyIds: geoIdsParam },
        paramsSerializer: { indexes: null }
      }),
      axios.get('/api/deaths/summary', {
        params: { startDate: searchParams.startDate, endDate: searchParams.endDate, geographyIds: geoIdsParam },
        paramsSerializer: { indexes: null }
      }),
      axios.get('/api/confirmed/daily', {
        params: { date: targetDate, geographyIds: geoIdsParam },
        paramsSerializer: { indexes: null }
      }),
      axios.get('/api/deaths/daily', {
        params: { date: targetDate, geographyIds: geoIdsParam },
        paramsSerializer: { indexes: null }
      })
    ])

    // 對應後端回傳的數值 
    summaryData.totalConfirmed = confSumRes.data.totalDailySum ?? confSumRes.data.sum ?? 0
    summaryData.totalDeaths = deathSumRes.data.totalDailySum ?? deathSumRes.data.sum ?? 0
    summaryData.latestConfirmedDaily = confDailyRes.data.dailySum ?? confDailyRes.data.value ?? 0
    summaryData.latestDeathsDaily = deathDailyRes.data.dailySum ?? deathDailyRes.data.value ?? 0

  } catch (error) {
    console.error('取得 總覽數據失敗:', error)
  }
}

// 💡 取得分頁表格資料 API 呼叫
const fetchTableData = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    let geoIdsParam = null
    if (searchParams.geographyIds.trim() !== '') {
      geoIdsParam = searchParams.geographyIds
        .split(',')
        .map(id => id.trim())
        .filter(id => id !== '')
        .join(',')
    }

    const sortParams = sorts.value.map(s => `${s.field},${s.direction}`)

    const response = await axios.get('/api/covid/table', {
      params: {
        startDate: searchParams.startDate,
        endDate: searchParams.endDate,
        geographyIds: geoIdsParam || undefined,
        page: pageInfo.number,
        size: pageInfo.size,
        sort: sortParams.length > 0 ? sortParams : undefined
      },
      paramsSerializer: { indexes: null }
    })

    const data = response.data
    tableData.value = data.content || data.data || []

    if (data.page) {
      pageInfo.number = data.page.number ?? 0
      pageInfo.size = data.page.size ?? 10
      pageInfo.totalPages = data.page.totalPages ?? 0
      pageInfo.totalElements = data.page.totalElements ?? 0

      pageInfo.first = data.page.number === 0
      pageInfo.last = data.page.number >= pageInfo.totalPages - 1
    }

  } catch (error) {
    console.error('API 請求失敗:', error)
    if (error.response && error.response.data) {
      errorMessage.value = error.response.data.message || '發生未知錯誤'
    } else {
      errorMessage.value = '無法連線至伺服器，請檢查網路或後端狀態'
    }
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 點擊查詢按鈕時的處理函式
const handleSearch = () => {
  pageInfo.number = 0 // 每次按查詢，強制回到第一頁！
  sorts.value = []    // 💡 新增這行：按查詢時清空所有排序條件
  fetchTableData()         // 重新發送表格 API 請求
  fetchSummaryData()      // 同步更新 頂部總覽卡片 數據
}

// 換頁動作
const changePage = (newPage) => {
  if (newPage < 0 || newPage >= pageInfo.totalPages) return
  pageInfo.number = newPage
  fetchTableData()
}

// 改變每頁筆數
const handleSizeChange = () => {
  pageInfo.number = 0
  fetchTableData()
}

// 點擊表頭排序
const handleSort = (field) => {
  const existing = sorts.value.find(s => s.field === field)
  if (existing) {
    if (existing.direction === 'asc') {
      existing.direction = 'desc'
    } else {
      sorts.value = sorts.value.filter(s => s.field !== field)
    }
  } else {
    sorts.value.push({ field, direction: 'asc' })
  }
  fetchTableData()
}

// 顯示排序圖示
const getSortIcon = (field) => {
  const found = sorts.value.find(s => s.field === field)
  if (!found) return '↕'
  return found.direction === 'asc' ? '▲' : '▼'
}

// 初始化載入
onMounted(() => {
  fetchTableData()
  fetchSummaryData()
})
</script>

<style scoped>
.covid-container {
  max-width: 1200px;
  margin: 20px auto;
  font-family: Arial, sans-serif;
  padding: 20px;
}

/* 頂部總覽卡片 樣式 */
.summary-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.summary-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  text-align: center;
}

.summary-card h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #6c757d;
}

.summary-value {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
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
