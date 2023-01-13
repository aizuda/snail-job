<template>
  <div>
    <page-header-wrapper>
      <a-card :bordered="false">
        <a-descriptions title="">
          <a-descriptions-item label="组名称">
            {{ retryTaskInfo.groupName }}
          </a-descriptions-item>
          <a-descriptions-item label="场景名称">
            {{ retryTaskInfo.sceneName }}
          </a-descriptions-item>
          <a-descriptions-item label="业务id" span="2">
            {{ retryTaskInfo.bizId }}
          </a-descriptions-item>
          <a-descriptions-item label="业务编号">
            {{ retryTaskInfo.bizNo }}
          </a-descriptions-item>
          <a-descriptions-item label="重试次数">
            {{ retryTaskInfo.retryCount }}
          </a-descriptions-item>
          <a-descriptions-item label="重试状态">
            {{ retryStatus[retryTaskInfo.retryStatus] }}
          </a-descriptions-item>
          <a-descriptions-item label="触发时间">
            {{ parseDate( retryTaskInfo.createDt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ parseDate(retryTaskInfo.updateDt) }}
          </a-descriptions-item>
          <a-descriptions-item label="执行器名称" span="2">
            {{ retryTaskInfo.executorName }}
          </a-descriptions-item>
          <a-descriptions-item label="扩展参数" span="2">
            {{ retryTaskInfo.extAttrs }}
          </a-descriptions-item>
          <a-descriptions-item label="参数" span="3">
            {{ retryTaskInfo.argsStr }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </page-header-wrapper>
  </div>
</template>

<script>
import { getRetryTaskById } from '@/api/manage'
import moment from 'moment'

export default {
  name: 'RetryTaskInfo',
  data () {
    return {
      retryTaskInfo: {},
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
      getRetryTaskById(id, { 'groupName': groupName }).then(res => {
        this.retryTaskInfo = res.data
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
