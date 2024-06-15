import{aA as C,aB as a,d as B,aF as $,aI as h,c6 as z,a as c,b7 as T,aP as _,cr as R,ap as i}from"./index-BztLELzb.js";const S=C("text",`
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
`,[a("strong",`
 font-weight: var(--n-font-weight-strong);
 `),a("italic",{fontStyle:"italic"}),a("underline",{textDecoration:"underline"}),a("code",`
 line-height: 1.4;
 display: inline-block;
 font-family: var(--n-font-famliy-mono);
 transition: 
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 box-sizing: border-box;
 padding: .05em .35em 0 .35em;
 border-radius: var(--n-code-border-radius);
 font-size: .9em;
 color: var(--n-code-text-color);
 background-color: var(--n-code-color);
 border: var(--n-code-border);
 `)]),P=Object.assign(Object.assign({},h.props),{code:Boolean,type:{type:String,default:"default"},delete:Boolean,strong:Boolean,italic:Boolean,underline:Boolean,depth:[String,Number],tag:String,as:{type:String,validator:()=>!0,default:void 0}}),k=B({name:"Text",props:P,setup(e){const{mergedClsPrefixRef:r,inlineThemeDisabled:n}=$(e),o=h("Typography","-text",S,z,e,r),s=c(()=>{const{depth:l,type:d}=e,u=d==="default"?l===void 0?"textColor":`textColor${l}Depth`:T("textColor",d),{common:{fontWeightStrong:g,fontFamilyMono:m,cubicBezierEaseInOut:x},self:{codeTextColor:b,codeBorderRadius:f,codeColor:v,codeBorder:y,[u]:p}}=o.value;return{"--n-bezier":x,"--n-text-color":p,"--n-font-weight-strong":g,"--n-font-famliy-mono":m,"--n-code-border-radius":f,"--n-code-text-color":b,"--n-code-color":v,"--n-code-border":y}}),t=n?_("text",c(()=>`${e.type[0]}${e.depth||""}`),s,e):void 0;return{mergedClsPrefix:r,compitableTag:R(e,["as","tag"]),cssVars:n?void 0:s,themeClass:t==null?void 0:t.themeClass,onRender:t==null?void 0:t.onRender}},render(){var e,r,n;const{mergedClsPrefix:o}=this;(e=this.onRender)===null||e===void 0||e.call(this);const s=[`${o}-text`,this.themeClass,{[`${o}-text--code`]:this.code,[`${o}-text--delete`]:this.delete,[`${o}-text--strong`]:this.strong,[`${o}-text--italic`]:this.italic,[`${o}-text--underline`]:this.underline}],t=(n=(r=this.$slots).default)===null||n===void 0?void 0:n.call(r);return this.code?i("code",{class:s,style:this.cssVars},this.delete?i("del",null,t):t):this.delete?i("del",{class:s,style:this.cssVars},t):i(this.compitableTag||"span",{class:s,style:this.cssVars},t)}});export{k as _};
