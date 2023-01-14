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
  retryTaskLogPage: '/retry-task-log/list',
  retryTaskLogById: '/retry-task-log/',
  retryDeadLetterPage: '/retry-dead-letter/list',
  retryDeadLetterById: '/retry-dead-letter/',
  retryDeadLetterRollback: '/retry-dead-letter/rollback/',
  deleteRetryDeadLetter: '/retry-dead-letter/',
  scenePageList: '/scene-config/page/list',
  sceneList: '/scene-config/list',
  notifyConfigList: '/notify-config/list',
  userPageList: '/user/page/list',
  saveUser: '/user',
  systemUserByUserName: '/user/username/user-info'
}

export default api

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

export function rollbackRetryDeadLetter (id, parameter) {
  return request({
    url: api.retryDeadLetterRollback + id,
    method: 'get',
    params: parameter
  })
}

export function deleteRetryDeadLetter (id, parameter) {
  return request({
    url: api.deleteRetryDeadLetter + id,
    method: 'delete',
    params: parameter
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
