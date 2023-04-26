<template>
  <div>
    <a-modal :visible="visible" title="分配" @ok="handleOk" @cancel="visible = false">
      <a-form @submit="handleOk" :form="form" :body-style="{padding: '24px 32px'}" v-bind="formItemLayout">
        <a-form-item
          label="组">
          <a-select
            placeholder="请选择组"
            v-decorator="[
              'groupName',
              {rules: [{ required: true, message: '请选择组'}]}
            ]"
            @change="value => handleChange(value)">
            <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item
          label="场景名称">
          <a-select
            placeholder="请选择场景名称"
            v-decorator="[
              'sceneName',
              {rules: [{ required: true, message: '请选择场景名称'}]}
            ]"
            @change="value => handleChange(value)">
            <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName"> {{ item.sceneName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item
          :wrapper-col="{
            xs: { span: 24, offset: 0 },
            sm: { span: 16, offset: 8 },
            lg: { span: 7 }
          }">
        <!--          <a-button htmlType="submit" type="primary">提交</a-button>-->
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>

import { getAllGroupNameList, getSceneList } from '@/api/manage'

export default {
  name: 'savRetryTask',
  props: {
  },
  data () {
    return {
      visible: false,
      form: this.$form.createForm(this),
      formItemLayout: {
        labelCol: { lg: { span: 7 }, sm: { span: 7 } },
        wrapperCol: { lg: { span: 10 }, sm: { span: 17 } }
      },
      groupNameList: [],
      sceneList: []
    }
  },
  methods: {
    handleOk (e) {
      console.log(e)

      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          console.log(values)
        }
      })
    },
    handleChange (value) {
      getSceneList({ 'groupName': value }).then(res => {
        this.sceneList = res.data
      })
    },
    isShow (visible, data) {
      this.visible = visible
      getAllGroupNameList().then(res => {
        this.groupNameList = res.data
      })
    }
  }
}
</script>

<style scoped>

</style>
