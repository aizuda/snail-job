import request from '@/utils/request'
const jobApi = {
  // 任务信息
  jobList: '/job/list',
  jobDetail: '/job/',
  saveJob: '/job/',
  updateJob: '/job/',
  updateJobStatus: '/job/status',
  delJob: '/job/',

  // 任务批次
  jobBatchList: '/job/batch/list',
  jobBatchDetail: '/job/batch/',

  // 任务
  jobTaskList: '/job/task/list',

  // 日志
  jobLogList: '/job/log/list'
}

export default jobApi

export function delJob (id) {
  return request({
    url: jobApi.delJob + id,
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
    url: jobApi.jobBatchDetail + id,
    method: 'get'
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
    url: jobApi.jobDetail + id,
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
