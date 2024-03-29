<template>
  <div>
    <page-header-wrapper @back="() => $router.replace('/group/list')" style="margin: -24px -1px 0" v-if="showHeader">
      <div></div>
    </page-header-wrapper>
    <a-card :bordered="false" v-if="groupInfo !==null ">
      <a-descriptions title="通用配置" :column="column" bordered>
        <a-descriptions-item label="组名称">
          {{ groupInfo.groupName }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="groupStatus[groupInfo.groupStatus].color">
            {{ groupStatus[groupInfo.groupStatus].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="版本">
          {{ groupInfo.version }}
        </a-descriptions-item>
        <a-descriptions-item label="描述" span="3">
          {{ groupInfo.description }}
        </a-descriptions-item>
        <a-descriptions-item label="Token" span="3">
          {{ groupInfo.token }}
        </a-descriptions-item>
        <a-descriptions-item label="OnLine机器" span="3">
          <a-tag color="blue" v-for="item in podInfo" :key="item">
            {{ item }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
      <br/>
      <a-descriptions title="重试配置" :column="column" bordered>
        <a-descriptions-item label="ID生成模式">
          <a-tag :color="idGeneratorMode[groupInfo.idGeneratorMode].color">
            {{ idGeneratorMode[groupInfo.idGeneratorMode].name }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="分区">
          {{ groupInfo.groupPartition }}
        </a-descriptions-item>
        <a-descriptions-item label="初始化场景">
          <a-tag :color="initScene[groupInfo.initScene].color">
            {{ initScene[groupInfo.initScene].name }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script>
import { getGroupConfigByGroupName, onlinePods } from '@/api/manage'
import enums from '@/utils/retryEnum'

export default {
  name: 'GroupInfo',
  components: {
  },
  props: {
    showHeader: {
      type: Boolean,
      default: true
    },
    column: {
      type: Number,
      default: 3
    }
  },
  data () {
    return {
      groupInfo: null,
      podInfo: [],
      initScene: enums.initScene,
      groupStatus: enums.groupStatus,
      idGeneratorMode: enums.idGenMode
    }
  },
  created () {
    const groupName = this.$route.query.groupName
    if (groupName) {
      this.groupConfigDetail(groupName)
    } else {
      if (this.showHeader) {
        this.$router.push({ path: '/404' })
      }
    }
  },
  methods: {
    groupConfigDetail (groupName) {
      getGroupConfigByGroupName(groupName).then(res => {
        this.groupInfo = res.data
      })

      onlinePods(groupName).then(res => {
        this.podInfo = res.data
      })
    }
  }
}
</script>

<style scoped lang='less'>

</style>
