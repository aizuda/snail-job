<template>
  <div>
    <page-header-wrapper @back="() => $router.replace('/job/list')" style="margin: -24px -1px 0" v-if="showHeader">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" v-if="jobInfo !==null ">
      <a-descriptions title="" :column="column" bordered>
        <a-descriptions-item label="组名称">
          {{ jobInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务名称">
          {{ jobInfo.jobName }}
        </a-descriptions-item>
        <a-descriptions-item label="重试状态">
          <a-tag :color="jobStatusEnum[jobInfo.jobStatus].color">
            {{ jobStatusEnum[jobInfo.jobStatus].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="路由策略">
          <a-tag :color="routeKey[jobInfo.routeKey].color">
            {{ routeKey[jobInfo.routeKey].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="阻塞策略">
          <a-tag :color="blockStrategy[jobInfo.blockStrategy].color">
            {{ blockStrategy[jobInfo.blockStrategy].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="并行数">
          {{ jobInfo.parallelNum }}
        </a-descriptions-item>
        <a-descriptions-item label="最大重试次数">
          {{ jobInfo.maxRetryTimes }}次
        </a-descriptions-item>
        <a-descriptions-item label="重试间隔">
          {{ jobInfo.retryInterval }}(秒)
        </a-descriptions-item>
        <a-descriptions-item label="超时时间">
          {{ jobInfo.executorTimeout }}(秒)
        </a-descriptions-item>
        <a-descriptions-item label="下次触发时间">
          {{ jobInfo.nextTriggerAt }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间" span="4">
          {{ jobInfo.updateDt }}
        </a-descriptions-item>
        <a-descriptions-item label="触发类型" span="1">
          <a-tag :color="triggerType[jobInfo.triggerType].color">
            {{ triggerType[jobInfo.triggerType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="间隔时长" span="4">
          {{ jobInfo.triggerInterval }}
        </a-descriptions-item>
        <a-descriptions-item label="执行器类型">
          <a-tag :color="executorType[jobInfo.executorType].color">
            {{ executorType[jobInfo.executorType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" span="4">
          {{ jobInfo.executorInfo }}
        </a-descriptions-item>
        <a-descriptions-item label="任务类型">
          <a-tag :color="taskType[jobInfo.taskType].color">
            {{ taskType[jobInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="参数" span="4">
          {{ jobInfo.taskType === 3 ? JSON.parse(jobInfo.argsStr).map((item, index) => `分区:${index}=>${item}`).join('; ') : jobInfo.argsStr }}
        </a-descriptions-item>
        <a-descriptions-item label="描述" span="4">
          {{ jobInfo.extAttrs }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
import { getJobDetail } from '@/api/jobApi'
import moment from 'moment'
import enums from '@/utils/jobEnum'

export default {
  name: 'JobInfo',
  components: {
  },
  props: {
    showHeader: {
      type: Boolean,
      default: true
    },
    column: {
      type: Number,
      default: 4
    }
  },
  data () {
    return {
      jobInfo: null,
      jobStatusEnum: enums.jobStatusEnum,
      taskType: enums.taskType,
      triggerType: enums.triggerType,
      blockStrategy: enums.blockStrategy,
      executorType: enums.executorType,
      routeKey: enums.routeKey
    }
  },
  created () {
    const id = this.$route.query.id
    const groupName = this.$route.query.groupName
    if (id && groupName) {
      this.jobDetail(id)
    } else {
      if (this.showHeader) {
        this.$router.push({ path: '/404' })
      }
    }
  },
  methods: {
    parseDate (date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    },
    jobDetail (id) {
      getJobDetail(id).then(res => {
        this.jobInfo = res.data
      })
    }
  }
}
</script>

<style scoped>

</style>
