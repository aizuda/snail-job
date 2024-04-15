<template>
  <global-footer class="footer custom-render">
    <template v-slot:links>
      <a href="https://www.easyretry.com/" target="_blank">Easy Retry</a>
      <a href="http://aizuda.com/" target="_blank">Team Aizudai</a>
      <a href="https://github.com/byteblogs168" target="_blank">@byteblogs168</a>
    </template>
    <template v-slot:copyright >
      <a href="mailto:598092184@qq.com" target="_blank" style="margin: 10px"><icon-font style="fontSize: 20px" type="icon-youxiang" /></a>
      <a href="https://github.com/aizuda/snail-job" target="_blank" style="margin: 10px"><icon-font style="fontSize: 20px" type="icon-github2" /> </a>
      <a href="https://gitee.com/aizuda/snail-job" target="_blank" style="margin: 10px">   <icon-font style="fontSize: 20px" type="icon-GITEE" /></a>
    </template>
  </global-footer>
</template>

<script>
import { GlobalFooter } from '@ant-design-vue/pro-layout'
import { Icon } from 'ant-design-vue'

const IconFont = Icon.createFromIconfontCN({
  scriptUrl: '//at.alicdn.com/t/c/font_1460205_qu2antnauc.js'
})

export default {
  name: 'ProGlobalFooter',
  components: {
    GlobalFooter,
    IconFont
  }
}
</script>
