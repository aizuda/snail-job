<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline" v-if="showSearch">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="组名称">
              <a-select v-model="queryParam.groupName" placeholder="请输入组名称" @change="value => handleChange(value)" allowClear>
                <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="场景名称">
              <a-select v-model="queryParam.sceneName" placeholder="请选择场景名称" allowClear>
                <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName"> {{ item.sceneName }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <a-col :md="8" :sm="24">
              <a-form-item label="业务编号">
                <a-input v-model="queryParam.bizNo" placeholder="请输入业务编号" allowClear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="幂等id">
                <a-input v-model="queryParam.idempotentId" placeholder="请输入幂等id" allowClear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="UniqueId">
                <a-input v-model="queryParam.uniqueId" placeholder="请输入唯一id" allowClear/>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
              <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
              <a @click="toggleAdvanced" style="margin-left: 8px">
                {{ advanced ? '收起' : '展开' }}
                <a-icon :type="advanced ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div >

    <s-table
      ref="table"
      size="default"
      rowKey="key"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
      :scroll="{ x: 2000 }"
    >
      <span slot="serial" slot-scope="text, record, index">
        {{ index + 1 }}
      </span>
      <span slot="taskType" slot-scope="text">
        <a-tag :color="taskType[text].color">
          {{ taskType[text].name }}
        </a-tag>
      </span>
      <span slot="retryStatus" slot-scope="text">
        <a-tag :color="retryStatus[text].color">
          {{ retryStatus[text].name }}
        </a-tag>
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleInfo(record)">详情</a>
        </template>
      </span>
    </s-table>
  </a-card>
</template>

<script>

import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
import { getAllGroupNameList, getRetryTaskLogPage, getSceneList } from '@/api/manage'

import { STable } from '@/components'
import moment from 'moment'

export default {
  name: 'RetryTaskLog',
  components: {
    AInput,
    ATextarea,
    STable
  },
  props: {
    showSearch: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      record: '',
      mdl: {},
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      retryStatus: {
        '0': {
          'name': '处理中',
          'color': '#9c1f1f'
        },
        '1': {
          'name': '处理成功',
          'color': '#f5a22d'
        },
        '2': {
          'name': '最大次数',
          'color': '#68a5d0'
        },
        '3': {
          'name': '暂停',
          'color': '#f52d8e'
        }
      },
      taskType: {
        '1': {
          'name': '重试数据',
          'color': '#d06892'
        },
        '2': {
          'name': '回调数据',
          'color': '#f5522d'
        }
      },
      // 表头
      columns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' },
          width: '5%'
        },
        {
          title: 'UniqueId',
          dataIndex: 'uniqueId',
          width: '10%'
        },
        {
          title: '组名称',
          dataIndex: 'groupName',
          ellipsis: true,
          width: '10%'
        },
        {
          title: '场景id',
          dataIndex: 'sceneName',
          ellipsis: true,
          width: '10%'
        },
        {
          title: '重试状态',
          dataIndex: 'retryStatus',
          scopedSlots: { customRender: 'retryStatus' },
          width: '5%'
        },
        {
          title: '任务类型',
          dataIndex: 'taskType',
          scopedSlots: { customRender: 'taskType' },
          width: '5%'
        },
        {
          title: '幂等id',
          dataIndex: 'idempotentId',
          width: '15%'
        },
        {
          title: '业务编号',
          dataIndex: 'bizNo',
          ellipsis: true,
          width: '15%'
        },
        {
          title: '触发时间',
          dataIndex: 'createDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
          ellipsis: true
        },
        {
          title: '操作',
          dataIndex: 'action',
          fixed: 'right',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        if (this.groupName !== '' && this.uniqueId !== '') {
          parameter['groupName'] = this.groupName
          parameter['uniqueId'] = this.uniqueId
        }
        console.log('loadData.parameter', parameter)
        return getRetryTaskLogPage(Object.assign(parameter, this.queryParam))
          .then(res => {
            return res
          })
      },
      selectedRowKeys: [],
      selectedRows: [],

      // custom table alert & rowSelection
      options: {
        alert: { show: true, clear: () => { this.selectedRowKeys = [] } },
        rowSelection: {
          selectedRowKeys: this.selectedRowKeys,
          onChange: this.onSelectChange
        }
      },
      optionAlertShow: false,
      groupNameList: [],
      sceneList: [],
      groupName: '',
      uniqueId: '',
      sceneName: ''
    }
  },
  created () {
    getAllGroupNameList().then(res => {
      this.groupNameList = res.data
    })
  },
  methods: {
    handleNew () {
      this.$router.push('/form/basic-config')
    },
    refreshTable (v) {
      this.groupName = v.groupName
      this.uniqueId = v.uniqueId
      this.$refs.table.refresh(true)
    },
    handleChange (value) {
      getSceneList({ 'groupName': value }).then(res => {
        this.sceneList = res.data
      })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleInfo (record) {
      this.$router.push({ path: '/retry-log/info', query: { id: record.id } })
    }
  }
}
</script>
