<template>
  <div>
    <page-header-wrapper @back="() => $router.replace('/retry/list')" style="margin: -24px -1px 0" v-if="showHeader">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" :loading="loading">
      <a-descriptions title="" :column="column" bordered>
        <a-descriptions-item label="组名称">
          {{ retryTaskInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="场景名称">
          {{ retryTaskInfo.sceneName }}
        </a-descriptions-item>
        <a-descriptions-item label="幂等id">
          {{ retryTaskInfo.idempotentId }}
        </a-descriptions-item>
        <a-descriptions-item label="唯一id">
          {{ retryTaskInfo.uniqueId }}
        </a-descriptions-item>
        <a-descriptions-item label="业务编号">
          {{ retryTaskInfo.bizNo }}
        </a-descriptions-item>
        <a-descriptions-item label="重试次数">
          {{ retryTaskInfo.retryCount }}
        </a-descriptions-item>
        <a-descriptions-item label="重试状态 | 数据类型">
          <a-tag v-if="retryTaskInfo.retryStatus" color="red">
            {{ retryStatus[retryTaskInfo.retryStatus] }}
          </a-tag>
          <a-divider type="vertical" />
          <a-tag v-if="retryTaskInfo.retryStatus" :color="taskType[retryTaskInfo.taskType].color">
            {{ taskType[retryTaskInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="下次触发时间">
          {{ retryTaskInfo.nextTriggerAt }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ retryTaskInfo.updateDt }}
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" span="3">
          {{ retryTaskInfo.executorName }}
        </a-descriptions-item>
        <a-descriptions-item label="参数" span="3">
          {{ retryTaskInfo.argsStr }}
        </a-descriptions-item>
        <a-descriptions-item label="扩展参数" span="3">
          {{ retryTaskInfo.extAttrs }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
    <RetryTaskLogMessage :value="retryTaskInfo" />
  </div>
</template>

<script>
import { getRetryTaskById } from '@/api/manage'
import RetryTaskLogMessage from './RetryTaskLogMessage'

export default {
  name: 'RetryTaskInfo',
  components: {
    RetryTaskLogMessage
  },
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
      loading: true,
      retryTaskInfo: {},
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
      this.getRetryTaskById(id, groupName)
    } else {
      if (this.showHeader) {
        this.$router.push({ path: '/404' })
      }
    }
  },
  methods: {
    getRetryTaskById (id, groupName) {
      getRetryTaskById(id, { 'groupName': groupName }).then(res => {
        this.retryTaskInfo = res.data
        this.queryParam = {
          groupName: this.retryTaskInfo.groupName,
          uniqueId: this.retryTaskInfo.uniqueId
        }
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>

</style>
