(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-3f8db0bc"],{"12b3":function(e,t){var a={sceneStatus:{0:{name:"停用",color:"#9c1f1f"},1:{name:"启用",color:"#f5a22d"}},backOffLabels:{1:{name:"延迟等级",color:"#d06892"},2:{name:"固定时间",color:"#f5a22d"},3:{name:"CRON表达式",color:"#e1f52d"},4:{name:"随机等待",color:"#a127f3"}},triggerInterval:{1:{name:"CRON表达式",color:"#d06892"},2:{name:"固定时间",color:"#f5a22d"}},notifyScene:{1:{name:"重试数量超过阈值",color:"#d06892"},2:{name:"重试失败数量超过阈值",color:"#f5a22d"},3:{name:"客户端上报失败",color:"#e1f52d"},4:{name:"客户端组件异常",color:"#a127f3"}},routeKey:{4:{name:"轮询",color:"#8f68d2"},1:{name:"一致性Hash",color:"#d06892"},2:{name:"随机",color:"#f5a22d"},3:{name:"LRU",color:"#e1f52d"}},notifyType:{1:{name:"钉钉通知",color:"#64a6ea"},2:{name:"邮箱通知",color:"#1b7ee5"},4:{name:"飞书",color:"#087da1"}},notifyStatus:{0:{name:"停用",color:"#9c1f1f"},1:{name:"启用",color:"#f5a22d"}},idGenMode:{1:{name:"号段模式",color:"#1b7ee5"},2:{name:"雪花算法",color:"#087da1"}},groupStatus:{0:{name:"停用",color:"#9c1f1f"},1:{name:"启用",color:"#f5a22d"}},initScene:{0:{name:"否",color:"#9c1f1f"},1:{name:"是",color:"#f5a22d"}}};e.exports=a},ba93:function(e,t,a){"use strict";a.r(t);a("b0c0");var o=function(){var e=this,t=e._self._c;return t("a-card",{attrs:{bordered:!1}},[t("div",{staticClass:"table-page-search-wrapper"},[t("a-form",{attrs:{layout:"inline"}},[t("a-row",{attrs:{gutter:48}},[[t("a-col",{attrs:{md:8,sm:24}},[t("a-form-item",{attrs:{label:"组名称"}},[t("a-input",{attrs:{placeholder:"请输入组名称",allowClear:""},model:{value:e.queryParam.groupName,callback:function(t){e.$set(e.queryParam,"groupName",t)},expression:"queryParam.groupName"}})],1)],1)],t("a-col",{attrs:{md:e.advanced?24:8,sm:24}},[t("span",{staticClass:"table-page-search-submitButtons",style:e.advanced&&{float:"right",overflow:"hidden"}||{}},[t("a-button",{attrs:{type:"primary"},on:{click:function(t){return e.$refs.table.refresh(!0)}}},[e._v("查询")]),t("a-button",{staticStyle:{"margin-left":"8px"},on:{click:function(){return e.queryParam={}}}},[e._v("重置")])],1)])],2)],1)],1),t("div",{staticClass:"table-operator"},[e.$auth("group.add")?t("a-button",{attrs:{type:"primary",icon:"plus"},on:{click:function(t){return e.handleNew()}}},[e._v("新建")]):e._e()],1),t("s-table",{ref:"table",attrs:{size:"default",rowKey:function(e){return e.id},columns:e.columns,data:e.loadData,alert:e.options.alert,rowSelection:e.options.rowSelection,scroll:{x:1600}},scopedSlots:e._u([{key:"serial",fn:function(a,o){return t("span",{},[e._v(" "+e._s(o.id)+" ")])}},{key:"groupStatus",fn:function(a){return t("span",{},[t("a-tag",{attrs:{color:e.groupStatus[a].color}},[e._v(" "+e._s(e.groupStatus[a].name)+" ")])],1)}},{key:"initScene",fn:function(a){return t("span",{},[t("a-tag",{attrs:{color:e.initScene[a].color}},[e._v(" "+e._s(e.initScene[a].name)+" ")])],1)}},{key:"idGeneratorMode",fn:function(a){return t("span",{},[t("a-tag",{attrs:{color:e.idGeneratorMode[a].color}},[e._v(" "+e._s(e.idGeneratorMode[a].name)+" ")])],1)}},{key:"action",fn:function(a,o){return t("span",{},[[t("a",{on:{click:function(t){return e.handleEdit(o)}}},[e._v("编辑")]),t("a-divider",{attrs:{type:"vertical"}}),t("a-popconfirm",{attrs:{title:1===o.groupStatus?"是否停用?":"是否启用?","ok-text":"确定","cancel-text":"取消"},on:{confirm:function(t){return e.handleEditStatus(o)}}},[t("a",{attrs:{href:"javascript:;"}},[e._v(e._s(1===o.groupStatus?"停用":"启用"))])])]],2)}}])})],1)},n=[],r=(a("d3b7"),a("25f0"),a("27e3")),c=a("0fea"),s=a("2af9"),i=a("c1df"),l=a.n(i),d=a("12b3"),u={name:"TableListWrapper",components:{AInput:r["a"],STable:s["j"]},data:function(){var e=this;return{advanced:!1,queryParam:{},columns:[{title:"#",scopedSlots:{customRender:"serial"}},{title:"名称",dataIndex:"groupName"},{title:"状态",dataIndex:"groupStatus",scopedSlots:{customRender:"groupStatus"}},{title:"版本",dataIndex:"version"},{title:"分区",dataIndex:"groupPartition",needTotal:!0},{title:"ID生成模式",dataIndex:"idGeneratorMode",scopedSlots:{customRender:"idGeneratorMode"}},{title:"初始化场景",dataIndex:"initScene",scopedSlots:{customRender:"initScene"}},{title:"更新时间",dataIndex:"updateDt",sorter:!0,customRender:function(e){return l()(e).format("YYYY-MM-DD HH:mm:ss")}},{title:"描述",dataIndex:"description"},{title:"OnLine机器",dataIndex:"onlinePodList",customRender:function(e){return e.toString()}},{title:"操作",dataIndex:"action",width:"150px",fixed:"right",scopedSlots:{customRender:"action"}}],loadData:function(t){return Object(c["l"])(Object.assign(t,e.queryParam)).then((function(e){return e}))},selectedRowKeys:[],selectedRows:[],options:{alert:{show:!0,clear:function(){e.selectedRowKeys=[]}},rowSelection:{selectedRowKeys:this.selectedRowKeys,onChange:this.onSelectChange}},initScene:d.initScene,groupStatus:d.groupStatus,idGeneratorMode:d.idGenMode}},created:function(){},methods:{handleNew:function(){this.$router.push("/basic-config")},handleEdit:function(e){this.$router.push({path:"/basic-config",query:{groupName:e.groupName}})},toggleAdvanced:function(){this.advanced=!this.advanced},handleEditStatus:function(e){var t=this,a=e.groupStatus,o=e.groupName,n=this.$notification;Object(c["K"])({groupName:o,groupStatus:1===a?0:1}).then((function(e){0===e.status?n["error"]({message:e.message}):(n["success"]({message:e.message}),t.$refs.table.refresh())}))}}},f=u,m=a("2877"),p=Object(m["a"])(f,o,n,!1,null,null,null);t["default"]=p.exports}}]);