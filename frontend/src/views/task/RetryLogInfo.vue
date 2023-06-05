<template>
  <div>
    <page-header-wrapper @back="() => $router.go(-1)" style='margin: -24px -1px 0'>
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false">
      <a-descriptions title="" bordered>
        <a-descriptions-item label="组名称">
          {{ retryInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="场景名称">
          {{ retryInfo.sceneName }}
        </a-descriptions-item>
        <a-descriptions-item label="唯一id">
          {{ retryInfo.uniqueId }}
        </a-descriptions-item>
        <a-descriptions-item label="幂等id" :span="2">
          {{ retryInfo.idempotentId }}
        </a-descriptions-item>
        <a-descriptions-item label="业务编号">
          {{ retryInfo.bizNo }}
        </a-descriptions-item>
        <a-descriptions-item label="下次触发时间">
          {{ parseDate(retryInfo.nextTriggerAt) }}
        </a-descriptions-item>
        <a-descriptions-item label="执行时间">
          {{ parseDate( retryInfo.createDt) }}
        </a-descriptions-item>
        <a-descriptions-item label="当前重试状态">
          <a-tag color="red">
            {{ retryStatus[retryInfo.retryStatus] }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" :span="3">
          {{ retryInfo.executorName }}
        </a-descriptions-item>
        <a-descriptions-item label="参数" :span="3">
          {{ retryInfo.argsStr }}
        </a-descriptions-item>
        <a-descriptions-item label="失败原因" :span="3">
          {{ retryInfo.errorMessage }}
        </a-descriptions-item>
        <a-descriptions-item label="扩展参数" :span="3">
          {{ retryInfo.extAttrs }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
import { getRetryTaskLogById } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'RetryLogInfo',
  data () {
    return {
      retryInfo: {},
      retryStatus: {
        '0': '处理中',
        '1': '处理成功',
        '2': '最大次数'
      }
    }
  },
  created () {
    const id = this.$route.query.id
    if (id) {
      getRetryTaskLogById(id).then(res => {
        this.retryInfo = res.data
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
