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
              <a-select v-model="queryParam.sceneName" placeholder="请选择场景名称">
                <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName"> {{ item.sceneName }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <a-col :md="8" :sm="24">
              <a-form-item label="调用业务编号">
                <a-input v-model="queryParam.bizNo" placeholder="请输入业务编号"/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="业务id">
                <a-input v-model="queryParam.bizId" placeholder="请输入业务id"/>
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
          <a-popconfirm
            title="是否确认回滚?"
            ok-text="回滚"
            cancel-text="取消"
            @confirm="handleRollback(record)"
          >
            <a href="javascript:;" >回滚</a>
          </a-popconfirm>
          <a-divider type="vertical" />
        </template>
        <a-dropdown>
          <a class="ant-dropdown-link">
            更多 <a-icon type="down" />
          </a>
          <a-menu slot="overlay">
            <a-menu-item>
              <a @click="handleInfo(record)">详情</a>
            </a-menu-item>
            <a-menu-item>
              <a-popconfirm
                title="是否删除?"
                ok-text="删除"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a href="javascript:;">删除</a>
              </a-popconfirm>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </span>
    </s-table>
  </a-card>
</template>

<script>

import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
// 动态切换组件
import Edit from '@/views/list/table/Edit'
import { getAllGroupNameList, getRetryDeadLetterPage, getSceneList, rollbackRetryDeadLetter, deleteRetryDeadLetter } from '@/api/manage'

import { STable } from '@/components'
import moment from 'moment'

export default {
  name: 'RetryDeadLetterList',
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
          title: '场景id',
          dataIndex: 'sceneName',
          ellipsis: true
        },
        {
          title: '业务id',
          dataIndex: 'bizId',
          ellipsis: true
        },
        {
          title: '业务编号',
          dataIndex: 'bizNo',
          ellipsis: true
        },
        {
          title: '创建时间',
          dataIndex: 'createDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
          ellipsis: true
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
        return getRetryDeadLetterPage(Object.assign(parameter, this.queryParam))
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
      this.$router.push('/form/basic-config')
    },
    handleChange (value) {
      getSceneList({ 'groupName': value }).then(res => {
        this.sceneList = res.data
      })
    },
    handleRollback (record) {
      rollbackRetryDeadLetter(record.id, { groupName: record.groupName }).then(res => {
        this.$refs.table.refresh(true)
      })
    },
    handleDelete (record) {
      deleteRetryDeadLetter(record.id, { groupName: record.groupName }).then(res => {
        this.$refs.table.refresh(true)
      })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleInfo (record) {
      this.$router.push({ path: '/retry-dead-letter/info', query: { id: record.id, groupName: record.groupName } })
    }
  }
}
</script>
