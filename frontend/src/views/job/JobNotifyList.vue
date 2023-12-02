<template>
  <div>
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
                >
                  <a-select-option v-for="(item, index) in jobNameList" :value="item.id" :key="index">
                    {{item.jobName}}
                  </a-select-option>

                </a-select>
              </a-form-item>
            </a-col>
            <template>

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
      </div>
      <s-table
        ref="table"
        size="default"
        :rowKey="(record) => record.id"
        :columns="notifyColumns"
        :data="loadData"
        :alert="options.alert"
        :rowSelection="options.rowSelection"
      >

        <span slot="notifyType" slot-scope="text">
          <a-tag :color="notifyTypeList[text].color">
            {{ notifyTypeList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyStatus" slot-scope="text">
          <a-tag :color="notifyStatusList[text].color">
            {{ notifyStatusList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyScene" slot-scope="text">
          <a-tag :color="notifySceneList[text].color">
            {{ notifySceneList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyAttribute" slot-scope="text, record">
          {{ parseJson(JSON.parse(text),record) }}
        </span>
        <span slot="notifyThreshold" slot-scope="text">
          {{ text === 0 ? '无' : text }}
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
import {getAllGroupNameList, getSceneList} from '@/api/manage'
import { STable } from '@/components'
import {getJobList, jobNameList, jobNotifyConfigPageList} from "@/api/jobApi";
const enums = require('@/utils/retryEnum')

export default {
  name: 'JobNotifyList',
  components: { STable },
  data () {
    return {
      notifyColumns: [
        {
          title: '组名',
          dataIndex: 'groupName',
          key: 'groupName',
          width: '10%',
          scopedSlots: { customRender: 'groupName' }
        },
        {
          title: '任务',
          dataIndex: 'jobName',
          key: 'jobName',
          width: '10%',
          scopedSlots: { customRender: 'jobName' }
        },
        {
          title: '通知状态',
          dataIndex: 'notifyStatus',
          width: '10%',
          scopedSlots: { customRender: 'notifyStatus' }
        },
        {
          title: '通知类型',
          dataIndex: 'notifyType',
          key: 'notifyType',
          width: '10%',
          scopedSlots: { customRender: 'notifyType' }
        },
        {
          title: '通知场景',
          dataIndex: 'notifyScene',
          key: 'notifyScene',
          width: '10%',
          scopedSlots: { customRender: 'notifyScene' }
        },
        {
          title: '通知阈值',
          dataIndex: 'notifyThreshold',
          key: 'notifyThreshold',
          width: '10%',
          scopedSlots: { customRender: 'notifyThreshold' }
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          width: '20%',
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
      loading: false,
      form: this.$form.createForm(this),
      // 高级搜索 展开/关闭
      advanced: false,
      queryParam: {},
      loadData: (parameter) => {
        return jobNotifyConfigPageList(Object.assign(parameter, this.queryParam)).then((res) => {
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
      memberLoading: false,
      notifySceneList: enums.notifyScene,
      notifyTypeList: enums.notifyType,
      notifyStatusList: enums.notifyStatus,
      visible: false,
      key: '',
      notifyTypeValue: '1',
      groupNameList: [],
      jobNameList: [],
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
      getJobList({ groupName: value }).then((res) => {
        this.jobNameList = res.data
      })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleNew () {
      this.$router.push({ path: '/job/notify/config' })
    },
    handleEdit (record) {
      this.$router.push({ path: '/job/notify/config', query: { id: record.id } })
    },
  }
}
</script>
<style scoped>
</style>
