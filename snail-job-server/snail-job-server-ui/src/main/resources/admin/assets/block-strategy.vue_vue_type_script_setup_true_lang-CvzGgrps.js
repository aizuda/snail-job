import{b5 as O,bv as T,d as b,b6 as L,b9 as h,b1 as $,a as d,bw as g,bc as w,aj as m,Z as K,a0 as P,ac as p,bx as v,o as f,c as _,h as y,$ as k,ad as x,r as V,by as j}from"./index-BHXExu3l.js";const I=O("input-group-label",`
 position: relative;
 user-select: none;
 -webkit-user-select: none;
 box-sizing: border-box;
 padding: 0 12px;
 display: inline-block;
 border-radius: var(--n-border-radius);
 background-color: var(--n-group-label-color);
 color: var(--n-group-label-text-color);
 font-size: var(--n-font-size);
 line-height: var(--n-height);
 height: var(--n-height);
 flex-shrink: 0;
 white-space: nowrap;
 transition: 
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 box-shadow .3s var(--n-bezier);
`,[T("border",`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border-radius: inherit;
 border: var(--n-group-label-border);
 transition: border-color .3s var(--n-bezier);
 `)]),M=Object.assign(Object.assign({},h.props),{size:{type:String,default:"medium"},bordered:{type:Boolean,default:void 0}}),E=b({name:"InputGroupLabel",props:M,setup(e){const{mergedBorderedRef:s,mergedClsPrefixRef:o,inlineThemeDisabled:r}=L(e),l=h("Input","-input-group-label",I,$,e,o),a=d(()=>{const{size:n}=e,{common:{cubicBezierEaseInOut:i},self:{groupLabelColor:u,borderRadius:c,groupLabelTextColor:z,lineHeight:C,groupLabelBorder:R,[g("fontSize",n)]:B,[g("height",n)]:S}}=l.value;return{"--n-bezier":i,"--n-group-label-color":u,"--n-group-label-border":R,"--n-border-radius":c,"--n-group-label-text-color":z,"--n-font-size":B,"--n-line-height":C,"--n-height":S}}),t=r?w("input-group-label",d(()=>e.size[0]),a,e):void 0;return{mergedClsPrefix:o,mergedBordered:s,cssVars:r?void 0:a,themeClass:t==null?void 0:t.themeClass,onRender:t==null?void 0:t.onRender}},render(){var e,s,o;const{mergedClsPrefix:r}=this;return(e=this.onRender)===null||e===void 0||e.call(this),m("div",{class:[`${r}-input-group-label`,this.themeClass],style:this.cssVars},(o=(s=this.$slots).default)===null||o===void 0?void 0:o.call(s),this.mergedBordered?m("div",{class:`${r}-input-group-label__border`}):null)}}),G=b({name:"RouterKey",__name:"route-key",props:K({taskType:{}},{value:{default:4},valueModifiers:{}}),emits:["update:value"],setup(e){const s=e,o=P(e,"value"),r=d(()=>s.taskType===2||s.taskType===3?p(v.filter(l=>l.value===4)):p(v));return(l,a)=>{const t=x;return f(),_(t,{value:o.value,"onUpdate:value":a[0]||(a[0]=n=>o.value=n),placeholder:y(k)("common.routeKey.routeForm"),options:r.value},null,8,["value","placeholder","options"])}}}),H=b({name:"BlockStrategy",__name:"block-strategy",props:{ignore:{}},emits:["update:value"],setup(e,{emit:s}){const o=e,r=V(),l=s,a=n=>{l("update:value",n)},t=d(()=>{const n=p(j);return o.ignore?n.filter(i=>{var u;return(u=o.ignore)==null?void 0:u.includes(i.value)}):n});return(n,i)=>{const u=x;return f(),_(u,{value:r.value,"onUpdate:value":[i[0]||(i[0]=c=>r.value=c),a],placeholder:y(k)("common.blockStrategy.form"),options:t.value},null,8,["value","placeholder","options"])}}});export{E as _,G as a,H as b};
