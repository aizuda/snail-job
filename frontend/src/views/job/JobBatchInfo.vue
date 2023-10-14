<template>
  <div>
    <page-header-wrapper @back="() => $router.go(-1)" style="margin: -24px -1px 0">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" v-if="jobBatchInfo !==null ">
      <a-descriptions title="" :column="5" bordered>
        <a-descriptions-item label="组名称">
          {{ jobBatchInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务名称">
          {{ jobBatchInfo.jobName }}
        </a-descriptions-item>
        <a-descriptions-item label="重试状态">
          <a-tag :color="taskStatus[jobBatchInfo.taskStatus].color">
            {{ taskStatus[jobBatchInfo.taskStatus].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ jobBatchInfo.updateDt }}
        </a-descriptions-item>
        <a-descriptions-item label="执行器名称" span="3">
          {{ jobBatchInfo.executorName }}
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
    <div style="margin: 20px 0; border-left: #f5222d 5px solid; font-size: medium; font-weight: bold">
      &nbsp;&nbsp; 任务项列表
    </div>
    <JobTaskList ref="JobTaskListRef" />
  </div>
</template>

<script>
import { jobBatchDetail, jobTaskList } from '@/api/jobApi'
import moment from 'moment'
import enums from '@/utils/enum'
import JobTaskList from './JobTaskList'

export default {
  name: 'JobInfo',
  components: {
    JobTaskList

  },
  data () {
    return {
      jobBatchInfo: null,
      taskStatus: enums.taskStatus,
      operationReason: enums.operationReason
    }
  },
  created () {
    const id = this.$route.query.id
    const groupName = this.$route.query.groupName
    if (id && groupName) {
      jobBatchDetail(id).then(res => {
        this.jobBatchInfo = res.data
        this.queryParam = {
          groupName: this.jobBatchInfo.groupName,
          taskBatchId: id
        }
        this.$refs.JobTaskListRef.refreshTable(this.queryParam)
      })
    } else {
      this.$router.push({ path: '/404' })
    }
  },
  methods: {
    jobTaskList,
    parseDate (date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  }
}
</script>

<style scoped>

</style>
