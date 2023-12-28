<template>
  <work-flow value="D7Rzd7Oe" />
</template>

<script>
import WorkFlow from './form/WorkFlow.vue'

export default {
  name: 'WorkFlowEdit',
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
