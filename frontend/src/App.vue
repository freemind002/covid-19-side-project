<template>
  <div id="app">
    <!-- 頂部導覽列（這條橫幅在切換 /dashboard 和 /covid 時都會一直存在） -->
    <nav class="navbar">
      <router-link to="/dashboard">主儀表板</router-link> |
      <router-link to="/covid">COVID-19 資料修改</router-link>
    </nav>

    <!-- 這裡才是動態切換頁面的地方 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style>
.navbar {
  padding: 15px 20px;
  background-color: #2d3748;
  color: white;
}

.navbar a {
  color: white;
  text-decoration: none;
  margin-right: 15px;
  font-weight: bold;
}

.navbar a.router-link-exact-active {
  color: #63b3ed;
}

.main-content {
  padding: 20px;
}
</style>