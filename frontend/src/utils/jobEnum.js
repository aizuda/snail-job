const enums = {
  jobStatusEnum: {
    '0': {
      'name': '关闭',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '开启',
      'color': '#f5a22d'
    }
  },
  taskType: {
    '1': {
      'name': '集群模式',
      'color': '#d06892'
    },
    '2': {
      'name': '广播模式',
      'color': '#f5a22d'
    },
    '3': {
      'name': '分片模式',
      'color': '#e1f52d'
    }
  },
  triggerType: {
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
    },
    '4': {
      'name': '任务丢弃',
      'color': '#3a2f81'
    },
    '5': {
      'name': '任务被覆盖',
      'color': '#c2238a'
    },
    '6': {
      'name': '无可执行任务项',
      'color': '#23c28a'
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
