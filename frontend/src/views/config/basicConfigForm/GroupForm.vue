<template>
  <a-form @submit="handleSubmit" :form="form" class="form">

    <a-row class="form-row" :gutter="16">
      <a-col :lg="6" :md="12" :sm="24">
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
            :disabled="this.id && this.id > 0"
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
              {rules: [{ required: true, message: '请选择状态类型'}]}
            ]" >
            <a-select-option value="0">停用</a-select-option>
            <a-select-option value="1">启动</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :lg="6" :md="12" :sm="24">
        <a-form-item>
          <span slot="label">
            路由策略&nbsp;
            <a :href="officialWebsite + '/pages/32e4a0/#什么是路由策略'" target="_blank">
              <a-icon type="question-circle-o" />
            </a>
          </span>
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
            :maxLength="256"
            v-decorator="[
              'description',
              {rules: [{ required: true, message: '请输入描述', whitespace: true}]}
            ]" />
        </a-form-item>
      </a-col>
      <a-col :lg="3" :md="6" :sm="12">
        <a-form-item>
          <span slot="label">
            指定分区&nbsp;
            <a :href="officialWebsite + '/pages/32e4a0/#什么是分区'" target="_blank">
              <a-icon type="question-circle-o" />
            </a>
          </span>
          <a-input-number
            id="inputNumber"
            placeholder="分区"
            v-decorator="[
              'groupPartition'
            ]"
            :min="0"
            :max="maxGroupPartition"
          />
        </a-form-item>
      </a-col>
      <a-col :lg="3" :md="6" :sm="12">
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
              {rules: [{ required: true, message: '请选择Id生成模式'}]}
            ]" >
            <a-select-option :value="key" v-for="(value, key) in idGenMode" :key="key">{{ value }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <a-col :lg="3" :md="6" :sm="12">
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
              {rules: [{ required: true, message: '请选择是否初始化场景'}]}
            ]" >
            <a-select-option :value="key" v-for="(value, key) in initScene" :key="key">{{ value }}</a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
    </a-row>
    <a-form-item v-if="showSubmit">
      <a-button htmlType="submit" >Submit</a-button>
    </a-form-item>
  </a-form>
</template>

<script>
import { getGroupConfigByGroupName, getTotalPartition } from '@/api/manage'
import pick from 'lodash.pick'
import { officialWebsite } from '@/utils/util'

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
      maxGroupPartition: 32,
      routeKey: {
        '1': '一致性hash算法',
        '2': '随机算法',
        '3': '最近最久未使用算法'
      },
      idGenMode: {
        '1': '号段模式',
        '2': '雪花算法'
      },
      initScene: {
        '0': '否',
        '1': '是'
      },
      officialWebsite: officialWebsite()
    }
  },
  mounted () {
    this.$nextTick(() => {
      getTotalPartition().then(res => {
        this.maxGroupPartition = res.data
      })

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
        setTimeout(resolve, 1500)
      }).then(() => {
        const formData = pick(data, ['id', 'groupName', 'routeKey', 'groupStatus', 'description', 'groupPartition', 'idGeneratorMode', 'initScene'])
        formData.groupStatus = formData.groupStatus.toString()
        formData.routeKey = formData.routeKey.toString()
        formData.idGeneratorMode = formData.idGeneratorMode.toString()
        formData.initScene = formData.initScene.toString()
        this.id = formData.id

        form.setFieldsValue(formData)
      })
    }
  }
}
</script>

<style scoped>

</style>
