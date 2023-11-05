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
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <s-table
      ref="table"
      size="default"
      rowKey="key"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
      :scroll="{ x: 1600 }"
    >
      <span slot="serial" slot-scope="text, record, index">
        {{ index + 1 }}
      </span>
      <span slot="contextPath" slot-scope="text, record">
        <div v-if="record.nodeType === 1">
          Path:
          <a-tag color="#108ee9" >
            {{ text }}
          </a-tag>
        </div>

        <div v-else>
          Bucket:
          <a-popover placement="topLeft">
            <template slot="content">
              <a-tag color="pink" v-for="item in record.consumerBuckets" :key="item" style="margin-bottom: 16px">
                {{ item }}
              </a-tag>
            </template>
            <template slot="title">
              <span>Bucket列表</span>
            </template>
            <a-tag color="pink" v-for="item in 5" :key="item" style="margin-bottom: 16px">
              {{ record.consumerBuckets[item-1] }}
            </a-tag>
            <a-tag color="pink" style="margin-bottom: 16px" v-if="record.consumerBuckets.length > 5">
              ...
            </a-tag>
          </a-popover>
        </div>

      </span>
    </s-table>
  </a-card>
</template>

<script>
import moment from 'moment'
import { pods } from '@/api/manage'
import { STable } from '@/components'

export default {
  name: 'PodList',
  components: {
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
          scopedSlots: { customRender: 'serial' },
          width: '6%'
        },
        {
          title: '类型',
          dataIndex: 'nodeType',
          customRender: (text) => this.nodeType[text],
          width: '8%'
        },
        {
          title: '组名称',
          dataIndex: 'groupName',
          width: '10%'
        },
        {
          title: 'PodId',
          dataIndex: 'hostId',
          width: '18%'
        },
        {
          title: 'IP',
          dataIndex: 'hostIp',
          width: '12%'
        },
        {
          title: 'Port',
          dataIndex: 'hostPort',
          width: '8%'
        },
        {
          title: '路径/组',
          dataIndex: 'contextPath',
          scopedSlots: { customRender: 'contextPath' },
          ellipsis: true,
          width: '22%'
        },
        {
          title: '更新时间',
          dataIndex: 'updateDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        return pods(Object.assign(parameter, this.queryParam))
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
      nodeType: {
        '1': '客户端',
        '2': '服务端'
      }
    }
  },
  methods: {
  }
}
</script>

<style scoped>

</style>
