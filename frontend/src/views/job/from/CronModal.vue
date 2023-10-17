<template>
  <div>
    <a-modal :visible="visible" title="Cron表达式" @ok="handleOk" @cancel="handlerCancel" width="650px">
      <cron-input v-model="cron" :item="cronItem" @change="showFive"/>
      <a-input placeholder="请输入cron表达式" v-model="cron"/>
      <div style="margin: 20px 0; border-left: #f5222d 5px solid; font-size: medium; font-weight: bold">
        &nbsp;&nbsp; 近5次的运行时间:
      </div>
      <div v-for="(item, index) in list" :key="item" style="margin-top: 10px"> 第{{ index + 1 }}次: {{ item }}</div>
    </a-modal>
  </div>
</template>

<script>
import { timeByCron } from '@/api/jobApi'

export default {
  name: 'CronModal',
  data () {
    return {
      visible: false,
      cronItem: ['second', 'minute', 'hour', 'day', 'month', 'week', 'year'],
      cron: '',
      list: []
    }
  },
  methods: {
    handleOk () {
      this.visible = false
      this.$emit('getCron', this.cron)
    },
    handlerCancel () {
      this.visible = false
    },
    isShow (cron) {
      this.cron = cron
      this.visible = true
    },
    showFive (cron) {
      this.cron = cron
      timeByCron({ 'cron': cron }).then(res => {
        this.list = res.data
      })
    }

  }
}
</script>
<style>
 .ant-cron-result{
   display: none;
 }

</style>
