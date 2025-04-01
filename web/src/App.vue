<template>
  <!-- 移除原有a-layout的style绑定 -->
  <a-layout id="app-container">
    <the-header></the-header>
    <router-view/>
    <the-footer></the-footer>
  </a-layout>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import TheHeader from '@/components/the-header.vue';
  import TheFooter from '@/components/the-footer.vue';

  export default defineComponent({
    name: 'app',
    components: {
      TheHeader,
      TheFooter,
    },
  });
</script>

<style scoped>/* 添加这个样式块 */
#app-container {
  background: transparent !important;
}
</style>