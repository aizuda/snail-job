<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <template>
              <a-col :md="8" :sm="24">
                <a-form-item label="组名称">
                  <a-select
                    v-model="queryParam.groupName"
                    placeholder="请输入组名称"
                  >
                    <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="场景名称">
                  <a-input v-model="queryParam.sceneName" placeholder="请输入场景名称" allowClear/>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => (queryParam = {})">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleNew()">新增</a-button>
        <!--        <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">-->
        <!--          <a-menu slot="overlay" @click="onClick">-->
        <!--            <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>-->
        <!--            <a-menu-item key="2"><a-icon type="edit" />更新</a-menu-item>-->
        <!--          </a-menu>-->
        <!--          <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /> </a-button>-->
        <!--        </a-dropdown>-->
      </div>
      <s-table
        ref="table"
        size="default"
        :rowKey="(record) => record.id"
        :columns="sceneColumns"
        :data="loadData"
        :alert="options.alert"
        :rowSelection="options.rowSelection"
        :scroll="{ x: 2000 }"
      >
        <span slot="serial" slot-scope="text, record">
          {{ record.id }}
        </span>
        <span slot="sceneStatus" slot-scope="text">
          <a-tag :color="sceneStatus[text].color">
            {{ sceneStatus[text].name }}
          </a-tag>
        </span>
        <span slot="backOff" slot-scope="text">
          <a-tag :color="backOffLabels[text].color">
            {{ backOffLabels[text].name }}
          </a-tag>
        </span>
        <span slot="triggerInterval" slot-scope="text">
          <a-tooltip>
            <template slot="title">
              {{ text ? text : '10s,15s,30s,35s,40s,50s,1m,2m,4m,6m,8m,10m,20m,40m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,11h,12h' }}
            </template>
            {{ text ? text : '10s,15s,30s,35s,40s,50s,1m,2m,4m,6m,8m,10m,20m,40m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,11h,12h' }}
          </a-tooltip>
        </span>
        <span slot="deadlineRequest" slot-scope="text">
          {{ text }}(ms)
        </span>
        <span slot="executorTimeout" slot-scope="text">
          {{ text }}(s)
        </span>
        <span slot="action" slot-scope="record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
          </template>
        </span>
      </s-table>
    </a-card>
  </div>

</template>

<script>
import { getAllGroupNameList, getScenePage } from '@/api/manage'
import { STable } from '@/components'
const enums = require('@/utils/retryEnum')
export default {
  name: 'SceneList',
  components: {
    STable
  },
  data () {
    return {
      sceneColumns: [
        {
          title: '场景名称',
          dataIndex: 'sceneName',
          width: '10%'
        },
        {
          title: '场景状态',
          dataIndex: 'sceneStatus',
          width: '10%',
          scopedSlots: { customRender: 'sceneStatus' }
        },
        {
          title: '退避策略',
          dataIndex: 'backOff',
          key: 'backOff',
          width: '10%',
          scopedSlots: { customRender: 'backOff' }
        },
        {
          title: '最大重试次数',
          dataIndex: 'maxRetryCount',
          key: 'maxRetryCount',
          width: '10%',
          scopedSlots: { customRender: 'maxRetryCount' }
        },
        {
          title: '间隔时间',
          dataIndex: 'triggerInterval',
          key: 'triggerInterval',
          ellipsis: true,
          width: '10%',
          scopedSlots: { customRender: 'triggerInterval' }
        },
        {
          title: '调用链超时时间',
          dataIndex: 'deadlineRequest',
          key: 'deadlineRequest',
          width: '10%',
          scopedSlots: { customRender: 'deadlineRequest' }
        },
        {
          title: '执行超时时间',
          dataIndex: 'executorTimeout',
          key: 'executorTimeout',
          width: '10%',
          scopedSlots: { customRender: 'executorTimeout' }
        },
        {
          title: '创建时间',
          dataIndex: 'createDt',
          key: 'createDt',
          width: '10%'
        },
        {
          title: '更新时间',
          dataIndex: 'updateDt',
          key: 'updateDt',
          width: '10%'
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          width: '10%',
          scopedSlots: { customRender: 'description' }
        },
        {
          title: '操作',
          key: 'action',
          fixed: 'right',
          width: '180px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      data: [],
      pagination: {},
      backOffLabels: enums.backOffLabels,
      sceneStatus: enums.sceneStatus,
      // 高级搜索 展开/关闭
      advanced: false,
      queryParam: {},
      loadData: (parameter) => {
        return getScenePage(Object.assign(parameter, this.queryParam)).then((res) => {
          return res
        })
      },
      selectedRowKeys: [],
      selectedRows: [],

      // custom table alert & rowSelection
      options: {
        alert: {
          show: true,
          clear: () => {
            this.selectedRowKeys = []
          }
        },
        rowSelection: {
          selectedRowKeys: this.selectedRowKeys,
          onChange: this.onSelectChange
        }
      },
      optionAlertShow: false,
      groupNameList: []
    }
  },
  created () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
    })
  },
  methods: {
    handleNew () {
      this.$router.push({ path: '/retry/scene/config' })
    },
    handleEdit (record) {
      this.$router.push({ path: '/retry/scene/config', query: { id: record.id } })
    }
  }
}
</script>

<style scoped>

</style>
