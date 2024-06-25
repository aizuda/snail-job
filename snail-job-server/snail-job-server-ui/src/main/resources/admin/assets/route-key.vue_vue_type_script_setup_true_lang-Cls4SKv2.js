import{b6 as k,bD as R,d as m,b7 as B,ba as v,b2 as T,a as d,bE as c,bd as L,aj as p,Q as b,R as O,X as g,bF as h,o as $,c as w,h as K,$ as P,a6 as S}from"./index-D3rICic-.js";const V=k("input-group-label",`
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
`,[R("border",`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border-radius: inherit;
 border: var(--n-group-label-border);
 transition: border-color .3s var(--n-bezier);
 `)]),j=Object.assign(Object.assign({},v.props),{size:{type:String,default:"medium"},bordered:{type:Boolean,default:void 0}}),I=m({name:"InputGroupLabel",props:j,setup(e){const{mergedBorderedRef:s,mergedClsPrefixRef:o,inlineThemeDisabled:n}=B(e),a=v("Input","-input-group-label",V,T,e,o),l=d(()=>{const{size:t}=e,{common:{cubicBezierEaseInOut:i},self:{groupLabelColor:u,borderRadius:f,groupLabelTextColor:_,lineHeight:z,groupLabelBorder:x,[c("fontSize",t)]:y,[c("height",t)]:C}}=a.value;return{"--n-bezier":i,"--n-group-label-color":u,"--n-group-label-border":x,"--n-border-radius":f,"--n-group-label-text-color":_,"--n-font-size":y,"--n-line-height":z,"--n-height":C}}),r=n?L("input-group-label",d(()=>e.size[0]),l,e):void 0;return{mergedClsPrefix:o,mergedBordered:s,cssVars:n?void 0:l,themeClass:r==null?void 0:r.themeClass,onRender:r==null?void 0:r.onRender}},render(){var e,s,o;const{mergedClsPrefix:n}=this;return(e=this.onRender)===null||e===void 0||e.call(this),p("div",{class:[`${n}-input-group-label`,this.themeClass],style:this.cssVars},(o=(s=this.$slots).default)===null||o===void 0?void 0:o.call(s),this.mergedBordered?p("div",{class:`${n}-input-group-label__border`}):null)}}),M=m({name:"RouterKey",__name:"route-key",props:b({taskType:{}},{value:{},valueModifiers:{}}),emits:b(["update:value"],["update:value"]),setup(e,{emit:s}){const o=e,n=s,a=O(e,"value"),l=d(()=>(n("update:value",4),o.taskType===2||o.taskType===3?g(h.filter(r=>r.value===4)):g(h)));return(r,t)=>{const i=S;return $(),w(i,{value:a.value,"onUpdate:value":t[0]||(t[0]=u=>a.value=u),placeholder:K(P)("common.routeKey.routeForm"),options:l.value},null,8,["value","placeholder","options"])}}});export{I as _,M as a};
