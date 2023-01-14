<template>
  <div>
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <template>
            <a-col :md="8" :sm="24">
              <a-form-item label="场景名称">
                <a-input v-model="queryParam.sceneName" placeholder="请输入场景名称" allowClear/>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="queryChange()">查询</a-button>
              <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <a-table
      :columns="sceneColumns"
      :row-key="record => record.key"
      :dataSource="data"
      :pagination="pagination"
      :loading="memberLoading"
      @change="handleTableChange"
    >
      <template v-for="(col, i) in ['sceneName', 'description']" :slot="col" slot-scope="text, record">
        <a-input
          :key="col"
          v-if="record.editable"
          style="margin: -5px 0"
          :value="text"
          :placeholder="sceneColumns.find(item => item.key === col).title"
          @change="e => handleChange(e.target.value, record.key, col)"
        />
        <template v-else>{{ text }}</template>
      </template>
      <template slot="sceneStatus" slot-scope="text, record">
        <a-select
          v-if="record.editable"
          placeholder="场景状态"
          style="width: 100%;"
          :value="text === 0 ? '1': text"
          @change="value => handleChange(value, record.key, 'sceneStatus')">
          <a-select-option value="0">停用</a-select-option>
          <a-select-option value="1">启用</a-select-option>
        </a-select>
        <template v-else>{{ sceneStatus[text] }}</template>
      </template>
      <template slot="backOff" slot-scope="text, record">
        <a-select
          v-if="record.editable"
          placeholder="退避策略"
          style="width: 100%;"
          :value="text === 0 ? null: text"
          @change="value => handleChange(value, record.key, 'backOff')">
          <a-select-option value="1">延迟等级</a-select-option>
          <a-select-option value="2">固定定时间</a-select-option>
          <a-select-option value="3">CRON表达式</a-select-option>
          <a-select-option value="4">随机等待</a-select-option>
        </a-select>
        <template v-else>{{ backOffLabels[text] }}</template>
      </template>
      <template slot="maxRetryCount" slot-scope="text, record">
        <a-input-number
          v-if="record.editable"
          :min="1"
          :max="99999"
          style="width: 100%;"
          :value="text"
          placeholder="最大重试次数"
          @change="value => handleChange(value, record.key, 'maxRetryCount')"/>
        <template v-else>{{ text }}</template>
      </template>
      <template slot="triggerInterval" slot-scope="text, record">
        <a-input
          v-if="record.editable"
          style="margin: -5px 0"
          placeholder="间隔时间"
          :value="text"
          :disabled="data.find(item => item.key === record.key).backOff === '1'"
          @change="e => handleChange(e.target.value, record.key, 'triggerInterval')"
        />
        <template v-else>{{ text }}</template>
      </template>
      <template slot="operation" slot-scope="text, record">
        <template v-if="record.editable">
          <span v-if="record.isNew">
            <a @click="saveRow(record)">添加</a>
            <a-divider type="vertical" />
            <a-popconfirm title="是否要删除此行？" @confirm="remove(record.key)">
              <a>删除</a>
            </a-popconfirm>
          </span>
          <span v-else>
            <a @click="saveRow(record)">保存</a>
            <a-divider type="vertical" />
            <a @click="cancel(record.key)">取消</a>
          </span>
        </template>
        <span v-else>
          <a @click="toggle(record.key)">编辑</a>
          <a-divider type="vertical" />
          <a-popconfirm title="是否要删除此行？" @confirm="remove(record.key)">
            <a>删除</a>
          </a-popconfirm>
        </span>
      </template>
    </a-table>
    <a-button style="width: 100%; margin-top: 16px; margin-bottom: 8px" type="dashed" icon="plus" @click="newMember">新增成员</a-button>
  </div>

</template>

<script>
import { getScenePage } from '@/api/manage'
import { STable } from '@/components'

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
          key: 'sceneName',
          width: '15%',
          scopedSlots: { customRender: 'sceneName' }
        },
        {
          title: '场景状态',
          dataIndex: 'sceneStatus',
          key: 'sceneStatus',
          width: '12%',
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
          width: '12%',
          scopedSlots: { customRender: 'maxRetryCount' }
        },
        {
          title: '间隔时间',
          dataIndex: 'triggerInterval',
          key: 'triggerInterval',
          width: '12%',
          scopedSlots: { customRender: 'triggerInterval' }
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          width: '25%',
          scopedSlots: { customRender: 'description' }
        },
        {
          title: '操作',
          key: 'action',
          scopedSlots: { customRender: 'operation' }
        }
      ],
      data: [],
      formData: [],
      loading: false,
      advanced: false,
      memberLoading: false,
      triggerIntervalDisabled: false,
      max: 21,
      pagination: {},
      backOffLabels: {
        '1': '延迟等级',
        '2': '固定定时间',
        '3': 'CRON表达式',
        '4': '随机等待'
      },
      sceneStatus: {
        '0': '停用',
        '1': '启用'
      },
      queryParam: {}
    }
  },
  created () {
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
          const { id, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description } = record
          this.data.push({
            key: id,
            sceneName: sceneName,
            sceneStatus: sceneStatus.toString(),
            maxRetryCount: maxRetryCount,
            backOff: backOff.toString(),
            triggerInterval: triggerInterval,
            description: description,
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
      const { key, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description } = delData
      this.formData.push({
        key: key,
        sceneName: sceneName,
        sceneStatus: sceneStatus,
        maxRetryCount: maxRetryCount,
        backOff: backOff,
        triggerInterval: triggerInterval,
        description: description,
        isDeleted: 1
      })

      const newData = this.data.filter(item => item.key !== delKey)
      this.data = newData
    },
    saveRow (record) {
      this.memberLoading = true
      const { key, sceneName, sceneStatus, maxRetryCount, backOff, triggerInterval, description } = record
      if (!sceneName || !sceneStatus || !maxRetryCount || !backOff || (backOff === '1' ? false : !triggerInterval)) {
        this.memberLoading = false
        this.$message.error('请填写完整成员信息。')
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
            this.max = 21
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
    },
    newMember () {
      const length = this.data.length
      this.data.unshift({
        key: length === 0 ? '1' : (parseInt(this.data[length - 1].key) + 1).toString(),
        sceneName: '',
        sceneStatus: '1',
        maxRetryCount: null,
        backOff: '1',
        triggerInterval: '',
        description: '',
        editable: true,
        isNew: true
      })
    }
  }
}
</script>

<style scoped>

</style>
