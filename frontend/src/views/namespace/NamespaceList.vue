<template>
  <div>
    <a-card :bordered="false">

      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="用户名">
                <a-input v-model="queryParam.keyword" placeholder="请输入空间名称/唯一标识" allowClear/>
              </a-form-item>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => queryParam = {}">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleNew()">新建</a-button>
      </div>

      <s-table
        ref="table"
        size="default"
        :rowKey="record => record.id"
        :columns="columns"
        :data="loadData"
        :alert="options.alert"
        :rowSelection="options.rowSelection"
      >
        <span slot="serial" slot-scope="record">
          {{ record.id }}
        </span>
        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <!--            <a-divider type="vertical" />-->
            <!--            <a-popconfirm-->
            <!--              title="命名空间删除后不可恢复，请确认是否删除这个空间?"-->
            <!--              ok-text="删除"-->
            <!--              cancel-text="取消"-->
            <!--              @confirm="handleDel(record)">-->
            <!--              <a href="javascript:;">删除</a>-->
            <!--            </a-popconfirm>-->

          </template>
        </span>
      </s-table>
    </a-card>

    <NamespaceForm ref="namespaceFormRef" :isEdit="isEdit" @refreshTable="refreshTable"/>
  </div>
</template>
<script>

import AInput from 'ant-design-vue/lib/input/Input'
import ATextarea from 'ant-design-vue/lib/input/TextArea'
import { STable } from '@/components'
import moment from 'moment/moment'
import { namespaceList, delNamespace } from '@/api/manage'
import NamespaceForm from '@/views/namespace/NamespaceForm.vue'

export default {
  name: 'NamespaceList',
  components: {
    AInput,
    ATextarea,
    STable,
    NamespaceForm
  },
  data () {
    return {
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
          title: '名称',
          dataIndex: 'name'
        },
        {
          title: 'UniqueId',
          dataIndex: 'uniqueId'
        },
        {
          title: '创建时间',
          dataIndex: 'updateDt',
          customRender: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss')
        },
        {
          title: '更新时间',
          dataIndex: 'createDt',
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
        return namespaceList(Object.assign(parameter, this.queryParam))
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
      optionAlertShow: false,
      isEdit: false
    }
  },
  filters: {
  },
  methods: {
    handleNew () {
      this.isEdit = false
      this.$refs.namespaceFormRef.isShow()
    },
    refreshTable (v) {
      this.$refs.table.refresh()
    },
    handleEdit (record) {
      this.isEdit = true
      this.$refs.namespaceFormRef.isShow(record)
    },
    handleDel (record) {
      delNamespace(record.id).then(res => {
        this.$message.success('删除成功')
        this.$refs.table.refresh()
        this.$store.dispatch('GetInfo')
      })
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

<style scoped lang='less'>

</style>
