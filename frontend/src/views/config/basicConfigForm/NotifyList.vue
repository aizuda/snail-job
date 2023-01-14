<template>
  <div>
    <a-table
      :columns="notifyColumns"
      :dataSource="data"
      :pagination="false"
      :loading="memberLoading"
    >
      <template v-for="(col, i) in ['notifyAddress', 'description']" :slot="col" slot-scope="text, record">
        <a-input
          :key="col"
          v-if="record.editable"
          style="margin: -5px 0"
          :value="text"
          :placeholder="notifyColumns.find(item => item.key === col).title"
          @change="e => handleChange(e.target.value, record.key, col)"
        />
        <template v-else>{{ text }}</template>
      </template>
      <template slot="notifyScene" slot-scope="text, record">
        <a-select
          v-if="record.editable"
          placeholder="通知场景"
          style="width: 100%;"
          :value="text"
          @change="value => handleChange(value, record.key, 'notifyScene')">
          <a-select-option :value="key" v-for="(value, key) in notifyScene" :key="key">{{ value }}</a-select-option>
        </a-select>
        <template v-else>{{ notifyScene[text] }}</template>
      </template>
      <template slot="notifyType" slot-scope="text, record">
        <a-select
          v-if="record.editable"
          placeholder="通知类型"
          style="width: 100%;"
          :value="text"
          @change="value => handleChange(value, record.key, 'notifyType')">
          <a-select-option :value="key" v-for="(value, key) in notifyType" :key="key">{{ value }}</a-select-option>
        </a-select>
        <template v-else>{{ notifyType[text] }}</template>
      </template>
      <template slot="notifyThreshold" slot-scope="text, record">
        <a-input-number
          v-if="record.editable"
          :min="1"
          :max="999999"
          style="width: 100%;"
          :value="text"
          :disabled="notifyThresholdDisabled.includes(data.find(item => item.key === record.key).notifyScene)"
          placeholder="通知阈值"
          @change="value => handleChange(value, record.key, 'notifyThreshold')"/>
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
import { getNotifyConfigList } from '@/api/manage'

export default {
  name: 'NotifyList',
  data () {
    return {
      notifyColumns: [
        {
          title: '通知类型',
          dataIndex: 'notifyType',
          key: 'notifyType',
          width: '12%',
          scopedSlots: { customRender: 'notifyType' }
        },
        {
          title: '通知场景',
          dataIndex: 'notifyScene',
          key: 'notifyScene',
          width: '15%',
          scopedSlots: { customRender: 'notifyScene' }
        },
        {
          title: '通知阈值',
          dataIndex: 'notifyThreshold',
          key: 'notifyThreshold',
          width: '12%',
          scopedSlots: { customRender: 'notifyThreshold' }
        },
        {
          title: '通知地址',
          dataIndex: 'notifyAddress',
          key: 'notifyAddress',
          width: '25%',
          scopedSlots: { customRender: 'notifyAddress' }
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
      memberLoading: false,
      notifyScene: {
        '1': '重试数量超过阈值',
        '2': '重试失败数量超过阈值',
        '3': '客户端上报失败',
        '4': '客户端组件异常'
      },
      notifyType: {
        '1': '钉钉通知',
        '2': '邮箱通知',
        '3': '企业微信'
      },
      notifyThresholdDisabled: ['3', '4']
    }
  },
  created () {
    const groupName = this.$route.query.groupName
    if (groupName) {
      getNotifyConfigList({ groupName: groupName }).then(res => {
        res.data.map(record => {
          const { id, notifyType, notifyThreshold, notifyScene, description, notifyAddress } = record
          this.data.push({
            key: id,
            notifyType: notifyType.toString(),
            notifyThreshold: notifyThreshold,
            notifyScene: notifyScene.toString(),
            description: description,
            notifyAddress: notifyAddress,
            editable: false,
            isNew: false
          })
        })
      })
    }
  },
  methods: {
    remove (delKey) {
      const delData = this.data.find(item => delKey === item.key)
      const { key, notifyType, notifyThreshold, notifyAddress, notifyScene, description } = delData
      this.formData.push({
        key: key,
        notifyType: notifyType,
        notifyThreshold: notifyThreshold,
        notifyScene: notifyScene,
        notifyAddress: notifyAddress,
        description: description,
        isDeleted: 1
      })

      const newData = this.data.filter(item => item.key !== key)
      this.data = newData
    },
    saveRow (record) {
      this.memberLoading = true
      const { key, notifyType, notifyThreshold, notifyAddress, notifyScene, description } = record

      if (!notifyType || !notifyScene || !notifyAddress || !description || (this.notifyThresholdDisabled.includes(notifyScene) ? false : !notifyThreshold)) {
        this.memberLoading = false
        this.$message.error('请填写完整成员信息。')
        return
      }

      const target = this.formData.find(item => key === item.key)
      if (!target) {
        this.formData.push({
          key: key,
          notifyType: notifyType,
          notifyThreshold: notifyThreshold,
          notifyScene: notifyScene,
          notifyAddress: notifyAddress,
          description: description,
          isDeleted: 0
        })
      }

      // 模拟网络请求、卡顿 800ms
      new Promise((resolve) => {
        setTimeout(() => {
          resolve({ loop: false })
        }, 100)
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
      const newData = [...this.data]
      const target = newData.find(item => key === item.key)
      if (target) {
        target[column] = value
        this.data = newData
      }
    },
    newMember () {
      const length = this.data.length
      this.data.push({
        key: length === 0 ? '1' : (parseInt(this.data[length - 1].key) + 1).toString(),
        notifyType: '1',
        notifyScene: '1',
        notifyThreshold: null,
        notifyAddress: '',
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
