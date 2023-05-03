<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="组名称">
              <a-select
                v-model="queryParam.groupName"
                placeholder="请输入组名称"
                @change="(value) => handleChange(value)"
              >
                <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="场景名称">
              <a-select v-model="queryParam.sceneName" placeholder="请选择场景名称" allowClear>
                <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName">
                  {{ item.sceneName }}</a-select-option
                >
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="重试状态">
              <a-select v-model="queryParam.retryStatus" placeholder="请选择状态" allowClear>
                <a-select-option v-for="(value, key) in retryStatus" :value="key" :key="key">
                  {{ value }}</a-select-option
                >
              </a-select>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <a-col :md="8" :sm="24">
              <a-form-item label="业务编号">
                <a-input v-model="queryParam.bizNo" placeholder="请输入业务编号" allowClear />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="业务id">
                <a-input v-model="queryParam.bizId" placeholder="请输入业务id" allowClear />
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="(!advanced && 8) || 24" :sm="24">
            <span
              class="table-page-search-submitButtons"
              :style="(advanced && { float: 'right', overflow: 'hidden' }) || {}"
            >
              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
              <a-button style="margin-left: 8px" @click="() => (queryParam = {})">重置</a-button>
              <a @click="toggleAdvanced" style="margin-left: 8px">
                {{ advanced ? '收起' : '展开' }}
                <a-icon :type="advanced ? 'up' : 'down'" />
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <div class="table-operator">
      <a-button type="primary" icon="plus" @click="handleNew()">新增</a-button>
      <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay" @click="onClick">
          <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>
          <a-menu-item key="2"><a-icon type="edit" />更新</a-menu-item>
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
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleInfo(record)">详情</a>
          <a-divider type="vertical" />
          <a @click="handleSuspend(record)" v-if="record.retryStatus === 0">暂停</a>
          <a-divider type="vertical" v-if="record.retryStatus === 0" />
          <a @click="handleRecovery(record)" v-if="record.retryStatus === 3">恢复</a>
          <a-divider type="vertical" v-if="record.retryStatus === 3" />
          <a @click="handleFinish(record)" v-if="record.retryStatus !== 1">完成</a>
          <a-divider type="vertical" v-if="record.retryStatus !== 1" />
        </template>
      </span>
    </s-table>

    <SaveRetryTask ref="saveRetryTask" @refreshTable="refreshTable"/>
    <BatchUpdateRetryTaskInfo ref="batchUpdateRetryTaskInfo" @refreshTable="refreshTable"/>
  </a-card>
</template>

<script>
import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
// 动态切换组件
import Edit from '@/views/list/table/Edit'
import { getAllGroupNameList, getRetryTaskPage, getSceneList, updateRetryTaskStatus, batchDelete } from '@/api/manage'
import { STable } from '@/components'
import SaveRetryTask from './form/SaveRetryTask'
import BatchUpdateRetryTaskInfo from './form/BatchUpdateRetryTaskInfo'

export default {
  name: 'RetryTask',
  components: {
    AInput,
    ATextarea,
    Edit,
    STable,
    SaveRetryTask,
    BatchUpdateRetryTaskInfo
  },
  data () {
    return {
      currentComponet: 'List',
      record: '',
      mdl: {},
      visible: false,
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      retryStatus: {
        0: '重试中',
        1: '重试完成',
        2: '最大次数',
        3: '暂停'
      },
      // 表头
      columns: [
        {
          title: 'ID',
          scopedSlots: { customRender: 'serial' },
          fixed: 'left'
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
          needTotal: false
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
          sorter: true
        },
        {
          title: '操作',
          fixed: 'right',
          dataIndex: 'action',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: (parameter) => {
        return getRetryTaskPage(Object.assign(parameter, this.queryParam)).then((res) => {
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
      groupNameList: [],
      sceneList: []
    }
  },
  created () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
      if (this.groupNameList !== null && this.groupNameList.length > 0) {
        this.queryParam['groupName'] = this.groupNameList[0]
        this.$refs.table.refresh(true)
        this.handleChange(this.groupNameList[0])
      }
    })
  },
  methods: {
    handleNew () {
      this.$refs.saveRetryTask.isShow(true, null)
    },
    handleChange (value) {
      getSceneList({ groupName: value }).then((res) => {
        this.sceneList = res.data
      })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleInfo (record) {
      this.$router.push({ path: '/retry-task/info', query: { id: record.id, groupName: record.groupName } })
    },
    handleOk (record) {},
    handleSuspend (record) {
      updateRetryTaskStatus({ id: record.id, groupName: record.groupName, retryStatus: 3 }).then((res) => {
        const { status } = res
        if (status === 0) {
          this.$message.error('暂停失败')
        } else {
          this.$refs.table.refresh(true)
          this.$message.success('暂停成功')
        }
      })
    },
    handleRecovery (record) {
      updateRetryTaskStatus({ id: record.id, groupName: record.groupName, retryStatus: 0 }).then((res) => {
        const { status } = res
        if (status === 0) {
          this.$message.error('恢复失败')
        } else {
          this.$refs.table.refresh(true)
          this.$message.success('恢复成功')
        }
      })
    },
    handleFinish (record) {
      updateRetryTaskStatus({ id: record.id, groupName: record.groupName, retryStatus: 1 }).then((res) => {
        const { status } = res
        if (status === 0) {
          this.$message.error('重试完成失败')
        } else {
          this.$refs.table.refresh(true)
          this.$message.success('重试完成成功')
        }
      })
    },
    refreshTable (v) {
      this.$refs.table.refresh(true)
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    handlerDel () {
      var that = this
      this.$confirm({
        title: '您要删除这些数据吗?',
        content: h => <div style="color:red;">删除后数据不可恢复，请确认!</div>,
        onOk () {
           batchDelete({ groupName: that.selectedRows[0].groupName, ids: that.selectedRowKeys }).then(res => {
           that.$refs.table.refresh(true)
           that.$message.success(`成功删除${res.data}条数据`)
           that.selectedRowKeys = []
          })
        },
        onCancel () {
        },
        class: 'test'
      })
    },
    onClick ({ key }) {
      if (key === '2') {
        this.$refs.batchUpdateRetryTaskInfo.isShow(true, this.selectedRows, this.selectedRowKeys)
        return
      }

      if (key === '1') {
        this.handlerDel()
      }
    }
  }
}
</script>
