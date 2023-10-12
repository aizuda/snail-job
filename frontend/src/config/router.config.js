// eslint-disable-next-line
import { UserLayout, BasicLayout, BlankLayout } from '@/layouts'

const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

export const asyncRouterMap = [
  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: 'menu.home' },
    redirect: '/dashboard/analysis',
    children: [
      // dashboard
      {
        path: '/dashboard',
        name: 'dashboard',
        redirect: '/dashboard/analysis',
        hideChildrenInMenu: true,
        component: RouteView,
        meta: { title: 'menu.dashboard', keepAlive: true, icon: 'dashboard', permission: ['dashboard'] },
        children: [
          {
            path: '/dashboard/analysis',
            name: 'Analysis',
            component: () => import('@/views/dashboard/Analysis'),
            meta: { title: 'menu.dashboard.analysis', keepAlive: true, permission: ['dashboard'] }
          },
          {
            path: '/dashboard/pods',
            name: 'PodList',
            component: () => import('@/views/dashboard/PodList'),
            meta: { title: 'menu.dashboard.analysis', keepAlive: true, permission: ['dashboard'] }
          }
        ]
      },
      // profile
      {
        path: '/basic-config-list',
        name: 'basicConfigList',
        component: () => import('@/views/config/GroupList'),
        meta: { title: '组管理', icon: 'team', permission: ['group'] }
      },
      {
        path: '/basic-config',
        name: 'basicConfig',
        hidden: true,
        component: () => import('@/views/config/basicConfigForm/BasicConfigForm'),
        meta: { title: '基础信息配置', hidden: true, hideChildrenInMenu: true, icon: 'profile', permission: ['basicConfig'] }
      },
      {
        path: '/retry',
        name: 'RetryTask',
        component: RouteView,
        redirect: '/retry/list',
        meta: { title: '重试任务管理', icon: 'schedule', hideChildrenInMenu: true, keepAlive: true, permission: ['retryTask'] },
        children: [
          {
            path: '/retry/list',
            name: 'RetryTaskList',
            component: () => import('@/views/task/RetryTaskList'),
            meta: { title: '重试任务', icon: 'profile', keepAlive: true, permission: ['retryTask'] }
          },
          {
            path: '/retry/info',
            name: 'RetryTaskInfo',
            hidden: true,
            component: () => import('@/views/task/RetryTaskInfo'),
            meta: { title: '任务管理详情', icon: 'profile', keepAlive: true, permission: ['retryTask'] }
          },
          {
            path: '/retry/dead-letter/list',
            name: 'RetryDeadLetterList',
            component: () => import('@/views/task/RetryDeadLetterList'),
            meta: { title: '死信队列', icon: 'profile', permission: ['retryDeadLetter'] }
          },
          {
            path: '/retry/dead-letter/info',
            name: 'RetryDeadLetterInfo',
            hidden: true,
            component: () => import('@/views/task/RetryDeadLetterInfo'),
            meta: { title: '死信队列管理详情', icon: 'profile', permission: ['retryDeadLetter'] }
          },
          {
            path: '/retry/log/list',
            name: 'RetryLogList',
            component: () => import('@/views/task/RetryLogList'),
            meta: { title: '重试日志', icon: 'profile', permission: ['retryLog'] }
          },
          {
            path: '/retry/log/info',
            name: 'RetryLogInfo',
            hidden: true,
            component: () => import('@/views/task/RetryLogInfo'),
            meta: { title: '重试日志详情', icon: 'profile', permission: ['retryLog'] }
          }
          ]
      },
      // {
      //   path: '/retry-dead-letter',
      //   name: 'RetryDeadLetter',
      //   component: RouteView,
      //   hideChildrenInMenu: true,
      //   redirect: '/retry-dead-letter/list',
      //   meta: { title: '死信队列管理', icon: 'exception', permission: ['retryDeadLetter'] },
      //   children: [
      //     {
      //       path: '/retry-dead-letter/list',
      //       name: 'RetryDeadLetterList',
      //       component: () => import('@/views/task/RetryDeadLetterList'),
      //       meta: { title: '死信队列管理列表', icon: 'profile', permission: ['retryDeadLetter'] }
      //     },
      //     {
      //       path: '/retry-dead-letter/info',
      //       name: 'RetryDeadLetterInfo',
      //       component: () => import('@/views/task/RetryDeadLetterInfo'),
      //       meta: { title: '死信队列管理详情', icon: 'profile', permission: ['retryDeadLetter'] }
      //     }
      //   ]
      // },
      {
        path: '/job',
        name: 'Job',
        component: RouteView,
        redirect: '/job/list',
        meta: { title: '定时任务管理', icon: 'profile', permission: ['retryLog'] },
        children: [
          {
            path: '/job/list',
            name: 'JobList',
            component: () => import('@/views/job/JobList'),
            meta: { title: '任务信息', icon: 'profile', permission: ['retryLog'] }
          },
          {
            path: '/job/info',
            name: 'JobInfo',
            hidden: true,
            component: () => import('@/views/job/JobInfo'),
            meta: { title: '定时任务详情', icon: 'profile', permission: ['retryLog'] }
          },
          {
            path: '/job/batch/list',
            name: 'JobBatch',
            component: () => import('@/views/job/JobBatch'),
            meta: { title: '任务批次', icon: 'profile', permission: ['retryLog'] }
          },
          {
            path: '/job/task/list',
            name: 'JobTask',
            component: () => import('@/views/job/JobTask'),
            meta: { title: '任务项', icon: 'profile', permission: ['retryLog'] }
          }
        ]
      },
      {
        path: '/user-list',
        name: 'UserList',
        component: () => import('@/views/user/UserList'),
        meta: { title: '用户管理', icon: 'user', permission: ['user'] }
      },
      {
        path: '/user-form',
        name: 'UserForm',
        hidden: true,
        component: () => import('@/views/user/UserForm'),
        meta: { title: '新增或更新用户', icon: 'profile', permission: ['userForm'] }
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      },
      {
        path: 'recover',
        name: 'recover',
        component: undefined
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
