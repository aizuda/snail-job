<template>
  <a-card :bordered="false">

<!--    <div class="table-page-search-wrapper">-->
<!--      <a-form layout="inline">-->
<!--        <a-row :gutter="48">-->
<!--          <a-col :md="8" :sm="24">-->
<!--            <a-form-item label="规则编号">-->
<!--              <a-input v-model="queryParam.id" placeholder=""/>-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--          <a-col :md="8" :sm="24">-->
<!--            <a-form-item label="使用状态">-->
<!--              <a-select v-model="queryParam.status" placeholder="请选择" default-value="0">-->
<!--                <a-select-option value="0">全部</a-select-option>-->
<!--                <a-select-option value="1">关闭</a-select-option>-->
<!--                <a-select-option value="2">运行中</a-select-option>-->
<!--              </a-select>-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--          <template v-if="advanced">-->
<!--            <a-col :md="8" :sm="24">-->
<!--              <a-form-item label="调用次数">-->
<!--                <a-input-number v-model="queryParam.callNo" style="width: 100%"/>-->
<!--              </a-form-item>-->
<!--            </a-col>-->
<!--            <a-col :md="8" :sm="24">-->
<!--              <a-form-item label="更新日期">-->
<!--                <a-date-picker v-model="queryParam.date" style="width: 100%" placeholder="请输入更新日期"/>-->
<!--              </a-form-item>-->
<!--            </a-col>-->
<!--            <a-col :md="8" :sm="24">-->
<!--              <a-form-item label="使用状态">-->
<!--                <a-select v-model="queryParam.useStatus" placeholder="请选择" default-value="0">-->
<!--                  <a-select-option value="0">全部</a-select-option>-->
<!--                  <a-select-option value="1">关闭</a-select-option>-->
<!--                  <a-select-option value="2">运行中</a-select-option>-->
<!--                </a-select>-->
<!--              </a-form-item>-->
<!--            </a-col>-->
<!--            <a-col :md="8" :sm="24">-->
<!--              <a-form-item label="使用状态">-->
<!--                <a-select placeholder="请选择" default-value="0">-->
<!--                  <a-select-option value="0">全部</a-select-option>-->
<!--                  <a-select-option value="1">关闭</a-select-option>-->
<!--                  <a-select-option value="2">运行中</a-select-option>-->
<!--                </a-select>-->
<!--              </a-form-item>-->
<!--            </a-col>-->
<!--          </template>-->
<!--          <a-col :md="!advanced && 8 || 24" :sm="24">-->
<!--            <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">-->
<!--              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>-->
<!--              <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>-->
<!--              <a @click="toggleAdvanced" style="margin-left: 8px">-->
<!--                {{ advanced ? '收起' : '展开' }}-->
<!--                <a-icon :type="advanced ? 'up' : 'down'"/>-->
<!--              </a>-->
<!--            </span>-->
<!--          </a-col>-->
<!--        </a-row>-->
<!--      </a-form>-->
<!--    </div>-->

    <div class="table-operator">
      <a-button type="primary" icon="plus" @click="handleNew()">新建</a-button>
      <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>
          <!-- lock | unlock -->
          <a-menu-item key="2"><a-icon type="lock" />锁定</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px">
          批量操作 <a-icon type="down" />
        </a-button>
      </a-dropdown>
    </div>

    <s-table
      ref="table"
      size="default"
      rowKey="key"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
    >
      <span slot="serial" slot-scope="text, record, index">
        {{ index + 1 }}
      </span>
      <span slot="groupNameList" slot-scope="text, record">
        {{ record.role === 2 ? '所有组' : text.toString() }}
      </span>
      <span slot="role" slot-scope="text, record">
        {{ record.role === 2 ? '管理员' : '普通用户' }}
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
           <a href="javascript:;">删除</a>
        </template>
      </span>
    </s-table>
  </a-card>
</template>

<script>

import ATextarea from 'ant-design-vue/es/input/TextArea'
import AInput from 'ant-design-vue/es/input/Input'
import moment from 'moment'
// 动态切换组件
import Edit from '@/views/list/table/Edit'
import { getUserPage } from '@/api/manage'
import { STable } from '@/components'

export default {
  name: 'TableListWrapper',
  components: {
    AInput,
    ATextarea,
    Edit,
    STable
  },
  data () {
    return {
      currentComponet: 'List',
      record: '',
      mdl: {},
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      // 表头
      columns: [
        {
          title: '#',
          width: '5%',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: '用户名',
          width: '12%',
          dataIndex: 'username'
        },
        {
          title: '角色',
          dataIndex: 'role',
          width: '10%',
          scopedSlots: { customRender: 'role' }
        },
        {
          title: '权限',
          dataIndex: 'groupNameList',
          width: '45%',
          scopedSlots: { customRender: 'groupNameList' }
        },
        {
          title: '更新时间',
          width: '18%',
          dataIndex: 'updateDt',
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '操作',
          width: '10%',
          dataIndex: 'action',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        console.log('loadData.parameter', parameter)
        return getUserPage(Object.assign(parameter, this.queryParam))
          .then(res => {
            return res
          })
      },
      selectedRowKeys: [],
      selectedRows: [],

      // custom table alert & rowSelection
      options: {
        alert: { show: true, clear: () => { this.selectedRowKeys = [] } },
        rowSelection: {
          selectedRowKeys: this.selectedRowKeys,
          onChange: this.onSelectChange
        }
      },
      optionAlertShow: false
    }
  },
  filters: {
    // filterTime(time) {
    //   return moment(time).format("YYYY-MM-DD HH:mm:ss")
    // }
  },
  methods: {
    handleNew () {
      this.$router.push('/user-form')
    },
    handleEdit (record) {
      this.record = record || ''
      this.$router.push({ path: '/user-form', query: { username: record.username } })
    },
    handleGoBack () {
      this.record = ''
      this.currentComponet = 'List'
    }
  },
  watch: {
    '$route.path' () {
      this.record = ''
      this.currentComponet = 'List'
    }
  }
}
</script>
