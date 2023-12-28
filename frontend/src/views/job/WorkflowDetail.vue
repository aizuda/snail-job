<template>
  <work-flow value="kaxC8Iml" />
</template>

<script>
import WorkFlow from './form/WorkFlow.vue'

export default {
  name: 'WorkFlowDetail',
  components: { WorkFlow },
  data () {
    return {
      id: ''
    }
  },
  mounted () {
    this.id = this.$route.query.id
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
