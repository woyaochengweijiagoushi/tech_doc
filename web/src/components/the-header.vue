<template>
  <a-layout-header>
    <a-row>
      <a-col :span="2">
        <div class="logo">觉哥团队</div>
      </a-col>
      <a-col :span="20">
        <a-menu
            theme="dark"
            mode="horizontal"
        >
          <a-menu-item key="/">
            <router-link to="/">首页</router-link>
          </a-menu-item>
          <a-menu-item key="/admin/user" v-if="user.id">
            <router-link to="/admin/user">用户管理</router-link>
          </a-menu-item>
          <a-menu-item key="/admin/ebook" v-if="user.id">
            <router-link to="/admin/ebook">电子书管理</router-link>
          </a-menu-item>
          <a-menu-item key="/admin/category" v-if="user.id">
            <router-link to="/admin/category">分类管理</router-link>
          </a-menu-item>
          <a-menu-item key="/admin/pay" v-if="user.id">
            <router-link to="/admin/pay">积分充值</router-link>
          </a-menu-item>
          <a-menu-item key="/about">
            <router-link to="/about">关于我们</router-link>
          </a-menu-item>
          <a-menu-item key="/aliyun">
            <router-link to="/aliyun">服务器优惠</router-link>
          </a-menu-item>
        </a-menu>
      </a-col>
      <a-col :span="2">
        <div style="float: right">
          <!-- 未登录状态 -->
          <template v-if="!user.id">
            <a-button type="link" @click="showLoginModal">登录</a-button>
          </template>
          <template v-if="user.id">
            <a-popconfirm
                title="确认退出登录?"
                ok-text="是"
                cancel-text="否"
                @confirm="logout()"
            >
              <a-button type="link">退出登录 您好：{{ user.name }}</a-button>
            </a-popconfirm>
          </template>
        </div>
      </a-col>
    </a-row>


    <a-modal
        title="登录"
        v-model:visible="loginModalVisible"
        :confirm-loading="loginModalLoading"
        @ok="login"
    >
      <a-form :model="loginUser" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="登录名">
          <a-input v-model:value="loginUser.loginName"/>
        </a-form-item>
        <a-form-item label="密码">
          <a-input v-model:value="loginUser.password" type="password"/>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout-header>
</template>

<script lang="ts">
import {computed, defineComponent, ref} from 'vue';
import axios from 'axios';
import {message} from 'ant-design-vue';
import store from "@/store";

declare let hexMd5: any;
declare let KEY: any;

export default defineComponent({
  name: 'the-header',
  setup() {
    // 登录后保存
    const user = computed(() => store.state.user);

    // 用来登录
    const loginUser = ref({
      loginName: "test",
      password: "test"
    });
    const loginModalVisible = ref(false);
    const loginModalLoading = ref(false);
    const showLoginModal = () => {
      loginModalVisible.value = true;
    };

    // 登录
    const login = () => {
      console.log("开始登录");
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

    // 退出登录
    const logout = () => {
      console.log("退出登录开始");
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
      logout
    }
  }
});
</script>

<style>
.logo {
  float: left;
  color: white;
  font-size: 18px;
}

</style>
