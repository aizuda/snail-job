<template>
  <div class="antd-pro-pages-dashboard-analysis-twoColLayout" :class="!isMobile && 'desktop'">
    <a-row>
      <a-col :xl="16" :lg="12" :md="12" :sm="24" :xs="24">
        <g2-work-flow-line ref="workFlowViewChart" name="G2WorkFlowLine" />
      </a-col>
      <a-col :xl="8" :lg="12" :md="12" :sm="24" :xs="24">
        <rank-list title="失败任务排名" :list="rankList" />
      </a-col>
    </a-row>
    <a-row :gutter="24" type="flex" :style="{ marginTop: '24px' }">
      <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card :loading="loading" :bordered="false" :title="$t('dashboard.analysis.online-top-search')" :style="{ height: '100%' }">
          <s-table
            ref="table"
            size="default"
            :rowKey="(record,index) => index"
            :columns="columns"
            :data="loadData"
            :scroll="{ x: 200 }"
          >
          </s-table>
        </a-card>
      </a-col>
      <a-col :xl="12" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card class="antd-pro-pages-dashboard-analysis-salesCard" :loading="loading" :bordered="false" :title="$t('dashboard.analysis.the-proportion-of-sales')" :style="{ height: '100%' }">
          <h4>{{ $t('dashboard.analysis.job.sales') }}</h4>
          <div>
            <div>
              <v-chart :force-fit="true" :height="405" :data="pieData" :scale="pieScale" >
                <v-tooltip :showTitle="false" dataKey="value*percent" />
                <v-axis />
                <v-legend dataKey="value" />
                <v-pie position="percent" color="value" :vStyle="pieStyle" />
                <v-coord type="theta" :radius="0.95" :innerRadius="0.7" />
              </v-chart>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import {
  RankList,
  STable
} from '@/components'
import { getAllGroupNameList, getDashboardJobLine } from '@/api/manage'
import { baseMixin } from '@/store/app-mixin'
import G2WorkFlowLine from '@/components/Charts/WorkFlowLine.vue'
import moment from 'moment'

export default {
  name: 'WorkFlowAnalysis',
  mixins: [ baseMixin ],
  components: {
    G2WorkFlowLine,
    RankList,
    STable
  },
  data () {
    return {
      loading: true,
      rankList: [],
      taskList: [],
      dashboardLineResponseDOList: [],
      type: 'WEEK',
      groupName: '',
      mode: '',
      startTime: '',
      endTime: '',
      success: 0,
      fail: 0,
      stop: 0,
      cancel: 0,
      total: 0,
      groupNameList: [],
      pieScale: [{
        dataKey: 'percent',
        min: 0,
        formatter: '.0%'
      }],
      pieData: [],
      pieStyle: {
        stroke: '#fff',
        lineWidth: 1
      },
      columns: [
        {
          title: '组名称',
          dataIndex: 'groupName'
        },
        {
          title: '运行中任务数',
          dataIndex: 'run'
        },
        {
          title: '总任务数',
          dataIndex: 'total'
        }
      ],
      loadData: (parameter) => {
        return getDashboardJobLine(Object.assign(parameter)).then((res) => {
          this.rankList = res.data.rankList
          return res.data.taskList
        })
      }
    }
  },
  mounted () {
    this.$bus.$on('WORKFLOW', (res) => {
      this.total = 0
      this.success = 0
      this.fail = 0
      this.stop = 0
      this.cancel = 0
      this.rankList = res.data.rankList
      this.taskList = res.data.taskList
      res.data.dashboardLineResponseDOList.forEach(res => {
        this.success += res.success
        this.fail += res.fail
        this.stop += res.stop
        this.cancel += res.cancel
      })
      this.total = this.success + this.fail + this.stop + this.cancel
      this.pieData = [
        { value: 'SUCCESS', name: this.success, percent: this.success / this.total },
        { value: 'FAIL', name: this.fail, percent: this.fail / this.total },
        { value: 'STOP', name: this.stop, percent: this.stop / this.total },
        { value: 'CANCEL', name: this.cancel, percent: this.cancel / this.total }
      ]
    })
  },
  methods: {
    moment,
    dataHandler (mode, type) {
      this.mode = mode
      this.type = type
      this.$refs.workFlowViewChart.getDashboardJobLine(this.mode, this.groupName, this.type, this.startTime, this.endTime)
    },
    handleChange (mode, value) {
      this.mode = mode
      this.groupName = value
      this.$refs.workFlowViewChart.getDashboardJobLine(this.mode, this.groupName, this.type, this.startTime, this.endTime)
    },
    dateChange (mode, date, dateString) {
      this.mode = mode
      this.startTime = dateString[0]
      this.endTime = dateString[1]
      this.type = this.startTime === '' ? 'WEEK' : 'OTHERS'
      this.$refs.workFlowViewChart.getDashboardJobLine(this.mode, this.groupName, this.type, this.startTime, this.endTime)
    }
  },
  created () {
    getAllGroupNameList().then(res => {
      this.groupNameList = res.data
    })

    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
  }
}
</script>

<style lang='less' scoped>
@import 'Analysis.less';
</style>
