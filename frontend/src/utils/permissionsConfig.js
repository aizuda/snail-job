const permissionsConfig = {
  RETRY: {
    1: [
      {
        roleId: 1,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: []
      },
      {
        roleId: 1,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 1,
        permissionId: 'retryTask',
        permissionName: '任务管理'
      },
      {
        roleId: 1,
        permissionId: 'retryDeadLetter',
        permissionName: '死信队列管理'
      },
      {
        roleId: 1,
        permissionId: 'retryLog',
        permissionName: '重试日志管理'
      },
      {
        roleId: 1,
        permissionId: 'basicConfig',
        permissionName: '基础信息配置'
      }
    ],
    2: [
      {
        roleId: 2,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: [
          {
            action: 'add',
            describe: '新增',
            defaultCheck: false
          }
        ]
      },
      {
        roleId: 2,
        permissionId: 'user',
        permissionName: '用户'
      },
      {
        roleId: 2,
        permissionId: 'userForm',
        permissionName: '新增或更新用户'
      },
      {
        roleId: 2,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 2,
        permissionId: 'retryTask',
        permissionName: '任务管理'
      },
      {
        roleId: 2,
        permissionId: 'retryDeadLetter',
        permissionName: '死信队列管理'
      },
      {
        roleId: 2,
        permissionId: 'retryLog',
        permissionName: '重试日志管理'
      },
      {
        roleId: 2,
        permissionId: 'basicConfig',
        permissionName: '基础信息配置'
      }
    ]
  },
  JOB: {
    1: [
      {
        roleId: 1,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: []
      },
      {
        roleId: 1,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 1,
        permissionId: 'job',
        permissionName: '定时任务管理'
      },
      {
        roleId: 1,
        permissionId: 'jobBatch',
        permissionName: '任务批次'
      }
    ],
    2: [
      {
        roleId: 2,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: [
          {
            action: 'add',
            describe: '新增',
            defaultCheck: false
          }
        ]
      },
      {
        roleId: 2,
        permissionId: 'user',
        permissionName: '用户'
      },
      {
        roleId: 2,
        permissionId: 'userForm',
        permissionName: '新增或更新用户'
      },
      {
        roleId: 2,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 1,
        permissionId: 'job',
        permissionName: '定时任务管理'
      },
      {
        roleId: 1,
        permissionId: 'jobBatch',
        permissionName: '任务批次'
      }
    ]
  },
  ALL: {
    1: [
      {
        roleId: 1,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: []
      },
      {
        roleId: 1,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 1,
        permissionId: 'retryTask',
        permissionName: '任务管理'
      },
      {
        roleId: 1,
        permissionId: 'retryDeadLetter',
        permissionName: '死信队列管理'
      },
      {
        roleId: 1,
        permissionId: 'retryLog',
        permissionName: '重试日志管理'
      },
      {
        roleId: 1,
        permissionId: 'basicConfig',
        permissionName: '基础信息配置'
      },
      {
        roleId: 1,
        permissionId: 'job',
        permissionName: '定时任务管理'
      },
      {
        roleId: 1,
        permissionId: 'jobBatch',
        permissionName: '任务批次'
      }
    ],
    2: [
      {
        roleId: 2,
        permissionId: 'group',
        permissionName: '组配置',
        actionEntitySet: [
          {
            action: 'add',
            describe: '新增',
            defaultCheck: false
          }
        ]
      },
      {
        roleId: 2,
        permissionId: 'user',
        permissionName: '用户'
      },
      {
        roleId: 2,
        permissionId: 'userForm',
        permissionName: '新增或更新用户'
      },
      {
        roleId: 2,
        permissionId: 'dashboard',
        permissionName: '看板'
      },
      {
        roleId: 2,
        permissionId: 'retryTask',
        permissionName: '任务管理'
      },
      {
        roleId: 2,
        permissionId: 'retryDeadLetter',
        permissionName: '死信队列管理'
      },
      {
        roleId: 2,
        permissionId: 'retryLog',
        permissionName: '重试日志管理'
      },
      {
        roleId: 2,
        permissionId: 'basicConfig',
        permissionName: '基础信息配置'
      },
      {
        roleId: 1,
        permissionId: 'job',
        permissionName: '定时任务管理'
      },
      {
        roleId: 1,
        permissionId: 'jobBatch',
        permissionName: '任务批次'
      }
    ]
  }
}

module.exports = permissionsConfig
