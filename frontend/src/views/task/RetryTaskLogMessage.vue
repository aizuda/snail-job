<template>
  <div>
    <div style="margin: 20px 0; border-left: #f5222d 5px solid; font-size: medium; font-weight: bold">
      <span style="padding-left: 18px">调用日志详情</span>
      <span style="padding-left: 18px"><a-icon type="sync" @click="getLogData"/></span>
    </div>
    <a-card>
      <log :value="logList" />
    </a-card>
  </div>

</template>

<script>
import { getRetryTaskLogMessagePage } from '@/api/manage'
import Log from '@/components/Log/index.vue'

export default {
  name: 'RetryTaskLogMessage',
  components: { Log },
  props: {
    value: {
      type: Object,
      default: () => {}
    }
  },
  watch: {
    value: {
      deep: true,
      immediate: true,
      handler () {
        this.getLogData()
      }
    }
  },
  data () {
    return {
      startId: 0,
      fromIndex: 0,
      finished: false,
      logList: [],
      interval: null,
      controller: new AbortController()
    }
  },
  mounted () {
    this.getLogData()
  },
  beforeDestroy () {
    this.stopLog()
  },
  methods: {
    stopLog () {
      this.finished = true
      this.controller.abort()
      clearTimeout(this.interval)
      this.interval = undefined
    },
    getLogData () {
      if (!this.value) {
        return
      }
      getRetryTaskLogMessagePage({ groupName: this.value.groupName, uniqueId: this.value.uniqueId, startId: this.startId, fromIndex: this.fromIndex })
        .then((res) => {
          this.finished = res.data.finished
          this.startId = res.data.nextStartId
          this.fromIndex = res.data.fromIndex
          if (res.data.message) {
            this.logList.push(...res.data.message)
            this.logList.sort((a, b) => a.time_stamp - b.time_stamp)
          }
          if (!this.finished) {
            clearTimeout(this.interval)
            this.interval = setTimeout(this.getLogData, 1000)
          }
        })
        .catch(() => {
          this.finished = true
        })
    }
  }

}
</script>

<style scoped>

</style>
