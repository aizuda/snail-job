<template>
  <div>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <template>
              <a-col :md="8" :sm="24">
                <a-form-item label="组名称">
                  <a-select
                    v-model="queryParam.groupName"
                    placeholder="请输入组名称"
                  >
                    <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="queryChange()">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
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
          <a-tag :color="notifyType[text].color">
            {{ notifyType[text].name }}
          </a-tag>
        </span>
        <span slot="notifyScene" slot-scope="text">
          <a-tag :color="notifyScene[text].color">
            {{ notifyScene[text].name }}
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
import { getAllGroupNameList, getNotifyConfigList } from '@/api/manage'
import { STable } from '@/components'
const enums = require('@/utils/retryEnum')

export default {
  name: 'NotifyList',
  components: { STable },
  data () {
    return {
      notifyColumns: [
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
          title: '配置属性',
          dataIndex: 'notifyAttribute',
          key: 'notifyAttribute',
          width: '30%',
          scopedSlots: { customRender: 'notifyAttribute' }
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
      notifyScene: enums.notifyScene,
      notifyType: enums.notifyType,
      notifyStatus: enums.notifyStatus,
      visible: false,
      key: '',
      notifyTypeValue: '1',
      groupNameList: []
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
    handleNew () {
      this.$router.push({ path: '/retry/notify/config' })
    },
    handleEdit (record) {
      this.$router.push({ path: '/retry/notify/config', query: { id: record.id } })
    },
    parseJson (text, record) {
      if (!text) {
        return null
      }

      let s =
        '用户名:' + text['user'] + ';\r\n' +
        '密码:' + text['pass'] + ';\r\n' +
        'SMTP地址:' + text['host'] + ';\r\n' +
        'SMTP端口:' + text['port'] + ';\r\n' +
        '发件人:' + text['from'] + ';\r\n' +
        '收件人:' + text['tos'] + ';'

      if (record.notifyType === 1) {
         s = text['dingDingUrl']
      } else if (record.notifyType === 4) {
        s = text['larkUrl']
      }

      return s
    }
  }
}
</script>

<style scoped>

</style>
