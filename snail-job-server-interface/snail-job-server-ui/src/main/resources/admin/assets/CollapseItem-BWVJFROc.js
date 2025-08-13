import{d as P,aj as s,b9 as f,ba as x,b8 as z,bx as o,by as H,dK as O,bb as D,r as K,a as N,bJ as V,bc as T,ee as W,cp as F,be as q,bw as J,bO as Q,bQ as I,cl as Z,c4 as G,ef as X,bK as k,dF as Y,dp as $,eg as ee,dI as re,bF as ae,bA as te,dr as le,du as A,bz as oe,eh as se}from"./index-C0p55rrf.js";const ne=P({name:"ChevronLeft",render(){return s("svg",{viewBox:"0 0 16 16",fill:"none",xmlns:"http://www.w3.org/2000/svg"},s("path",{d:"M10.3536 3.14645C10.5488 3.34171 10.5488 3.65829 10.3536 3.85355L6.20711 8L10.3536 12.1464C10.5488 12.3417 10.5488 12.6583 10.3536 12.8536C10.1583 13.0488 9.84171 13.0488 9.64645 12.8536L5.14645 8.35355C4.95118 8.15829 4.95118 7.84171 5.14645 7.64645L9.64645 3.14645C9.84171 2.95118 10.1583 2.95118 10.3536 3.14645Z",fill:"currentColor"}))}}),ie=f("collapse","width: 100%;",[f("collapse-item",`
 font-size: var(--n-font-size);
 color: var(--n-text-color);
 transition:
 color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 margin: var(--n-item-margin);
 `,[x("disabled",[o("header","cursor: not-allowed;",[o("header-main",`
 color: var(--n-title-text-color-disabled);
 `),f("collapse-item-arrow",`
 color: var(--n-arrow-color-disabled);
 `)])]),f("collapse-item","margin-left: 32px;"),z("&:first-child","margin-top: 0;"),z("&:first-child >",[o("header","padding-top: 0;")]),x("left-arrow-placement",[o("header",[f("collapse-item-arrow","margin-right: 4px;")])]),x("right-arrow-placement",[o("header",[f("collapse-item-arrow","margin-left: 4px;")])]),o("content-wrapper",[o("content-inner","padding-top: 16px;"),O({duration:"0.15s"})]),x("active",[o("header",[x("active",[f("collapse-item-arrow","transform: rotate(90deg);")])])]),z("&:not(:first-child)","border-top: 1px solid var(--n-divider-color);"),H("disabled",[x("trigger-area-main",[o("header",[o("header-main","cursor: pointer;"),f("collapse-item-arrow","cursor: default;")])]),x("trigger-area-arrow",[o("header",[f("collapse-item-arrow","cursor: pointer;")])]),x("trigger-area-extra",[o("header",[o("header-extra","cursor: pointer;")])])]),o("header",`
 font-size: var(--n-title-font-size);
 display: flex;
 flex-wrap: nowrap;
 align-items: center;
 transition: color .3s var(--n-bezier);
 position: relative;
 padding: var(--n-title-padding);
 color: var(--n-title-text-color);
 `,[o("header-main",`
 display: flex;
 flex-wrap: nowrap;
 align-items: center;
 font-weight: var(--n-title-font-weight);
 transition: color .3s var(--n-bezier);
 flex: 1;
 color: var(--n-title-text-color);
 `),o("header-extra",`
 display: flex;
 align-items: center;
 transition: color .3s var(--n-bezier);
 color: var(--n-text-color);
 `),f("collapse-item-arrow",`
 display: flex;
 transition:
 transform .15s var(--n-bezier),
 color .3s var(--n-bezier);
 font-size: 18px;
 color: var(--n-arrow-color);
 `)])])]),de=Object.assign(Object.assign({},T.props),{defaultExpandedNames:{type:[Array,String],default:null},expandedNames:[Array,String],arrowPlacement:{type:String,default:"left"},accordion:{type:Boolean,default:!1},displayDirective:{type:String,default:"if"},triggerAreas:{type:Array,default:()=>["main","extra","arrow"]},onItemHeaderClick:[Function,Array],"onUpdate:expandedNames":[Function,Array],onUpdateExpandedNames:[Function,Array],onExpandedNamesChange:{type:[Function,Array],validator:()=>!0,default:void 0}}),j=J("n-collapse"),fe=P({name:"Collapse",props:de,slots:Object,setup(e,{slots:i}){const{mergedClsPrefixRef:n,inlineThemeDisabled:l,mergedRtlRef:d}=D(e),a=K(e.defaultExpandedNames),h=N(()=>e.expandedNames),v=V(h,a),w=T("Collapse","-collapse",ie,W,e,n);function c(p){const{"onUpdate:expandedNames":t,onUpdateExpandedNames:m,onExpandedNamesChange:y}=e;m&&I(m,p),t&&I(t,p),y&&I(y,p),a.value=p}function g(p){const{onItemHeaderClick:t}=e;t&&I(t,p)}function r(p,t,m){const{accordion:y}=e,{value:E}=v;if(y)p?(c([t]),g({name:t,expanded:!0,event:m})):(c([]),g({name:t,expanded:!1,event:m}));else if(!Array.isArray(E))c([t]),g({name:t,expanded:!0,event:m});else{const b=E.slice(),_=b.findIndex(S=>t===S);~_?(b.splice(_,1),c(b),g({name:t,expanded:!1,event:m})):(b.push(t),c(b),g({name:t,expanded:!0,event:m}))}}Q(j,{props:e,mergedClsPrefixRef:n,expandedNamesRef:v,slots:i,toggleItem:r});const u=F("Collapse",d,n),R=N(()=>{const{common:{cubicBezierEaseInOut:p},self:{titleFontWeight:t,dividerColor:m,titlePadding:y,titleTextColor:E,titleTextColorDisabled:b,textColor:_,arrowColor:S,fontSize:B,titleFontSize:L,arrowColorDisabled:U,itemMargin:M}}=w.value;return{"--n-font-size":B,"--n-bezier":p,"--n-text-color":_,"--n-divider-color":m,"--n-title-padding":y,"--n-title-font-size":L,"--n-title-text-color":E,"--n-title-text-color-disabled":b,"--n-title-font-weight":t,"--n-arrow-color":S,"--n-arrow-color-disabled":U,"--n-item-margin":M}}),C=l?q("collapse",void 0,R,e):void 0;return{rtlEnabled:u,mergedTheme:w,mergedClsPrefix:n,cssVars:l?void 0:R,themeClass:C==null?void 0:C.themeClass,onRender:C==null?void 0:C.onRender}},render(){var e;return(e=this.onRender)===null||e===void 0||e.call(this),s("div",{class:[`${this.mergedClsPrefix}-collapse`,this.rtlEnabled&&`${this.mergedClsPrefix}-collapse--rtl`,this.themeClass],style:this.cssVars},this.$slots)}}),ce=P({name:"CollapseItemContent",props:{displayDirective:{type:String,required:!0},show:Boolean,clsPrefix:{type:String,required:!0}},setup(e){return{onceTrue:X(k(e,"show"))}},render(){return s(Z,null,{default:()=>{const{show:e,displayDirective:i,onceTrue:n,clsPrefix:l}=this,d=i==="show"&&n,a=s("div",{class:`${l}-collapse-item__content-wrapper`},s("div",{class:`${l}-collapse-item__content-inner`},this.$slots));return d?G(a,[[Y,e]]):e?a:null}})}}),pe={title:String,name:[String,Number],disabled:Boolean,displayDirective:String},ue=P({name:"CollapseItem",props:pe,setup(e){const{mergedRtlRef:i}=D(e),n=re(),l=ae(()=>{var r;return(r=e.name)!==null&&r!==void 0?r:n}),d=te(j);d||le("collapse-item","`n-collapse-item` must be placed inside `n-collapse`.");const{expandedNamesRef:a,props:h,mergedClsPrefixRef:v,slots:w}=d,c=N(()=>{const{value:r}=a;if(Array.isArray(r)){const{value:u}=l;return!~r.findIndex(R=>R===u)}else if(r){const{value:u}=l;return u!==r}return!0});return{rtlEnabled:F("Collapse",i,v),collapseSlots:w,randomName:n,mergedClsPrefix:v,collapsed:c,triggerAreas:k(h,"triggerAreas"),mergedDisplayDirective:N(()=>{const{displayDirective:r}=e;return r||h.displayDirective}),arrowPlacement:N(()=>h.arrowPlacement),handleClick(r){let u="main";A(r,"arrow")&&(u="arrow"),A(r,"extra")&&(u="extra"),h.triggerAreas.includes(u)&&d&&!e.disabled&&d.toggleItem(c.value,l.value,r)}}},render(){const{collapseSlots:e,$slots:i,arrowPlacement:n,collapsed:l,mergedDisplayDirective:d,mergedClsPrefix:a,disabled:h,triggerAreas:v}=this,w=$(i.header,{collapsed:l},()=>[this.title]),c=i["header-extra"]||e["header-extra"],g=i.arrow||e.arrow;return s("div",{class:[`${a}-collapse-item`,`${a}-collapse-item--${n}-arrow-placement`,h&&`${a}-collapse-item--disabled`,!l&&`${a}-collapse-item--active`,v.map(r=>`${a}-collapse-item--trigger-area-${r}`)]},s("div",{class:[`${a}-collapse-item__header`,!l&&`${a}-collapse-item__header--active`]},s("div",{class:`${a}-collapse-item__header-main`,onClick:this.handleClick},n==="right"&&w,s("div",{class:`${a}-collapse-item-arrow`,key:this.rtlEnabled?0:1,"data-arrow":!0},$(g,{collapsed:l},()=>[s(oe,{clsPrefix:a},{default:()=>this.rtlEnabled?s(ne,null):s(se,null)})])),n==="left"&&w),ee(c,{collapsed:l},r=>s("div",{class:`${a}-collapse-item__header-extra`,onClick:this.handleClick,"data-extra":!0},r))),s(ce,{clsPrefix:a,displayDirective:d,show:!l},i))}});export{fe as _,ue as a};
