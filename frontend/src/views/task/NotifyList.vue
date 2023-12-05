<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <template>
              <a-col :md="8" :sm="24">
                <a-form-item label="组名称">
                  <a-select v-model="queryParam.groupName" placeholder="请输入组名称" @change="value => handleChange(value)" allowClear>
                    <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="场景名称">
                  <a-select v-model="queryParam.sceneName" placeholder="请选择场景名称" allowClear>
                    <a-select-option v-for="item in sceneList" :value="item.sceneName" :key="item.sceneName"> {{ item.sceneName }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetFiled">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleNew()">新增</a-button>
      <!--        <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">-->
      <!--          <a-menu slot="overlay" @click="onClick">-->
      <!--            <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>-->
      <!--            <a-menu-item key="2"><a-icon type="edit" />更新</a-menu-item>-->
      <!--          </a-menu>-->
      <!--          <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /> </a-button>-->
      <!--        </a-dropdown>-->
      </div>
      <s-table
        ref="table"
        size="default"
        :rowKey="(record) => record.id"
        :columns="notifyColumns"
        :data="loadData"
        :alert="options.alert"
        :rowSelection="options.rowSelection"
      >
        <span slot="notifyType" slot-scope="text">
          <a-tag :color="notifyTypeList[text].color">
            {{ notifyTypeList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyStatus" slot-scope="text">
          <a-tag :color="notifyStatusList[text].color">
            {{ notifyStatusList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyScene" slot-scope="text">
          <a-tag :color="notifySceneList[text].color">
            {{ notifySceneList[text].name }}
          </a-tag>
        </span>
        <span slot="notifyAttribute" slot-scope="text, record">
          {{ parseJson(JSON.parse(text),record) }}
        </span>
        <span slot="notifyThreshold" slot-scope="text">
          {{ text === 0 ? '无' : text }}
        </span>
        <span slot="action" slot-scope="record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
          </template>
        </span>
      </s-table>
    </a-card>
  </div>

</template>

<script>
import { getAllGroupNameList, getNotifyConfigList, getSceneList } from '@/api/manage'
import { STable } from '@/components'
const enums = require('@/utils/retryEnum')

export default {
  name: 'NotifyList',
  components: { STable },
  data () {
    return {
      notifyColumns: [
        {
          title: '组名',
          dataIndex: 'groupName',
          key: 'groupName',
          width: '10%',
          scopedSlots: { customRender: 'groupName' }
        },
        {
          title: '场景名称',
          dataIndex: 'sceneName',
          key: 'sceneName',
          width: '10%',
          scopedSlots: { customRender: 'sceneName' }
        },
        {
          title: '通知状态',
          dataIndex: 'notifyStatus',
          width: '10%',
          scopedSlots: { customRender: 'notifyStatus' }
        },
        {
          title: '通知类型',
          dataIndex: 'notifyType',
          key: 'notifyType',
          width: '10%',
          scopedSlots: { customRender: 'notifyType' }
        },
        {
          title: '通知场景',
          dataIndex: 'notifyScene',
          key: 'notifyScene',
          width: '10%',
          scopedSlots: { customRender: 'notifyScene' }
        },
        {
          title: '通知阈值',
          dataIndex: 'notifyThreshold',
          key: 'notifyThreshold',
          width: '10%',
          scopedSlots: { customRender: 'notifyThreshold' }
        },
        {
          title: '描述',
          dataIndex: 'description',
          key: 'description',
          width: '20%',
          scopedSlots: { customRender: 'description' }
        },
        {
          title: '操作',
          key: 'action',
          fixed: 'right',
          width: '180px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      data: [],
      loading: false,
      form: this.$form.createForm(this),
      // 高级搜索 展开/关闭
      advanced: false,
      queryParam: {},
      loadData: (parameter) => {
        return getNotifyConfigList(Object.assign(parameter, this.queryParam)).then((res) => {
          return res
        })
      },
      selectedRowKeys: [],
      selectedRows: [],

      // custom table alert & rowSelection
      options: {
        alert: {
          show: true,
          clear: () => {
            this.selectedRowKeys = []
          }
        },
        rowSelection: {
          selectedRowKeys: this.selectedRowKeys,
          onChange: this.onSelectChange
        }
      },
      optionAlertShow: false,
      memberLoading: false,
      notifySceneList: enums.notifyScene,
      notifyTypeList: enums.notifyType,
      notifyStatusList: enums.notifyStatus,
      visible: false,
      key: '',
      notifyTypeValue: '1',
      groupNameList: [],
      sceneList: []
    }
  },
  created () {
    getAllGroupNameList().then((res) => {
      this.groupNameList = res.data
    })

    const groupName = this.$route.query.groupName
    if (groupName) {
      this.getNotifyConfigList(groupName)
    }
  },
  methods: {
    resetFiled () {
      this.queryParam = {}
      this.sceneList = []
    },
    handleNew () {
      this.$router.push({ path: '/retry/notify/config' })
    },
    handleEdit (record) {
      this.$router.push({ path: '/retry/notify/config', query: { id: record.id } })
    },
    handleChange (value) {
      if (value) {
        getSceneList({ groupName: value }).then((res) => {
          this.sceneList = res.data
        })
      } else {
        this.sceneList = []
      }
    }
  }
}
</script>

<style scoped>

</style>
