<template>
  <div>
    <a-modal :visible="visible" title="新增任务" @ok="handleOk" @cancel="visible = false" width="800px">
      <a-form @submit="handleOk" :form="form" v-bind="formItemLayout">
        <a-form-item label="组">
          <a-select
            placeholder="请选择组"
            v-decorator="['groupName', { rules: [{ required: true, message: '请选择组' }] }]"
            @change="(value) => handleChange(value)"
          >
            <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="重试状态">
          <a-select
            placeholder="请选择重试状态"
            v-decorator="['retryStatus', { rules: [{ required: true, message: '请选择重试状态' }] }]"
          >
            <a-select-option v-for="(value, key) in retryStatus" :value="key" :key="key"> {{ value }}</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="日志信息">
          <a-textarea
            rows="4"
            allow-clear
            placeholder="请输入日志信息"
            v-decorator="['logStr', { rules: [{ required: true, message: '请输入包含<|>参数<|>的日志信息.' }, { validator: handleLogStr }], validateTrigger: 'change' }]"
          />
          <a :href="officialWebsite + '/pages/b74542/#如何获取日志信息'" target="_blank"> 获取日志信息？</a>
        </a-form-item>

      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { getAllGroupNameList, getSceneList, batchSaveRetryTask } from '@/api/manage'
import { officialWebsite } from '@/utils/util'

export default {
  name: 'SavRetryTask',
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
      officialWebsite: officialWebsite()
    }
  },
  methods: {
    handleOk (e) {
      console.log(e)
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log(values)
          batchSaveRetryTask(values).then((res) => {
            this.form.resetFields()
            this.$message.success('新增任务成功')
            this.visible = false
            this.$emit('refreshTable', 1)
          })
        }
      })
    },
    handleChange (value) {
      getSceneList({ groupName: value }).then((res) => {
        this.sceneList = res.data
      })
    },
    isShow (visible, data) {
      this.visible = visible
      getAllGroupNameList().then((res) => {
        this.groupNameList = res.data
      })
    },
    handleLogStr (rule, value, callback) {
      if (!value) {
        return callback()
      }

      const regex = /<\|>(.*?)<\|>/g

      let matchCount = 0
      let result
      while ((result = regex.exec(value)) !== null) {
        const matchedData = result[1]
        console.log(matchedData)
        matchCount++
      }

      console.log('符合条件的数据条数：' + matchCount)

      if (matchCount === 0) {
        return callback(new Error('未包含<|>'))
      } else if (matchCount > 500) {
        return callback(new Error('最多只能提交500个有效数据'))
      } else {
        return callback()
      }
    }
  }
}
</script>

<style scoped>
</style>
