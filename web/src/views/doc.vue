<template>
  <a-layout>
    <a-layout-content :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }">
      <h3 v-if="level1.length === 0">对不起，找不到相关文档！</h3>
      <a-row>
        <a-col :span="6">
          <a-tree
            v-if="level1.length > 0"
            :tree-data="level1"
            @select="onSelect"
            :replaceFields="{title: 'name', key: 'id', value: 'id'}"
            :defaultExpandAll="true"
            :defaultSelectedKeys="defaultSelectedKeys"
          >
          </a-tree>
        </a-col>
        <a-col :span="18">
          <div>
            <h2>{{doc.name}}</h2>
            <div>
              <span>阅读数：{{doc.viewCount}}</span> &nbsp; &nbsp;
              <span>点赞数：{{doc.voteCount}}</span>
            </div>
            <a-divider style="height: 2px; background-color: #9999cc"/>
          </div>
          <div class="content-wrapper">
            <div v-if="isMarkdown" class="markdown-body">
              <v-md-preview :text="content"></v-md-preview>
            </div>
            <div v-else class="wangeditor" :innerHTML="html"></div>
          </div>
          <div class="vote-div">
            <a-button type="primary" shape="round" :size="'large'" @click="vote">
              <template #icon><LikeOutlined /> &nbsp;点赞：{{doc.voteCount}} </template>
            </a-button>
          </div>
        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, createVNode } from 'vue';
  import axios from 'axios';
  import {message} from 'ant-design-vue';
  import {Tool} from "@/util/tool";
  import {useRoute} from "vue-router";
  import VueMarkdownPreview from '@kangc/v-md-editor/lib/preview';
  import '@kangc/v-md-editor/lib/style/preview.css';
  import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
  import '@kangc/v-md-editor/lib/theme/style/vuepress.css';

  VueMarkdownPreview.use(vuepressTheme);

  export default defineComponent({
    name: 'Doc',
    components: {
      'v-md-preview': VueMarkdownPreview
    },
    setup() {
      const route = useRoute();
      const docs = ref();
      const html = ref();
      const content = ref('');
      const isMarkdown = ref(false);
      const defaultSelectedKeys = ref();
      defaultSelectedKeys.value = [];
      // 当前选中的文档
      const doc = ref();
      doc.value = {};

      /**
       * 一级文档树，children属性就是二级文档
       * [{
       *   id: "",
       *   name: "",
       *   children: [{
       *     id: "",
       *     name: "",
       *   }]
       * }]
       */
      const level1 = ref(); // 一级文档树，children属性就是二级文档
      level1.value = [];

      /**
       * 内容查询
       **/
      const handleQueryContent = (id: number) => {
        axios.get("/doc/find-content/" + id).then((response) => {
          const data = response.data;
          if (data.success) {
            // 检查内容是否为 Markdown 格式
            const docContent = data.content;
            // 更精确的 Markdown 内容检测
            isMarkdown.value = docContent.startsWith('#') || 
              docContent.includes('```') || 
              docContent.includes('*') || 
              docContent.includes('_') ||
              docContent.includes('>') ||
              docContent.includes('|') ||
              docContent.includes('- ') ||
              docContent.includes('1. ');
            
            if (isMarkdown.value) {
              content.value = docContent;
            } else {
              html.value = docContent;
            }
          } else {
            message.error(data.message);
          }
        });
      };

      /**
       * 数据查询
       **/
      const handleQuery = () => {
        axios.get("/doc/all/" + route.query.ebookId).then((response) => {
          const data = response.data;
          if (data.success) {
            docs.value = data.content;

            level1.value = [];
            level1.value = Tool.array2Tree(docs.value, 0);

            if (Tool.isNotEmpty(level1)) {
              defaultSelectedKeys.value = [level1.value[0].id];
              handleQueryContent(level1.value[0].id);
              // 初始显示文档信息
              doc.value = level1.value[0];
            }
          } else {
            message.error(data.message);
          }
        });
      };

      const onSelect = (selectedKeys: any, info: any) => {
        console.log('selected', selectedKeys, info);
        if (Tool.isNotEmpty(selectedKeys)) {
          // 选中某一节点时，加载该节点的文档信息
          doc.value = info.selectedNodes[0].props;
          // 加载内容
          handleQueryContent(selectedKeys[0]);
        }
      };

      // 点赞
      const vote = () => {
        axios.get('/doc/vote/' + doc.value.id).then((response) => {
          const data = response.data;
          if (data.success) {
            doc.value.voteCount++;
          } else {
            message.error(data.message);
          }
        });
      };

      onMounted(() => {
        handleQuery();
      });

      return {
        level1,
        html,
        content,
        isMarkdown,
        onSelect,
        defaultSelectedKeys,
        doc,
        vote
      }
    }
  });
</script>

<style>
  /* 内容区域包装器 */
  .content-wrapper {
    margin-left: 0;
    padding: 0 20px;
  }

  /* wangeditor默认样式 */
  .wangeditor {
    font-size: 16px;
    line-height: 1.6;
  }

  /* table 样式 */
  .wangeditor table {
    border-top: 1px solid #ccc;
    border-left: 1px solid #ccc;
    margin: 10px 0;
    width: 100%;
    border-collapse: collapse;
  }
  .wangeditor table td,
  .wangeditor table th {
    border-bottom: 1px solid #ccc;
    border-right: 1px solid #ccc;
    padding: 8px 12px;
  }
  .wangeditor table th {
    border-bottom: 2px solid #ccc;
    text-align: center;
    background-color: #f6f8fa;
  }

  /* blockquote 样式 */
  .wangeditor blockquote {
    display: block;
    border-left: 8px solid #d0e5f2;
    padding: 12px 16px;
    margin: 16px 0;
    line-height: 1.6;
    font-size: 16px;
    background-color: #f1f1f1;
    color: #333;
  }

  /* code 样式 */
  .wangeditor code {
    display: inline-block;
    background-color: #f6f8fa;
    border-radius: 3px;
    padding: 0.2em 0.4em;
    margin: 0 3px;
    font-family: SFMono-Regular,Consolas,Liberation Mono,Menlo,monospace;
    font-size: 14px;
  }
  .wangeditor pre code {
    display: block;
    padding: 16px;
    margin: 16px 0;
    overflow-x: auto;
  }

  /* ul ol 样式 */
  .wangeditor ul, 
  .wangeditor ol {
    margin: 16px 0;
    padding-left: 24px;
  }
  .wangeditor li {
    margin: 8px 0;
  }

  /* 和antdv p冲突，覆盖掉 */
  .wangeditor blockquote p {
    font-family: "YouYuan";
    margin: 0 !important;
    font-size: 16px !important;
    font-weight: 600;
  }

  /* 段落样式 */
  .wangeditor p {
    margin: 16px 0;
    line-height: 1.6;
  }

  /* 标题样式 */
  .wangeditor h1,
  .wangeditor h2,
  .wangeditor h3,
  .wangeditor h4,
  .wangeditor h5,
  .wangeditor h6 {
    margin: 24px 0 16px;
    line-height: 1.25;
    font-weight: 600;
  }

  /* 点赞 */
  .vote-div {
    padding: 15px;
    text-align: center;
  }

  /* 图片自适应 */
  .wangeditor img {
    max-width: 100%;
    height: auto;
    margin: 16px 0;
    border-radius: 4px;
  }

  /* 视频自适应 */
  .wangeditor iframe {
    width: 100%;
    height: 400px;
    margin: 16px 0;
    border: none;
  }

  /* Markdown 样式 */
  .markdown-body {
    font-size: 16px;
    line-height: 1.6;
  }

  /* vuepress-markdown-body 样式 */
  .vuepress-markdown-body {
    padding: 0 !important;
  }

  .markdown-body img {
    max-width: 100%;
    height: auto;
    margin: 16px 0;
    border-radius: 4px;
  }

  .markdown-body pre {
    background-color: #f6f8fa;
    padding: 16px;
    border-radius: 6px;
    margin: 16px 0;
    overflow-x: auto;
  }

  .markdown-body code {
    background-color: #f6f8fa;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-family: SFMono-Regular,Consolas,Liberation Mono,Menlo,monospace;
    font-size: 14px;
  }
</style>
