<template>
  <div>
    <a-card :bordered="false">
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
            {{ sceneStatus[text].name }}>
          </a-tag>
        </span>
        <span slot="backOff" slot-scope="text">
          <a-tag :color="backOffLabels[text].color">
            {{ backOffLabels[text].name }}
          </a-tag>
        </span>
        <span slot="triggerInterval" slot-scope="text">
          {{ text }}
        </span>
      </s-table>
    </a-card>

    <!--    <a-table-->
    <!--      :columns="sceneColumns"-->
    <!--      :row-key="record => record.key"-->
    <!--      :dataSource="data"-->
    <!--      :pagination="pagination"-->
    <!--      :loading="memberLoading"-->
    <!--      @change="handleTableChange"-->
    <!--      :scroll="{ x: 1800 }"-->
    <!--    >-->
    <!--      <template v-for="(col, i) in ['sceneName', 'description']" :slot="col" slot-scope="text, record">-->
    <!--        <a-input-->
    <!--          :key="col"-->
    <!--          v-if="record.editable"-->
    <!--          style="margin: -5px 0"-->
    <!--          :value="text"-->
    <!--          :placeholder="sceneColumns.find(item => item.key === col).title"-->
    <!--          @change="e => handleChange(e.target.value, record.key, col)"-->
    <!--        />-->
    <!--        <template v-else>{{ text }}</template>-->
    <!--      </template>-->
    <!--      <template slot="sceneStatus" slot-scope="text, record">-->
    <!--        <a-select-->
    <!--          v-if="record.editable"-->
    <!--          placeholder="场景状态"-->
    <!--          style="width: 100%;"-->
    <!--          :value="text === 0 ? '1': text"-->
    <!--          @change="value => handleChange(value, record.key, 'sceneStatus')">-->
    <!--          <a-select-option value="0">停用</a-select-option>-->
    <!--          <a-select-option value="1">启用</a-select-option>-->
    <!--        </a-select>-->
    <!--        <template v-else>{{ sceneStatus[text] }}</template>-->
    <!--      </template>-->
    <!--      <template slot="backOff" slot-scope="text, record">-->
    <!--        <a-select-->
    <!--          v-if="record.editable"-->
    <!--          placeholder="退避策略"-->
    <!--          style="width: 100%;"-->
    <!--          :value="text === 0 ? null: text"-->
    <!--          @change="value => handleChange(value, record.key, 'backOff')">-->
    <!--          <a-select-option value="1">延迟等级</a-select-option>-->
    <!--          <a-select-option value="2">固定时间</a-select-option>-->
    <!--          <a-select-option value="3">CRON表达式</a-select-option>-->
    <!--          <a-select-option value="4">随机等待</a-select-option>-->
    <!--        </a-select>-->
    <!--        <template v-else>{{ backOffLabels[text] }}</template>-->
    <!--      </template>-->
    <!--      <template slot="maxRetryCount" slot-scope="text, record">-->
    <!--        <a-input-number-->
    <!--          v-if="record.editable"-->
    <!--          :min="1"-->
    <!--          :max="max"-->
    <!--          style="width: 100%;"-->
    <!--          :value="text"-->
    <!--          :placeholder="maxRetryCount[data.find(item => item.key === record.key).backOff].placeholder"-->
    <!--          @change="value => handleChange(value, record.key, 'maxRetryCount')">-->
    <!--        </a-input-number>-->
    <!--        <template v-else>{{ text }}</template>-->
    <!--      </template>-->
    <!--      <template slot="deadlineRequest" slot-scope="text, record">-->
    <!--        <a-input-number-->
    <!--          v-if="record.editable"-->
    <!--          :min="100"-->
    <!--          :max="60000"-->
    <!--          style="width: 100%;"-->
    <!--          :value="text"-->
    <!--          placeholder="调用链超时时间(毫秒)"-->
    <!--          @change="value => handleChange(value, record.key, 'deadlineRequest')"/>-->
    <!--        <template v-else>{{ text }}(毫秒)</template>-->
    <!--      </template>-->
    <!--      <template slot="triggerInterval" slot-scope="text, record">-->
    <!--        <a-input-->
    <!--          v-if="record.editable"-->
    <!--          style="margin: -5px 0"-->
    <!--          :placeholder="triggerInterval[data.find(item => item.key === record.key).backOff].placeholder"-->
    <!--          :value="text"-->
    <!--          :disabled="data.find(item => item.key === record.key).backOff === '1'"-->
    <!--          @change="e => handleChange(e.target.value, record.key, 'triggerInterval')"-->
    <!--        >-->
    <!--          <a-tooltip slot="suffix" :title="triggerInterval[data.find(item => item.key === record.key).backOff].tooltip">-->
    <!--            <a-icon type="info-circle" style="color: rgba(0, 0, 0, 0.45)" />-->
    <!--          </a-tooltip>-->
    <!--        </a-input>-->
    <!--        <template v-else>{{ text }}(秒)</template>-->
    <!--      </template>-->
    <!--      <template slot="operation" slot-scope="text, record">-->
    <!--        <template v-if="record.editable">-->
    <!--          <span v-if="record.isNew">-->
    <!--            <a @click="saveRow(record)">添加</a>-->
    <!--            <a-divider type="vertical" />-->
    <!--            <a-popconfirm title="是否要删除此行？" @confirm="remove(record.key)">-->
    <!--              <a>删除</a>-->
    <!--            </a-popconfirm>-->
    <!--          </span>-->
    <!--          <span v-else>-->
    <!--            <a @click="saveRow(record)">保存</a>-->
    <!--            <a-divider type="vertical" />-->
    <!--            <a @click="cancel(record.key)">取消</a>-->
    <!--          </span>-->
    <!--        </template>-->
    <!--        <span v-else>-->
    <!--          <a @click="toggle(record.key)">编辑</a>-->
    <!--          <a-divider type="vertical" />-->
    <!--          <a-popconfirm title="是否要删除此行？" @confirm="remove(record.key)">-->
    <!--            <a>删除</a>-->
    <!--          </a-popconfirm>-->
    <!--        </span>-->
    <!--      </template>-->
    <!--    </a-table>-->
    <!--    <a-button style="width: 100%; margin-top: 16px; margin-bottom: 8px" type="dashed" icon="plus" @click="newMember">新增成员</a-button>-->
  </div>

</template>

<script>
import { getScenePage } from '@/api/manage'
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
          scopedSlots: { customRender: 'operation' }
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
      optionAlertShow: false
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
    handleNew () {
      this.$router.push({ path: '/retry/scene/config' })
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
        deadlineRequest: '60000',
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
