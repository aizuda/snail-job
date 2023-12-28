<template>
  <work-flow value="xkjIc2ZHZ0" />
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
