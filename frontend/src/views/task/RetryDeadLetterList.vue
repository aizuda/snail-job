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
    </div>

    <div class="table-operator">
      <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay" @click="onClick">
          <a-menu-item key="1"><a-icon type="delete" />回滚</a-menu-item>
          <a-menu-item key="2"><a-icon type="edit" />删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /> </a-button>
      </a-dropdown>
    </div>

    <s-table
      ref="table"
      size="default"
      :rowKey="(record) => record.id"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
      :scroll="{ x: 2000 }"
    >
      <span slot="serial" slot-scope="text, record">
        {{ record.id }}
      </span>
      <span slot="taskType" slot-scope="text">
        <a-tag :color="taskType[text].color">
          {{ taskType[text].name }}
        </a-tag>
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
import {
  getAllGroupNameList,
  getRetryDeadLetterPage,
  getSceneList,
  rollbackRetryDeadLetter,
  deleteRetryDeadLetter
} from '@/api/manage'

import { STable } from '@/components'
import moment from 'moment'

export default {
  name: 'RetryDeadLetterList',
  components: {
    AInput,
    ATextarea,
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
      taskType: {
        '1': {
          'name': '重试数据',
          'color': '#d06892'
        },
        '2': {
          'name': '回调数据',
          'color': '#f5a22d'
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
          title: 'UniqueId',
          dataIndex: 'uniqueId',
          width: '10%'
        },
        {
          title: '幂等id',
          dataIndex: 'idempotentId',
          ellipsis: true
        },
        {
          title: '业务编号',
          dataIndex: 'bizNo',
          ellipsis: true
        },
        {
          title: '任务类型',
          dataIndex: 'taskType',
          scopedSlots: { customRender: 'taskType' },
          width: '5%'
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
          fixed: 'right',
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
      if (this.groupNameList !== null && this.groupNameList.length > 0) {
        this.queryParam['groupName'] = this.groupNameList[0]
        this.$refs.table.refresh(true)
      }
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
      rollbackRetryDeadLetter({ groupName: record.groupName, ids: [ record.id ] }).then(res => {
        this.$refs.table.refresh(true)
      })
    },
    handleDelete (record) {
      deleteRetryDeadLetter({ groupName: record.groupName, ids: [ record.id ] }).then(res => {
        this.$refs.table.refresh(true)
      })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleInfo (record) {
      this.$router.push({ path: '/retry-dead-letter/info', query: { id: record.id, groupName: record.groupName } })
    },
    onClick ({ key }) {
      if (key === '1') {
        this.handlerRollback()
        return
      }

      if (key === '2') {
        this.handlerDel()
      }
    },
    handlerRollback () {
      var that = this
      this.$confirm({
        title: '您要回滚这些数据吗?',
        content: h => <div style="color:red;">请确认是否回滚!</div>,
        onOk () {
          rollbackRetryDeadLetter({ groupName: that.selectedRows[0].groupName, ids: that.selectedRowKeys }).then(res => {
            that.$refs.table.refresh(true)
            that.$message.success(`成功删除${res.data}条数据`)
            that.selectedRowKeys = []
          })
        },
        onCancel () {},
        class: 'test'
      })
    },
    handlerDel () {
      var that = this
      this.$confirm({
        title: '您要删除这些数据吗?',
        content: h => <div style="color:red;">删除后数据不可恢复，请确认!</div>,
        onOk () {
          deleteRetryDeadLetter({ groupName: that.selectedRows[0].groupName, ids: that.selectedRowKeys }).then(res => {
            that.$refs.table.refresh(true)
            that.$message.success(`成功删除${res.data}条数据`)
            that.selectedRowKeys = []
          })
        },
        onCancel () {},
        class: 'test'
      })
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    }
  }
}
</script>
