<template>
  <iframe
    ref="iframe"
    :src="`/lib?token=${token}&x1c2Hdd6=D7Rzd7Oe&namespaceId=${namespaceId}&id=${id}`"
    marginwidth="0"
    frameborder="no"
    :style="`width: 100%;height:calc(99vh - 60px)`"
  />
</template>

<script>
import storage from 'store'

export default {
  name: 'WorkFlowEdit',
  components: {},
  data () {
    return {
      id: '',
      token: '',
      namespaceId: ''
    }
  },
  mounted () {
    this.id = this.$route.query.id
    this.token = storage.get('Access-Token')
    this.namespaceId = storage.get('app_namespace')
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
      this.$message.info('工作流提交成功')
      this.$router.push({ path: '/job/workflow/list' })
    },
    cancel () {
      window.removeEventListener('message', this.handleMessage)
      this.$router.push({ path: '/job/workflow/list' })
    },
    handleMessage (e) {
      if (typeof e.data === 'object') {
        if (e.data.code === 'SV5ucvLBhvFkOftb') {
          this.save()
        } else if (e.data.code === 'kb4DO9h6WIiqFhbp') {
          this.cancel()
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
</style>
