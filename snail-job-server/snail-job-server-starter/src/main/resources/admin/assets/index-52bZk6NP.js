import{_ as ge}from"./table-header-operation.vue_vue_type_script_setup_true_lang-CAikEgod.js";import{d as Z,U as Y,p as x,V as X,Y as P,a as be,$ as t,i as _e,aF as he,r as ve,W as ue,o as N,c as L,w as n,f as a,g as D,t as I,h as e,b as oe,Q as ne,bu as le,R as ie,cm as Se,S as q,a3 as pe,bt as Te,n as Ne,B as Q,cn as se,co as we,cp as ke,a4 as O,cq as fe,bw as Ce,cr as de,cs as me,N as E,s as De,a8 as Ie,a9 as z,ct as Ue,I as Re,aa as Le}from"./index-DydImCNJ.js";import{d as $e,e as Ve,g as Oe,h as je,i as re,j as Be}from"./notify-NyU66L01.js";import{_ as Fe,u as Me,a as xe,N as Ae}from"./table-BB81mEh2.js";import{u as Ge,a as Pe,_ as ze,b as Ee}from"./form-CXP3EjI2.js";import{_ as ce}from"./operate-drawer-B2Yn2ELy.js";import{f as qe}from"./retry-scene-OT8iOQeb.js";import{f as We}from"./workflow-DGhU4bwu.js";import{i as He}from"./job-BRYX1dc1.js";import{_ as Ke}from"./select-group.vue_vue_type_script_setup_true_lang-Bwh6kebD.js";import{_ as Je}from"./Space-HTlydpS2.js";import{_ as Qe,a as Ye,b as Xe}from"./Grid-C0ryvMLX.js";import{_ as Ze}from"./search-form.vue_vue_type_script_setup_true_lang-G2vaTHhL.js";import{_ as et,a as tt}from"./DescriptionsItem-BKhQ5GOr.js";import{_ as at}from"./status-switch.vue_vue_type_script_setup_true_lang-D630MRHb.js";import{u as ot}from"./auth-Cj-gcKGG.js";import"./close-fullscreen-rounded-Cdf1DUar.js";import"./group-DHxaJaDV.js";import"./round-search-BN-yNV5a.js";const nt=Z({name:"NotifyConfigOperateDrawer",__name:"notify-config-operate-drawer",props:Y({operateType:{},rowData:{}},{visible:{type:Boolean,default:!1},visibleModifiers:{},retrySceneDisable:{type:Boolean,default:!0},retrySceneDisableModifiers:{}}),emits:Y(["update:value","submitted"],["update:visible","update:retrySceneDisable"]),setup(T,{emit:j}){const U=x([]),f=x([]),k=x([]),g=x([]),y=T,h=j,v=X(T,"visible"),d=X(T,"retrySceneDisable"),w=x(P(se)),{formRef:r,validate:m,restoreValidation:ee}=Ge(),{defaultRequiredRule:C}=Pe(),te=be(()=>({add:t("page.notifyConfig.addNotifyConfig"),edit:t("page.notifyConfig.editNotifyConfig")})[y.operateType]);_e(()=>{he(()=>{ae()})});async function ae(){const c=await $e();U.value=c.data}const o=ve(W());function W(){return{groupName:"",businessId:"",recipientIds:[],systemTaskType:null,notifyStatus:1,notifyScene:null,notifyThreshold:16,rateLimiterStatus:0,rateLimiterThreshold:100,description:""}}const B={groupName:C,businessId:C,notifyStatus:C,notifyScene:C,recipientIds:C,rateLimiterStatus:C,notifyThreshold:C};function H(){if(y.operateType==="add"){Object.assign(o,W()),d.value=!0;return}y.operateType==="edit"&&y.rowData&&(Object.assign(o,y.rowData),u(o.systemTaskType),l(o.notifyScene))}function K(){v.value=!1}async function J(){var c;if(await m(),y.operateType==="add"){const{groupName:i,businessId:p,recipientIds:$,systemTaskType:R,notifyStatus:V,notifyScene:b,notifyThreshold:S,rateLimiterStatus:A,rateLimiterThreshold:G,description:F}=o,{error:s}=await Ve({groupName:i,businessId:p,recipientIds:$,systemTaskType:R,notifyStatus:V,notifyScene:b,notifyThreshold:S,rateLimiterStatus:A,rateLimiterThreshold:G,description:F});if(s)return}if(y.operateType==="edit"){const{id:i,groupName:p,businessId:$,recipientIds:R,notifyStatus:V,systemTaskType:b,notifyScene:S,notifyThreshold:A,rateLimiterStatus:G,rateLimiterThreshold:F,description:s}=o,{error:ye}=await Oe({id:i,groupName:p,businessId:$,recipientIds:R,systemTaskType:b,notifyStatus:V,notifyScene:S,notifyThreshold:A,rateLimiterStatus:G,rateLimiterThreshold:F,description:s});if(ye)return}(c=window.$message)==null||c.success(t("common.updateSuccess")),K(),h("submitted")}async function u(c){var i,p,$,R,V;if(c===1){const b=await qe({groupName:o.groupName});f.value=b.data,w.value=P(se)}else if(c===3){const b=await He({groupName:o.groupName});k.value=(i=b.data)==null?void 0:i.map(S=>(S.id=String(S.id),S)),w.value=P(we)}else if(c===4){const b=await We({groupName:o.groupName});g.value=(p=b.data)==null?void 0:p.map(S=>(S.id=String(S.id),S)),w.value=P(ke)}c!==(($=y.rowData)==null?void 0:$.systemTaskType)?(o.businessId=null,o.notifyScene=null):(o.businessId=(R=y.rowData)==null?void 0:R.businessId,o.notifyScene=(V=y.rowData)==null?void 0:V.notifyScene)}async function l(c){d.value=!(c===5||c===6)}function _(c){H(),o.groupName=c,u(1),l(1)}return ue(v,()=>{v.value&&(H(),ee())}),(c,i)=>{const p=ze,$=Ye,R=Je,V=Qe,b=pe,S=Te,A=Ne,G=Ee,F=Q;return N(),L(ce,{modelValue:v.value,"onUpdate:modelValue":i[12]||(i[12]=s=>v.value=s),title:te.value,onHandleSubmit:J},{footer:n(()=>[a(R,{size:16},{default:n(()=>[a(F,{onClick:K},{default:n(()=>[D(I(e(t)("common.cancel")),1)]),_:1}),a(F,{type:"primary",onClick:J},{default:n(()=>[D(I(e(t)("common.save")),1)]),_:1})]),_:1})]),default:n(()=>[a(G,{ref_key:"formRef",ref:r,model:o,rules:B},{default:n(()=>[a(p,{label:e(t)("page.notifyConfig.groupName"),path:"groupName"},{default:n(()=>[a(Ke,{modelValue:o.groupName,"onUpdate:modelValue":[i[0]||(i[0]=s=>o.groupName=s),_]},null,8,["modelValue"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.notifyStatus"),path:"notifyStatus"},{default:n(()=>[a(V,{value:o.notifyStatus,"onUpdate:value":i[1]||(i[1]=s=>o.notifyStatus=s),name:"notifyStatus"},{default:n(()=>[a(R,null,{default:n(()=>[(N(!0),oe(ie,null,ne(e(le),s=>(N(),L($,{key:s.value,value:s.value,label:e(t)(s.label)},null,8,["value","label"]))),128))]),_:1})]),_:1},8,["value"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.systemTaskType"),path:"systemTaskType"},{default:n(()=>[a(b,{value:o.systemTaskType,"onUpdate:value":[i[2]||(i[2]=s=>o.systemTaskType=s),u],placeholder:e(t)("page.notifyConfig.form.systemTaskType"),options:e(P)(e(Se)),clearable:""},null,8,["value","placeholder","options"])]),_:1},8,["label"]),o.systemTaskType===1?(N(),L(p,{key:0,label:e(t)("page.notifyConfig.retryScene"),path:"businessId"},{default:n(()=>[a(b,{value:o.businessId,"onUpdate:value":i[3]||(i[3]=s=>o.businessId=s),placeholder:e(t)("page.notifyConfig.form.sceneName"),options:f.value,"label-field":"sceneName","value-field":"sceneName",clearable:""},null,8,["value","placeholder","options"])]),_:1},8,["label"])):q("",!0),o.systemTaskType===3?(N(),L(p,{key:1,label:e(t)("page.notifyConfig.job"),path:"businessId"},{default:n(()=>[a(b,{value:o.businessId,"onUpdate:value":i[4]||(i[4]=s=>o.businessId=s),placeholder:e(t)("page.notifyConfig.form.jobName"),options:k.value,"label-field":"jobName","value-field":"id",clearable:""},null,8,["value","placeholder","options"])]),_:1},8,["label"])):q("",!0),o.systemTaskType===4?(N(),L(p,{key:2,label:e(t)("page.notifyConfig.workflow"),path:"businessId"},{default:n(()=>[a(b,{value:o.businessId,"onUpdate:value":i[5]||(i[5]=s=>o.businessId=s),placeholder:e(t)("page.notifyConfig.form.workflowName"),options:g.value,"label-field":"workflowName","value-field":"id",clearable:""},null,8,["value","placeholder","options"])]),_:1},8,["label"])):q("",!0),a(p,{label:e(t)("page.notifyConfig.notifyScene"),path:"notifyScene"},{default:n(()=>[a(b,{value:o.notifyScene,"onUpdate:value":[i[6]||(i[6]=s=>o.notifyScene=s),l],placeholder:e(t)("page.notifyConfig.form.notifyScene"),options:w.value,clearable:""},null,8,["value","placeholder","options"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.notifyRecipient"),path:"recipientIds"},{default:n(()=>[a(b,{value:o.recipientIds,"onUpdate:value":i[7]||(i[7]=s=>o.recipientIds=s),placeholder:e(t)("page.notifyConfig.form.notifyRecipient"),options:U.value,clearable:"",multiple:""},null,8,["value","placeholder","options"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.rateLimiterStatus"),path:"rateLimiterStatus"},{default:n(()=>[a(V,{value:o.rateLimiterStatus,"onUpdate:value":i[8]||(i[8]=s=>o.rateLimiterStatus=s),name:"rateLimiterStatus",disabled:d.value},{default:n(()=>[a(R,null,{default:n(()=>[(N(!0),oe(ie,null,ne(e(le),s=>(N(),L($,{key:s.value,value:s.value,label:e(t)(s.label)},null,8,["value","label"]))),128))]),_:1})]),_:1},8,["value","disabled"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.rateLimiterThreshold"),path:"notifyThreshold"},{default:n(()=>[a(S,{value:o.rateLimiterThreshold,"onUpdate:value":i[9]||(i[9]=s=>o.rateLimiterThreshold=s),min:1,placeholder:e(t)("page.notifyConfig.form.notifyThreshold"),disabled:d.value},null,8,["value","placeholder","disabled"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.notifyThreshold"),path:"notifyThreshold"},{default:n(()=>[a(S,{value:o.notifyThreshold,"onUpdate:value":i[10]||(i[10]=s=>o.notifyThreshold=s),min:1,placeholder:e(t)("page.notifyConfig.form.notifyThreshold"),disabled:d.value},null,8,["value","placeholder","disabled"])]),_:1},8,["label"]),a(p,{label:e(t)("page.notifyConfig.description"),path:"description"},{default:n(()=>[a(A,{value:o.description,"onUpdate:value":i[11]||(i[11]=s=>o.description=s),type:"textarea",placeholder:e(t)("page.notifyConfig.form.description")},null,8,["value","placeholder"])]),_:1},8,["label"])]),_:1},8,["model"])]),_:1},8,["modelValue","title"])}}}),lt=Z({name:"NotifyConfigSearch",__name:"notify-config-search",props:{model:{required:!0},modelModifiers:{}},emits:Y(["reset","search"],["update:model"]),setup(T,{emit:j}){const U=j,f=X(T,"model");function k(){U("reset")}function g(){U("search")}return(y,h)=>{const v=pe,d=Fe,w=Ze;return N(),L(w,{model:f.value,onSearch:g,onReset:k},{default:n(()=>[a(d,{span:"24 s:12 m:6",label:e(t)("page.notifyConfig.groupName"),path:"groupName",class:"pr-24px"},{default:n(()=>[a(v,{value:f.value.groupName,"onUpdate:value":h[0]||(h[0]=r=>f.value.groupName=r),placeholder:e(t)("page.notifyConfig.groupName"),clearable:""},null,8,["value","placeholder"])]),_:1},8,["label"]),a(d,{span:"24 s:12 m:6",label:e(t)("page.notifyConfig.notifyStatus"),path:"notifyStatus",class:"pr-24px"},{default:n(()=>[a(v,{value:f.value.notifyStatus,"onUpdate:value":h[1]||(h[1]=r=>f.value.notifyStatus=r),placeholder:e(t)("page.notifyConfig.notifyStatus"),clearable:""},null,8,["value","placeholder"])]),_:1},8,["label"]),a(d,{span:"24 s:12 m:6",label:e(t)("page.notifyConfig.notifyScene"),path:"notifyScene",class:"pr-24px"},{default:n(()=>[a(v,{value:f.value.notifyScene,"onUpdate:value":h[2]||(h[2]=r=>f.value.notifyScene=r),placeholder:e(t)("page.notifyConfig.notifyScene"),clearable:""},null,8,["value","placeholder"])]),_:1},8,["label"])]),_:1},8,["model"])}}}),it=Z({name:"NotifyConfigDetailDrawer",__name:"notify-config-detail-drawer",props:Y({rowData:{}},{visible:{type:Boolean,default:!1},visibleModifiers:{}}),emits:["update:visible"],setup(T){const j=T,U=X(T,"visible");return ue(()=>j.rowData,()=>{console.log(j.rowData)},{immediate:!0}),(f,k)=>{const g=et,y=E,h=tt,v=ce;return N(),L(v,{modelValue:U.value,"onUpdate:modelValue":k[0]||(k[0]=d=>U.value=d),title:e(t)("page.groupConfig.detail")},{default:n(()=>[a(h,{"label-placement":"top",bordered:"",column:2},{default:n(()=>{var d,w;return[a(g,{label:e(t)("page.notifyConfig.businessName"),span:2},{default:n(()=>{var r;return[D(I((r=f.rowData)==null?void 0:r.businessName),1)]}),_:1},8,["label"]),a(g,{label:e(t)("page.groupConfig.groupName"),span:2},{default:n(()=>{var r;return[D(I((r=f.rowData)==null?void 0:r.groupName),1)]}),_:1},8,["label"]),a(g,{label:e(t)("page.notifyConfig.systemTaskType"),span:1},{default:n(()=>{var r;return[a(y,{type:e(O)((r=f.rowData)==null?void 0:r.systemTaskType)},{default:n(()=>{var m;return[D(I(e(t)(e(fe)[(m=f.rowData)==null?void 0:m.systemTaskType])),1)]}),_:1},8,["type"])]}),_:1},8,["label"]),a(g,{label:e(t)("page.notifyConfig.notifyStatus"),span:1},{default:n(()=>{var r;return[a(y,{type:e(O)((r=f.rowData)==null?void 0:r.notifyStatus)},{default:n(()=>{var m;return[D(I(e(t)(e(Ce)[(m=f.rowData)==null?void 0:m.notifyStatus])),1)]}),_:1},8,["type"])]}),_:1},8,["label"]),((d=f.rowData)==null?void 0:d.systemTaskType)===1?(N(),L(g,{key:0,label:e(t)("page.notifyConfig.notifyScene"),span:1},{default:n(()=>{var r;return[a(y,{type:e(O)((r=f.rowData)==null?void 0:r.notifyScene)},{default:n(()=>{var m;return[D(I(e(t)(e(de)[(m=f.rowData)==null?void 0:m.notifyScene])),1)]}),_:1},8,["type"])]}),_:1},8,["label"])):q("",!0),((w=f.rowData)==null?void 0:w.systemTaskType)===3?(N(),L(g,{key:1,label:e(t)("page.notifyConfig.notifyScene"),span:1},{default:n(()=>{var r;return[a(y,{type:e(O)((r=f.rowData)==null?void 0:r.notifyScene)},{default:n(()=>{var m;return[D(I(e(t)(e(me)[(m=f.rowData)==null?void 0:m.notifyScene])),1)]}),_:1},8,["type"])]}),_:1},8,["label"])):q("",!0),a(g,{label:e(t)("page.notifyConfig.notifyThreshold"),span:1},{default:n(()=>{var r;return[a(y,{type:e(O)((r=f.rowData)==null?void 0:r.notifyThreshold)},{default:n(()=>{var m;return[D(I((m=f.rowData)==null?void 0:m.notifyThreshold),1)]}),_:1},8,["type"])]}),_:1},8,["label"]),a(g,{label:e(t)("common.createDt"),span:2},{default:n(()=>{var r;return[D(I((r=f.rowData)==null?void 0:r.createDt),1)]}),_:1},8,["label"]),a(g,{label:e(t)("page.notifyConfig.description"),span:2},{default:n(()=>{var r;return[D(I((r=f.rowData)==null?void 0:r.description),1)]}),_:1},8,["label"])]}),_:1})]),_:1},8,["modelValue","title"])}}}),st={class:"min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto"};function M(T){return typeof T=="function"||Object.prototype.toString.call(T)==="[object Object]"&&!Le(T)}const Dt=Z({name:"notify_scene",__name:"index",setup(T){const{hasAuth:j}=ot(),U=De(),f=x(),{bool:k,setTrue:g}=Ie(!1),{columns:y,columnChecks:h,data:v,getData:d,loading:w,mobilePagination:r,searchParams:m,resetSearchParams:ee}=Me({apiFn:je,apiParams:{page:1,size:10,groupName:null,notifyStatus:null,notifyScene:null},columns:()=>[{type:"selection",align:"center",width:48},{key:"index",title:t("common.index"),align:"center",width:64},{key:"businessName",title:t("page.notifyConfig.businessName"),align:"left",width:120,render:u=>{function l(){f.value=u||null,g()}return a(Q,{text:!0,tag:"a",type:"primary",onClick:l,class:"ws-normal"},{default:()=>[u.businessName]})}},{key:"groupName",title:t("page.notifyConfig.groupName"),align:"left",width:120},{key:"systemTaskType",title:t("page.notifyConfig.systemTaskType"),align:"left",width:120,render:u=>{if(u.systemTaskType===null)return null;const l=t(fe[u.systemTaskType]);return a(E,{type:O(u.systemTaskType)},M(l)?l:{default:()=>[l]})}},{key:"notifyStatus",title:t("page.notifyConfig.notifyStatus"),align:"left",width:120,render:u=>{const l=async(_,c)=>{var p;const{error:i}=await Be(u.id,_);i||(u.notifyStatus=_,(p=window.$message)==null||p.success(t("common.updateSuccess"))),c()};return a(at,{value:u.notifyStatus,"onUpdate:value":_=>u.notifyStatus=_,onFetch:l},null)}},{key:"notifyScene",title:t("page.notifyConfig.notifyScene"),align:"left",width:160,render:u=>{if(u.notifyScene===null)return null;if(u.systemTaskType===1){const l=t(de[u.notifyScene]);return a(E,{type:O(u.notifyScene)},M(l)?l:{default:()=>[l]})}if(u.systemTaskType===3){const l=t(me[u.notifyScene]);return a(E,{type:O(u.notifyScene)},M(l)?l:{default:()=>[l]})}if(u.systemTaskType===4){const l=t(Ue[u.notifyScene]);return a(E,{type:O(u.notifyScene)},M(l)?l:{default:()=>[l]})}return null}},{key:"notifyThreshold",title:t("page.notifyConfig.notifyThreshold"),align:"left",width:120},{key:"createDt",title:t("common.createDt"),align:"left",width:120},{key:"description",title:t("page.notifyConfig.description"),align:"left",width:120},{key:"operate",title:t("common.operate"),align:"center",width:130,render:u=>{let l;return a("div",{class:"flex-center gap-8px"},[a(Q,{type:"primary",ghost:!0,size:"small",onClick:()=>J(u.id)},M(l=t("common.edit"))?l:{default:()=>[l]}),j("R_ADMIN")?a(Ae,{onPositiveClick:()=>K(u.id)},{default:()=>t("common.confirmDelete"),trigger:()=>{let _;return a(Q,{type:"error",ghost:!0,size:"small"},M(_=t("common.delete"))?_:{default:()=>[_]})}}):""])}}]}),{drawerVisible:C,operateType:te,editingData:ae,handleAdd:o,handleEdit:W,checkedRowKeys:B}=xe(v,d);async function H(){var l;const{error:u}=await re(B.value);u||((l=window.$message)==null||l.success(t("common.deleteSuccess")),d())}async function K(u){var _;const{error:l}=await re([u]);l||((_=window.$message)==null||_.success(t("common.deleteSuccess")),d())}function J(u){W(u)}return(u,l)=>{const _=ge,c=Xe,i=Re;return N(),oe("div",st,[a(lt,{model:e(m),"onUpdate:model":l[0]||(l[0]=p=>z(m)?m.value=p:null),onReset:e(ee),onSearch:e(d)},null,8,["model","onReset","onSearch"]),a(i,{title:e(t)("page.notifyConfig.title"),bordered:!1,size:"small",class:"sm:flex-1-hidden card-wrapper","header-class":"view-card-header"},{"header-extra":n(()=>[a(_,{columns:e(h),"onUpdate:columns":l[1]||(l[1]=p=>z(h)?h.value=p:null),"disabled-delete":e(B).length===0,loading:e(w),onAdd:e(o),onDelete:H,onRefresh:e(d)},null,8,["columns","disabled-delete","loading","onAdd","onRefresh"])]),default:n(()=>[a(c,{"checked-row-keys":e(B),"onUpdate:checkedRowKeys":l[2]||(l[2]=p=>z(B)?B.value=p:null),columns:e(y),data:e(v),"flex-height":!e(U).isMobile,"scroll-x":962,loading:e(w),remote:"","row-key":p=>p.id,pagination:e(r),class:"sm:h-full"},null,8,["checked-row-keys","columns","data","flex-height","loading","row-key","pagination"]),a(nt,{visible:e(C),"onUpdate:visible":l[3]||(l[3]=p=>z(C)?C.value=p:null),"operate-type":e(te),"row-data":e(ae),onSubmitted:e(d)},null,8,["visible","operate-type","row-data","onSubmitted"]),a(it,{visible:e(k),"onUpdate:visible":l[4]||(l[4]=p=>z(k)?k.value=p:null),"row-data":f.value},null,8,["visible","row-data"])]),_:1},8,["title"])])}}});export{Dt as default};