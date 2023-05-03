<template>
  <div>
    <a-modal :visible="visible" title="批量更新" @ok="handleOk" @cancel="visible = false" width="800px">
      <a-form @submit="handleOk" :form="form" v-bind="formItemLayout">
        <a-alert message="批量更新只根据选择的数据进行更新, 请操作前确认您的选择的数据是否正确?" banner />
        <a-form-item label="执行器名称">
          <a-input
            v-decorator="['executorName', { rules: [{ required: false, message: '请输入执行器名称' }] }]"
            name="executorName"
            placeholder="请输入执行器名称"
          />
        </a-form-item>
        <a-form-item label="重试状态">
          <a-select
            placeholder="请选择重试状态"
            v-decorator="['retryStatus', { rules: [{ required: false, message: '请选择重试状态' }] }]"
          >
            <a-select-option v-for="(value, key) in retryStatus" :value="key" :key="key"> {{ value }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { batchUpdate } from '@/api/manage'

export default {
  name: 'BatchUpdateRetryTaskInfo',
  props: {},
  data () {
    return {
      visible: false,
      form: this.$form.createForm(this),
      formItemLayout: {
        labelCol: { lg: { span: 6 }, sm: { span: 7 } },
        wrapperCol: { lg: { span: 14 }, sm: { span: 17 } }
      },
      groupNameList: [],
      sceneList: [],
      retryStatus: {
        0: '重试中',
        1: '重试完成',
        2: '最大次数',
        3: '暂停'
      },
      groupName: '',
      ids: []
    }
  },
  methods: {
    handleOk (e) {
      console.log(e)
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log(values)
          if (values['executorName'] === undefined && values['retryStatus'] === undefined) {
            this.$message.error('无需要更新的内容, 请填写任意一项')
            return
          }
          values['groupName'] = this.groupName
          values['ids'] = this.ids
          batchUpdate(values).then((res) => {
            this.$emit('refreshTable', 1)
            this.form.resetFields()
            this.$message.success('更新任务成功')
            this.visible = false
          })
        }
      })
    },
    isShow (visible, data, ids) {
      this.visible = visible
      this.groupName = data[0].groupName
      this.ids = ids
    }
  }
}
</script>

<style scoped>
</style>
