import storage from 'store'
import { login, getInfo } from '@/api/login'
import { ACCESS_TOKEN, APP_NAMESPACE } from '@/store/mutation-types'
import { welcome, permissionsConfig } from '@/utils/util'

const user = {
  state: {
    token: '',
    name: '',
    welcome: '',
    avatar: '',
    roles: [],
    info: {},
    namespaces: []
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_NAME: (state, { name, welcome }) => {
      state.name = name
      state.welcome = welcome
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_INFO: (state, info) => {
      state.info = info
    },
    SET_NAMESPACES: (state, namespaces) => {
      state.namespaces = namespaces
      if (storage.get(APP_NAMESPACE)) {
      } else {
        storage.set(APP_NAMESPACE, namespaces[0].uniqueId)
      }
    }
  },

  actions: {
    // 登录
    Login ({ commit }, userInfo) {
      return new Promise((resolve, reject) => {
        login(userInfo).then(response => {
          const { data } = response
          storage.set(ACCESS_TOKEN, data.token, 7 * 60 * 60 * 1000)
          commit('SET_TOKEN', data.token)
          commit('SET_NAMESPACES', data.namespaceIds)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 获取用户信息
    GetInfo ({ commit }) {
      return new Promise((resolve, reject) => {
        getInfo().then(response => {
          const result = response.data
          result['role'] = {
            permissions: permissionsConfig(result.role, result.mode)
          }

          commit('SET_NAMESPACES', result.namespaceIds)

          if (result.role && result.role.permissions.length > 0) {
            const role = result.role
            role.permissions = result.role.permissions
            role.permissions.map(per => {
              if (per.actionEntitySet != null && per.actionEntitySet.length > 0) {
                const action = per.actionEntitySet.map(action => {
                  return action.action
                })
                per.actionList = action
              }
            })

            role.permissionList = role.permissions.map(permission => {
              return permission.permissionId
            })
            commit('SET_ROLES', result.role)
            commit('SET_INFO', result)
          } else {
            reject(new Error('getInfo: roles must be a non-null array !'))
          }

          commit('SET_NAME', { name: result.username, welcome: welcome() })

          resolve(response)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 登出
    Logout ({ commit, state }) {
      return new Promise((resolve) => {
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        storage.remove(ACCESS_TOKEN)
        storage.remove(APP_NAMESPACE)
        resolve()
      })
    }

  }
}

export default user
