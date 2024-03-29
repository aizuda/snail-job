<template>
  <div>
    <a-card :bordered="false" :loading="loading">
      <a-form @submit="handleSubmit" :form="form" class="form" style="width: 40%;margin: auto;">
        <Collapse v-model="activeKey" :bordered="false" :expand-icon-position="expandIconPosition">
          <template #expandIcon="props">
            <a-icon type="caret-right" :rotate="props.isActive ? 90 : 0" />
          </template>
          <CollapsePanel key="1" header="通用配置" :style="customStyle">
            <a-row class="form-row" :gutter="16">
              <a-col :lg="18" :md="12" :sm="24">
                <a-form-item hidden>
                  <a-input
                    hidden
                    v-decorator="[
                      'id',
                    ]" />
                </a-form-item>
                <a-form-item>
                  <span slot="label">
                    组名称&nbsp;
                    <a
                      :href="officialWebsite + '/pages/32e4a0/#组是何物' +
                        ''"
                      target="_blank">
                      <a-icon type="question-circle-o" />
                    </a>
                  </span>
                  <a-input
                    placeholder="请输入组名称"
                    :maxLength="64"
                    :disabled="this.id > 0"
                    v-decorator="[
                      'groupName',
                      {rules: [{ required: true, message: '请输入组名称', whitespace: true},{required: true, max: 64, message: '最多支持64个字符！'}, {validator: validate}]}
                    ]" />
                </a-form-item>
              </a-col>
              <a-col :lg="6" :md="12" :sm="24">
                <a-form-item>
                  <span slot="label">
                    状态&nbsp;
                    <a :href="officialWebsite + '/pages/32e4a0/#什么是组状态'" target="_blank">
                      <a-icon type="question-circle-o" />
                    </a>
                  </span>
                  <a-select
                    placeholder="请选择状态"
                    v-decorator="[
                      'groupStatus',
                      {initialValue: '1', rules: [{ required: true, message: '请选择状态类型'}]}
                    ]" >
                    <a-select-option value="0">停用</a-select-option>
                    <a-select-option value="1">启动</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row class="form-row" :gutter="16">
              <a-col :lg="24" :md="24" :sm="24">
                <a-form-item label="Token">
                  <a-input
                    placeholder="请输入Token"
                    :maxLength="64"
                    :disabled="this.id > 0"
                    v-decorator="[
                      'token',
                      {rules: [{ required: true, message: '请输入Token', whitespace: true}]}
                    ]" >
                    <a-icon slot="addonAfter" type="sync" @click="getToken()" v-if="!this.id"/>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row class="form-row" :gutter="16">
              <a-col :lg="24" :md="24" :sm="24">
                <a-form-item label="描述">
                  <a-textarea
                    placeholder="请输入描述"
                    :maxLength="256"
                    v-decorator="[
                      'description',
                      {rules: [{ required: false, message: '请输入描述', whitespace: true}]}
                    ]" />
                </a-form-item>
              </a-col>
            </a-row>
          </CollapsePanel>
          <CollapsePanel key="2" header="重试配置" :style="customStyle">
            <a-row class="form-row" :gutter="16" >
              <a-col :lg="8" :md="12" :sm="24">
                <a-form-item>
                  <span slot="label">
                    Id生成模式&nbsp;
                    <a :href="officialWebsite + '/pages/32e4a0/#什么是id生成模式'" target="_blank">
                      <a-icon type="question-circle-o" />
                    </a>
                  </span>
                  <a-select
                    placeholder="请选择Id生成模式"
                    v-decorator="[
                      'idGeneratorMode',
                      { initialValue: '1', rules: [{ required: true, message: '请选择Id生成模式'}]}
                    ]" >
                    <a-select-option :value="key" v-for="(value, key) in idGenMode" :key="key">{{ value }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :lg="8" :md="6" :sm="12">
                <a-form-item>
                  <span slot="label">
                    初始化场景&nbsp;
                    <a
                      :href="officialWebsite + '/pages/32e4a0/#什么是初始化场景'"
                      target="_blank">
                      <a-icon type="question-circle-o" />
                    </a>
                  </span>
                  <a-select
                    placeholder="请选择是否初始化场景"
                    v-decorator="[
                      'initScene',
                      {initialValue: '1' ,rules: [{ required: true, message: '请选择是否初始化场景'}]}
                    ]" >
                    <a-select-option :value="key" v-for="(value, key) in initScene" :key="key">{{ value }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :lg="8" :md="6" :sm="12">
                <a-form-item>
                  <span slot="label">
                    指定分区&nbsp;
                    <a :href="officialWebsite + '/pages/32e4a0/#什么是分区'" target="_blank">
                      <a-icon type="question-circle-o" />
                    </a>
                  </span>
                  <a-select
                    placeholder="请选择是否分区"
                    v-decorator="[
                      'groupPartition',
                      {initialValue: '0' ,rules: [{ required: true, message: '请选择是否分区'}]}
                    ]" >
                    <a-select-option :value="key" v-for="(value, key) in initPartition" :key="key">{{ value }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </CollapsePanel>
        </Collapse>
        <a-form-item v-if="showSubmit">
          <a-button htmlType="submit" >Submit</a-button>
        </a-form-item>
      </a-form>
    </a-card>

  </div>

</template>

<script>
import { getGroupConfigByGroupName, getPartitionTableList } from '@/api/manage'
import pick from 'lodash.pick'
import { officialWebsite } from '@/utils/util'
import CollapsePanel from 'ant-design-vue/lib/collapse/CollapsePanel'
import Collapse from 'ant-design-vue/es/collapse/Collapse'
import 'ant-design-vue/es/collapse/style/index'
import { right } from 'core-js/internals/array-reduce'

export default {
  name: 'GroupForm',
  components: { CollapsePanel, Collapse },
  props: {
    showSubmit: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      idGenMode: {
        '1': '号段模式',
        '2': '雪花算法'
      },
      initScene: {
        '0': '否',
        '1': '是'
      },
      initPartition: {

      },
      officialWebsite: officialWebsite(),
      expandIconPosition: 'left',
      activeKey: ['1', '2', '3'],
      customStyle:
        'background: white;border-radius: 4px;margin-bottom: 24px;border: 0;overflow: hidden',
      id: 0,
      loading: false
    }
  },
  mounted () {
    getPartitionTableList().then(res => {
      this.initPartition = res.data
    })
    this.$nextTick(() => {
      const groupName = this.$route.query.groupName
      if (groupName) {
        this.loading = true
        getGroupConfigByGroupName(groupName).then(res => {
          this.loadEditInfo(res.data)
          this.loading = false
        })
      }
    })
  },
  methods: {
    right,
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
      const regex = /^[A-Za-z0-9_]+$/
      if (!regex.test(value)) {
        callback(new Error('仅支持数字字母下划线'))
      }
      callback()
    },
    loadEditInfo (data) {
      const { form } = this
      // ajax
      console.log(`将加载 ${this.id} 信息到表单`)
      new Promise((resolve) => {
        setTimeout(resolve, 100)
      }).then(() => {
        const formData = pick(data, ['id', 'groupName', 'groupStatus', 'description', 'groupPartition', 'idGeneratorMode', 'initScene', 'token'])
        formData.groupStatus = formData.groupStatus.toString()
        formData.idGeneratorMode = formData.idGeneratorMode.toString()
        formData.initScene = formData.initScene.toString()
        this.id = formData.id

        form.setFieldsValue(formData)
      })
    },
    getToken () {
      const { form } = this
      const token = this.generatePassword(32)
      form.setFieldsValue({ token: token })
    },
    generatePassword (length) {
      const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
      let password = 'ER_'
      for (let i = 0; i < length; i++) {
        const randomNumber = Math.floor(Math.random() * chars.length)
        password += chars.substring(randomNumber, randomNumber + 1)
      }
      return password
    }
  }
}
</script>

<style scoped>

</style>
