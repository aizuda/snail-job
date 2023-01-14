<template>
  <a-card :bordered="false">

    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <template>
            <a-col :md="8" :sm="24">
              <a-form-item label="组名称">
                <a-input v-model="queryParam.groupName" placeholder="请输入组名称" allowClear/>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
              <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
<!--              <a @click="toggleAdvanced" style="margin-left: 8px">-->
<!--                {{ advanced ? '收起' : '展开' }}-->
<!--                <a-icon :type="advanced ? 'up' : 'down'"/>-->
<!--              </a>-->
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <div class="table-operator">
      <a-button type="primary" icon="plus" @click="handleNew()" v-if="$auth('group.add')">新建</a-button>
    </div>

    <s-table
      ref="table"
      size="default"
      rowKey="key"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
    >
      <span slot="serial" slot-scope="text, record, index">
        {{ index + 1 }}
      </span>
      <span slot="groupStatus" slot-scope="text">
        {{ text === 0 ? '停用': '启用' }}
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleEditStatus(record)">{{ record.groupStatus === 1 ? '停用': '启用' }}</a>
        </template>
      </span>
    </s-table>
  </a-card>
</template>

<script>

import AInput from 'ant-design-vue/es/input/Input'
import { getGroupConfigForPage, saveGroup } from '@/api/manage'
import { STable } from '@/components'
import moment from 'moment'

export default {
  name: 'TableListWrapper',
  components: {
    AInput,
    STable
  },
  data () {
    return {
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      // 表头
      columns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: '名称',
          dataIndex: 'groupName'
        },
        {
          title: '状态',
          dataIndex: 'groupStatus',
          scopedSlots: { customRender: 'groupStatus' }
        },
        {
          title: '路由策略',
          dataIndex: 'routeKey',
          customRender: (text) => this.routeKey[text]
        },
        {
          title: '版本',
          dataIndex: 'version'
        },
        {
          title: '分区',
          dataIndex: 'groupPartition',
          needTotal: true
        },
        {
          title: '更新时间',
          dataIndex: 'updateDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '描述',
          dataIndex: 'description'
        },
        {
          title: 'OnLine机器',
          dataIndex: 'onlinePodList',
          customRender: (text) => text.toString()
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '150px',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        console.log('loadData.parameter', parameter)
        return getGroupConfigForPage(Object.assign(parameter, this.queryParam))
          .then(res => {
            return res
          })
      },
      selectedRowKeys: [],
      selectedRows: [],

      // custom table alert & rowSelection
      options: {
        alert: { show: true, clear: () => { this.selectedRowKeys = [] } },
        rowSelection: {
          selectedRowKeys: this.selectedRowKeys,
          onChange: this.onSelectChange
        }
      },
      routeKey: {
        '1': '一致性hash算法',
        '2': '随机算法',
        '3': '最近最久未使用算法'
      }
    }
  },
  created () {

  },
  methods: {
    handleNew () {
      this.$router.push('/basic-config')
    },
    handleEdit (record) {
      this.$router.push({ path: '/basic-config', query: { groupName: record.groupName } })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleEditStatus (record) {
      const { id, groupStatus, groupName } = record
      const { $notification } = this
      saveGroup({ id: id, groupName: groupName, groupStatus: (groupStatus === 1 ? 0 : 1) }).then(res => {
        if (res.status === 0) {
          $notification['error']({
            message: res.message
          })
        } else {
          $notification['success']({
            message: res.message
          })
          // 刷新表格
          this.$refs.table.refresh()
        }
      })
    }
  }
}
</script>
