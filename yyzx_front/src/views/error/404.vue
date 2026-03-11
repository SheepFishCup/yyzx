<template>
    <div class="error-container">
      <!-- 根据图片实际存放路径调整 src -->
      <img src="@/assets/404.jpg" alt="404 Not Found" class="error-image" />
      <h1 class="error-text">页面走丢了</h1>
    </div>
  </template>
  
  <script>
  export default {
    name: '404'
  }
  </script>
  
  <style scoped>
  .error-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
  }
  .error-image {
    max-width: 400px;
    height: auto;
  }
  .error-text {
    margin-top: 20px;
    color: #666;
  }
  </style>