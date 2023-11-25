<template>
  <div>
    <page-header-wrapper @back="() => $router.replace('/retry/dead-letter/list')" style="margin: -24px -1px 0" v-if="showHeader">
      <div></div>
    </page-header-wrapper>

    <a-card :bordered="false">
      <a-descriptions title="" :column="column" bordered v-if="retryDealLetterInfo !== null">
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
        <a-descriptions-item label="数据类型">
          <a-tag :color="taskType[retryDealLetterInfo.taskType].color">
            {{ taskType[retryDealLetterInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ retryDealLetterInfo.createDt }}
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

export default {
  name: 'RetryDeadLetterInfo',
  props: {
    showHeader: {
      type: Boolean,
      default: true
    },
    column: {
      type: Number,
      default: 3
    }
  },
  data () {
    return {
      retryDealLetterInfo: null,
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
      this.retryDeadLetterById(id, groupName)
    }
  },
  methods: {
    retryDeadLetterById (id, groupName) {
      getRetryDeadLetterById(id, { 'groupName': groupName }).then(res => {
        this.retryDealLetterInfo = res.data
      })
    }

  }
}
</script>

<style scoped>

</style>
