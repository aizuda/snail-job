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
        :scroll="{ x: 1500 }"
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
          {{ text ? text : '10s,15s,30s,35s,40s,50s,1m,2m,4m,6m,8m,10m,20m,40m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,11h,12h' }}
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
          width: '15%'
        },
        {
          title: '场景状态',
          dataIndex: 'sceneStatus',
          width: '8%',
          scopedSlots: { customRender: 'sceneStatus' }
        },
        {
          title: '退避策略',
          dataIndex: 'backOff',
          key: 'backOff',
          width: '12%',
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
          title: '调用链超时时间',
          dataIndex: 'deadlineRequest',
          key: 'deadlineRequest',
          width: '10%',
          scopedSlots: { customRender: 'deadlineRequest' }
        },
        {
          title: '间隔时间',
          dataIndex: 'triggerInterval',
          key: 'triggerInterval',
          ellipsis: true,
          width: '15%',
          scopedSlots: { customRender: 'triggerInterval' }
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          width: '18%',
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
      triggerInterval: {
        '1': {
          placeholder: '',
          tooltip: ''
        },
        '2': {
          placeholder: '请输入固定间隔时间',
          tooltip: '请输入固定间隔时间'
        },
        '3': {
          placeholder: '请输入CRON表达式',
          tooltip: '通过CRON表达式计算执行时间'
        },
        '4': {
          placeholder: '请输入最大间隔时间',
          tooltip: '随机生成范围在[0, x]内的延迟时间; 其中x代表最大间隔时间'
        }
      },
      // 高级搜索 展开/关闭
      advanced: false,
      maxRetryCount: {
        '1': {
          placeholder: '请输入延迟等级(max:26)',
          tooltip: '请输入延迟等级（max:26)'
        },
        '2': {
          placeholder: '请输入最大重试次数',
          tooltip: '请输入最大重试次数'
        },
        '3': {
          placeholder: '请输入最大重试次数',
          tooltip: '请输入最大重试次数'
        },
        '4': {
          placeholder: '请输入最大重试次数',
          tooltip: '请输入最大重试次数'
        }
      },
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

    const groupName = this.$route.query.groupName
    if (groupName) {
      this.fetch({
          groupName: groupName,
          size: 6,
          page: 1
        }
      )
    }
  },
  methods: {
    handleNew () {
      this.$router.push({ path: '/retry/scene/config' })
    },
    handleEdit (record) {
      this.$router.push({ path: '/retry/scene/config', query: { id: record.id } })
    },
    reset () {
      this.formData = []
      this.data = []
      const groupName = this.$route.query.groupName
      if (groupName) {
        this.fetch({
            groupName: groupName,
            size: 6,
            page: 1
          }
        )
      }
    },
    handleTableChange (pagination, filters, sorter) {
      console.log(pagination)
      const pager = { ...this.pagination }
      pager.current = pagination.current
      this.pagination = pager
      this.fetch({
        groupName: this.$route.query.groupName,
        size: pagination.pageSize,
        page: pagination.current,
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...filters
      })
    },
    queryChange () {
      this.fetch({
          groupName: this.$route.query.groupName,
          size: 6,
          page: 1,
          sceneName: this.queryParam.sceneName
        }
      )
    },
    fetch (params = {}) {
      this.loading = true
      getScenePage(params).then(res => {
        this.data = []
        res.data.map(record => {
          this.loading = false
          const { id, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description, deadlineRequest } = record
          this.data.push({
            key: id,
            sceneName: sceneName,
            sceneStatus: sceneStatus.toString(),
            maxRetryCount: maxRetryCount,
            backOff: backOff.toString(),
            triggerInterval: triggerInterval,
            description: description,
            deadlineRequest: deadlineRequest,
            editable: false,
            isNew: false
          })
        })

        const pagination = { ...this.pagination }

        pagination.pageSize = res.size
        pagination.current = res.page
        pagination.total = res.total

        this.pagination = pagination
      })
    },
    remove (delKey) {
      const delData = this.data.find(item => item.key === delKey)
      const { key, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description, deadlineRequest } = delData
      this.formData.push({
        key: key,
        sceneName: sceneName,
        sceneStatus: sceneStatus,
        maxRetryCount: maxRetryCount,
        backOff: backOff,
        triggerInterval: triggerInterval,
        deadlineRequest: deadlineRequest,
        description: description,
        isDeleted: 1
      })

      const newData = this.data.filter(item => item.key !== delKey)
      this.data = newData
    },
    saveRow (record) {
      this.memberLoading = true
      const { key, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description, deadlineRequest } = record
      if (!sceneName || !sceneStatus || !maxRetryCount || !backOff || (backOff === '1' ? false : !triggerInterval)) {
        this.memberLoading = false
        this.$message.error('请填写完整成员信息。')
        return
      }

      const regex = /^[A-Za-z0-9_]{1,64}$/
      if (!regex.test(sceneName)) {
        this.memberLoading = false
        this.$message.error('场景名称: 仅支持长度为:1~64位字符.格式为:数字、字母、下划线。')
        return
      }

      if (description.length > 256) {
        this.memberLoading = false
        this.$message.error('描述:  仅支持长度为:1~256位字符')
        return
      }

      if ((backOff === '2' || backOff === '4') && triggerInterval < 10) {
        this.memberLoading = false
        this.$message.error('描述:  间隔时间最小为10秒')
        return
      }

      const target = this.formData.find(item => key === item.key)
      if (!target) {
        this.formData.push({
          key: key,
          sceneName: sceneName,
          sceneStatus: sceneStatus,
          maxRetryCount: maxRetryCount,
          backOff: backOff,
          triggerInterval: triggerInterval,
          description: description,
          deadlineRequest: deadlineRequest,
          isDeleted: 0
        })
      }

      new Promise((resolve) => {
        setTimeout(() => {
          resolve({ loop: false })
        }, 200)
      }).then(() => {
        const target = this.data.find(item => item.key === key)
        target.editable = false
        target.isNew = false
        this.memberLoading = false
        this.$message.warning('请点击右下角提交按钮以保存所有页面数据')
      })
    },
    toggle (key) {
      const target = this.data.find(item => item.key === key)
      target._originalData = { ...target }
      target.editable = !target.editable
    },
    getRowByKey (key, newData) {
      const data = this.data
      return (newData || data).find(item => item.key === key)
    },
    cancel (key) {
      const target = this.data.find(item => item.key === key)
      Object.keys(target).forEach(key => { target[key] = target._originalData[key] })
      target._originalData = undefined
    },
    handleChange (value, key, column) {
      if (column === 'backOff') {
        switch (value) {
          case '1':
            this.triggerIntervalDisabled = true
            this.max = 26
            break
          default:
            this.triggerIntervalDisabled = false
            this.max = 99999
        }
      }

      const newData = [...this.data]
      const target = newData.find(item => key === item.key)
      if (target) {
        target[column] = value
        this.data = newData
      }
    }
  }
}
</script>

<style scoped>

</style>
