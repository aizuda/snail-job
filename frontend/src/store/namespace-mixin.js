import { mapState } from 'vuex'

const namespaceMixin = {
  computed: {
    ...mapState({
      currentLang: state => state.app.lang
    })
  },
  methods: {
    setNamespace (namespaceId) {
      this.$store.dispatch('setNamespace', namespaceId)
    }
  }
}

export default namespaceMixin
