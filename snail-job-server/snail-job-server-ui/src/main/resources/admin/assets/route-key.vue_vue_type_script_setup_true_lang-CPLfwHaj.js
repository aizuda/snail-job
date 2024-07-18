import{b0 as C,bp as k,d as b,b1 as R,b4 as g,aY as B,a as i,bq as u,b7 as T,al as d,Z as L,a0 as O,a4 as c,br as p,o as $,c as w,h as K,$ as P,aa as S}from"./index-D2gfy4BV.js";const V=C("input-group-label",`
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
`,[k("border",`
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 border-radius: inherit;
 border: var(--n-group-label-border);
 transition: border-color .3s var(--n-bezier);
 `)]),I=Object.assign(Object.assign({},g.props),{size:{type:String,default:"medium"},bordered:{type:Boolean,default:void 0}}),j=b({name:"InputGroupLabel",props:I,setup(e){const{mergedBorderedRef:n,mergedClsPrefixRef:o,inlineThemeDisabled:s}=R(e),a=g("Input","-input-group-label",V,B,e,o),t=i(()=>{const{size:l}=e,{common:{cubicBezierEaseInOut:h},self:{groupLabelColor:m,borderRadius:v,groupLabelTextColor:f,lineHeight:_,groupLabelBorder:z,[u("fontSize",l)]:x,[u("height",l)]:y}}=a.value;return{"--n-bezier":h,"--n-group-label-color":m,"--n-group-label-border":z,"--n-border-radius":v,"--n-group-label-text-color":f,"--n-font-size":x,"--n-line-height":_,"--n-height":y}}),r=s?T("input-group-label",i(()=>e.size[0]),t,e):void 0;return{mergedClsPrefix:o,mergedBordered:n,cssVars:s?void 0:t,themeClass:r==null?void 0:r.themeClass,onRender:r==null?void 0:r.onRender}},render(){var e,n,o;const{mergedClsPrefix:s}=this;return(e=this.onRender)===null||e===void 0||e.call(this),d("div",{class:[`${s}-input-group-label`,this.themeClass],style:this.cssVars},(o=(n=this.$slots).default)===null||o===void 0?void 0:o.call(n),this.mergedBordered?d("div",{class:`${s}-input-group-label__border`}):null)}}),E=b({name:"RouterKey",__name:"route-key",props:L({taskType:{}},{value:{default:4},valueModifiers:{}}),emits:["update:value"],setup(e){const n=e,o=O(e,"value"),s=i(()=>n.taskType===2||n.taskType===3?c(p.filter(a=>a.value===4)):c(p));return(a,t)=>{const r=S;return $(),w(r,{value:o.value,"onUpdate:value":t[0]||(t[0]=l=>o.value=l),placeholder:K(P)("common.routeKey.routeForm"),options:s.value},null,8,["value","placeholder","options"])}}});export{j as _,E as a};
