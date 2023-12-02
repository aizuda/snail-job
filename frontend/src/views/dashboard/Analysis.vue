<template>
  <div>
    <a-row :gutter="24">
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" :title="$t('dashboard.analysis.total-sales')" :total="retryTask.totalNum">
          <a-tooltip title="总任务量: 重试/回调任务量" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div class="antv-chart-mini">
            <div class="chart-wrapper" :style="{ height: 46 }">
              <v-chart :force-fit="true" :height="height" :data="retryTaskBarList" :padding="[36, 5, 18, 5]">
                <v-tooltip />
                <v-bar position="x*y" />
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
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
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
          </template>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
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
        <a-tabs>
          <a-tab-pane loading="true" :tab="$t('dashboard.analysis.sales')" v-if="$auth('RetryAnalysis.retry')" key="1">
            <div>
              <retry-analysis />
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="$t('dashboard.analysis.visits')" v-if="$auth('JobAnalysis.job')" key="2">
            <div>
              <job-analysis />
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
import {
  getDashboardTaskRetryJob
} from '@/api/manage'

import RetryAnalysis from '@/views/dashboard/RetryAnalysis.vue'
import JobAnalysis from '@/views/dashboard/JobAnalysis.vue'

export default {
  name: 'Analysis',
  components: {
    RetryAnalysis,
    JobAnalysis,
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
    jumpPosList () {
      this.$router.push({ path: '/dashboard/pods' })
    }
  },
  created () {
    getDashboardTaskRetryJob().then(res => {
      this.retryTask = res.data.retryTask
      this.jobTask = res.data.jobTask
      this.onLineService = res.data.onLineService
      this.retryTaskBarList = res.data.retryTaskBarList
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
  line-height: 55px;
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
