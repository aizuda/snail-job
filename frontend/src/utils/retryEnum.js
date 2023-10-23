const enums = {
  sceneStatus: {
    '0': {
      'name': '停用',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '启用',
      'color': '#f5a22d'
    }
  },
  backOffLabels: {
    '1': {
      'name': '延迟等级',
      'color': '#d06892'
    },
    '2': {
      'name': '固定时间',
      'color': '#f5a22d'
    },
    '3': {
      'name': 'CRON表达式',
      'color': '#e1f52d'
    },
    '4': {
      'name': '随机等待',
      'color': '#a127f3'
    }
  },
  triggerInterval: {
    '1': {
      'name': 'CRON表达式',
      'color': '#d06892'
    },
    '2': {
      'name': '固定时间',
      'color': '#f5a22d'
    }
  },
  blockStrategy: {
    '1': {
      'name': '丢弃策略',
      'color': '#d06892'
    },
    '2': {
      'name': '覆盖',
      'color': '#f5a22d'
    },
    '3': {
      'name': '并行',
      'color': '#e1f52d'
    }
  },
  executorType: {
    '1': {
      'name': 'Java',
      'color': '#d06892'
    }
  },
  routeKey: {
    '4': {
      'name': '轮询',
      'color': '#8f68d2'
    },
    '1': {
      'name': '一致性Hash',
      'color': '#d06892'
    },
    '2': {
      'name': '随机',
      'color': '#f5a22d'
    },
    '3': {
      'name': 'LRU',
      'color': '#e1f52d'
    }
  },
  taskBatchStatus: {
    '1': {
      'name': '待处理',
      'color': '#64a6ea'
    },
    '2': {
      'name': '运行中',
      'color': '#1b7ee5'
    },
    '3': {
      'name': '成功',
      'color': '#087da1'
    },
    '4': {
      'name': '失败',
      'color': '#f52d80'
    },
    '5': {
      'name': '停止',
      'color': '#ac2df5'
    },
    '6': {
      'name': '取消',
      'color': '#f5732d'
    }
  },
  operationReason: {
    '0': {
      'name': ''
    },
    '1': {
      'name': '执行超时',
      'color': '#64a6ea'
    },
    '2': {
      'name': '无客户端节点',
      'color': '#1b7ee5'
    },
    '3': {
      'name': '任务已关闭',
      'color': '#087da1'
    }
  },
  taskStatus: {
    '2': {
      'name': '运行中',
      'color': '#1b7ee5'
    },
    '3': {
      'name': '成功',
      'color': '#087da1'
    },
    '4': {
      'name': '失败',
      'color': '#f52d80'
    },
    '5': {
      'name': '停止',
      'color': '#ac2df5'
    }
  }
}

module.exports = enums
