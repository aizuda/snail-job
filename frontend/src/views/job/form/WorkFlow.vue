<template>
  <a-spin :spinning="spinning" tip=" 工作流正在加载中">
    <a-icon slot="indicator" type="loading" style="font-size: 58px; top: 39%" spin />
    <iframe
      ref="iframe"
      :src="`${mode === 'production' ? baseUrl : ''}/lib/index.html?id=${id}&mode=${mode}&x1c2Hdd6=${value}`"
      marginwidth="0"
      frameborder="no"
      :style="`width: 100%;height:calc(99vh - 60px)`"
    />
  </a-spin>
</template>

<script>
export default {
  name: 'WorkFlow',
  components: {},
  props: {
    value: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      id: '',
      spinning: true,
      mode: process.env.NODE_ENV,
      baseUrl: process.env.VUE_APP_API_BASE_URL,
      indicator: <a-icon type="loading" style="font-size: 36px" spin />
    }
  },
  mounted () {
    this.iframeLoad()
    this.id = this.$route.query.id
  },
  created () {
    window.addEventListener('message', this.handleMessage, false)
  },
  destroyed () {
    window.removeEventListener('message', this.handleMessage)
  },
  methods: {
    save () {
      window.removeEventListener('message', this.handleMessage)
      this.$message.info('工作流新增成功')
      this.$router.push({ path: '/job/workflow/list' })
    },
    cancel () {
      window.removeEventListener('message', this.handleMessage)
      this.$router.push({ path: '/job/workflow/list' })
    },
    update () {
      this.$message.info('工作流修改成功')
      this.$router.push({ path: '/job/workflow/list' })
    },
    handleMessage (e) {
      if (typeof e.data === 'object') {
        if (e.data.code === 'SV5ucvLBhvFkOftb') {
          this.save()
        } else if (e.data.code === 'kb4DO9h6WIiqFhbp') {
          this.cancel()
        } else if (e.data.code === '8Rr3XPtVVAHfduQg') {
          this.update()
        }
      }
    },
    iframeLoad () {
      const that = this
      const iframe = this.$refs.iframe
      // 处理兼容行问题 兼容IE
      if (iframe.attachEvent) {
        iframe.attachEvent('onload', function () {
          // iframe加载完毕以后执行操作
          that.spinning = false
        })
      } else {
        iframe.onload = function () {
          // iframe加载完毕以后执行操作
          that.spinning = false
        }
      }
    }
  }
}
</script>

<style>
.ant-layout-content {
  margin: 0;
}

.ant-layout-footer {
  display: none;
}

.ant-spin-text {
  font-size: 20px;
}

.ant-spin-nested-loading > div > .ant-spin {
  top: 23%;
}
</style>
