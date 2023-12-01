<template>
  <a-modal :visible="visible" title="命名空间配置" @ok="handleOk" @cancel="visible = false" width="650px">
    <a-form @submit="handleOk" :form="form" :body-style="{padding: '24px 32px'}" v-bind="formItemLayout" >
      <a-form-item>
        <a-input
          hidden
          v-decorator="['id']" />
      </a-form-item>
      <a-form-item
        label="唯一标识(默认UUID)">
        <a-input
          placeholder="唯一标识"
          :disabled="isEdit"
          v-decorator="[
            'uniqueId',
            {rules: [{ required: false, message: '请输入空间名称', whitespace: true},{required: true, max: 64, message: '最多支持64个字符！'}, {validator: validate, trigger: ['change', 'blur']}]}
          ]" />
      </a-form-item>
      <a-form-item
        label="空间名称">
        <a-input
          placeholder="请输入空间名称"
          v-decorator="[
            'name',
            {rules: [{ required: true, message: '请输入空间名称', whitespace: true}]}
          ]" />
      </a-form-item>
    </a-form></a-modal>

</template>

<script>
import { addNamespace, updateNamespace } from '@/api/manage'
import pick from 'lodash.pick'

export default {
  name: 'NamespaceForm',
  props: {
    isEdit: {
      type: Boolean,
      default: false
    }
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
      visible: false
    }
  },
  methods: {
    isShow (record) {
      this.formType = record ? 'edit' : 'create'
      this.loadEditInfo(record)
      this.visible = true
      this.form.resetFields()
    },
    validate (rule, value, callback) {
      const regex = /^[A-Za-z0-9_]+$/
      if (!regex.test(value)) {
        callback(new Error('仅支持数字字母下划线'))
      }
      callback()
    },
    handleOk (e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          if (!this.isEdit) {
            addNamespace(values).then(res => {
              this.$message.success('操作成功')
              this.$emit('refreshTable', 1)
              this.visible = false
            })
          } else {
            updateNamespace(values).then(res => {
              this.$message.success('操作成功')
              this.$emit('refreshTable', 1)
              this.visible = false
            })
          }

          this.$store.dispatch('GetInfo')
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
        const formData = pick(data, ['id', 'name', 'uniqueId'])
        form.setFieldsValue(formData)
      })
    }
  }
}
</script>

<style scoped>

</style>
