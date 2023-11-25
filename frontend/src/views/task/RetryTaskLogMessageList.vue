<template>
  <div>
    <div style="margin: 20px 0; border-left: #f5222d 5px solid; font-size: medium; font-weight: bold">
      <span style="padding-left: 18px">调用日志详情 (总调度次数: {{ total }})</span>
      <span style="padding-left: 18px"><a-icon type="sync" @click="()=> this.$refs.table.refresh(true)"/></span>
    </div>
    <a-card>
      <s-table
        ref="table"
        size="default"
        rowKey="key"
        :columns="columns"
        :data="loadData"
      >
        <span slot="serial" slot-scope="text, record">
          {{ record.id }}
        </span>
        <span slot="clientInfo" slot-scope="text">
          {{ text ? text.split('@')[1] : '无' }}
        </span>
      </s-table>
    </a-card>
  </div>

</template>

<script>
import moment from 'moment'
import { getRetryTaskLogMessagePage } from '@/api/manage'
import { STable } from '@/components'

export default {
  name: 'RetryTaskLogMessageList',
  components: {
    STable
  },
  data () {
    return {
      // 表头
      columns: [
        {
          title: '#',
          scopedSlots: { customRender: 'serial' },
          width: '10%'
        },
        {
          title: '信息',
          dataIndex: 'message',
          width: '50%'
        },
        {
          title: '地址',
          dataIndex: 'clientInfo',
          scopedSlots: { customRender: 'clientInfo' },
          width: '10%'
        },
        {
          title: '触发时间',
          dataIndex: 'createDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
          width: '10%'
        }
      ],
      queryParam: {},
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        if (!this.queryParam['groupName']) {
          return
        }

        return getRetryTaskLogMessagePage(Object.assign(parameter, this.queryParam))
          .then(res => {
            this.total = res.total
            return res
          })
      },
      total: 0
    }
  },
  methods: {
    refreshTable (v) {
      this.queryParam = v
      this.$refs.table.refresh(true)
    }
  }

}
</script>

<style scoped>

</style>
