<template>
  <div>
    <page-header-wrapper>
      <a-card :bordered="false">
        <a-descriptions title="">
          <a-descriptions-item label="组名称">
            {{ retryDealLetterInfo.groupName }}
          </a-descriptions-item>
          <a-descriptions-item label="场景名称">
            {{ retryDealLetterInfo.sceneName }}
          </a-descriptions-item>
          <a-descriptions-item label="业务id" span="2">
            {{ retryDealLetterInfo.bizId }}
          </a-descriptions-item>
          <a-descriptions-item label="业务编号">
            {{ retryDealLetterInfo.bizNo }}
          </a-descriptions-item>
          <a-descriptions-item label="下次触发时间">
            {{ parseDate(retryDealLetterInfo.nextTriggerAt) }}
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
    </page-header-wrapper>
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
      retryStatus: {
        '0': '重试中',
        '1': '重试完成',
        '2': '最大次数'
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
