import request from '@/utils/request'
const jobApi = {
  // 任务信息
  jobList: '/job/list',
  jobDetail: '/job/',
  saveJob: '/job/',
  updateJob: '/job/',

  // 任务批次
  jobBatchList: '/job/batch/list',
  jobBatchDetail: '/job/batch/'

}

export default jobApi

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
