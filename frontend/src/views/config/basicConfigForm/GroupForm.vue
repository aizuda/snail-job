<template>
  <a-form @submit="handleSubmit" :form="form" class="form">

    <a-row class="form-row" :gutter="16">
      <a-col :lg="6" :md="12" :sm="24">
        <a-form-item label="组名称">
          <a-input
            placeholder="请输入组名称"
            hidden
            v-decorator="[
              'id',
            ]" />
          <a-input
            placeholder="请输入组名称"
            v-decorator="[
              'groupName',
              {rules: [{ required: true, message: '请输入组名称', whitespace: true}]}
            ]" />
        </a-form-item>
      </a-col>
      <a-col :lg="6" :md="12" :sm="24">
        <a-form-item
          label="状态">
          <a-select
            placeholder="请选择状态"
            v-decorator="[
              'groupStatus',
              {rules: [{ required: true, message: '请选择状态类型'}]}
            ]" >
            <a-select-option value="0">停用</a-select-option>
            <a-select-option value="1">启动</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :lg="6" :md="12" :sm="24">
        <a-form-item
          label="路由策略">
          <a-select
            placeholder="请选择路由策略"
            v-decorator="[
              'routeKey',
              {rules: [{ required: true, message: '请选择路由策略'}]}
            ]" >
            <a-select-option :value="key" v-for="(value, key) in routeKey" :key="key">{{ value }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :lg="6" :md="12" :sm="24">
        <a-form-item label="描述">
          <a-input
            placeholder="请输入描述"
            v-decorator="[
              'description',
              {rules: [{ required: true, message: '请输入描述', whitespace: true}]}
            ]" />
        </a-form-item>
      </a-col>
      <a-col :lg="3" :md="6" :sm="12">
        <a-form-item label="指定分区">
          <a-input-number
            id="inputNumber"
            placeholder="分区"
            v-decorator="[
              'groupPartition'
            ]"
            :min="1"
            :max="10"
          />
        </a-form-item>
      </a-col>
    </a-row>
    <a-form-item v-if="showSubmit">
      <a-button htmlType="submit" >Submit</a-button>
    </a-form-item>
  </a-form>
</template>

<script>
import { getGroupConfigByGroupName } from '@/api/manage'
import pick from 'lodash.pick'

export default {
  name: 'GroupForm',
  props: {
    showSubmit: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      routeKey: {
        '1': '一致性hash算法',
        '2': '随机算法',
        '3': '最近最久未使用算法'
      }
    }
  },
  mounted () {
    this.$nextTick(() => {
      const groupName = this.$route.query.groupName
      if (groupName) {
        getGroupConfigByGroupName(groupName).then(res => {
          this.loadEditInfo(res.data)
        })
      }
    })
  },
  methods: {
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          this.$notification['error']({
            message: 'Received values of form:',
            description: values
          })
        }
      })
    },
    validate (rule, value, callback) {
      const regex = /^user-(.*)$/
      if (!regex.test(value)) {
        callback(new Error('需要以 user- 开头'))
      }
      callback()
    },
    loadEditInfo (data) {
      const { form } = this
      // ajax
      console.log(`将加载 ${this.id} 信息到表单`)
      new Promise((resolve) => {
        setTimeout(resolve, 1500)
      }).then(() => {
        const formData = pick(data, ['id', 'groupName', 'routeKey', 'groupStatus', 'description', 'groupPartition'])
        formData.groupStatus = formData.groupStatus.toString()
        formData.routeKey = formData.routeKey.toString()
        console.log('formData', formData)
        form.setFieldsValue(formData)
      })
    }
  }
}
</script>

<style scoped>

</style>
