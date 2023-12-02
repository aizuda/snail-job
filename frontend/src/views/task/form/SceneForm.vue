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
                :disabled="this.formType === 'edit'"
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
                :disabled="this.formType === 'edit'"
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
                placeholder="10s,15s,30s,35s,40s,50s,1m,2m,4m,6m,8m,10m,20m,40m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,11h,12h"
                disabled
              >
                <a-tooltip slot="suffix" title="10s,15s,30s,35s,40s,50s,1m,2m,4m,6m,8m,10m,20m,40m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,11h,12h">
                  <a-icon type="info-circle" style="color: rgba(0, 0, 0, 0.45)" />
                </a-tooltip>
              </a-input>

              <a-input-number
                v-if="backOff === '2' || backOff === '4'"
                style="width: -webkit-fill-available"
                placeholder="请输入间隔时长(秒)"
                :min="10"
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
                :max="this.backOff === '1' ? 26 : 9999999"
                v-decorator="[
                  'maxRetryCount',
                  {
                    initialValue: '16',
                    rules: [{ required: true, message: '请输入最大重试次数'}]
                  }
                ]" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row class="form-row" :gutter="16">
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
          <a-button style="margin-left: 8px" @click="resetFiled">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <cron-modal ref="cronModalRef" @getCron="getCron"/>
  </div>
</template>

<script>
import { getAllGroupNameList } from '@/api/manage'
import { saveScene, updateScene, getSceneDetail } from '@/api/retryApi'
import pick from 'lodash.pick'
import CronModal from '@/views/job/form/CronModal'

const enums = require('@/utils/retryEnum')
export default {
  name: 'SceneFrom',
  components: { CronModal },
  props: {},
  comments: {
    CronModal
  },
  data () {
    return {
      form: this.$form.createForm(this),
      formType: 'create',
      groupNameList: [],
      routeKey: enums.routeKey,
      backOffLabels: enums.backOffLabels,
      sceneStatus: enums.sceneStatus,
      loading: false,
      visible: false,
      count: 0,
      backOff: '2'
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
        getSceneDetail(id).then(res => {
          this.loadEditInfo(res.data)
          this.loading = false
        })
      }
    })
  },
  methods: {
    resetFiled () {
      this.form.resetFields()
    },
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
            saveScene(values).then(res => {
              this.$message.success('场景新增完成')
              this.form.resetFields()
              this.$router.go(-1)
            })
          } else {
            updateScene(values).then(res => {
              this.$message.success('场景更新完成')
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
          'id', 'sceneName', 'groupName', 'sceneStatus', 'deadlineRequest', 'maxRetryCount', 'description',
          'backOff', 'triggerInterval', 'executorTimeout'])
        formData.sceneStatus = formData.sceneStatus.toString()
        formData.backOff = formData.backOff.toString()
        this.backOff = formData.backOff
        form.setFieldsValue(formData)
      })
    }
  }
}
</script>
