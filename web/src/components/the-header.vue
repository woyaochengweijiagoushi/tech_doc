<template>
  <a-layout>
    <a-layout-header class="header">
      <div class="logo">
        觉哥技术团队 v0.2
      </div>
      <a-menu theme="dark" mode="horizontal" :selectedKeys="[currentRoute]" style="flex: 1;">
        <a-menu-item key="/">
          <router-link to="/" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <home-outlined /> 首页
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/admin/user" v-if="user.id">
          <router-link to="/admin/user" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <user-outlined /> 用户管理
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/resourceQuery">
          <router-link to="/resourceQuery" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <search-outlined /> 资源查询
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/admin/ebook" v-if="user.id">
          <router-link to="/admin/ebook" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <book-outlined /> 资源管理
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/admin/category" v-if="user.id">
          <router-link to="/admin/category" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <appstore-outlined /> 分类管理
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/admin/pay" v-if="user.id">
          <router-link to="/admin/pay" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <wallet-outlined /> 积分充值
            </span>
          </router-link>
        </a-menu-item>
        <a-menu-item key="/about">
          <router-link to="/about" custom v-slot="{ navigate }">
            <span @click="navigate" role="link">
              <team-outlined /> 关于我们
            </span>
          </router-link>
        </a-menu-item>
      </a-menu>
      <div class="user-action">
        <template v-if="!user.id">
          <a-button type="primary" ghost @click="showLoginModal">
            <login-outlined /> 登录 / 注册
          </a-button>
        </template>
        <template v-if="user.id">
          <a-dropdown placement="bottomRight">
            <div class="user-profile">
              <a-avatar class="user-avatar" :style="{ backgroundColor: '#1890ff' }">
                {{ user.name?.charAt(0) }}
              </a-avatar>
              <span class="user-name">{{ user.name }}</span>
              <down-outlined class="dropdown-icon" />
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="logout">
                  <logout-outlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
      </div>
    </a-layout-header>
    <a-layout-content class="content" v-if="showContent">
      <div class="welcome-message">
        <h1 class="announcement-title">公告</h1>
        <a-row :gutter="[16, 16]">
          <a-col :span="24">
            <p>1.本网站有上万种计算机相关电子资源(均有对应编号且存在在网盘) 可以通过本网站资源查询界面获取 部分资源免费 部分需要消耗积分解锁(vip可直接查看)</p>
            <p>2.积分可通过充值/签到/发布资源/发布资源被收藏或点赞等方式获取</p>
            <p>3.网站展示的所有项目列表 如果需要一对一辅导部署/搭建/指导的可在任务栏中发布 有其它需求也可发布悬赏金额若为现金则可寻求觉哥进行担保 若盲目转账导致被骗概不负责</p>
            <p>4.若有任何其它建议请在下方填下并提交 合理建议通常会在一个月内得到处理</p>
          </a-col>
        </a-row>

        <!-- 添加表单部分 -->
        <div v-if="showForm" class="advice-form">
          <a-form :model="adviceForm" @submit.prevent="submitAdvice">
            <a-form-item label="建议内容" name="content">
              <a-textarea v-model:value="adviceForm.content" placeholder="请输入您的建议（不超过200字）" :maxlength="200" />
            </a-form-item>
            <a-form-item>
              <div class="form-button-container">
                <a-button type="primary" html-type="submit">提交建议</a-button>
              </div>
            </a-form-item>
          </a-form>
        </div>

        <!-- 添加展开/收缩按钮 -->
        <div class="toggle-button-container">
          <div class="toggle-button" @click="toggleForm">
            {{ showForm ? '收起' : '提交建议' }}
          </div>
        </div>
      </div>
    </a-layout-content>
    <!-- 添加展开/收缩按钮 -->
    <div class="toggle-content-button-container">
      <div class="toggle-content-button" @click="toggleContent">
        {{ showContent ? '收起' : '查看' }}公告
      </div>
    </div>
  </a-layout>

  <!-- 登录模态框 -->
  <a-modal
      title="用户登录"
      v-model:visible="loginModalVisible"
      :confirm-loading="loginModalLoading"
      @ok="login"
      :width="400"
      centered
      :keyboard="false"
      :maskClosable="false"
  >
    <a-form
        :model="loginUser"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
        class="login-form"
    >
      <a-form-item label="登录名" name="loginName">
        <a-input
            v-model:value="loginUser.loginName"
            placeholder="请输入登录名"
            allow-clear
        >
          <template #prefix>
            <user-outlined />
          </template>
        </a-input>
      </a-form-item>

      <a-form-item label="密码" name="password">
        <a-input-password
            v-model:value="loginUser.password"
            placeholder="请输入密码"
        >
          <template #prefix>
            <lock-outlined />
          </template>
        </a-input-password>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';
import store from "@/store";
import {
  HomeOutlined,
  UserOutlined,
  SearchOutlined,
  BookOutlined,
  AppstoreOutlined,
  WalletOutlined,
  TeamOutlined,
  LoginOutlined,
  LogoutOutlined,
  DownOutlined,
  LockOutlined
} from '@ant-design/icons-vue';

declare let hexMd5: any;
declare let KEY: any;

export default defineComponent({
  name: 'HeaderAndContent',
  components: {
    HomeOutlined,
    UserOutlined,
    SearchOutlined,
    BookOutlined,
    AppstoreOutlined,
    WalletOutlined,
    TeamOutlined,
    LoginOutlined,
    LogoutOutlined,
    DownOutlined,
    LockOutlined
  },
  setup() {
    const user = computed(() => store.state.user);
    const loginUser = ref({ loginName: "test", password: "test" });
    const loginModalVisible = ref(false);
    const loginModalLoading = ref(false);
    const showLoginModal = () => { loginModalVisible.value = true; };
    const currentRoute = computed(() => window.location.pathname);

    const adviceForm = ref({ content: '' });
    const showForm = ref(false);
    const showContent = ref(false); // 控制 a-layout-content 显示与隐藏

    const toggleForm = () => {
      showForm.value = !showForm.value;
    };

    const toggleContent = () => {
      showContent.value = !showContent.value;
    };

    const submitAdvice = async () => {
      if (!user.value.id || !user.value.name) {
        message.error('请先登录');
        return;
      }

      if (adviceForm.value.content.length > 200) {
        message.error('建议内容不能超过200字');
        return;
      }

      try {
        await axios.post('/user/advice', {
          userId: user.value.id,
          userName: user.value.name,
          content: adviceForm.value.content
        });

        message.success('建议提交成功！');
        adviceForm.value.content = '';
      } catch (error) {
        message.error('建议提交失败，请稍后再试');
      }
    };

    const login = () => {
      loginModalLoading.value = true;
      loginUser.value.password = hexMd5(loginUser.value.password + KEY);
      axios.post('/user/login', loginUser.value).then((response) => {
        loginModalLoading.value = false;
        const data = response.data;
        if (data.success) {
          loginModalVisible.value = false;
          message.success("登录成功！");
          store.commit("setUser", data.content);
        } else {
          message.error(data.message);
        }
      });
    };

    const logout = () => {
      axios.get('/user/logout/' + user.value.token).then((response) => {
        const data = response.data;
        if (data.success) {
          message.success("退出登录成功！");
          store.commit("setUser", {});
        } else {
          message.error(data.message);
        }
      });
    };

    return {
      loginModalVisible,
      loginModalLoading,
      showLoginModal,
      loginUser,
      login,
      user,
      logout,
      currentRoute,
      adviceForm,
      showForm,
      showContent,
      toggleForm,
      toggleContent,
      submitAdvice
    };
  }
});
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.logo {
  color: white;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 1px;
}

.user-action {
  display: flex;
  align-items: center;
}

.login-btn {
  border-color: rgba(255,255,255,0.8);
  color: white;
  height: 36px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.login-btn:hover {
  border-color: white !important;
  background: rgba(255,255,255,0.1) !important;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.user-profile:hover {
  background: rgba(255,255,255,0.1);
}

.user-avatar {
  background: #40a9ff;
  vertical-align: middle;
}

.user-name {
  color: white;
  font-size: 14px;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-icon {
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  margin-left: 4px;
}

.content {
  padding: 24px;
  background-color: #f0f2f5;
  border-radius: 8px;
}

.welcome-message {
  border-left: 2px dashed #ccc;
  padding-left: 24px;
  margin-bottom: 24px;
}

.announcement-title {
  text-align: center;
  margin-bottom: 16px;
  font-size: 24px;
  color: #333;
  font-weight: bold;
  background: linear-gradient(to right, #ffcc00, #ff6600);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  padding: 8px 0;
  border-bottom: 2px solid #ff6600;
}

.welcome-message p {
  margin-bottom: 8px;
  line-height: 1.5;
  font-size: 14px;
  color: #666;
}

.advice-form {
  margin-top: 24px;
  background-color: #fff;
  padding: 24px;
  border-radius: 8px;
}

.form-button-container,
.toggle-button-container,
.toggle-content-button{
  text-align: center;
  margin-top: 16px;
}

.toggle-button,
.toggle-content-button {
  cursor: pointer;
  color: #1890ff;
}


</style>
