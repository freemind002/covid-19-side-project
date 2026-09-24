<!-- src/App.vue -->
<template>
  <!-- 這裡就像一個動態視窗，當網址切換時，Vue Router 會自動把對應的 View 塞進這裡 -->
  <router-view />
</template>

<script setup>
// App.vue 變得非常簡單，通常只需要放共用的全域樣式或初始化邏輯
</script>

<style>
/* 全域共用樣式可以放這裡 */
body {
  margin: 0;
  font-family: Arial, sans-serif;
  background-color: #f4f6f8;
}
</style>