<template>
  <div>
    <page-header-wrapper @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false">
      <a-descriptions title="" bordered v-if="retryInfo !== null">
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
        <a-descriptions-item label="当前重试状态 | 数据类型">
          <a-tag color="red">
            {{ retryStatus[retryInfo.retryStatus] }}
          </a-tag>
          <a-divider type="vertical" />
          <a-tag :color="taskType[retryInfo.taskType].color">
            {{ taskType[retryInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" :span="3">
          {{ retryInfo.executorName }}
        </a-descriptions-item>
        <a-descriptions-item label="参数" :span="3">
          {{ retryInfo.argsStr }}
        </a-descriptions-item>
        <a-descriptions-item label="扩展参数" :span="3">
          {{ retryInfo.extAttrs }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
    <RetryTaskLogMessageList ref="retryTaskLogMessageListRef" />
  </div>
</template>

<script>
import { getRetryTaskLogById } from '@/api/manage'
import moment from 'moment'
import { STable } from '@/components'
import RetryTaskLogMessageList from '@/views/task/RetryTaskLogMessageList'

export default {
  name: 'RetryLogInfo',
  components: {
    RetryTaskLogMessageList,
    STable
  },
  data () {
    return {
      retryInfo: null,
      retryStatus: {
        '0': '处理中',
        '1': '处理成功',
        '2': '最大次数'
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
    if (id) {
      getRetryTaskLogById(id).then(res => {
        this.retryInfo = res.data
        this.queryParam = {
          groupName: this.retryInfo.groupName,
          uniqueId: this.retryInfo.uniqueId
        }
        this.$refs.retryTaskLogMessageListRef.refreshTable(this.queryParam)
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
