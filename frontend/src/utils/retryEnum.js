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
  notifyScene: {
    '1': {
      'name': '场景重试数量超过阈值',
      'color': '#d06892'
    },
    '2': {
      'name': '场景重试失败数量超过阈值',
      'color': '#f5a22d'
    },
    '3': {
      'name': '客户端上报失败',
      'color': '#e1f52d'
    },
    '4': {
      'name': '客户端组件异常',
      'color': '#a127f3'
    },
    '5': {
      'name': '任务重试失败数量超过阈值',
      'color': '#f5a22d'
    },
    '6': {
      'name': '任务重试失败进入死信队列',
      'color': '#f5a22d'
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
  notifyType: {
    '1': {
      'name': '钉钉通知',
      'color': '#64a6ea'
    },
    '2': {
      'name': '邮箱通知',
      'color': '#1b7ee5'
    },
    '4': {
      'name': '飞书',
      'color': '#087da1'
    }
  },
  rateLimiterStatus: {
    '0': {
      'name': '未启用',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '启用',
      'color': '#f5a22d'
    }
  },
  notifyStatus: {
    '0': {
      'name': '停用',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '启用',
      'color': '#f5a22d'
    }
  },
  idGenMode: {
    '1': {
      'name': '号段模式',
      'color': '#1b7ee5'
    },
    '2': {
      'name': '雪花算法',
      'color': '#087da1'
    }
  },
  groupStatus: {
    '0': {
      'name': '停用',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '启用',
      'color': '#f5a22d'
    }
  },
  initScene: {
    '0': {
      'name': '否',
      'color': '#9c1f1f'
    },
    '1': {
      'name': '是',
      'color': '#f5a22d'
    }
  }
}

module.exports = enums
