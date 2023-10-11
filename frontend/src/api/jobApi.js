import request from '@/utils/request'
const jobApi = {

  jobList: '/job/list'
}

export default jobApi

export function getJobList (parameter) {
  return request({
    url: jobApi.jobList,
    method: 'get',
    params: parameter
  })
}
