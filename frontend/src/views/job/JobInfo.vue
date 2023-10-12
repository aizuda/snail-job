<template>
  <div>
    <page-header-wrapper @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" v-if="jobInfo !==null ">
      <a-descriptions title="" bordered>
        <a-descriptions-item label="组名称">
          {{ jobInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务名称">
          {{ jobInfo.jobName }}
        </a-descriptions-item>
        <a-descriptions-item label="重试次数">
          {{ jobInfo.retryCount }}
        </a-descriptions-item>
        <a-descriptions-item label="重试状态 | 数据类型">
          <a-tag color="red">
            {{ retryStatus[jobInfo.retryStatus] }}
          </a-tag>
          <a-divider type="vertical" />
          <a-tag :color="taskType[jobInfo.taskType].color">
            {{ taskType[jobInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="下次触发时间">
          {{ jobInfo.nextTriggerAt }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ jobInfo.updateDt }}
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" span="3">
          {{ jobInfo.executorName }}
        </a-descriptions-item>
        <a-descriptions-item label="参数" span="3">
          {{ jobInfo.argsStr }}
        </a-descriptions-item>
        <a-descriptions-item label="扩展参数" span="3">
          {{ jobInfo.extAttrs }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
import { getJobDetail } from '@/api/jobApi'
import moment from 'moment'

export default {
  name: 'JobInfo',
  components: {

  },
  data () {
    return {
      jobInfo: null,
      retryStatus: {
        '0': '处理中',
        '1': '处理成功',
        '2': '最大次数',
        '3': '暂停'
      },
      taskType: {
        '1': {
          'name': '重试数据',
          'color': '#d06892'
        },
        '2': {
          'name': '回调数据',
          'color': '#f5a22d'
        }
      }
    }
  },
  created () {
    const id = this.$route.query.id
    const groupName = this.$route.query.groupName
    if (id && groupName) {
      getJobDetail(id).then(res => {
        this.jobInfo = res.data
        // this.queryParam = {
        //   groupName: this.retryTaskInfo.groupName,
        //   uniqueId: this.retryTaskInfo.uniqueId
        // }
        // this.$refs.retryTaskLogMessageListRef.refreshTable(this.queryParam)
      })
    } else {
      this.$router.push({ path: '/404' })
    }
  },
  methods: {
    parseDate (date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  }
}
</script>

<style scoped>

</style>
