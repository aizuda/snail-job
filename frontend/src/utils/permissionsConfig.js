
const commonAdmin = [
  {
    roleId: 2,
    permissionId: 'group',
    permissionName: '组配置',
    actionEntitySet: [
      {
        action: 'add',
        describe: '新增',
        defaultCheck: false
      },
      {
        action: 'edit',
        describe: '更新',
        defaultCheck: false
      },
      {
        action: 'stop',
        describe: '停止',
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
    permissionId: 'basicConfig',
    permissionName: '基础信息配置'
  },
  {
    roleId: 1,
    permissionId: 'namespace',
    permissionName: '命名空间'
  }
]
const retryAdmin = [
  {
    roleId: 2,
    permissionId: 'RetryAnalysis',
    permissionName: '重试任务',
    actionEntitySet: [
      {
        action: 'retry',
        describe: '重试',
        defaultCheck: false
      }
    ]
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
  }
]
const jobAdmin = [
  {
    roleId: 2,
    permissionId: 'JobAnalysis',
    permissionName: '定时任务',
    actionEntitySet: [
      {
        action: 'job',
        describe: '定时',
        defaultCheck: false
      }
    ]
  },
  {
    roleId: 2,
    permissionId: 'dashboard',
    permissionName: '看板'
  },
  {
    roleId: 1,
    permissionId: 'job',
    permissionName: '定时任务管理',
    actionEntitySet: [
      {
        action: 'del',
        describe: '新增',
        defaultCheck: false
      }
    ]
  },
  {
    roleId: 1,
    permissionId: 'jobBatch',
    permissionName: '任务批次'
  }, {
    roleId: 1,
    permissionId: 'jobNotify',
    permissionName: '任务通知'
  }
]

const commonUser = [
  {
    roleId: 1,
    permissionId: 'group',
    permissionName: '组配置',
    actionEntitySet: []
  }
]
const retryUser = [
  {
    roleId: 1,
    permissionId: 'RetryAnalysis',
    permissionName: '重试任务',
    actionEntitySet: [
      {
        action: 'retry',
        describe: '重试',
        defaultCheck: false
      }
    ]
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
  }
]
const jobUser = [
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
  },
  {
    roleId: 1,
    permissionId: 'jobNotify',
    permissionName: '任务通知'
  }
]

const permissionsConfig = {
  RETRY: {
    1: [...retryUser, ...commonUser],
    2: [...retryAdmin, ...commonAdmin]
  },
  JOB: {
    1: [...jobUser, ...commonUser],
    2: [...jobAdmin, ...commonAdmin]
  },
  ALL: {
    1: [...retryUser, ...jobUser, ...commonUser],
    2: [...retryAdmin, ...jobAdmin, ...commonAdmin]
  }
}

module.exports = permissionsConfig
