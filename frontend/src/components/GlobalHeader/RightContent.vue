<template>
  <div :class="wrpCls">
    <a-dropdown>
      <span placement="bottomRight">
        Default <a-icon type="down" />
      </span>
      <a-menu slot="overlay">
        <a-menu-item>
          <a href="javascript:;">Uat</a>
        </a-menu-item>
        <a-menu-item>
          <a href="javascript:;">Dev</a>
        </a-menu-item>
        <a-menu-item>
          <a href="javascript:;">Prod</a>
        </a-menu-item>
      </a-menu>
    </a-dropdown>
    <a href="https://www.easyretry.com" target="_blank" :class="prefixCls"><a-icon type="question-circle" :style="{ fontSize: '18px', color: '#08c' }"/></a>
    <avatar-dropdown :menu="showMenu" :current-user="currentUser" :class="prefixCls"/>
    <!--    <select-lang :class="prefixCls" />-->
  </div>
</template>

<script>
import AvatarDropdown from './AvatarDropdown'
import SelectLang from '@/components/SelectLang'

export default {
  name: 'RightContent',
  components: {
    AvatarDropdown,
    SelectLang
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
