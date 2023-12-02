<template>
  <div class="antd-pro-pages-dashboard-analysis-twoColLayout" :class="!isMobile && 'desktop'">
    <div class="extra-wrapper" slot="tabBarExtraContent">
      <div class="extra-item">
        <a href="#" @click="dataHandler('DAY')"><a-tag :class="dateType == 'DAY' ? 'in' : 'on'">{{ $t('dashboard.analysis.all-day') }}</a-tag></a>
        <a href="#" @click="dataHandler('WEEK')"><a-tag :class="dateType == 'WEEK' ? 'in' : 'on'">{{ $t('dashboard.analysis.all-week') }}</a-tag></a>
        <a href="#" @click="dataHandler('MONTH')"><a-tag :class="dateType == 'MONTH' ? 'in' : 'on'">{{ $t('dashboard.analysis.all-month') }}</a-tag></a>
        <a href="#" @click="dataHandler('YEAR')"><a-tag :class="dateType == 'YEAR' ? 'in' : 'on'">{{ $t('dashboard.analysis.all-year') }}</a-tag></a>
      </div>
      <div class="extra-item">
        <a-range-picker @change="dateChange" :show-time="{format: 'HH:mm:ss',defaultValue: [moment('00:00:00', 'HH:mm:ss'),moment('23:59:59', 'HH:mm:ss')]}" format="YYYY-MM-DD HH:mm:ss" :placeholder="['Start Time', 'End Time']" />
      </div>
      <a-select placeholder="请输入组名称" @change="value => handleChange(value)" :style="{width: '256px'}">
        <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
      </a-select>
    </div>
    <a-row>
      <a-col :xl="16" :lg="12" :md="12" :sm="24" :xs="24">
        <g2-retry-line ref="viewChart" name="RetryLine" />
      </a-col>
      <a-col :xl="8" :lg="12" :md="12" :sm="24" :xs="24">
        <rank-list :title="$t('dashboard.analysis.sales-ranking')" :list="rankList" />
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
          <div slot="extra" style="height: inherit;">
            <div class="analysis-salesTypeRadio">
              <a-radio-group defaultValue="a">
                <a-radio-button value="retry">{{ $t('dashboard.analysis.channel.online') }}</a-radio-button>
              </a-radio-group>
            </div>
          </div>
          <h4>{{ $t('dashboard.analysis.sales') }}</h4>
          <div>
            <div>
              <v-chart :force-fit="true" :height="405" :data="pieData" :scale="pieScale" >
                <v-tooltip :showTitle="true" dataKey="value*percent" />
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
import { getAllGroupNameList, getDashboardRetryLine } from '@/api/manage'
import { baseMixin } from '@/store/app-mixin'
import G2RetryLine from '@/components/Charts/RetryLine.vue'
import moment from 'moment'

export default {
  name: 'RetryAnalysis',
  mixins: [ baseMixin ],
  components: {
    G2RetryLine,
    RankList,
    STable
  },
  data () {
    return {
      loading: true,
      rankList: [],
      taskList: [],
      dateType: 'WEEK',
      type: 'WEEK',
      groupName: '',
      startTime: [],
      endTime: [],
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
          title: '启动中',
          dataIndex: 'run'
        },
        {
          title: '总场景',
          dataIndex: 'total'
        }
      ],
      loadData: (parameter) => {
        return getDashboardRetryLine(Object.assign(parameter)).then((res) => {
          this.rankList = res.data.rankList
          return res.data.taskList
        })
      }
    }
  },
  mounted () {
    this.$bus.$on('retry', (res) => {
      this.total = 0
      this.successNum = 0
      this.runningNum = 0
      this.maxCountNum = 0
      this.suspendNum = 0
      this.rankList = res.data.rankList
      this.taskList = res.data.taskList
      res.data.retryLinkeResponseVOList.forEach(res => {
        this.successNum += res.successNum
        this.runningNum += res.runningNum
        this.maxCountNum += res.maxCountNum
        this.suspendNum += res.suspendNum
      })
      this.total = this.successNum + this.runningNum + this.maxCountNum + this.suspendNum
      this.pieData = [
        { value: 'SUCCESS', name: this.successNum, percent: this.successNum / this.total },
        { value: 'RUNNING', name: this.runningNum, percent: this.runningNum / this.total },
        { value: 'MAXCOUNT', name: this.maxCountNum, percent: this.maxCountNum / this.total },
        { value: 'SUSPEND', name: this.suspendNum, percent: this.suspendNum / this.total }
      ]
    })
  },
  methods: {
    moment,
    dataHandler (type) {
      this.dateType = type
      this.type = type
      this.$refs.viewChart.getDashboardRetryLine(this.groupName, this.type, this.startTime, this.endTime)
    },
    handleChange (value) {
      this.groupName = value
      this.$refs.viewChart.getDashboardRetryLine(this.groupName, this.type, this.startTime, this.endTime)
    },
    dateChange (date, dateString) {
      this.startTime = dateString[0]
      this.endTime = dateString[1]
      this.type = this.startTime === '' ? 'WEEK' : 'OTHERS'
      this.$refs.viewChart.getDashboardRetryLine(this.groupName, this.type, this.startTime, this.endTime)
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
