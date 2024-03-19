import request from '@/utils/request'
const jobApi = {
  // 任务信息
  jobPageList: '/job/page/list',
  jobList: '/job/list',
  jobDetail: '/job',
  saveJob: '/job',
  updateJob: '/job',
  updateJobStatus: '/job/status',
  delJob: '/job',
  timeByCron: '/job/cron',
  jobNameList: '/job/job-name/list',
  triggerJob: '/job/trigger',

  // 任务批次
  jobBatchList: '/job/batch/list',
  jobBatchDetail: '/job/batch',
  stop: '/job/batch/stop',
  retry: '/job/batch/retry',

  // 通知
  jobNotifyConfigPageList: '/job/notify/config/page/list',
  jobNotifyConfigDetail: '/job/notify/config',
  saveJobNotify: '/job/notify/config',
  updateJobNotify: '/job/notify/config',

  // 任务
  jobTaskList: '/job/task/list',

  // 日志
  jobLogList: '/job/log/list',

  // DAG
  workflowListPage: '/workflow/page/list',
  saveWorkflow: '/workflow',
  updateWorkflow: '/workflow',
  workflowDetail: '/workflow',
  workflowBatchListPage: '/workflow/batch/page/list',
  workflowBatchDetail: '/workflow/batch',
  updateStatus: '/workflow/update/status',
  delWorkflow: '/workflow',
  triggerWorkflow: '/workflow/trigger',
  stopWorkflowBatch: '/workflow/batch/stop',
  workflowNameList: '/workflow/workflow-name/list'
}

export default jobApi

export function retry (id) {
  return request({
    url: `${jobApi.retry}/${id}`,
    method: 'post'
  })
}
export function workflowNameList (parameter) {
  return request({
    url: jobApi.workflowNameList,
    method: 'get',
    params: parameter
  })
}

export function stopWorkflowBatch (id) {
  return request({
    url: `${jobApi.stopWorkflowBatch}/${id}`,
    method: 'post'
  })
}

export function triggerWorkflow (id) {
  return request({
    url: `${jobApi.triggerWorkflow}/${id}`,
    method: 'post'
  })
}

export function delWorkflow (id) {
  return request({
    url: `${jobApi.delWorkflow}/${id}`,
    method: 'delete'
  })
}

export function updateWorkflowStatus (id) {
  return request({
    url: `${jobApi.updateStatus}/${id}`,
    method: 'put'
  })
}

export function workflowBatchDetail (id) {
  return request({
    url: `${jobApi.workflowBatchDetail}/${id}`,
    method: 'get'
  })
}

export function workflowBatchListPage (parameter) {
  return request({
    url: jobApi.workflowBatchListPage,
    method: 'get',
    params: parameter
  })
}

export function workflowListPage (parameter) {
  return request({
    url: jobApi.workflowListPage,
    method: 'get',
    params: parameter
  })
}

export function triggerJob (id) {
  return request({
    url: `${jobApi.triggerJob}/${id}`,
    method: 'post'
  })
}

export function stop (id) {
  return request({
    url: jobApi.stop + id,
    method: 'post'
  })
}

export function jobNameList (parameter) {
  return request({
    url: jobApi.jobNameList,
    method: 'get',
    params: parameter
  })
}

export function timeByCron (parameter) {
  return request({
    url: jobApi.timeByCron,
    method: 'get',
    params: parameter
  })
}

export function delJob (id) {
  return request({
    url: `${jobApi.delJob}/${id}`,
    method: 'delete'
  })
}

export function updateJobStatus (data) {
  return request({
    url: jobApi.updateJobStatus,
    method: 'put',
    data
  })
}

export function jobLogList (parameter) {
  return request({
    url: jobApi.jobLogList,
    method: 'get',
    params: parameter
  })
}

export function jobTaskList (parameter) {
  return request({
    url: jobApi.jobTaskList,
    method: 'get',
    params: parameter
  })
}

export function jobBatchList (parameter) {
  return request({
    url: jobApi.jobBatchList,
    method: 'get',
    params: parameter
  })
}

export function jobBatchDetail (id) {
  return request({
    url: `${jobApi.jobBatchDetail}/${id}`,
    method: 'get'
  })
}

export function getJobPageList (parameter) {
  return request({
    url: jobApi.jobPageList,
    method: 'get',
    params: parameter
  })
}

export function getJobList (parameter) {
  return request({
    url: jobApi.jobList,
    method: 'get',
    params: parameter
  })
}

export function getJobDetail (id) {
  return request({
    url: `${jobApi.jobDetail}/${id}`,
    method: 'get'
  })
}

export function saveJob (data) {
  return request({
    url: jobApi.saveJob,
    method: 'post',
    data
  })
}

export function updateJob (data) {
  return request({
    url: jobApi.updateJob,
    method: 'put',
    data
  })
}

export function jobNotifyConfigPageList (parameter) {
  return request({
    url: jobApi.jobNotifyConfigPageList,
    method: 'get',
    params: parameter
  })
}
export function getJobNotifyConfigDetail (id) {
  return request({
    url: `${jobApi.jobNotifyConfigDetail}/${id}`,
    method: 'get'
  })
}

export function saveJobNotify (data) {
  return request({
    url: jobApi.saveJobNotify,
    method: 'post',
    data
  })
}

export function updateJobNotify (data) {
  return request({
    url: jobApi.updateJobNotify,
    method: 'put',
    data
  })
}
