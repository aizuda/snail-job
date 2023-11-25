<template>
  <a-drawer
    placement="right"
    :width="width"
    @afterVisibleChange="afterVisibleChange"
    :visible="visible"
    @close="onClose"
  >
    <span slot="title">
      {{ title }}
      <button class="ant-drawer-close" style="margin-right: 40px" v-if="visibleAmplify">
        <a-icon type="arrows-alt" style="font-size : 16px;" @click="this.onAmplify"/>
      </button>
    </span>
    <slot></slot>
  </a-drawer>

</template>

<script>

export default {
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    visibleAmplify: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    width: {
      type: Number,
      default: 300
    }
  },
  methods: {
    onClose () {
      this.$emit('closeDrawer')
    },
    onAmplify () {
      this.$emit('handlerAmplify')
    },
    afterVisibleChange () {
      this.$emit('afterVisibleChange')
    }
  }
}
</script>

<style scoped lang='less'>
.amplify {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 10;
  display: block;
  width: 56px;
  height: 56px;
  color: rgba(0, 0, 0, 0.45);
  font-weight: 700;
  font-size: 16px;
  font-style: normal;
  line-height: 56px;
  text-align: center;
  text-transform: none;
  text-decoration: none;
  background: transparent;
  border: 0;
  outline: 0;
  cursor: pointer;
  -webkit-transition: color 0.3s;
  transition: color 0.3s;
  text-rendering: auto;
  margin-right: 45px;
}
</style>
