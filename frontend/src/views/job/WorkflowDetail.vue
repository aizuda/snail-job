<template>
  <iframe
    ref="iframe"
    :src="`/lib?token=${token}&x1c2Hdd6=kaxC8Iml&namespaceId=${namespaceId}&id=${id}`"
    marginwidth="0"
    frameborder="no"
    :style="`width: 100%;height:calc(99vh - 60px)`"
  />
</template>

<script>
import storage from 'store'

export default {
  name: 'WorkFlowDetail',
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
  methods: {}
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
