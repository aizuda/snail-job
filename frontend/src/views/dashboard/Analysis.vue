<template>
  <div>
    <a-row :gutter="24">
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" :title="$t('dashboard.analysis.total-sales')" :total="retryTask.totalNum">
          <a-tooltip title="总任务量: 重试/回调任务量" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div class="antv-chart-mini">
            <div class="chart-wrapper" :style="{ height: 46 }">
              <v-chart :force-fit="true" :height="height" :data="retryTaskBarList" :padding="[30, 22, 18, 10]">
                <v-tooltip />
                <v-bar position="x*taskTotal" />
              </v-chart>
            </div>
          </div>
          <template slot="footer">
            <div>
              <span slot="term">完成</span>
              {{ retryTask.finishNum }}
              <a-divider type="vertical" />
              <span slot="term">运行中</span>
              {{ retryTask.runningNum }}
              <a-divider type="vertical" />
              <span slot="term">最大次数</span>
              {{ retryTask.maxCountNum }}
              <a-divider type="vertical" />
              <span slot="term">暂停重试</span>
              {{ retryTask.suspendNum }}
            </div>
          </template>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="定时任务" :total="jobTask.totalNum">
          <a-tooltip title="成功率:总完成/总调度量;" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div>
            <a-tooltip title="成功率">
              <a-progress stroke-linecap="square" :percent="jobTask.successRate" />
            </a-tooltip>
          </div>
          <template slot="footer">
            {{ $t('dashboard.analysis.job_success') }}
            <span>{{ jobTask.successNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_fail') }}
            <span>{{ jobTask.failNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_stop') }}
            <span>{{ jobTask.stopNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_cancel') }}
            <span>{{ jobTask.cancelNum }}</span>
          </template>
        </chart-card>
      </a-col>

      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="工作流任务" :total="workFlowTask.totalNum"><!-- -->
          <a-tooltip title="成功率:总完成/总调度量;" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div>
            <a-tooltip title="成功率">
              <a-progress stroke-linecap="square" :percent="workFlowTask.successRate" />
            </a-tooltip>
          </div>
          <template slot="footer">
            {{ $t('dashboard.analysis.job_success') }}
            <span>{{ workFlowTask.successNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_fail') }}
            <span>{{ workFlowTask.failNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_stop') }}
            <span>{{ workFlowTask.stopNum }}</span>
            <a-divider type="vertical" />
            {{ $t('dashboard.analysis.job_cancel') }}
            <span>{{ workFlowTask.cancelNum }}</span>
          </template>
        </chart-card>
      </a-col>

      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <a href="#" @click="jumpPosList">
          <chart-card :loading="loading" title="总在线机器" :total="onLineService.total">
            <a-tooltip title="总在线机器:注册到系统的客户端和服务端之和" slot="action" >
              <a-icon type="info-circle-o" />
            </a-tooltip>
            <template slot="footer">
              <div>
                <span slot="term">客户端</span>
                {{ onLineService.clientTotal }}
                <a-divider type="vertical" />
                <span slot="term">服务端</span>
                {{ onLineService.serverTotal }}
              </div>
            </template>
          </chart-card>
        </a>
      </a-col>
    </a-row>

    <a-card :loading="loading" :bordered="true" :body-style="{padding: '0'}">
      <div class="salesCard">
        <a-tabs @change="callback">
          <div class="extra-wrapper" slot="tabBarExtraContent">
            <div class="extra-item">
              <a href="#" @click="dataHandler('DAY')"><a-checkable-tag :checked="type == 'DAY'">{{ $t('dashboard.analysis.all-day') }}</a-checkable-tag></a>
              <a href="#" @click="dataHandler('WEEK')"><a-checkable-tag :checked="type == 'WEEK'">{{ $t('dashboard.analysis.all-week') }}</a-checkable-tag></a>
              <a href="#" @click="dataHandler('MONTH')"><a-checkable-tag :checked="type == 'MONTH'">{{ $t('dashboard.analysis.all-month') }}</a-checkable-tag></a>
              <a href="#" @click="dataHandler('YEAR')"><a-checkable-tag :checked="type == 'YEAR'">{{ $t('dashboard.analysis.all-year') }}</a-checkable-tag></a>
            </div>
            <div class="extra-item">
              <a-range-picker @change="dateChange" :show-time="{format: 'HH:mm:ss',defaultValue: [moment('00:00:00', 'HH:mm:ss'),moment('23:59:59', 'HH:mm:ss')]}" format="YYYY-MM-DD HH:mm:ss" :placeholder="['开始时间', '结束时间']" />
            </div>
            <a-select placeholder="请输入组名称" @change="value => handleChange(value)" :style="{width: '256px'}" :allowClear="true">
              <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
            </a-select>
          </div>
          <a-tab-pane loading="true" :tab="$t('dashboard.analysis.sales')" v-if="$auth('RetryAnalysis.retry')" key="RETRY">
            <div>
              <retry-analysis ref="retryAnalysisRef"/>
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="$t('dashboard.analysis.visits')" v-if="$auth('JobAnalysis.job')" key="JOB">
            <div>
              <job-analysis ref="jobAnalysisRef"/>
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="$t('dashboard.analysis.work-flow-job')" key="WORKFLOW">
            <div>
              <work-flow-analysis ref="workFlowAnalysisRef"/>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-card>
  </div>
</template>

<script>
import {
  ChartCard,
  MiniArea,
  MiniProgress,
  Bar,
  Trend,
  NumberInfo,
  MiniSmoothArea
} from '@/components'

import { getAllGroupNameList, getDashboardTaskRetryJob } from '@/api/manage'
import RetryAnalysis from '@/views/dashboard/RetryAnalysis.vue'
import JobAnalysis from '@/views/dashboard/JobAnalysis.vue'
import WorkFlowAnalysis from '@/views/dashboard/WorkFlowAnalysis.vue'
import { APP_MODE } from '@/store/mutation-types'
import storage from 'store'
import moment from 'moment'

export default {
  name: 'Analysis',
  components: {
    RetryAnalysis,
    JobAnalysis,
    WorkFlowAnalysis,
    ChartCard,
    MiniArea,
    MiniProgress,
    Bar,
    Trend,
    NumberInfo,
    MiniSmoothArea
  },
  data () {
    return {
      loading: true,
      height: 100,
      retryTaskBarList: [],
      groupNameList: [],
      type: 'WEEK',
      mode: '',
      retryTask: {
        totalNum: 0,
        runningNum: 0,
        finishNum: 0,
        maxCountNum: 0,
        suspendNum: 0
      },
      jobTask: {
        successRate: 0,
        successNum: 0,
        failNum: 0,
        cancelNum: 0,
        stopNum: 0,
        totalNum: 0
      },
      workFlowTask: {
        successRate: 0,
        successNum: 0,
        failNum: 0,
        cancelNum: 0,
        stopNum: 0,
        totalNum: 0
      },
      onLineService: {
        clientTotal: 0,
        serverTotal: 0,
        total: 0
      }
    }
  },
  computed: {
  },
  methods: {
    moment,
    callback (key) {
      this.mode = key
    },
    jumpPosList () {
      this.$router.push({ path: '/dashboard/pods' })
    },
    dataHandler (type) {
      this.type = type
      if (this.mode === 'ALL' || this.mode === 'RETRY') {
        this.$refs.retryAnalysisRef.dataHandler(this.type)
      } else if (this.mode === 'JOB') {
        this.$refs.jobAnalysisRef.dataHandler(this.mode, this.type)
      } else if (this.mode === 'WORKFLOW') {
        this.$refs.workFlowAnalysisRef.dataHandler(this.mode, this.type)
      }
    },
    dateChange (date, dateString) {
      if (this.mode === 'ALL' || this.mode === 'RETRY') {
        this.$refs.retryAnalysisRef.dateChange(date, dateString)
      } else if (this.mode === 'JOB') {
        this.$refs.jobAnalysisRef.dateChange(this.mode, date, dateString)
      } else if (this.mode === 'WORKFLOW') {
        this.$refs.workFlowAnalysisRef.dateChange(this.mode, date, dateString)
      }
    },
    handleChange (value) {
      if (this.mode === 'ALL' || this.mode === 'RETRY') {
        this.$refs.retryAnalysisRef.handleChange(value)
      } else if (this.mode === 'JOB') {
        this.$refs.jobAnalysisRef.handleChange(this.mode, value)
      } else if (this.mode === 'WORKFLOW') {
        this.$refs.workFlowAnalysisRef.handleChange(this.mode, value)
      }
    }
  },
  created () {
    this.mode = storage.get(APP_MODE)
    getDashboardTaskRetryJob().then(res => {
      this.jobTask = res.data.jobTask
      this.retryTask = res.data.retryTask
      this.workFlowTask = res.data.workFlowTask
      this.onLineService = res.data.onLineService
      this.retryTaskBarList = res.data.retryTaskBarList
    })

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
.antv-chart-mini {
  position: relative;
  width: 100%;

  .chart-wrapper {
    position: absolute;
    bottom: -28px;
    width: 100%;
  }
}

.extra-wrapper {
  padding-right: 24px;

  .extra-item {
    display: inline-block;
    margin-right: 24px;

    a {
      margin-left: 24px;
    }
  }
}

.antd-pro-pages-dashboard-analysis-twoColLayout {
  position: relative;
  display: flex;
  display: block;
  flex-flow: row wrap;
}

.antd-pro-pages-dashboard-analysis-salesCard {
  height: calc(100% - 24px);

  /deep/ .ant-card-head {
    position: relative;
  }
}

.dashboard-analysis-iconGroup {
  i {
    margin-left: 16px;
    color: rgba(0, 0, 0, .45);
    cursor: pointer;
    transition: color .32s;
    color: black;
  }
}

.analysis-salesTypeRadio {
  position: absolute;
  right: 54px;
  bottom: 12px;
}
</style>
