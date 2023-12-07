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
            <a-form-item label="任务名称">
              <a-select
                show-search
                v-model="queryParam.jobId"
                placeholder="请输入任务名称"
                :default-active-first-option="false"
                :show-arrow="true"
                :filter-option="false"
                :not-found-content="null"
                @search="handleSearch"
                @change="handleChange"
              >
                <a-select-option v-for="(item, index) in jobNameList" :value="item.id" :key="index">
                  {{ item.jobName }}
                </a-select-option>

              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="状态">
              <a-select v-model="queryParam.taskBatchStatus" placeholder="请选择状态" allowClear>
                <a-select-option v-for="(item, index) in taskBatchStatus" :value="index" :key="index">
                  {{ item.name }}</a-select-option
                >
              </a-select>
            </a-form-item>
          </a-col>
          <template v-if="advanced">
            <!--            <a-col :md="8" :sm="24">-->
            <!--              <a-form-item label="业务编号">-->
            <!--                <a-input v-model="queryParam.bizNo" placeholder="请输入业务编号" allowClear />-->
            <!--              </a-form-item>-->
            <!--            </a-col>-->
            <!--            <a-col :md="8" :sm="24">-->
            <!--              <a-form-item label="幂等id">-->
            <!--                <a-input v-model="queryParam.idempotentId" placeholder="请输入幂等id" allowClear />-->
            <!--              </a-form-item>-->
            <!--            </a-col>-->
            <!--            <a-col :md="8" :sm="24">-->
            <!--              <a-form-item label="UniqueId">-->
            <!--                <a-input v-model="queryParam.uniqueId" placeholder="请输入唯一id" allowClear/>-->
            <!--              </a-form-item>-->
            <!--            </a-col>-->
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
    </div>

    <s-table
      ref="table"
      size="default"
      :rowKey="(record) => record.id"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
      :scroll="{ x: 1500 }"
    >
      <span slot="serial" slot-scope="record">
        <a href="#" @click="handlerOpenDrawer(record)">{{ record.id }}</a>
      </span>
      <span slot="taskBatchStatus" slot-scope="text">
        <a-tag :color="taskBatchStatus[text].color">
          {{ taskBatchStatus[text].name }}
        </a-tag>
      </span>
      <span slot="operationReason" slot-scope="text">
        {{ operationReason[text].name }}
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleInfo(record)">详情</a>
          <a-divider type="vertical" />
          <a-popconfirm
            title="是否停止?"
            ok-text="停止"
            cancel-text="取消"
            @confirm="handleStop(record)"
          >
            <a href="javascript:;" v-if="record.taskBatchStatus === 1 || record.taskBatchStatus === 2">停止</a>
          </a-popconfirm>
        </template>
      </span>
    </s-table>

    <Drawer
      title="任务详情"
      placement="right"
      :width="800"
      :visibleAmplify="true"
      :visible="openDrawer"
      @closeDrawer="onClose"
      @handlerAmplify="handleInfo"
    >
      <job-batch-info ref="jobBatchInfoRef" :showHeader="false" :column="2"/>
    </Drawer>

  </a-card>
</template>

<script>
import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
import { Drawer, STable } from '@/components'
import { jobBatchList, jobNameList, stop } from '@/api/jobApi'
import { getAllGroupNameList } from '@/api/manage'
import JobBatchInfo from '@/views/job/JobBatchInfo'
const enums = require('@/utils/jobEnum')

export default {
  name: 'JobBatchList',
  components: {
    JobBatchInfo,
    Drawer,
    AInput,
    ATextarea,
    STable
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
      queryParam: {
        jobId: null
      },
      taskBatchStatus: enums.taskBatchStatus,
      operationReason: enums.operationReason,
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
          ellipsis: true,
          width: '15%'
        },
        {
          title: '任务名称',
          dataIndex: 'jobName',
          ellipsis: true,
          width: '15%'
        },
        {
          title: '开始执行时间',
          dataIndex: 'executionAt',
          width: '15%'
        },
        {
          title: '状态',
          dataIndex: 'taskBatchStatus',
          scopedSlots: { customRender: 'taskBatchStatus' },
          width: '5%'
        },
        {
          title: '操作原因',
          dataIndex: 'operationReason',
          scopedSlots: { customRender: 'operationReason' },
          width: '15%'
        },
        {
          title: '创建时间',
          dataIndex: 'createDt',
          sorter: true,
          width: '15%'
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
        // this.queryParam['jobId'] = this.$route.query.jobId
        return jobBatchList(Object.assign(parameter, this.queryParam)).then((res) => {
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
      jobNameList: [],
      openDrawer: false,
      currentShowRecord: null
    }
  },
  created () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
    })

    const jobId = this.$route.query.jobId
    jobNameList({ jobId: jobId }).then(res => {
      this.jobNameList = res.data
      console.log(jobId)
      if (jobId) {
        this.queryParam['jobId'] = this.jobNameList[0].id
        this.$refs.table.refresh(true)
      }
    })
  },
  methods: {
    handleSearch (value) {
      jobNameList({ keywords: value }).then(res => {
        this.jobNameList = res.data
      })
    },
    handleChange (value) {
      console.log(`handleChange ${value}`)
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleInfo (record) {
      record = record || this.currentShowRecord
      this.$router.push({ path: '/job/batch/info', query: { id: record.id, groupName: record.groupName } })
    },
    handleOk (record) {},
    handleStop (record) {
      stop(record.id).then((res) => {
        const { status } = res
        if (status === 0) {
          this.$message.error('停止失败')
        } else {
          this.$refs.table.refresh(true)
          this.$message.success('停止成功')
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
    handlerOpenDrawer (record) {
      this.currentShowRecord = record
      this.openDrawer = true
      setTimeout(() => {
        this.$refs.jobBatchInfoRef.jobBatchDetail(record.id)
      }, 200)
    },
    onClose () {
      this.openDrawer = false
      this.currentShowRecord = null
    }
  }
}
</script>
