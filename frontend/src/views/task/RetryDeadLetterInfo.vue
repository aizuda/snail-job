<template>
  <div>
    <page-header-wrapper @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>

    <a-card :bordered="false">
      <a-descriptions title="" bordered>
        <a-descriptions-item label="组名称">
          {{ retryDealLetterInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="场景名称">
          {{ retryDealLetterInfo.sceneName }}
        </a-descriptions-item>
        <a-descriptions-item label="业务id" span="2">
          {{ retryDealLetterInfo.idempotentId }}
        </a-descriptions-item>
        <a-descriptions-item label="业务编号">
          {{ retryDealLetterInfo.bizNo }}
        </a-descriptions-item>
        <a-descriptions-item label="下次触发时间">
          {{ parseDate(retryDealLetterInfo.nextTriggerAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="数据类型">
          <a-tag :color="taskType[retryDealLetterInfo.taskType].color">
            {{ taskType[retryDealLetterInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ parseDate( retryDealLetterInfo.createDt) }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ parseDate(retryDealLetterInfo.updateDt) }}
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" span="2">
          {{ retryDealLetterInfo.executorName }}
        </a-descriptions-item>
        <a-descriptions-item label="扩展参数" span="3">
          {{ retryDealLetterInfo.bizNo }}
        </a-descriptions-item>
        <a-descriptions-item label="参数" span="3">
          {{ retryDealLetterInfo.argsStr }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

  </div>
</template>

<script>
import { getRetryDeadLetterById } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'RetryDeadLetterInfo',
  data () {
    return {
      retryDealLetterInfo: {},
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
      getRetryDeadLetterById(id, { 'groupName': groupName }).then(res => {
        this.retryDealLetterInfo = res.data
      })
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
