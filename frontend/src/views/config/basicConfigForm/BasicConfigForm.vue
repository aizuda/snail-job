<template>
  <div>
    <page-header-wrapper content="配置组、场景、通知配置" @back="() => $router.go(-1)" style='margin: -24px -1px 0'>
      <div></div>
    </page-header-wrapper>
    <a-card class="card" title="组配置" :bordered="false">
      <group-form ref="groupConfig" :showSubmit="false" />
    </a-card>
    <a-card class="card" title="通知配置" :bordered="false">
      <notify-list ref="notify"></notify-list>
    </a-card>
    <a-card class="card" title="场景配置" :bordered="false">
      <scene-list ref="scene"></scene-list>
    </a-card>

    <!-- fixed footer toolbar -->
    <footer-tool-bar :is-mobile="isMobile" :collapsed="sideCollapsed" style="width: 100%">
      <span class="popover-wrapper">
        <a-popover title="表单校验信息" overlayClassName="antd-pro-pages-forms-style-errorPopover" trigger="click" :getPopupContainer="trigger => trigger.parentNode">
          <template slot="content">
            <li v-for="item in errors" :key="item.key" @click="scrollToField(item.key)" class="antd-pro-pages-forms-style-errorListItem">
              <a-icon type="cross-circle-o" class="antd-pro-pages-forms-style-errorIcon" />
              <div class="">{{ item.message }}</div>
              <div class="antd-pro-pages-forms-style-errorField">{{ item.fieldLabel }}</div>
            </li>
          </template>
          <span class="antd-pro-pages-forms-style-errorIcon" v-if="errors.length > 0">
            <a-icon type="exclamation-circle" />{{ errors.length }}
          </span>
        </a-popover>
      </span>
      <a-button type="primary" @click="validate" :loading="loading">提交</a-button>
    </footer-tool-bar>
  </div>

</template>

<script>
import GroupForm from './GroupForm'
import SceneList from './SceneList'
import NotifyList from './NotifyList'
import FooterToolBar from '@/components/FooterToolbar'
import { baseMixin } from '@/store/app-mixin'
import { saveGroup } from '@/api/manage'

const fieldLabels = {
  groupName: '组名称',
  groupStatus: '组状态',
  description: '描述'
}

export default {
  name: 'AdvancedForm',
  mixins: [baseMixin],
  components: {
    FooterToolBar,
    GroupForm,
    SceneList,
    NotifyList
  },
  data () {
    return {
      loading: false,
      memberLoading: false,
      // table
      errors: []
    }
  },
  methods: {
    handleSubmit (e) {
      e.preventDefault()
    },
    // 最终全页面提交
    validate () {
      const { $refs: { groupConfig, scene, notify }, $notification } = this
      const groupConfigForm = new Promise((resolve, reject) => {
        groupConfig.form.validateFields((err, values) => {
          if (err) {
            console.log(err)
            reject(err)
            return
          }
          resolve(values)
        })
      })
      // clean this.errors
      this.errors = []

      groupConfigForm.then(value => {
        if (!value['id']) {
          value['id'] = 0
        }
        value['sceneList'] = scene.formData
        value['notifyList'] = notify.formData
        saveGroup(value).then(res => {
          if (res.status === 0) {
            $notification['error']({
              message: res.message
            })
          } else {
            $notification['success']({
              message: res.message
            })
            this.$refs.notify.reset()
            this.$router.go(-1)
          }
        })
      }).catch(() => {
        const errors = Object.assign({}, groupConfig.form.getFieldsError())
        const tmp = { ...errors }
        this.errorList(tmp)
      })
    },
    errorList (errors) {
      if (!errors || errors.length === 0) {
        return
      }
      this.errors = Object.keys(errors)
        .filter(key => errors[key])
        .map(key => ({
          key: key,
          message: errors[key][0],
          fieldLabel: fieldLabels[key]
        }))
    },
    scrollToField (fieldKey) {
      const labelNode = document.querySelector(`label[for="${fieldKey}"]`)
      if (labelNode) {
        labelNode.scrollIntoView(true)
      }
    }
  }
}
</script>

<style lang="less" scoped>
  .card{
    margin-bottom: 24px;
  }
  .popover-wrapper {
    /deep/ .antd-pro-pages-forms-style-errorPopover .ant-popover-inner-content {
      min-width: 256px;
      max-height: 290px;
      padding: 0;
      overflow: auto;
    }
  }
  .antd-pro-pages-forms-style-errorIcon {
    user-select: none;
    margin-right: 24px;
    color: #f5222d;
    cursor: pointer;
    i {
          margin-right: 4px;
    }
  }
  .antd-pro-pages-forms-style-errorListItem {
    padding: 8px 16px;
    list-style: none;
    border-bottom: 1px solid #e8e8e8;
    cursor: pointer;
    transition: all .3s;

    &:hover {
      background: #e6f7ff;
    }
    .antd-pro-pages-forms-style-errorIcon {
      float: left;
      margin-top: 4px;
      margin-right: 12px;
      padding-bottom: 22px;
      color: #f5222d;
    }
    .antd-pro-pages-forms-style-errorField {
      margin-top: 2px;
      color: rgba(0,0,0,.45);
      font-size: 12px;
    }
  }
</style>
