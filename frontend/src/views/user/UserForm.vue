<template>
  <div>
    <page-header-wrapper :content="formType === 'create' ? '新增用户':'更新用户信息'" @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card class="card" title="" :bordered="false">
      <a-form @submit="handleSubmit" :form="form" :body-style="{padding: '24px 32px'}" v-bind="formItemLayout">
        <a-form-item>
          <a-input
            hidden
            v-decorator="['id']" />
        </a-form-item>
        <a-form-item
          label="用户名">
          <a-input
            placeholder="请输入用户名"
            v-decorator="[
              'username',
              {rules: [{ required: true, message: '请输入用户名', whitespace: true}]}
            ]" />
        </a-form-item>
        <a-form-item
          label="修改密码"
          v-if="formType === 'edit'">
          <a-switch checked-children="修改" un-checked-children="不修改" @change="onChange" />
        </a-form-item>
        <a-form-item
          label="密码"
          v-if="updatePass">
          <a-input
            placeholder="请输入密码"
            type="password"
            autocomplete="off"
            v-decorator="[
              'password',
              {rules: [{ required: true, message: '请输入密码', whitespace: true}, {validator: validatePass, trigger: ['change', 'blur']}]}
            ]"/>
        </a-form-item>
        <a-form-item
          label="确认密码"
          v-if="updatePass">
          <a-input
            placeholder="请输入确认密码"
            type="password"
            autocomplete="off"
            v-decorator="[
              'checkPassword',
              {rules: [{ required: true, message: '请输入确认密码', whitespace: true}, {validator: validateCheckPass, trigger: ['change', 'blur']}]}
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
          label="命名空间"
          v-if="role !== '2'">
          <a-select
            mode="tags"
            style="width: 100%"
            :token-separators="[',']"
            @change="value => handleNamespacesIdChange(value)"
            v-decorator="[
              'namespaceIds',
              {rules: [{ required: true, message: '请分配命名空间'}]}
            ]">
            <a-select-option v-for="(item, index) in namespaceList" :key="index" :value="item.uniqueId">
              {{ item.name }} ({{ item.uniqueId }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item
          label="组"
          v-if="role !== '2'">
          <a-select
            mode="tags"
            style="width: 100%"
            :token-separators="[',']"
            v-decorator="[
              'permissions',
              {rules: [{ required: true, message: '请分配组'}]}
            ]">
            <a-select-option v-for="(item, index) in groupNameList" :key="index" :value="item.groupName + '@'+ item.namespaceId">
              {{ item.groupName }} ({{ item.namespaceId }})
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
  </div>

</template>

<script>
import md5 from 'md5'
import {
  saveUser,
  getSystemUserByUserName,
  getAllNamespace,
  allGroupConfigList
} from '@/api/manage'
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
      groupNameList: [],
      namespaceList: [],
      updatePass: true
    }
  },
  mounted () {
    getAllNamespace().then(res => {
      this.namespaceList = res.data
    })

    this.$nextTick(() => {
      if (this.$route.query.username) {
        this.updatePass = false
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
    handleNamespacesIdChange (namespacesIds) {
      allGroupConfigList(namespacesIds).then(res => {
        this.groupNameList = res.data
      })
    },
    handleSubmit (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          if (values.password) {
            values.password = md5(values.password)
          }

          if (values.permissions) {
            values['permissions'] = values.permissions.map(input => {
              const [groupName, namespaceId] = input.split('@')
              console.log(this.groupNameList)
              return this.groupNameList.filter(item =>
                item.groupName === groupName && item.namespaceId === namespaceId
              )
            }).flat()
          }

          values.role = parseInt(values.role)
          saveUser(values).then(res => {
            this.$message.success('操作成功')
            this.$router.push('/user-list')
          })
        }
      })
    },
    validatePass (rule, value, callback) {
      if (value) {
        if (this.form.getFieldValue('checkPassword') !== '') {
          this.form.validateFields(['checkPassword'], (errors, values) => {
            console.log(errors)
          })
        }
      }
        callback()
    },
    validateCheckPass (rule, value, callback) {
      console.log(value)
      if (value) {
        console.log(value)
        if (value !== this.form.getFieldValue('password')) {
          callback(new Error('两次密码不匹配!'))
        } else {
          callback()
        }
      }
    },
    loadEditInfo (data) {
      this.formType = 'edit'
      const { form } = this
      // ajax
      new Promise((resolve) => {
        setTimeout(resolve, 100)
      }).then(() => {
        const formData = pick(data, ['id', 'username', 'role', 'permissions', 'namespaceIds'])
        formData.role = formData.role.toString()
        this.role = formData.role
        formData.namespaceIds = formData.namespaceIds.map(i => i.uniqueId)
        formData.permissions = formData.permissions.map(i => i.groupName + '@' + i.namespaceId)
        form.setFieldsValue(formData)
        this.handleNamespacesIdChange(formData.namespaceIds)
      })
    },
    onChange (checked) {
      this.updatePass = checked
    }

  }
}
</script>

<style scoped>

</style>
