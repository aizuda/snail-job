<template>
  <a-card class="card" title="组配置" :bordered="false">
    <a-form @submit="handleSubmit" :form="form" :body-style="{padding: '24px 32px'}" v-bind="formItemLayout" >
      <a-form-item>
        <a-input
          hidden
          v-decorator="['id']" />
      </a-form-item>
      <a-form-item
        label="组名称">
        <a-input
          placeholder="请输入用户名"
          v-decorator="[
            'username',
            {rules: [{ required: true, message: '请输入用户名', whitespace: true}]}
          ]" />
      </a-form-item>
      <a-form-item
        label="密码">
        <a-input
          placeholder="请输入密码"
          v-decorator="[
            'password',
            {rules: [{ required: formType === 'create', message: '请输入密码', whitespace: true}]}
          ]"/>
      </a-form-item>
      <a-form-item
        label="角色">
        <a-select
          placeholder="请选择角色"
          v-decorator="[
            'role',
            {rules: [{ required: true, message: '请选择角色'}]}
          ]"
          @change="value => handleChange(value)">
          <a-select-option value="1">普通用户</a-select-option>
          <a-select-option value="2">管理员</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        label="权限"
        v-if="role !== '2'">
        <a-select
          mode="tags"

          style="width: 100%"
          :token-separators="[',']"
          v-decorator="[
            'groupNameList',
            {rules: [{ required: true, message: '请分配组'}]}
          ]">
          <a-select-option v-for="(item, index) in groupNameList" :key="index" :value="item">
            {{ item }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item
        :wrapper-col="{
          xs: { span: 24, offset: 0 },
          sm: { span: 16, offset: 8 },
          lg: { span: 7 }
        }">
        <a-button htmlType="submit" type="primary">提交</a-button>
      </a-form-item>
    </a-form>
  </a-card>

</template>

<script>
import md5 from 'md5'
import { getAllGroupNameList, saveUser, getSystemUserByUserName } from '@/api/manage'
import pick from 'lodash.pick'

export default {
  name: 'UserForm',
  props: {
  },
  data () {
    return {
      form: this.$form.createForm(this),
      role: 0,
      formType: 'create',
      formItemLayout: {
        labelCol: { lg: { span: 7 }, sm: { span: 7 } },
        wrapperCol: { lg: { span: 10 }, sm: { span: 17 } }
      },
      groupNameList: []
    }
  },
  mounted () {
    getAllGroupNameList().then(res => {
      this.groupNameList = res.data
    })

    this.$nextTick(() => {
      if (this.$route.query.username) {
        getSystemUserByUserName({ username: this.$route.query.username }).then(res => {
          this.loadEditInfo(res.data)
        })
      }
    })
  },
  methods: {
    handleChange (role) {
      this.role = role
    },
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.password) {
            values.password = md5(values.password)
          }

          values.role = parseInt(values.role)
          saveUser(values).then(res => {
            this.$message.success('操作成功')
            this.$router.push('/user-list')
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
      this.formType = 'edit'
      const { form } = this
      // ajax
      new Promise((resolve) => {
        setTimeout(resolve, 1500)
      }).then(() => {
        const formData = pick(data, ['id', 'username', 'role', 'groupNameList'])
        formData.role = formData.role.toString()
        form.setFieldsValue(formData)
      })
    }
  }
}
</script>

<style scoped>

</style>
