import request from '@/utils/request'

const api = {
  user: '/user',
  role: '/role',
  service: '/service',
  permission: '/permission',
  permissionNoPager: '/permission/no-pager',
  orgTree: '/org/tree',
  groupConfigForPage: '/group/list',
  saveGroup: '/group',
  groupConfigByGroupName: `/group`,
  allGroupNameList: `/group/all/group-name/list`,
  retryTaskPage: '/retry-task/list',
  retryTaskById: '/retry-task/',
  saveRetryTask: '/retry-task',
  batchSaveRetryTask: '/retry-task/batch',
  idempotentIdGenerate: '/retry-task/generate/idempotent-id',
  batchUpdate: '/retry-task/batch',
  deleteRetryTask: '/retry-task/batch',
  updateRetryTaskStatus: '/retry-task/status',
  manualTriggerRetryTask: '/retry-task/manual/trigger/retry/task',
  manualTriggerCallbackTask: '/retry-task/manual/trigger/callback/task',
  retryTaskLogPage: '/retry-task-log/list',
  retryTaskLogMessagePage: '/retry-task-log/message/list',
  retryTaskLogById: '/retry-task-log/',
  retryDeadLetterPage: '/retry-dead-letter/list',
  retryDeadLetterById: '/retry-dead-letter/',
  retryDeadLetterRollback: '/retry-dead-letter/batch/rollback',
  deleteRetryDeadLetter: '/retry-dead-letter/batch',
  scenePageList: '/scene-config/page/list',
  sceneList: '/scene-config/list',
  notifyConfigList: '/notify-config/list',
  userPageList: '/user/page/list',
  delUser: '/user/',
  saveUser: '/user',
  systemUserByUserName: '/user/username/user-info',
  countTask: '/dashboard/task/count',
  countDispatch: '/dashboard/dispatch/count',
  countActivePod: '/dashboard/active-pod/count',
  rankSceneQuantity: '/dashboard/scene/rank',
  lineDispatchQuantity: '/dashboard/dispatch/line',
  partitionTableList: '/group/partition-table/list',
  totalPartition: '/group/partition',
  systemVersion: '/system/version',
  pods: '/dashboard/pods',
  consumerGroup: '/dashboard/consumer/group',
  updateGroupStatus: '/group/status'
}

export default api

export function delUser (id) {
  return request({
    url: api.delUser + id,
    method: 'delete'
  })
}

export function updateGroupStatus (data) {
  return request({
    url: api.updateGroupStatus,
    method: 'put',
    data
  })
}

export function getRetryTaskLogMessagePage (parameter) {
  return request({
    url: api.retryTaskLogMessagePage,
    method: 'get',
    params: parameter
  })
}

export function pods (parameter) {
  return request({
    url: api.pods,
    method: 'get',
    params: parameter
  })
}

export function systemVersion () {
  return request({
    url: api.systemVersion,
    method: 'get'
  })
}

export function batchDelete (data) {
  return request({
    url: api.deleteRetryTask,
    method: 'delete',
    data
  })
}

export function batchUpdate (data) {
  return request({
    url: api.batchUpdate,
    method: 'put',
    data
  })
}

export function idempotentIdGenerate (data) {
  return request({
    url: api.idempotentIdGenerate,
    method: 'post',
    data
  })
}

export function saveRetryTask (data) {
  return request({
    url: api.saveRetryTask,
    method: 'post',
    data
  })
}

export function batchSaveRetryTask (data) {
  return request({
    url: api.batchSaveRetryTask,
    method: 'post',
    data
  })
}

export function getPartitionTableList () {
  return request({
    url: api.partitionTableList,
    method: 'get'
  })
}

export function getTotalPartition () {
  return request({
    url: api.totalPartition,
    method: 'get'
  })
}

export function getLineDispatchQuantity (parameter) {
  return request({
    url: api.lineDispatchQuantity,
    method: 'get',
    params: parameter
  })
}

export function rankSceneQuantity (parameter) {
  return request({
    url: api.rankSceneQuantity,
    method: 'get',
    params: parameter
  })
}

export function countActivePod () {
  return request({
    url: api.countActivePod,
    method: 'get'
  })
}

export function countTask () {
  return request({
    url: api.countTask,
    method: 'get'
  })
}

export function countDispatch () {
  return request({
    url: api.countDispatch,
    method: 'get'
  })
}

export function getGroupConfigForPage (parameter) {
  return request({
    url: api.groupConfigForPage,
    method: 'get',
    params: parameter
  })
}

export function getAllGroupNameList (parameter) {
  return request({
    url: api.allGroupNameList,
    method: 'get',
    params: parameter
  })
}

export function getRetryTaskPage (parameter) {
  return request({
    url: api.retryTaskPage,
    method: 'get',
    params: parameter
  })
}

export function getRetryTaskById (id, parameter) {
  return request({
    url: api.retryTaskById + id,
    method: 'get',
    params: parameter
  })
}

export function updateRetryTaskStatus (data) {
  return request({
    url: api.updateRetryTaskStatus,
    method: 'put',
    data
  })
}

export function manualTriggerCallbackTask (data) {
  return request({
    url: api.manualTriggerCallbackTask,
    method: 'post',
    data
  })
}

export function manualTriggerRetryTask (data) {
  return request({
    url: api.manualTriggerRetryTask,
    method: 'post',
    data
  })
}

export function getScenePage (parameter) {
  return request({
    url: api.scenePageList,
    method: 'get',
    params: parameter
  })
}

export function getSceneList (parameter) {
  return request({
    url: api.sceneList,
    method: 'get',
    params: parameter
  })
}

export function getRetryTaskLogPage (parameter) {
  return request({
    url: api.retryTaskLogPage,
    method: 'get',
    params: parameter
  })
}

export function getRetryTaskLogById (id) {
  return request({
    url: api.retryTaskLogById + id,
    method: 'get'
  })
}

export function getRetryDeadLetterPage (parameter) {
  return request({
    url: api.retryDeadLetterPage,
    method: 'get',
    params: parameter
  })
}

export function getRetryDeadLetterById (id, parameter) {
  return request({
    url: api.retryDeadLetterById + id,
    method: 'get',
    params: parameter
  })
}

export function rollbackRetryDeadLetter (data) {
  return request({
    url: api.retryDeadLetterRollback,
    method: 'post',
    data
  })
}

export function deleteRetryDeadLetter (data) {
  return request({
    url: api.deleteRetryDeadLetter,
    method: 'delete',
    data
  })
}

export function getUserPage (parameter) {
  return request({
    url: api.userPageList,
    method: 'get',
    params: parameter
  })
}

export function getSystemUserByUserName (parameter) {
  return request({
    url: api.systemUserByUserName,
    method: 'get',
    params: parameter
  })
}

export function getNotifyConfigList (parameter) {
  return request({
    url: api.notifyConfigList,
    method: 'get',
    params: parameter
  })
}

export function getGroupConfigByGroupName (groupName) {
  return request({
    url: api.groupConfigByGroupName + `/${groupName}`,
    method: 'get'
  })
}

export function saveGroup (parameter) {
  return request({
    url: api.saveGroup,
    method: parameter.id === 0 ? 'post' : 'put',
    data: parameter
  })
}

export function saveUser (parameter) {
  return request({
    url: api.saveUser,
    method: parameter.id === undefined ? 'post' : 'put',
    data: parameter
  })
}

export function getUserList (parameter) {
  return request({
    url: api.user,
    method: 'get',
    params: parameter
  })
}

export function getRoleList (parameter) {
  return request({
    url: api.role,
    method: 'get',
    params: parameter
  })
}

export function getServiceList (parameter) {
  return request({
    url: api.service,
    method: 'get',
    params: parameter
  })
}

export function getPermissions (parameter) {
  return request({
    url: api.permissionNoPager,
    method: 'get',
    params: parameter
  })
}

export function getOrgTree (parameter) {
  return request({
    url: api.orgTree,
    method: 'get',
    params: parameter
  })
}

// id == 0 add     post
// id != 0 update  put
export function saveService (parameter) {
  return request({
    url: api.service,
    method: parameter.id === 0 ? 'post' : 'put',
    data: parameter
  })
}

export function saveSub (sub) {
  return request({
    url: '/sub',
    method: sub.id === 0 ? 'post' : 'put',
    data: sub
  })
}
