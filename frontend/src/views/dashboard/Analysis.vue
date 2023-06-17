<template>
  <div>
    <a-row :gutter="24">
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" :title="$t('dashboard.analysis.total-sales')" :total="taskQuantity.total">
          <a-tooltip title="总任务量: 重试/回调任务量" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div>
            <span slot="term">完成</span>
            {{ taskQuantity.finish }}
            <a-divider type="vertical" />
            <span slot="term">运行中</span>
            {{ taskQuantity.running }}
            <a-divider type="vertical" />
            <span slot="term">最大次数</span>
            {{ taskQuantity.maxRetryCount }}
          </div>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="总调度量" :total="dispatchQuantity.total">
          <a-tooltip title="成功率:总完成/总调度量;" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div>
            <a-tooltip title="成功率">
              <a-progress stroke-linecap="square" :percent="dispatchQuantity.successPercent" />
            </a-tooltip>
          </div>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="8" :style="{ marginBottom: '24px' }">
        <a href="#" @click="jumpPosList">
          <chart-card :loading="loading" title="总在线机器" :total="countActivePodQuantity.total">
            <a-tooltip title="总在线机器:注册到系统的客户端和服务端之和" slot="action" >
              <a-icon type="info-circle-o" />
            </a-tooltip>
            <div>
              <span slot="term">客户端</span>
              {{ countActivePodQuantity.clientTotal }}
              <a-divider type="vertical" />
              <span slot="term">服务端</span>
              {{ countActivePodQuantity.serverTotal }}
            </div>
          </chart-card>
        </a>
      </a-col>
    </a-row>

    <a-card :loading="loading" :bordered="false" :body-style="{padding: '0'}">
      <div class="salesCard">
        <a-tabs default-active-key="1" size="large" :tab-bar-style="{marginBottom: '24px', paddingLeft: '16px'}">
          <div class="extra-wrapper" slot="tabBarExtraContent">
            <div class="extra-item">
              <a href="#" @click="dataHandler('day')">{{ $t('dashboard.analysis.all-day') }}</a>
              <a href="#" @click="dataHandler('week')">{{ $t('dashboard.analysis.all-week') }}</a>
              <a href="#" @click="dataHandler('month')">{{ $t('dashboard.analysis.all-month') }}</a>
              <a href="#" @click="dataHandler('year')">{{ $t('dashboard.analysis.all-year') }}</a>
            </div>
            <div class="extra-item">
              <a-range-picker :style="{width: '256px'}" @change="dateChange" />
            </div>
            <a-select placeholder="请输入组名称" @change="value => handleChange(value)" :style="{width: '256px'}">
              <a-select-option v-for="item in groupNameList" :value="item" :key="item">{{ item }}</a-select-option>
            </a-select>
          </div>
          <a-tab-pane loading="true" :tab="$t('dashboard.analysis.sales')" key="1">
            <a-row>
              <a-col :xl="16" :lg="12" :md="12" :sm="24" :xs="24">
                <g2-line ref="viewChart" />
              </a-col>
              <a-col :xl="8" :lg="12" :md="12" :sm="24" :xs="24">
                <rank-list :title="$t('dashboard.analysis.sales-ranking')" :list="rankList" />
              </a-col>
            </a-row>
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
  MiniBar,
  MiniProgress,
  RankList,
  Bar,
  NumberInfo,
  MiniSmoothArea,
  G2Line
} from '@/components'
import { baseMixin } from '@/store/app-mixin'
import { getAllGroupNameList, countTask, countDispatch, countActivePod, rankSceneQuantity } from '@/api/manage'

export default {
  name: 'Analysis',
  mixins: [baseMixin],
  components: {
    ChartCard,
    MiniArea,
    MiniBar,
    MiniProgress,
    RankList,
    Bar,
    NumberInfo,
    MiniSmoothArea,
    G2Line
  },
  data () {
    return {
      loading: true,
      rankList: [],
      groupNameList: [],
      taskQuantity: {
        total: 0,
        running: 0,
        finish: 0,
        maxRetryCount: 0
      },
      dispatchQuantity: {
        successPercent: '0',
        total: 0
      },
      countActivePodQuantity: {
        clientTotal: 0,
        serverTotal: 0,
        total: 0
      },
      pieStyle: {
        stroke: '#fff',
        lineWidth: 1
      },
      value: ''
    }
  },
  computed: {},
  methods: {
    jumpPosList () {
      this.$router.push({ path: '/dashboard/pods' })
    },
    dataHandler (type) {
      this.$refs.viewChart.getLineDispatchQuantity(this.value, type)
      this.getRankSceneQuantity(this.value, type)
    },
    handleChange (value) {
      this.value = value
      this.$refs.viewChart.getLineDispatchQuantity(value)
      this.getRankSceneQuantity(this.value)
    },
    dateChange (date, dateString) {
      console.log(dateString)
      const startTime = dateString[0]
      const endTime = dateString[1]
      this.$refs.viewChart.getLineDispatchQuantity(this.value, 'others', startTime, endTime)
      this.getRankSceneQuantity(this.value, 'day', startTime, endTime)
    },
    getRankSceneQuantity (groupName, type = 'day', startTime, endTime) {
      rankSceneQuantity({
        'groupName': groupName,
        'type': type,
        'startTime': startTime,
        'endTime': endTime
      }).then(res => {
        this.rankList = []
        res.data.forEach(res => {
          this.rankList.push({
            name: res.groupName + '/' + res.sceneName,
            total: res.total
          })
        })
      })
    }
  },
  created () {
    getAllGroupNameList().then(res => {
      this.groupNameList = res.data
    })

    countTask().then(res => {
      this.taskQuantity = res.data
    })

    countDispatch().then(res => {
      this.dispatchQuantity = res.data
    })

    countActivePod().then(res => {
      this.countActivePodQuantity = res.data
    })

    this.getRankSceneQuantity()

    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
  }
}
</script>

<style lang='less' scoped>
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
