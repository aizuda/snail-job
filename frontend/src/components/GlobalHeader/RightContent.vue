<template>
  <div :class="wrpCls">
    <Namespace/>
    <a href="https://www.easyretry.com" target="_blank" :class="prefixCls"><a-icon type="question-circle" :style="{ fontSize: '18px', color: '#08c' }"/></a>
    <avatar-dropdown :menu="showMenu" :current-user="currentUser" :class="prefixCls"/>
    <!--    <select-lang :class="prefixCls" />-->
  </div>
</template>

<script>
import AvatarDropdown from './AvatarDropdown'
import SelectLang from '@/components/SelectLang'
import Namespace from '@/components/GlobalHeader/Namespace'

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    SelectLang,
    Namespace
  },
  props: {
    prefixCls: {
      type: String,
      default: 'ant-pro-global-header-index-action'
    },
    isMobile: {
      type: Boolean,
      default: () => false
    },
    topMenu: {
      type: Boolean,
      required: true
    },
    theme: {
      type: String,
      required: true
    }
  },
  data () {
    return {
      showMenu: true,
      currentUser: {}
    }
  },
  computed: {
    wrpCls () {
      return {
        'ant-pro-global-header-index-right': true,
        [`ant-pro-global-header-index-${(this.isMobile || !this.topMenu) ? 'light' : this.theme}`]: true
      }
    }
  },
  mounted () {
    setTimeout(() => {
      this.currentUser = {
        name: this.$store.getters.nickname
      }
    }, 1500)
  }
}
</script>
