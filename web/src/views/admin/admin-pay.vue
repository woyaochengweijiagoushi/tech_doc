<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
    >
      <a-row>
        <a-col :span="12" :offset="6">
          <a-divider/>
        </a-col>
      </a-row>

      <a-row>
        <a-col :span="12" :offset="6">
          <a-card title="支付信息">
            <template #extra>
              <span style="margin: 10px 10px; color: red">￥{{ totalAmount }}</span>
              <a-button type="primary" @click="submitPay">立即支付</a-button>
            </template>
            <a-card-grid style="width: 20%;" :hoverable="false">商品名称：</a-card-grid>
            <a-card-grid style="width: 80%;" :hoverable="false">支付接口</a-card-grid>
            <a-card-grid style="width: 20%; height: 80px" :hoverable="false">
              支付金额(元)：
            </a-card-grid>
            <a-card-grid style="width: 80%; height: 80px" :hoverable="false">
              <a-row>
                <a-radio-group v-model:value="selectTotalAmount">
                  <a-radio :value="'0.01'">0.01
                  </a-radio>
                  <a-radio :value="'0.1'">0.1</a-radio>
                  <a-radio :value="'1'">1</a-radio>
                  <a-radio :value="'10'">10</a-radio>
                  <a-radio :value="'custom'">
                    自定义
                  </a-radio>
                </a-radio-group>
                <a-input-number v-if="selectTotalAmount === 'custom'"
                                v-model:value="customTotalAmount" :min="0.01"/>
              </a-row>

            </a-card-grid>
            <a-card-grid style="width: 20%; height: 80px" :hoverable="false">
              支付方式：
            </a-card-grid>
            <a-card-grid style="width: 80%; height: 80px" :hoverable="false">
              <a-space>
                <a-button type="ghost">
                  <a-image
                      :width="58"
                      :height="168"
                      :src="alipayLogo"
                      :preview="false"
                  />
                </a-button>
              </a-space>
            </a-card-grid>
          </a-card>
        </a-col>
      </a-row>
      <a-row>
        <a-col :span="12" :offset="6">
          <a-card v-if="paySubmitResp && paySubmitResp.displayContent" title="支付码"
                  :bordered="false" style="width: 300px">
            <a-spin :spinning="alipayIframeLoading">
              <iframe ref="alipayIframe"
                      :srcdoc="paySubmitResp.displayContent"
                      width="600"
                      height="300"
                      style="overflow:hidden; border: 0">
              </iframe>
            </a-spin>
          </a-card>
        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>

</template>

<script lang="ts">
import {computed, defineComponent, ref} from 'vue';
import alipayLogo from '@/assets/58_168_alipay_pc.png'
import axios from "axios";
import {message} from "ant-design-vue";

interface PaySubmitResp {
  paymentStatus: number;
  displayContent: string;
  displayMode: string;
}

export default defineComponent({
  name: 'AdminPay',
  setup() {
    const selectTotalAmount = ref('0.1')
    const customTotalAmount = ref()
    const paymentMethod = ref()
    const totalAmount = computed(() => {
      if (selectTotalAmount.value === 'custom') {
        return customTotalAmount.value;
      } else {
        return selectTotalAmount.value;
      }
    })
    const alipayIframe = ref()
    const alipayIframeLoading = ref(false)
    const paySubmitResp = ref<PaySubmitResp>()
    const submitPay = () => {
      axios.post("/pay/submit", {
        "totalAmount": totalAmount.value,
        "remark": "",
        "subject": "支付"
      }).then((response) => {
        const data = response.data; // data = commonResp
        if (data.success) {
          paySubmitResp.value = data.content;
          alipayIframeLoading.value = true
        } else {
          message.error(data.message);
        }
      });
    }
    return {
      selectTotalAmount,
      customTotalAmount,
      totalAmount,
      paymentMethod,
      alipayLogo,
      submitPay,
      paySubmitResp
    }
  }
});
</script>

<style scoped>

</style>