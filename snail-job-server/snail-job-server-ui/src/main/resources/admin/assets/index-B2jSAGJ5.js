import{d as f,k as d,bT as w,r as i,i as k,o as _,c as v,h as g,$ as h}from"./index-D2gfy4BV.js";import{u as y,_ as S}from"./workflow.vue_vue_type_style_index_0_lang-DKtIAivx.js";import{h as $,i as x}from"./workflow-DL9sDB8e.js";import"./job-task-list-table.vue_vue_type_script_setup_true_lang-BWfE14-k.js";import"./table-CBd8wgyy.js";import"./Grid-DUVCwFN_.js";import"./job-zG0yc1zw.js";import"./detail-drawer-CQjfY8zD.js";import"./DescriptionsItem-LCBYJ9Xx.js";import"./log-drawer-BYtEGha_.js";import"./CollapseItem-BF9JL9RU.js";import"./code-mirror.vue_vue_type_script_setup_true_lang-qktOrMmh.js";import"./cron-input.vue_vue_type_style_index_0_lang-Bx5dJ8gU.js";import"./group-CHo9Y09B.js";const E=f({name:"workflow_form_edit",__name:"index",setup(V){const a=y(),u=d(),r=w(),s=i(!1),n=String(u.query.id),e=i({}),p=async()=>{s.value=!0;const{data:t,error:o}=await $(n);o||(e.value=t),s.value=!1};k(()=>{a.clear(),a.setType(0),a.setId(n),p()});const l=async()=>{var o;const{error:t}=await x(e.value);t||((o=window.$message)==null||o.info(h("common.updateSuccess")),r.push({path:"/workflow/task"}))},m=()=>{r.push("/workflow/task")};return(t,o)=>(_(),v(g(S),{modelValue:e.value,"onUpdate:modelValue":o[0]||(o[0]=c=>e.value=c),spinning:s.value,onSave:l,onCancel:m},null,8,["modelValue","spinning"]))}});export{E as default};