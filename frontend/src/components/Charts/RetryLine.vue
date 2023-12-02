<template>
  <div>
    <div id="viewData"></div>
  </div>
</template>

<script>
import * as G2 from '@antv/g2'
import { getDashboardRetryLine } from '@/api/manage'

const DataSet = require('@antv/data-set')

export default {
  name: 'RetryLine',
  data () {
    return {
      viewRecords: [],
      retryLinkeResponseVOList: [],
      chart: null
    }
  },
  mounted () {
    this.getDashboardRetryLine()
    this.createView()
  },
  methods: {
    getDashboardRetryLine (groupName, type = 'WEEK', startTime, endTime) {
      getDashboardRetryLine({
        'groupName': groupName,
        'type': type,
        'startTime': startTime,
        'endTime': endTime
      }).then(res => {
        this.$bus.$emit('retry', res)
        this.viewCharts(res.data.retryLinkeResponseVOList)
      })
    },
    viewCharts (viewRecords, type = 'WEEK') {
      var ds = new DataSet()
      if (viewRecords === undefined || viewRecords === null) {
        return
      }
      var dv = ds.createView().source(viewRecords)
      dv.transform({
        type: 'fold',
        fields: ['successNum', 'runningNum', 'maxCountNum', 'suspendNum'],
        key: 'name',
        value: 'viewTotal',
        retains: ['total', 'createDt']
      })

      this.chart.source(dv, {
        date: {
          type: 'cat'
        }
      })
      this.chart.axis('viewTotal', {
        label: {
          textStyle: {
            fill: '#aaaaaa'
          }
        }
      })
      this.chart.tooltip({
        crosshairs: {
          type: 'line'
        }
      })
      this.chart.line().position('createDt*viewTotal').color('name', ['#1890ff', '#c28c62']).shape('smooth')
      this.chart.point().position('createDt*viewTotal').color('name', ['#1890ff', '#c28c62']).size(4).shape('circle').style({
        stroke: '#fff',
        lineWidth: 1
      })

      this.chart.render()
    },
    createView () {
      this.chart = new G2.Chart({
        container: 'viewData',
        forceFit: true,
        height: 410,
        padding: [20, 90, 60, 50]
      })
    }
  }
}
</script>
