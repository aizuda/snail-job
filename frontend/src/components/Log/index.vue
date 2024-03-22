<!-- eslint-disable -->
<template>
  <div class="log">
    <table class="scroller">
      <tbody>
      <tr v-for="(log, index) in logList" :key="index">
        <td class="index">
          {{ index + 1 }}
        </td>
        <td>
          <div class="content">
            <div class="line">
              <div class="flex">
                <div class="text" style="color: #2db7f5">{{ timestampToDate(log.time_stamp) }}</div>
                <div class="text" :style="{ color: LevelEnum[log.level].color }">
                  {{ log.level.length === 4 ? log.level + ' ' : log.level }}
                </div>
                <div class="text" style="color: #00a3a3">[{{ log.thread }}]</div>
                <div class="text" style="color: #a771bf; font-weight: 500">{{ log.location }}</div>
                <div class="text">:</div>
              </div>
              <div class="text" style="font-size: 16px">{{ log.message }}</div>
              <div class="text" style="font-size: 16px">{{ log.throwable }}</div>
            </div>
          </div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'Log',
  components: {},
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    value: {
      deep: true,
      immediate: true,
      handler (val) {
        this.logList = val
      }
    }
  },
  data () {
    return {
      logList: [],
      indicator: <a-icon type="loading" style="font-size: 24px; color: '#d9d9d9'" spin/>,
      LevelEnum: {
        DEBUG: {
          name: 'DEBUG',
          color: '#2647cc'
        },
        INFO: {
          name: 'INFO',
          color: '#5c962c'
        },
        WARN: {
          name: 'WARN',
          color: '#da9816'
        },
        ERROR: {
          name: 'ERROR',
          color: '#dc3f41'
        }
      }
    }
  },
  methods: {
    timestampToDate (timestamp) {
      const date = new Date(Number.parseInt(timestamp.toString()))
      const year = date.getFullYear()
      const month =
        (date.getMonth() + 1).toString().length === 1 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1).toString()
      const day = date.getDate()
      const hours = date.getHours()
      const minutes = date.getMinutes().toString().length === 1 ? '0' + date.getMinutes() : date.getMinutes().toString()
      const seconds = date.getSeconds().toString().length === 1 ? '0' + date.getSeconds() : date.getSeconds().toString()
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}.${date.getMilliseconds()}`
    }
  }
}
</script>

<style scoped lang="less">
.log {
  height: calc(100vh - 56px);
  color: #abb2bf;
  background-color: #282c34;
  position: relative !important;
  box-sizing: border-box;
  display: flex !important;
  flex-direction: column;

  // 美化滚动条
  ::-webkit-scrollbar {
    width: 10px;
    height: 10px;
  }

  ::-webkit-scrollbar-track {
    width: 6px;
    background: rgba(#101F1C, 0.1);
    -webkit-border-radius: 2em;
    -moz-border-radius: 2em;
    border-radius: 2em;
  }

  ::-webkit-scrollbar-thumb {
    background-color: rgba(144,147,153,.5);
    background-clip: padding-box;
    min-height: 28px;
    -webkit-border-radius: 2em;
    -moz-border-radius: 2em;
    border-radius: 2em;
    transition: background-color .3s;
    cursor: pointer;
  }

  ::-webkit-scrollbar-thumb:hover {
    background-color: rgba(144,147,153,.3);
  }

  .scroller {
    height: 100%;
    overflow: auto;
    display: flex !important;
    align-items: flex-start !important;
    font-family: monospace;
    line-height: 1.4;
    position: relative;
    z-index: 0;

    .index{
      width: 32px;
      min-width: 32px;
      height: 100%;
      background-color: #1e1f22;
      color: #7d8799;
      text-align: center;
      vertical-align: top;
      padding-top: 4px;
      font-size: 16px;
      z-index: 200;
    }

    .gutters {
      min-height: 100%;
      position: sticky;
      background-color: #1e1f22;
      color: #7d8799;
      border: none;
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      box-sizing: border-box;
      inset-inline-start: 0;
      z-index: 200;

      .gutter-element {
        height: 25px;
        font-size: 14px;
        padding: 0 8px 0 5px;
        min-width: 20px;
        text-align: right;
        white-space: nowrap;
        box-sizing: border-box;
        color: #7d8799;
        display: flex;
        align-items: center;
        justify-content: flex-end;
      }
    }

    .content {
      tab-size: 4;
      caret-color: transparent !important;
      margin: 0;
      //flex-grow: 2;
      //flex-shrink: 0;
      // display: block;
      white-space: pre;
      //word-wrap: normal;
      //box-sizing: border-box;
      //min-height: 100%;
      padding: 4px 8px;
      outline: none;
      color: #bcbec4;

      .line {
        height: 25px;
        caret-color: transparent !important;
        font-size: 16px;
        // background-color: #6699ff0b;
        display: contents;
        padding: 0 2px 0 6px;

        .flex{
          display: flex;
          align-items: center;
          gap: 5px;
        }

        .text {
          font-size: 16px;
        }
      }
    }
  }
}

/deep/ .ant-modal {
  max-width: 100%;
  top: 0;
  padding-bottom: 0;
  margin: 0;
}
/deep/ .ant-modal-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh);
}
/deep/ .ant-modal-body {
  flex: 1;
  padding: 0;
}
</style>
