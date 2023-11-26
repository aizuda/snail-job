<template>
  <a-card :bordered="false">

    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <template>
            <a-col :md="8" :sm="24">
              <a-form-item label="组名称">
                <a-input v-model="queryParam.groupName" placeholder="请输入组名称" allowClear/>
              </a-form-item>
            </a-col>
          </template>
          <a-col :md="!advanced && 8 || 24" :sm="24">
            <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
              <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
              <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
              <!--              <a @click="toggleAdvanced" style="margin-left: 8px">-->
              <!--                {{ advanced ? '收起' : '展开' }}-->
              <!--                <a-icon :type="advanced ? 'up' : 'down'"/>-->
              <!--              </a>-->
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <div class="table-operator">
      <a-button type="primary" icon="plus" @click="handleNew()" v-if="$auth('group.add')">新建</a-button>
    </div>

    <s-table
      ref="table"
      size="default"
      :rowKey="(record) => record.id"
      :columns="columns"
      :data="loadData"
      :alert="options.alert"
      :rowSelection="options.rowSelection"
      :scroll="{ x: 1600 }"
    >
      <span slot="groupName" slot-scope="text, record">
        <a href="#" @click="handlerOpenDrawer(record)">{{ text }}</a>
      </span>
      <span slot="groupStatus" slot-scope="text">
        <a-tag :color="groupStatus[text].color">
          {{ groupStatus[text].name }}
        </a-tag>
      </span>
      <span slot="initScene" slot-scope="text">
        <a-tag :color="initScene[text].color">
          {{ initScene[text].name }}
        </a-tag>
      </span>
      <span slot="idGeneratorMode" slot-scope="text">
        <a-tag :color="idGeneratorMode[text].color">
          {{ idGeneratorMode[text].name }}
        </a-tag>
      </span>
      <span slot="action" slot-scope="text, record">
        <template>
          <a @click="handleInfo(record)">详情</a>
          <a-divider type="vertical"/>
          <a @click="handleEdit(record)" v-if="$auth('group.edit')">编辑</a>
          <a-divider type="vertical" v-if="$auth('group.edit')"/>
          <a-popconfirm
            :title="record.groupStatus === 1 ? '是否停用?': '是否启用?'"
            ok-text="确定"
            cancel-text="取消"
            @confirm="handleEditStatus(record)"
            v-if="$auth('group.stop')"
          >
            <a href="javascript:;">{{ record.groupStatus === 1 ? '停用': '启用' }}</a>
          </a-popconfirm>
        </template>
      </span>
    </s-table>

    <Drawer
      title="组配置详情"
      placement="right"
      :width="800"
      :visibleAmplify="true"
      :visible="openDrawer"
      @closeDrawer="onClose"
      @handlerAmplify="handleInfo"
    >
      <group-info ref="groupInfoRef" :showHeader="false" :column="1"/>
    </Drawer>

  </a-card>
</template>

<script>

import AInput from 'ant-design-vue/es/input/Input'
import { getGroupConfigForPage, updateGroupStatus } from '@/api/manage'
import { Drawer, STable } from '@/components'
import moment from 'moment'
import GroupInfo from '@/views/config/GroupInfo.vue'
const enums = require('@/utils/retryEnum')

export default {
  name: 'TableListWrapper',
  components: {
    GroupInfo,
    Drawer,
    AInput,
    STable
  },
  data () {
    return {
      // 高级搜索 展开/关闭
      advanced: false,
      // 查询参数
      queryParam: {},
      // 表头
      columns: [
        {
          title: '名称',
          dataIndex: 'groupName',
          scopedSlots: { customRender: 'groupName' }

        },
        {
          title: '状态',
          dataIndex: 'groupStatus',
          scopedSlots: { customRender: 'groupStatus' }
        },
        {
          title: '版本',
          dataIndex: 'version'
        },
        {
          title: '分区',
          dataIndex: 'groupPartition',
          needTotal: true
        },
        {
          title: 'ID生成模式',
          dataIndex: 'idGeneratorMode',
          scopedSlots: { customRender: 'idGeneratorMode' }
        },
        {
          title: '初始化场景',
          dataIndex: 'initScene',
          scopedSlots: { customRender: 'initScene' }
        },
        {
          title: '更新时间',
          dataIndex: 'updateDt',
          sorter: true,
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '描述',
          dataIndex: 'description'
        },
        {
          title: '操作',
          dataIndex: 'action',
          width: '150px',
          fixed: 'right',
          scopedSlots: { customRender: 'action' }
        }
      ],
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        console.log('loadData.parameter', parameter)
        return getGroupConfigForPage(Object.assign(parameter, this.queryParam))
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
      initScene: enums.initScene,
      groupStatus: enums.groupStatus,
      idGeneratorMode: enums.idGenMode,
      currentShowRecord: null,
      openDrawer: false
    }
  },
  created () {

  },
  methods: {
    handleNew () {
      this.$router.push('/group/config')
    },
    handleEdit (record) {
      this.$router.push({ path: '/group/config', query: { groupName: record.groupName } })
    },
    handleInfo (record) {
      record = record || this.currentShowRecord
      this.$router.push({ path: '/group/info', query: { groupName: record.groupName } })
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    handleEditStatus (record) {
      const { groupStatus, groupName } = record
      const { $notification } = this
      updateGroupStatus({ groupName: groupName, groupStatus: (groupStatus === 1 ? 0 : 1) }).then(res => {
        if (res.status === 0) {
          $notification['error']({
            message: res.message
          })
        } else {
          $notification['success']({
            message: res.message
          })
          // 刷新表格
          this.$refs.table.refresh()
        }
      })
    },
    handlerOpenDrawer (record) {
      this.currentShowRecord = record
      this.openDrawer = true
      setTimeout(() => {
        this.$refs.groupInfoRef.groupConfigDetail(record.groupName)
      }, 200)
    },
    onClose () {
      this.openDrawer = false
      this.currentShowRecord = null
    }
  }
}
</script>
