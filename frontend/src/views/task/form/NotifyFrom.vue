<template>
  <div>
    <page-header-wrapper content="场景配置" @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card :body-style="{padding: '24px 32px'}" :bordered="false" :loading="loading">
      <a-form @submit="handleSubmit" :form="form" class="form-row" layout="vertical" style="width: 40%;margin: auto;">
        <a-row class="form-row" :gutter="16">
          <a-col :lg="18" :md="12" :sm="24">
            <a-form-item>
              <a-input
                hidden
                v-decorator="['id']" />
            </a-form-item>
            <a-form-item label="通知场景">
              <a-select
                placeholder="通知场景"
                style="width: 100%;"
                @change="changeNotifyScene"
                v-decorator="[
                  'notifyScene',
                  {
                    initialValue: '1',
                    rules: [{ required: true, message: '请选择状态'}]
                  }
                ]"
              >
                <a-select-option :value="index" v-for="(item, index) in notifyScene" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item>
              <a-input
                hidden />
            </a-form-item>
            <a-form-item label="通知阈值">
              <a-input
                v-if="notifyThresholdDisabled.includes(this.notifySceneValue)"
                disabled />
              <a-input-number
                v-else
                id="inputNumber"
                :min="1"
                style="width: -webkit-fill-available"
                v-decorator="[
                  'notifyThreshold',
                  {
                    initialValue: '16',
                    rules: [{ required: !notifyThresholdDisabled.includes(this.notifySceneValue), message: '请输入通知阈值'}]
                  }
                ]" />

            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="18" :md="18" :sm="24">
            <a-form-item label="组">
              <a-select
                placeholder="请选择组"
                v-decorator="['groupName', { rules: [{ required: true, message: '请选择组' }] }]"
              >
                <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="6" :md="12" :sm="24">
            <a-form-item label="状态">
              <a-select
                placeholder="请选择状态"
                v-decorator="[
                  'notifyStatus',
                  {
                    initialValue: '1',
                    rules: [{ required: true, message: '请选择状态'}]
                  }
                ]" >
                <a-select-option :value="index" v-for="(item, index) in notifyStatus" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8" :md="12" :sm="12">
            <a-form-item label="通知类型">
              <a-select
                @change="handleChange"
                placeholder="通知类型"
                style="width: 100%;"
                v-decorator="[
                  'notifyType',
                  {
                    initialValue: '1',
                    rules: [{ required: true, message: '请选择状态'}]
                  }
                ]"
              >
                <a-select-option :value="index" v-for="(item, index) in notifyType" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="16" :md="12" :sm="12">
            <a-form-item label="配置属性">
              <a-input
                placeholder="请输入配置属性"
                @click="handleBlur"
                v-decorator="[
                  'notifyAttribute',
                  {rules: [{ required: true, message: '请输入配置属性', whitespace: true}]}
                ]" />

            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="24" :md="24" :sm="24">
            <a-form-item label="描述">
              <a-input
                placeholder="请输入描述"
                type="textarea"
                v-decorator="[
                  'description',
                  {rules: [{required: false, max: 256, message: '最多支持256个字符！'}]}
                ]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item
          :wrapperCol="{ span: 24 }"
          style="text-align: center"
        >
          <a-button htmlType="submit" type="primary">提交</a-button>
          <a-button style="margin-left: 8px">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <a-modal :visible="visible" title="添加配置" @ok="handleOk" @cancel="handlerCancel" width="1000px">
      <a-form :form="notifyAttributeForm" @submit="handleSubmit" :body-style="{padding: '0px 0px'}" v-bind="formItemLayout" >
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
          v-if="this.notifyTypeValue === '4'"
          label="飞书URL">
          <a-input
            placeholder="请输入飞书URL"
            v-decorator="[
              'larkUrl',
              {rules: [{ required: true, message: '请输入飞书URL', whitespace: true}]}
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
import { getAllGroupNameList } from '@/api/manage'
import { getNotifyConfigDetail, saveNotify, updateNotify } from '@/api/retryApi'
import pick from 'lodash.pick'
import CronModal from '@/views/job/from/CronModal'

const enums = require('@/utils/retryEnum')
export default {
  name: 'NotifyFrom',
  components: { CronModal },
  props: {},
  comments: {
    CronModal
  },
  data () {
    return {
      form: this.$form.createForm(this),
      formItemLayout: {
        labelCol: { lg: { span: 7 }, sm: { span: 7 } },
        wrapperCol: { lg: { span: 10 }, sm: { span: 17 } }
      },
      formItemLayoutWithOutLabel: {
        wrapperCol: {
          xs: { span: 24, offset: 0 },
          sm: { span: 20, offset: 4 }
        }
      },
      formType: 'create',
      groupNameList: [],
      notifyScene: enums.notifyScene,
      notifyType: enums.notifyType,
      notifyStatus: enums.notifyStatus,
      loading: false,
      visible: false,
      count: 0,
      notifyTypeValue: '1',
      notifyAttribute: '',
      notifyThresholdDisabled: ['3', '4'],
      notifySceneValue: '1'

    }
  },
  beforeCreate () {
    this.notifyAttributeForm = this.$form.createForm(this, { name: 'notify_attribute_form_item' })
  },
  mounted () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
    })

    this.$nextTick(() => {
      const id = this.$route.query.id
      console.log(id)
      if (id) {
        this.loading = true
        getNotifyConfigDetail(id).then(res => {
          this.loadEditInfo(res.data)
          this.loading = false
        })
      }
    })
  },
  methods: {
    handleChange (notifyType) {
      this.notifyTypeValue = notifyType
    },
    changeNotifyScene (notifyScene) {
      console.log(notifyScene)
      this.notifySceneValue = notifyScene
    },
    handleBlur () {
      new Promise((resolve) => {
        setTimeout(resolve, 100)
      }).then(() => {
        if (this.formType === 'edit') {
          const formData = pick(JSON.parse(this.notifyAttribute), ['dingDingUrl', 'larkUrl', 'user', 'pass', 'host', 'port', 'from', 'tos'])
          console.log(formData)

          this.notifyAttributeForm.getFieldDecorator(`dingDingUrl`, { initialValue: formData.dingDingUrl, preserve: true })
        }

        this.visible = !this.visible
      })
    },
    handlerCancel () {
      this.visible = false
    },
    handleOk () {
      this.notifyAttributeForm.validateFields((err, values) => {
        if (!err) {
          console.log(values)
          const { form } = this
          const formData = pick(values, ['dingDingUrl', 'larkUrl', 'user', 'pass', 'host', 'port', 'from', 'tos'])
          console.log(this.parseJson(formData))
          this.notifyAttribute = JSON.stringify(formData)
          form.setFieldsValue({
            notifyAttribute: this.parseJson(formData)
          })
          this.visible = false
        }
      })
    },
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          values['notifyAttribute'] = this.notifyAttribute
          if (this.formType === 'create') {
            saveNotify(values).then(res => {
              this.$message.success('任务新增完成')
              this.form.resetFields()
              this.$router.go(-1)
            })
          } else {
            updateNotify(values).then(res => {
              this.$message.success('任务更新完成')
              this.form.resetFields()
              this.$router.go(-1)
            })
          }
        }
      })
    },
    loadEditInfo (data) {
      this.formType = 'edit'
      const { form } = this
      // ajax
      new Promise((resolve) => {
        setTimeout(resolve, 100)
      }).then(() => {
        const formData = pick(data, [
          'id', 'notifyAttribute', 'groupName', 'notifyStatus', 'notifyScene', 'notifyThreshold', 'notifyType', 'notifyThreshold', 'description'])
        formData.notifyStatus = formData.notifyStatus.toString()
        formData.notifyScene = formData.notifyScene.toString()
        formData.notifyType = formData.notifyType.toString()
        formData.notifyThreshold = formData.notifyThreshold.toString()
        this.notifyTypeValue = formData.notifyType
        this.notifyAttribute = formData.notifyAttribute
        this.notifySceneValue = formData.notifyScene
        formData.notifyAttribute = this.parseJson(JSON.parse(formData.notifyAttribute))
        console.log(formData)
        form.setFieldsValue(formData)
      })
    },
    parseJson (json) {
      if (!json) {
        return null
      }

      let s =
        '用户名:' + json['user'] + ';' +
        '密码:' + json['pass'] + ';' +
        'SMTP地址:' + json['host'] + ';' +
        'SMTP端口:' + json['port'] + ';' +
        '发件人:' + json['from'] + ';' +
        '收件人:' + json['tos'] + ';'

      if (this.notifyTypeValue === '1') {
        s = json['dingDingUrl']
      } else if (this.notifyTypeValue === '4') {
        s = json['larkUrl']
      }

      return s
    }
  }
}
</script>
