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
                <a-select-option v-for="(index, value) in retryStatus" :value="value" :key="value">
                  {{ index.name }}</a-select-option
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
              <a-form-item label="幂等id">
                <a-input v-model="queryParam.idempotentId" placeholder="请输入幂等id" allowClear />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="UniqueId">
                <a-input v-model="queryParam.uniqueId" placeholder="请输入唯一id" allowClear/>
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
      <a-button type="primary" icon="plus" @click="handleNew()">单个</a-button>
      <a-button type="primary" icon="plus" @click="handleBatchNew()">批量</a-button>
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
          <a-divider type="vertical" />
          <a-popconfirm
            title="是否暂停?"
            ok-text="恢复"
            cancel-text="取消"
            @confirm="handleSuspend(record)"
          >
            <a href="javascript:;" v-if="record.retryStatus === 0">暂停</a>
          </a-popconfirm>
          <a-divider type="vertical" v-if="record.retryStatus === 0" />
          <a-popconfirm
            title="是否恢复?"
            ok-text="恢复"
            cancel-text="取消"
            @confirm="handleRecovery(record)"
          >
            <a href="javascript:;" v-if="record.retryStatus === 3">恢复</a>
          </a-popconfirm>
          <a-divider type="vertical" v-if="record.retryStatus === 3" />
          <a-popconfirm
            title="是否完成?"
            ok-text="完成"
            cancel-text="取消"
            @confirm="handleFinish(record)"
          >
            <a href="javascript:;" v-if="record.retryStatus !== 1 && record.retryStatus !== 2">完成</a>
          </a-popconfirm>
          <a-divider type="vertical" v-if="record.retryStatus !== 1 && record.retryStatus !== 2" />
          <a-popconfirm
            title="是否执行任务?"
            ok-text="执行"
            cancel-text="取消"
            @confirm="handleTrigger(record)"
          >
            <a href="javascript:;" v-if="record.retryStatus !== 1 && record.retryStatus !== 2">执行</a>
          </a-popconfirm>

        </template>
      </span>
    </s-table>

    <SaveRetryTask ref="saveRetryTask" @refreshTable="refreshTable"/>
    <BatchUpdateRetryTaskInfo ref="batchUpdateRetryTaskInfo" @refreshTable="refreshTable"/>
    <BatchSaveRetryTask ref="batchSaveRetryTask" @refreshTable="refreshTable"/>

  </a-card>
</template>

<script>
import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
import {
  getAllGroupNameList,
  getRetryTaskPage,
  getSceneList,
  updateRetryTaskStatus,
  batchDelete,
  manualTriggerCallbackTask, manualTriggerRetryTask
} from '@/api/manage'
import { STable } from '@/components'
import SaveRetryTask from './form/SaveRetryTask'
import BatchUpdateRetryTaskInfo from './form/BatchUpdateRetryTaskInfo'
import BatchSaveRetryTask from '@/views/task/form/BatchSaveRetryTask.vue'

export default {
  name: 'RetryTask',
  components: {
    AInput,
    ATextarea,
    STable,
    SaveRetryTask,
    BatchUpdateRetryTaskInfo,
    BatchSaveRetryTask
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
          'color': '#f5a22d'
        }
      },
      // 表头
      columns: [
        {
          title: 'ID',
          scopedSlots: { customRender: 'serial' },
          fixed: 'left'
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
          title: '场景名称',
          dataIndex: 'sceneName',
          width: '10%'
        },
        {
          title: '幂等id',
          dataIndex: 'idempotentId',
          width: '10%'
        },
        {
          title: '业务编号',
          dataIndex: 'bizNo',
          width: '10%'
        },
        {
          title: '下次触发时间',
          dataIndex: 'nextTriggerAt',
          needTotal: false,
          width: '10%'
        },
        {
          title: '次数',
          dataIndex: 'retryCount',
          sorter: true,
          width: '6%'
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
          title: '更新时间',
          dataIndex: 'updateDt',
          sorter: true,
          width: '10%'
        },
        {
          title: '操作',
          fixed: 'right',
          dataIndex: 'action',
          width: '180px',
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
    handleBatchNew () {
      this.$refs.batchSaveRetryTask.isShow(true, null)
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
          this.$message.error('执行失败')
        } else {
          this.$refs.table.refresh(true)
          this.$message.success('执行成功')
        }
      })
    },
    handleTrigger (record) {
      if (record.taskType === 1) {
        manualTriggerRetryTask({ groupName: record.groupName, uniqueIds: [ record.uniqueId ] }).then(res => {
          const { status } = res
          if (status === 0) {
            this.$message.error('执行失败')
          } else {
            this.$refs.table.refresh(true)
            this.$message.success('执行成功')
          }
        })
      } else {
        manualTriggerCallbackTask({ groupName: record.groupName, uniqueIds: [ record.uniqueId ] }).then(res => {
          const { status } = res
          if (status === 0) {
            this.$message.error('执行失败')
          } else {
            this.$refs.table.refresh(true)
            this.$message.success('执行完成')
          }
        })
      }
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
