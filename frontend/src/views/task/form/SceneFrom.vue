<template>
  <div>
    <page-header-wrapper content="场景配置" @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card :body-style="{padding: '24px 32px'}" :bordered="false" :loading="loading">
      <a-form @submit="handleSubmit" :form="form" class="form-row" layout="vertical" style="width: 40%;margin: auto;">
        <a-row class="form-row" :gutter="16">
          <a-col :lg="24" :md="24" :sm="24">
            <a-form-item>
              <a-input
                hidden
                v-decorator="['id']" />
            </a-form-item>
            <a-form-item label="场景名称" >
              <a-input
                placeholder="请输入场景名称"
                :maxLength="64"
                v-decorator="[
                  'sceneName',
                  {rules: [{ required: true, message: '请输入场景名称', whitespace: true},{required: true, max: 64, message: '最多支持64个字符！'}]}
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
                  'sceneStatus',
                  {
                    initialValue: '1',
                    rules: [{ required: true, message: '请选择状态'}]
                  }
                ]" >
                <a-select-option :value="index" v-for="(item, index) in sceneStatus" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8" :md="12" :sm="12">
            <a-form-item label="退避策略">
              <a-select
                placeholder="请选择退避策略"
                @change="handleChange"
                v-decorator="[
                  'backOff',
                  {
                    initialValue: '2',
                    rules: [{ required: true, message: '请选择退避策略'}]
                  }
                ]" >
                <a-select-option :value="index" v-for="(item, index) in backOffLabels" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :lg="16" :md="12" :sm="12">
            <a-form-item label="间隔时长">
              <a-input
                v-if="backOff === '1'"
                placeholder="1 2 4 5"
                disabled
              />

              <a-input-number
                v-if="backOff === '2' || backOff === '4'"
                style="width: -webkit-fill-available"
                placeholder="请输入间隔时长(秒)"
                :min="1"
                v-decorator="[
                  'triggerInterval',
                  {initialValue: '60',
                   rules: [ { required: true, message: '请输入间隔时长'}]}
                ]" />

              <a-input
                v-if="backOff === '3'"
                @click="handlerCron"
                placeholder="请输入间隔时长"
                v-decorator="[
                  'triggerInterval',
                  {rules: [{ required: true, message: '请输入间隔时长', whitespace: true}]}
                ]" />

            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8" :md="12" :sm="24">
            <a-form-item label="路由策略">
              <a-select
                placeholder="请选择路由策略"
                v-decorator="[
                  'routeKey',
                  {
                    initialValue: '4',
                    rules: [{ required: true, message: '请选择路由策略'}]
                  }
                ]" >
                <a-select-option :value="index" v-for="(item, index) in routeKey" :key="index">{{ item.name }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
          <a-col :lg="8" :md="24" :sm="24">
            <a-form-item label="超时时间(秒)">
              <a-input-number
                id="inputNumber"
                :min="1"
                :max="36000"
                style="width: -webkit-fill-available"
                v-decorator="[
                  'executorTimeout',
                  {
                    initialValue: '60',
                    rules: [{ required: true, message: '请输入超时时间'}]
                  }
                ]" />
            </a-form-item>
          </a-col>
          <a-col :lg="8" :md="24" :sm="24">
            <a-form-item label="最大重试次数">
              <a-input-number
                :min="1"
                style="width: -webkit-fill-available"
                v-decorator="[
                  'maxRetryCount',
                  {
                    initialValue: '3',
                    rules: [{ required: true, message: '请输入最大重试次数'}]
                  }
                ]" />
            </a-form-item>
          </a-col>
          <a-col :lg="8" :md="24" :sm="24">
            <a-form-item label="调用链超时时间(毫秒)">
              <a-input-number
                style="width: -webkit-fill-available"
                :min="100"
                :max="60000"
                v-decorator="[
                  'deadlineRequest',
                  {
                    initialValue: '60000',
                    rules: [{ required: true, message: '请输入调用链超时时间(毫秒)'}]
                  }
                ]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
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

    <cron-modal ref="cronModalRef" @getCron="getCron"/>
  </div>
</template>

<script>
import { getAllGroupNameList } from '@/api/manage'
import { getJobDetail, saveJob, updateJob } from '@/api/jobApi'
import pick from 'lodash.pick'
import CronModal from '@/views/job/from/CronModal'

const enums = require('@/utils/retryEnum')
export default {
  name: 'JobFrom',
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
      blockStrategy: enums.blockStrategy,
      executorType: enums.executorType,
      routeKey: enums.routeKey,
      backOffLabels: enums.backOffLabels,
      sceneStatus: enums.sceneStatus,
      loading: false,
      visible: false,
      count: 0,
      backOff: '1'

    }
  },
  beforeCreate () {
    this.dynamicForm = this.$form.createForm(this, { name: 'dynamic_form_item' })
    this.dynamicForm.getFieldDecorator('keys', { initialValue: [], preserve: true })
  },
  mounted () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
    })

    this.$nextTick(() => {
      const id = this.$route.query.id
      if (id) {
        this.loading = true
        getJobDetail(id).then(res => {
          this.loadEditInfo(res.data)
          this.loading = false
        })
      }
    })
  },
  methods: {
    handleChange (value) {
      console.log(value)
      this.backOff = value
      this.form.setFieldsValue({
        triggerInterval: null
      })
    },
    handlerCron () {
      const backOff = this.form.getFieldValue('backOff')
      if (backOff === '3') {
        let triggerInterval = this.form.getFieldValue('triggerInterval')
        if (triggerInterval === null || triggerInterval === '') {
          triggerInterval = '* * * * * ?'
        }
        this.$refs.cronModalRef.isShow(triggerInterval)
      }
    },
    getCron (cron) {
      this.form.setFieldsValue({
        triggerInterval: cron
      })
    },
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          if (this.formType === 'create') {
            saveJob(values).then(res => {
              this.$message.success('任务新增完成')
              this.form.resetFields()
              this.$router.go(-1)
            })
          } else {
            updateJob(values).then(res => {
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
          'id', 'jobName', 'groupName', 'jobStatus', 'executorInfo', 'argsStr', 'executorTimeout', 'description',
          'maxRetryTimes', 'parallelNum', 'retryInterval', 'triggerType', 'blockStrategy', 'executorType', 'taskType', 'triggerInterval'])
        formData.jobStatus = formData.jobStatus.toString()
        formData.taskType = formData.taskType.toString()
        formData.executorType = formData.executorType.toString()
        formData.blockStrategy = formData.blockStrategy.toString()
        formData.triggerType = formData.triggerType.toString()
        this.triggerTypeValue = formData.triggerType
        form.setFieldsValue(formData)
      })
    }
  }
}
</script>
