<template>
  <div>
    <div style="margin: 20px 0; border-left: #f5222d 5px solid; font-size: medium; font-weight: bold">
      &nbsp;&nbsp; 调用日志详情 (总调度次数: {{ total }})
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
        console.log('loadData.parameter', parameter)
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
