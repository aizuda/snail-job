<template>
  <a-dropdown v-if="namespaceIds && namespaceIds.length > 0">
    <a-menu slot="overlay" @click="handleMenuClick">
      <a-menu-item :key="item.uniqueId" v-for="item in namespaceIds">
        <a href="javascript:;">{{ item.name }}</a>
      </a-menu-item>
    </a-menu>
    <a-button shape="round">  {{ name }}  <a-icon type="down" /> </a-button>
  </a-dropdown>
  <span v-else>
    <a-spin size="small" :style="{ marginLeft: 8, marginRight: 8 }" />
  </span>
</template>
<script>
import namespaceMixin from '@/store/namespace-mixin'
import storage from 'store'
import { APP_NAMESPACE } from '@/store/mutation-types'
export default {
  mixins: [namespaceMixin],
  data () {
    return {
      namespaceIds: [],
      name: 'Default'
    }
  },
  computed: {
    refreshNamespace () {
      return this.$store.getters.namespaces
    }
  },
  watch: {
    refreshNamespace (val) {
      this.namespaceIds = val
    }
  },
  mounted () {
    setTimeout(() => {
      this.namespaceIds = this.$store.getters.namespaces
      this.name = this.namespaceIds.find(i => i.uniqueId === storage.get(APP_NAMESPACE)).name
    }, 1500)
  },
  methods: {
    handleMenuClick (e) {
      this.name = this.namespaceIds.find(i => i.uniqueId === e.key).name
      this.$store.dispatch('setNamespace', e.key)
      setTimeout(() => {
        this.$router.go(0)
      }, 500)
    }
  }
}
</script>
