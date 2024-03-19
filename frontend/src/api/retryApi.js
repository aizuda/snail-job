import request from '@/utils/request'
import api from '@/api/manage'
const retryApi = {
  // -------------- 场景配置 -------------------
  scenePageList: '/scene-config/page/list',
  sceneList: '/scene-config/list',
  sceneDetail: '/scene-config',
  saveScene: '/scene-config',
  updateScene: '/scene-config',

  // -------------- 通知配置 -------------------
  notifyConfigList: '/notify-config/list',
  notifyConfigDetail: '/notify-config',
  saveNotify: '/notify-config',
  updateNotify: '/notify-config'

}

export default retryApi

export function getNotifyConfigList (parameter) {
  return request({
    url: retryApi.notifyConfigList,
    method: 'get',
    params: parameter
  })
}

export function getNotifyConfigDetail (id) {
  return request({
    url: `${retryApi.notifyConfigDetail}/${id}`,
    method: 'get'
  })
}

export function saveNotify (data) {
  return request({
    url: retryApi.saveNotify,
    method: 'post',
    data
  })
}

export function updateNotify (data) {
  return request({
    url: retryApi.updateNotify,
    method: 'put',
    data
  })
}

// -------------- 场景配置 -------------------
export function getScenePage (parameter) {
  return request({
    url: api.scenePageList,
    method: 'get',
    params: parameter
  })
}

export function sceneList (parameter) {
  return request({
    url: retryApi.sceneList,
    method: 'get',
    params: parameter
  })
}

export function getSceneDetail (id) {
  return request({
    url: `${retryApi.sceneDetail}/${id}`,
    method: 'get'
  })
}

export function saveScene (data) {
  return request({
    url: retryApi.saveScene,
    method: 'post',
    data
  })
}

export function updateScene (data) {
  return request({
    url: retryApi.updateScene,
    method: 'put',
    data
  })
}
