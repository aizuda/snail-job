<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <!--          <a-col :md="8" :sm="24">-->
          <!--            <a-form-item label="组名称">-->
          <!--              <a-select-->
          <!--                v-model="queryParam.groupName"-->
          <!--                placeholder="请输入组名称"-->
          <!--                @change="(value) => handleChange(value)"-->
          <!--              >-->
          <!--                <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>-->
          <!--              </a-select>-->
          <!--            </a-form-item>-->
          <!--          </a-col>-->
          <!--          <a-col :md="8" :sm="24">-->
          <!--            <a-form-item label="场景名称">-->
          <!--              <a-select v-model="queryParam.sceneName" placeholder="请选择场景名称" allowClear>-->
          <!--                <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName">-->
          <!--                  {{ item.sceneName }}</a-select-option-->
          <!--                >-->
          <!--              </a-select>-->
          <!--            </a-form-item>-->
          <!--          </a-col>-->
          <!--          <a-col :md="8" :sm="24">-->
          <!--            <a-form-item label="状态">-->
          <!--              <a-select v-model="queryParam.jobStatus" placeholder="请选择状态" allowClear>-->
          <!--                <a-select-option v-for="(index, value) in jobStatus" :value="value" :key="value">-->
          <!--                  {{ index.name }}</a-select-option-->
          <!--                >-->
          <!--              </a-select>-->
          <!--            </a-form-item>-->
          <!--          </a-col>-->
          <!--          <template v-if="advanced">-->
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
          <!--          </template>-->
          <!--          <a-col :md="(!advanced && 8) || 24" :sm="24">-->
          <!--            <span-->
          <!--              class="table-page-search-submitButtons"-->
          <!--              :style="(advanced && { float: 'right', overflow: 'hidden' }) || {}"-->
          <!--            >-->
          <!--              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>-->
          <!--              <a-button style="margin-left: 8px" @click="() => (queryParam = {})">重置</a-button>-->
          <!--              <a @click="toggleAdvanced" style="margin-left: 8px">-->
          <!--                {{ advanced ? '收起' : '展开' }}-->
          <!--                <a-icon :type="advanced ? 'up' : 'down'" />-->
          <!--              </a>-->
          <!--            </span>-->
          <!--          </a-col>-->
        </a-row>
      </a-form>
    </div>
    <div class="table-operator">
      <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /> </a-button>
      </a-dropdown>
    </div>

    <a-table
      :columns="columns"
      :dataSource="data"
      :pagination="pagination"
      :loading="memberLoading"
      :scroll="{ x: 1200 }"
      rowKey="id"
      @expand="getRows"
    >
      <span slot="serial" slot-scope="text, record">
        {{ record.id }}
      </span>
      <span slot="taskStatus" slot-scope="text">
        <a-tag :color="taskStatus[text].color">
          {{ taskStatus[text].name }}
        </a-tag>
      </span>
      <span slot="clientInfo" slot-scope="text">
        {{ text !== '' ? text.split('@')[1] : '' }}
      </span>

      <a-table
        slot="expandedRowRender"
        slot-scope="record"
        :columns="logColumns"
        :data-source="record.logData"
        :pagination="false"
        rowKey="id"
      >
        <span slot="serial" slot-scope="text, record">
          {{ record.id }}
        </span>
      </a-table>
    </a-table>
  </a-card>
</template>

<script>
import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
import { STable } from '@/components'
import { jobLogList, jobTaskList } from '@/api/jobApi'
import enums from '@/utils/jobEnum'
import moment from 'moment/moment'

export default {
  name: 'JobTaskList',
  components: {
    AInput,
    ATextarea,
    STable
  },
  data () {
    return {
      currentComponet: 'List',
      visible: false,
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      data: [],
      logData: [],
      taskStatus: enums.taskStatus,
      // 表头
      columns: [
        {
          title: 'ID',
          scopedSlots: { customRender: 'serial' },
          width: '8%'
        },
        {
          title: '组名称',
          dataIndex: 'groupName'
        },
        {
          title: '地址',
          dataIndex: 'clientInfo',
          scopedSlots: { customRender: 'clientInfo' }
        },
        {
          title: '参数',
          dataIndex: 'argsStr',
          ellipsis: true
        },
        {
          title: '结果',
          dataIndex: 'resultMessage',
          ellipsis: true
        },
        {
          title: '状态',
          dataIndex: 'taskStatus',
          scopedSlots: { customRender: 'taskStatus' }
        },
        {
          title: '重试次数',
          dataIndex: 'retryCount'
        },
        {
          title: '开始执行时间',
          dataIndex: 'createDt',
          sorter: true,
          width: '10%'
        }
      ],
      logColumns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' },
          width: '5%'
        },
        {
          title: '信息',
          dataIndex: 'message',
          width: '50%'
        },
        {
          title: '执行时间',
          dataIndex: 'createDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
          width: '10%'
        }
      ],
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
      sceneList: [],
      memberLoading: false,
      pagination: {},
      logPagination: {}
    }
  },
  created () {
  },
  methods: {
    loadData (record) {
     const foundItem = this.logData.filter(item => item.taskId === record.id)
      return foundItem
    },
    handleChange (value) {
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    async getRows (expanded, record) {
      console.log(record)
      if (expanded) {
        await this.fetchLog({
          taskBatchId: record.taskBatchId,
          jobId: record.jobId,
          taskId: record.id
        }, record)
        this.$forceUpdate()
      }
    },
    handleOk (record) {},
    queryChange () {
      this.fetch()
    },
    fetch () {
      this.loading = true
      jobTaskList(this.queryParam).then(res => {
        this.data = res.data
        const pagination = { ...this.pagination }

        pagination.pageSize = res.size
        pagination.current = res.page
        pagination.total = res.total

        this.pagination = pagination
        this.loading = false
      })
    },
    refreshTable (v) {
      this.queryParam = v
      this.queryChange()
    },
    async fetchLog (queryParam, record) {
      const res = await jobLogList(queryParam)
      record.logData = res.data
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    }
  }
}
</script>
