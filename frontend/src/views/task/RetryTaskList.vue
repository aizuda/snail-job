<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="组名称">
              <a-select v-model="queryParam.groupName" placeholder="请输入组名称" @change="value => handleChange(value)">
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
          <a-col :md="8" :sm="24">
            <a-form-item label="重试状态">
              <a-select v-model="queryParam.retryStatus" placeholder="请选择状态" allowClear>
                <a-select-option v-for="(value, key) in retryStatus" :value="key" :key="key"> {{ value }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <a-col :md="8" :sm="24">
              <a-form-item label="业务编号">
                <a-input v-model="queryParam.bizNo" placeholder="请输入业务编号"  allowClear/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="业务id">
                <a-input v-model="queryParam.bizId" placeholder="请输入业务id" allowClear/>
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
    </div>
    <div class="table-operator">
      <!--      <a-button type="primary" icon="plus" @click="handleNew()">新增任务</a-button>-->
      <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>
          <!-- lock | unlock -->
          <a-menu-item key="2"><a-icon type="lock" />锁定</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px">
          批量操作 <a-icon type="down" />
        </a-button>
      </a-dropdown>
    </div>

    <s-table
      ref="table"
      size="default"
      rowKey="key"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
    >
      <span slot="serial" slot-scope="text, record, index">
        {{ index + 1 }}
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleInfo(record)">详情</a>
          <a-divider type="vertical" />
        </template>
        <!--        <a-dropdown>-->
        <!--          <a class="ant-dropdown-link">-->
        <!--            更多 <a-icon type="down" />-->
        <!--          </a>-->
        <!--          <a-menu slot="overlay">-->
        <!--            <a-menu-item>-->
        <!--              <a @click="handleInfo(record)">详情</a>-->
        <!--            </a-menu-item>-->
        <!--            <a-menu-item v-if="$auth('table.delete')">-->
        <!--              <a href="javascript:;">删除</a>-->
        <!--            </a-menu-item>-->
        <!--          </a-menu>-->
        <!--        </a-dropdown>-->
      </span>
    </s-table>
  </a-card>
</template>

<script>

import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
// 动态切换组件
import Edit from '@/views/list/table/Edit'
import { getAllGroupNameList, getRetryTaskPage, getSceneList } from '@/api/manage'
import { STable } from '@/components'
import moment from 'moment'

export default {
  name: 'RetryTask',
  components: {
    AInput,
    ATextarea,
    Edit,
    STable
  },
  data () {
    return {
      currentComponet: 'List',
      record: '',
      mdl: {},
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      retryStatus: {
        '0': '重试中',
        '1': '重试完成',
        '2': '最大次数'
      },
      // 表头
      columns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' },
          width: '5%'
        },
        {
          title: '组名称',
          dataIndex: 'groupName',
          ellipsis: true
        },
        {
          title: '场景名称',
          dataIndex: 'sceneName'
        },
        {
          title: '业务id',
          dataIndex: 'bizId'
        },
        {
          title: '业务编号',
          dataIndex: 'bizNo'
        },
        {
          title: '下次触发时间',
          dataIndex: 'nextTriggerAt',
          needTotal: false,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '重试次数',
          dataIndex: 'retryCount',
          sorter: true
        },
        {
          title: '重试状态',
          dataIndex: 'retryStatus',
          customRender: (text) => this.retryStatus[text]
        },
        {
          title: '更新时间',
          dataIndex: 'updateDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        console.log('loadData.parameter', parameter)
        return getRetryTaskPage(Object.assign(parameter, this.queryParam))
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
      sceneList: []
    }
  },
  created () {
    getAllGroupNameList().then(res => {
      this.groupNameList = res.data
    })
  },
  methods: {
    handleNew () {
      // this.$router.push('/form/basic-config')
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
      this.$router.push({ path: '/retry-task/info', query: { id: record.id, groupName: record.groupName } })
    },
    handleEdit (record) {

    }

  }
}
</script>
