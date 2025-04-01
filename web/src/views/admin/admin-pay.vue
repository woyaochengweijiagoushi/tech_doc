<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }">
      <a-row>
        <a-col :span="12" :offset="6">
          <a-divider/>
        </a-col>
      </a-row>

      <a-row>
        <a-col :span="12" :offset="6">
          <a-card title="支付信息" :hoverable="false" :style="{
      borderRadius: '8px',
      padding: '20px',
    }">
            <template #extra>
              <span
                  style="margin: 10px 10px; color: red; font-size: 20px; font-weight: bold;">￥{{
                  totalAmount
                }}</span>
              <a-popconfirm title="确认支付?" ok-text="确认" cancel-text="取消"
                            @confirm="submitPay">
                <a-button type="primary" :style="{
      borderRadius: '4px',
    }">
                  立即支付
                </a-button>
              </a-popconfirm>
            </template>
            <a-descriptions :column="1" :bordered="true"
                            :style="{ marginBottom: '20px' }">
              <a-descriptions-item label="商品名称">
                积分充值
              </a-descriptions-item>
              <a-descriptions-item label="支付金额(元)">
                <a-row align="middle">
                  <a-radio-group v-model:value="selectTotalAmount">
                    <a-radio :value="'0.01'">0.01</a-radio>
                    <a-radio :value="'0.1'">0.1</a-radio>
                    <a-radio :value="'1'">1</a-radio>
                    <a-radio :value="'10'">10</a-radio>
                    <a-radio :value="'custom'">
                      自定义
                    </a-radio>
                  </a-radio-group>
                  <div style="width: 80px; height: 30px; margin-left: 10px;">
                    <a-input-number v-if="selectTotalAmount === 'custom'"
                                    v-model:value="customTotalAmount" :min="0.01"
                                    :style="{ borderRadius: '4px' }"/>
                  </div>
                </a-row>
              </a-descriptions-item>
              <a-descriptions-item label="支付备注">
                <a-textarea v-model:value="paymentRemark" placeholder="备注" auto-size
                            :style="{ borderRadius: '4px' }"/>
              </a-descriptions-item>
              <a-descriptions-item label="支付方式">
                <a-space>
                  <a-button type="ghost" :style="{ borderRadius: '4px' }">
                    <a-image :width="58" :height="168" :src="alipayLogo"
                             :preview="false"/>
                  </a-button>
                </a-space>
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>

      <a-modal v-model:visible="showPayInfoModel" title="支付单详情" width="800px"
               :destroyOnClose="true"
               @ok="handleModelOk">
        <a-descriptions bordered :column="2">
          <a-descriptions-item label="支付单号" span="2">
            {{ payInfo.paymentNo || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="支付金额">
            ￥{{ payInfo.totalAmount / 100 }}
          </a-descriptions-item>
          <a-descriptions-item label="支付状态">
            <a-tag :color="payStatusObj.color">
              {{ payStatusObj.status }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ payInfo.successTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="备注信息" span="2">
            {{ payInfo.remark || '无' }}
          </a-descriptions-item>
        </a-descriptions>
        <template #footer>
          <a-button type="primary" @click="handleModelOk">确认</a-button>
        </template>
      </a-modal>

      <a-row>
        <a-col :span="12" :offset="6">
          <a-card title="支付记录" :hoverable="false" :style="{
      borderRadius: '8px',
      padding: '20px',
      marginTop: '20px',
    }">
            <template #extra>
              <a-button type="primary" :style="{
      borderRadius: '4px',
    }" @click="handleQuery">
                查询
              </a-button>
            </template>
            <a-table :columns="columns" :rowKey="record => record.id"
                     :data-source="tableList" :pagination="pagination"
                     :loading="loading" @change="handleTableChange">
              <template #paymentStatus="{ text }">
                <a-tag :color="getPayStatus(text).color">
                  {{ getPayStatus(text).status }}
                </a-tag>
              </template>
            </a-table>

          </a-card>
        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts" setup>
import {computed, defineComponent, ref, watch} from 'vue';
import alipayLogo from '@/assets/58_168_alipay_pc.png';
import axios from 'axios';
import {message} from 'ant-design-vue';
import router from '@/router';
import {useRoute} from 'vue-router';
import {useStore} from 'vuex';
import {usePagination} from 'vue-request';
import {TableProps} from "ant-design-vue/es/table/interface";

interface PaySubmitResp {
  paymentStatus: number;
  displayContent: string;
  displayMode: string;
  paymentId: string;
  paymentNo: string;
  subject: string;
  remark: string;
  successTime: string;
  totalAmount: number;
}

interface PayGetResp {
  id: number;
  paymentStatus: number;
  paymentId: string;
  paymentNo: string;
  subject: string;
  remark: string;
  createTime: string;
  successTime: string;
  totalAmount: number;
  userIp: string;
}

const store = useStore()

const payMessageKey = 'payMessageKey';

const route = useRoute()
const payInfo = ref<PayGetResp>({} as PayGetResp);
const showPayInfoModel = ref(false);
const interval = ref<any>(undefined) // 定时任务，轮询是否完成支付
const fetchData = async (paymentNo: any) => {
  console.log("paymentNo：" + paymentNo);
  // 这里可以添加具体的逻辑
  if (paymentNo) {
    await axios.get("/pay/get", {
      params: {
        paymentNo: paymentNo,
        sync: "true"
      }
    }).then((response) => {
      const data = response.data;
      if (data.success) {
        console.log(data);
        payInfo.value = data.content;
        const payGetResp: PayGetResp = data.content;
        if (payGetResp.paymentStatus != 0) {
          const closeMsg = () => {
            showPayInfoModel.value = true;
            clearInterval(interval.value);
            interval.value = undefined;
          }
          message.success({
            content: `支付号：${payGetResp.paymentNo}, 支付成功！`,
            key: payMessageKey,
            duration: 2,
            onClose: closeMsg
          });
          // 重新查询用户信息
          store.dispatch('getUser');
        } else {
          if (interval.value) {
            return;
          }
          message.loading({
            content: `支付号：${payGetResp.paymentNo}, 支付中...！`,
            key: payMessageKey,
            duration: 0
          });
          interval.value = setInterval(async () => {
            await fetchData(paymentNo);
          }, 2000);
        }
      } else {
        message.error(data.message);
      }
    })
  }
};
const getPayStatus = (payStatus: number) => {
  if (payStatus == 0) {
    return {status: "支付中", color: "processing"};
  } else if (payStatus == 1) {
    return {status: "支付完成", color: "success"};
  } else if (payStatus == 2) {
    return {status: "已退款", color: "warning"};
  } else {
    return {status: "支付关闭", color: "error"};
  }
}
watch(() => route.query.paymentNo, fetchData, {immediate: true})
const payStatusObj = computed(() => {
  const payGetResp: PayGetResp = payInfo.value;
  return getPayStatus(payGetResp.paymentStatus);
})


const handleModelOk = () => {
  showPayInfoModel.value = false;
  router.replace({path: '/admin/pay'});
}

const selectTotalAmount = ref('0.01');
const customTotalAmount = ref('0.01');

const totalAmount = computed(() => {
  if (selectTotalAmount.value === 'custom') {
    return customTotalAmount.value;
  } else {
    return selectTotalAmount.value;
  }
});
const paymentRemark = ref('');
const paySubmitResp = ref<PaySubmitResp>();

const submitPay = () => {
  axios.post("/pay/submit", {
    "totalAmount": totalAmount.value,
    "remark": paymentRemark.value,
    "subject": "积分充值"
  }).then((response) => {
    const data = response.data;
    if (data.success) {
      console.log(data);
      paySubmitResp.value = data.content;
      //2. 打开新窗口
      const win = window.open('', '_blank');
      if (win) {
        if (paySubmitResp.value?.displayContent) {
          win.document.write(paySubmitResp.value?.displayContent);
          win.document.close(); // 结束写入流
        } else {
          message.warn('支付信息异常！');
        }

      } else {
        message.warn('弹窗被浏览器拦截');
      }

    } else {
      message.error(data.message);
    }
  });
}

// 
const columns = [
  {
    title: '支付编码',
    dataIndex: 'paymentNo',
    key: 'paymentNo'
  },
  {
    title: '支付金额',
    dataIndex: 'totalAmount',
    key: 'totalAmount',
    customRender: ({text}) => {
      return text / 100 + '元'
    }
  },
  {
    title: '用户ip',
    dataIndex: 'userIp',
    key: 'userIp',
  },
  {
    title: '支付状态',
    key: 'paymentStatus',
    dataIndex: 'paymentStatus',
    slots: {customRender: 'paymentStatus'}
  },
  {
    title: '创建时间',
    key: 'createdTime',
    dataIndex: 'createdTime',
    customRender: ({text}) => {
      return text || '-'
    }
  },
  {
    title: '完成时间',
    key: 'successTime',
    dataIndex: 'successTime',
    customRender: ({text}) => {
      return text || '-'
    }
  },
];

const listByUser = (params: { page?: number; size?: number }) => {
  return axios.get("/pay/list-by-user", {
    params: {
      page: params.page,
      size: params.size
    }
  }).then((response) => {
    const data = response.data;
    if (data.success) {
      return data.content;
    } else {
      message.warn('查询失败！');
    }
  });
}

const {
  data,
  run,
  loading,
  current,
  pageSize,
  total
} = usePagination(listByUser, {
  pagination: {
    currentKey: 'page',
    pageSizeKey: 'size',
    totalKey: 'total'
  },
});
const pagination = computed(() => ({
  total: total.value,
  current: current.value,
  pageSize: pageSize.value,
  showSizeChanger: true,
}));
const tableList = computed(() => data.value?.records || []);
const handleTableChange: TableProps['onChange'] = (
    pag: { pageSize: number; current: number }
) => {
  console.log('Table current page: ', pag);
  console.log('data: ', data.value);
  run({
    size: pag.pageSize,
    page: pag?.current,
  });
};

const handleQuery = () => {
  run({
    size: pageSize.value,
    page: 1,
  });
}

</script>

<style scoped>
:deep(.ant-card):hover {
  box-shadow: none !important;
  transform: none !important;
  cursor: default !important;
}
</style>