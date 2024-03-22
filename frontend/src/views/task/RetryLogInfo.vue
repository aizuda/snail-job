<template>
  <div>
    <page-header-wrapper @back="() => $router.replace('/retry/log/list')" style="margin: -24px -1px 0" v-if="showHeader">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" :loading="loading">
      <a-descriptions title="" :column="column" bordered>
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
        <a-descriptions-item label="当前重试状态 | 数据类型">
          <a-tag v-if="retryInfo.taskType" color="red">
            {{ retryStatus[retryInfo.retryStatus] }}
          </a-tag>
          <a-divider type="vertical" />
          <a-tag v-if="retryInfo.taskType" :color="taskType[retryInfo.taskType].color">
            {{ taskType[retryInfo.taskType].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ retryInfo.createDt }}
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
    <RetryTaskLogMessage :value="retryInfo" />
  </div>
</template>

<script>
import { getRetryTaskLogById } from '@/api/manage'
import { STable } from '@/components'
import RetryTaskLogMessage from '@/views/task/RetryTaskLogMessage'

export default {
  name: 'RetryLogInfo',
  components: {
    RetryTaskLogMessage,
    STable
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
      retryInfo: {},
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
      this.getRetryTaskLogById(id)
    }
  },
  methods: {
    getRetryTaskLogById (id) {
      getRetryTaskLogById(id).then(res => {
        this.retryInfo = res.data
        this.queryParam = {
          groupName: res.data.groupName,
          uniqueId: res.data.uniqueId
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
