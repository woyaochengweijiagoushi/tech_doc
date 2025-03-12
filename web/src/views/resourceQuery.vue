<template>
  <a-layout>
    <a-layout-content
      :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-select v-model:value="param.category" placeholder="资源分类" style="width: 200px">
              <a-select-option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-input v-model:value="param.name" placeholder="资源名称" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({ page: 1, size: pagination.pageSize })">
              查询
            </a-button>
          </a-form-item>
        </a-form>
      </p>
      <a-table
        :columns="columns"
        :row-key="record => record.id"
        :data-source="resources"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
      >
        <template v-slot:address="{ text }">
          <a-tooltip title="点击查看地址">
            <a-button type="link" @click="showAddress(text)">查看地址</a-button>
          </a-tooltip>
        </template>
      </a-table>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';

export default defineComponent({
  name: 'ResourceQuery',
  setup() {
    const param = ref({ category: null, name: '' });
    const resources = ref([]);
    const categories = ref([]);
    const pagination = ref({
      current: 1,
      pageSize: 10,
      total: 0
    });
    const loading = ref(false);

    const columns = [
      {
        title: '资源名称',
        dataIndex: 'name'
      },
      {
        title: '资源类型',
        dataIndex: 'type'
      },
      {
        title: '资源描述',
        dataIndex: 'description'
      },
      {
        title: '资源地址',
        dataIndex: 'address',
        slots: { customRender: 'address' }
      },
      {
        title: '解锁积分',
        dataIndex: 'unlockPoints'
      }
    ];

    const handleQuery = (params: any) => {
      loading.value = true;
      axios.get("/doc/list", {
        params: {
          page: params.page,
          size: params.size,
          category: param.value.category,
          name: param.value.name
        }
      }).then((response) => {
        loading.value = false;
        const data = response.data;
        if (data.success) {
          resources.value = data.content.list;
          pagination.value.current = params.page;
          pagination.value.total = data.content.total;
        } else {
          message.error(data.message);
        }
      });
    };

    const handleTableChange = (pagination: any) => {
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      });
    };

    const showAddress = (address: string) => {
      message.info(`资源地址: ${address}`);
    };

    const fetchCategories = () => {
      axios.get("/category/all").then((response) => {
        const data = response.data;
        if (data.success) {
          categories.value = data.content;
        } else {
          message.error(data.message);
        }
      });
    };

    onMounted(() => {
      fetchCategories();
      handleQuery({ page: 1, size: pagination.value.pageSize });
    });

    return {
      param,
      resources,
      categories,
      pagination,
      columns,
      loading,
      handleTableChange,
      handleQuery,
      showAddress
    }
  }
});
</script>

<style scoped>
</style>
