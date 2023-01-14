<template>
  <div>
    <a-table
      :columns="notifyColumns"
      :dataSource="data"
      :pagination="false"
      :loading="memberLoading"
    >
      <template v-for="(col, i) in ['description']" :slot="col" slot-scope="text, record">
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
      <template slot="notifyAttribute" slot-scope="text, record">
        <a-textarea
          v-if="record.editable"
          style="margin: -5px 0"
          :value="parseJson(text, record)"
          auto-size
          :placeholder="notifyColumns.find(item => item.key === 'notifyAttribute').title"
          @click="handleBlur(record)"
        />
        <template v-else>
          <span v-html="parseJson(text, record).replaceAll(&quot;\r\n&quot;, &quot;</br>&quot;)"></span>
        </template>
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

    <a-modal :visible="visible" title="添加配置" @ok="handleOk" @cancel="handlerCancel" width="1000px">
      <a-form :form="form" @submit="handleSubmit" :body-style="{padding: '0px 0px'}" v-bind="formItemLayout" >
        <a-form-item
          v-if="this.notifyTypeValue === '1'"
          label="钉钉URL">
          <a-input
            placeholder="请输入钉钉URL"
            v-decorator="[
              'dingDingUrl',
              {rules: [{ required: true, message: '请输入钉钉URL', whitespace: true}]}
            ]" />
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="用户名">
          <a-input
            placeholder="请输入用户名"
            v-if="this.notifyTypeValue === '2'"
            v-decorator="[
              'user',
              {rules: [{ required: true, message: '请输入用户名', whitespace: true}]}
            ]" />
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="密码">
          <a-input
            placeholder="请输入密码"
            v-if="this.notifyTypeValue === '2'"
            v-decorator="[
              'pass',
              {rules: [{ required: true, message: '请输入密码', whitespace: true}]}
            ]"/>
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="SMTP地址">
          <a-input
            placeholder="请输入邮件服务器的SMTP地址"
            v-if="this.notifyTypeValue === '2'"
            v-decorator="[
              'host',
              {rules: [{ required: true, message: '请输入邮件服务器的SMTP地址', whitespace: true}]}
            ]"/>
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="SMTP端口">
          <a-input
            v-if="this.notifyTypeValue === '2'"
            placeholder="请输入邮件服务器的SMTP端口"
            v-decorator="[
              'port',
              {rules: [{ required: true, message: '请输入邮件服务器的SMTP端口', whitespace: true}]}
            ]"/>
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="发件人">
          <a-input
            v-if="this.notifyTypeValue === '2'"
            placeholder="请输入发件人"
            v-decorator="[
              'from',
              {rules: [{ required: true, message: '请输入发件人', whitespace: true}]}
            ]"/>
        </a-form-item>
        <a-form-item
          v-if="this.notifyTypeValue === '2'"
          label="收件人">
          <a-input
            v-if="this.notifyTypeValue === '2'"
            placeholder="请输入收件人"
            v-decorator="[
              'tos',
              {rules: [{ required: true, message: '请输入收件人', whitespace: true}]}
            ]"/>
        </a-form-item>
        <a-form-item
          :wrapper-col="{
            xs: { span: 24, offset: 0 },
            sm: { span: 16, offset: 8 },
            lg: { span: 7 }
          }">
        </a-form-item>
      </a-form>
    </a-modal>
  </div>

</template>

<script>
import { getNotifyConfigList } from '@/api/manage'
import pick from 'lodash.pick'

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
          title: '配置属性',
          dataIndex: 'notifyAttribute',
          key: 'notifyAttribute',
          width: '25%',
          scopedSlots: { customRender: 'notifyAttribute' }
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
      form: this.$form.createForm(this),
      formItemLayout: {
        labelCol: { lg: { span: 7 }, sm: { span: 7 } },
        wrapperCol: { lg: { span: 10 }, sm: { span: 17 } }
      },
      memberLoading: false,
      notifyScene: {
        '1': '重试数量超过阈值',
        '2': '重试失败数量超过阈值',
        '3': '客户端上报失败',
        '4': '客户端组件异常'
      },
      notifyType: {
        '1': '钉钉通知',
        '2': '邮箱通知'
        // '3': '企业微信'
      },
      notifyThresholdDisabled: ['3', '4'],
      visible: false,
      key: '',
      notifyTypeValue: '1'
    }
  },
  created () {
    const groupName = this.$route.query.groupName
    if (groupName) {
      getNotifyConfigList({ groupName: groupName }).then(res => {
        res.data.map(record => {
          const { id, notifyType, notifyThreshold, notifyScene, description, notifyAttribute } = record
          this.data.push({
            key: id,
            notifyType: notifyType.toString(),
            notifyThreshold: notifyThreshold,
            notifyScene: notifyScene.toString(),
            description: description,
            notifyAttribute: JSON.parse(notifyAttribute),
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
      const { key, notifyType, notifyThreshold, notifyAttribute, notifyScene, description } = delData
      this.formData.push({
        key: key,
        notifyType: notifyType,
        notifyThreshold: notifyThreshold,
        notifyScene: notifyScene,
        notifyAttribute: notifyAttribute,
        description: description,
        isDeleted: 1
      })

      const newData = this.data.filter(item => item.key !== key)
      this.data = newData
    },
    saveRow (record) {
      this.memberLoading = true
      const { key, notifyType, notifyThreshold, notifyAttribute, notifyScene, description } = record

      if (!notifyType || !notifyScene || !notifyAttribute || !description || (this.notifyThresholdDisabled.includes(notifyScene) ? false : !notifyThreshold)) {
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
          notifyAttribute: JSON.stringify(notifyAttribute),
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
    handleBlur (record) {
      this.key = record.key
      this.notifyTypeValue = record.notifyType
      new Promise((resolve) => {
        setTimeout(resolve, 1500)
      }).then(() => {
        const { form } = this
        const formData = pick(record.notifyAttribute, ['dingDingUrl', 'user', 'pass', 'host', 'port', 'from', 'tos'])
        console.log(formData)
        form.setFieldsValue(formData)
      })
      this.visible = !this.visible
    },
    handleOk () {
      this.form.validateFields((err, values) => {
        if (!err) {
          this.handleChange(values, this.key, 'notifyAttribute')
          this.visible = false
          this.key = ''
        }
      })
    },
    handleSubmit (e) {
      e.preventDefault()
    },
    handlerCancel () {
      this.visible = false
    },
    parseJson (text, record) {
      if (!text) {
        return null
      }

      let s =
        '用户名:' + text['user'] + ';\r\n' +
        '密码:' + text['pass'] + ';\r\n' +
        'SMTP地址:' + text['host'] + ';\r\n' +
        'SMTP端口:' + text['port'] + ';\r\n' +
        '发件人:' + text['from'] + ';\r\n' +
        '收件人:' + text['tos'] + ';'

      if (record.notifyType === '1') {
         s = '钉钉地址:' + text['dingDingUrl'] + ';'
      }

      return s
    },
    newMember () {
      const length = this.data.length
      this.data.push({
        key: length === 0 ? '1' : (parseInt(this.data[length - 1].key) + 1).toString(),
        notifyType: '1',
        notifyScene: '1',
        notifyThreshold: null,
        notifyAttribute: '',
        description: '',
        editable: true,
        isNew: true
      })

      const { form } = this
      form.resetFields()
    }
  }
}
</script>

<style scoped>

</style>
