import{aX as O,dU as D,bx as v,dV as b,b0 as _,bp as c,bk as P,dm as q,bj as K,d as $,b1 as Q,b4 as A,cA as U,a as y,dW as X,bq as h,b7 as G,r as J,al as a,dj as Y,bB as Z,cm as ee,b8 as oe,dQ as re,dR as ne,dS as te,dP as se,am as le,cx as ae,k as ie,y as ce,o as de,c as ue,w as he,e as E,g as R,t as ge,ai as fe,c_ as ve,c$ as be,l as Ce}from"./index-BxI_8ir5.js";const pe=r=>{const{lineHeight:e,borderRadius:i,fontWeightStrong:l,baseColor:n,dividerColor:g,actionColor:x,textColor1:f,textColor2:t,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,infoColor:o,successColor:I,warningColor:z,errorColor:S,fontSize:w}=r;return Object.assign(Object.assign({},D),{fontSize:w,lineHeight:e,titleFontWeight:l,borderRadius:i,border:`1px solid ${g}`,color:x,titleTextColor:f,iconColor:t,contentTextColor:t,closeBorderRadius:i,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,borderInfo:`1px solid ${v(n,b(o,{alpha:.25}))}`,colorInfo:v(n,b(o,{alpha:.08})),titleTextColorInfo:f,iconColorInfo:o,contentTextColorInfo:t,closeColorHoverInfo:d,closeColorPressedInfo:C,closeIconColorInfo:p,closeIconColorHoverInfo:m,closeIconColorPressedInfo:s,borderSuccess:`1px solid ${v(n,b(I,{alpha:.25}))}`,colorSuccess:v(n,b(I,{alpha:.08})),titleTextColorSuccess:f,iconColorSuccess:I,contentTextColorSuccess:t,closeColorHoverSuccess:d,closeColorPressedSuccess:C,closeIconColorSuccess:p,closeIconColorHoverSuccess:m,closeIconColorPressedSuccess:s,borderWarning:`1px solid ${v(n,b(z,{alpha:.33}))}`,colorWarning:v(n,b(z,{alpha:.08})),titleTextColorWarning:f,iconColorWarning:z,contentTextColorWarning:t,closeColorHoverWarning:d,closeColorPressedWarning:C,closeIconColorWarning:p,closeIconColorHoverWarning:m,closeIconColorPressedWarning:s,borderError:`1px solid ${v(n,b(S,{alpha:.25}))}`,colorError:v(n,b(S,{alpha:.08})),titleTextColorError:f,iconColorError:S,contentTextColorError:t,closeColorHoverError:d,closeColorPressedError:C,closeIconColorError:p,closeIconColorHoverError:m,closeIconColorPressedError:s})},me={name:"Alert",common:O,self:pe},_e=_("alert",`
 line-height: var(--n-line-height);
 border-radius: var(--n-border-radius);
 position: relative;
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-color);
 text-align: start;
 word-break: break-word;
`,[c("border",`
 border-radius: inherit;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 transition: border-color .3s var(--n-bezier);
 border: var(--n-border);
 pointer-events: none;
 `),P("closable",[_("alert-body",[c("title",`
 padding-right: 24px;
 `)])]),c("icon",{color:"var(--n-icon-color)"}),_("alert-body",{padding:"var(--n-padding)"},[c("title",{color:"var(--n-title-text-color)"}),c("content",{color:"var(--n-content-text-color)"})]),q({originalTransition:"transform .3s var(--n-bezier)",enterToProps:{transform:"scale(1)"},leaveToProps:{transform:"scale(0.9)"}}),c("icon",`
 position: absolute;
 left: 0;
 top: 0;
 align-items: center;
 justify-content: center;
 display: flex;
 width: var(--n-icon-size);
 height: var(--n-icon-size);
 font-size: var(--n-icon-size);
 margin: var(--n-icon-margin);
 `),c("close",`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 position: absolute;
 right: 0;
 top: 0;
 margin: var(--n-close-margin);
 `),P("show-icon",[_("alert-body",{paddingLeft:"calc(var(--n-icon-margin-left) + var(--n-icon-size) + var(--n-icon-margin-right))"})]),P("right-adjust",[_("alert-body",{paddingRight:"calc(var(--n-close-size) + var(--n-padding) + 2px)"})]),_("alert-body",`
 border-radius: var(--n-border-radius);
 transition: border-color .3s var(--n-bezier);
 `,[c("title",`
 transition: color .3s var(--n-bezier);
 font-size: 16px;
 line-height: 19px;
 font-weight: var(--n-title-font-weight);
 `,[K("& +",[c("content",{marginTop:"9px"})])]),c("content",{transition:"color .3s var(--n-bezier)",fontSize:"var(--n-font-size)"})]),c("icon",{transition:"color .3s var(--n-bezier)"})]),xe=Object.assign(Object.assign({},A.props),{title:String,showIcon:{type:Boolean,default:!0},type:{type:String,default:"default"},bordered:{type:Boolean,default:!0},closable:Boolean,onClose:Function,onAfterLeave:Function,onAfterHide:Function}),Ie=$({name:"Alert",inheritAttrs:!1,props:xe,setup(r){const{mergedClsPrefixRef:e,mergedBorderedRef:i,inlineThemeDisabled:l,mergedRtlRef:n}=Q(r),g=A("Alert","-alert",_e,me,r,e),x=U("Alert",n,e),f=y(()=>{const{common:{cubicBezierEaseInOut:s},self:o}=g.value,{fontSize:I,borderRadius:z,titleFontWeight:S,lineHeight:w,iconSize:H,iconMargin:T,iconMarginRtl:k,closeIconSize:B,closeBorderRadius:W,closeSize:j,closeMargin:L,closeMarginRtl:V,padding:N}=o,{type:u}=r,{left:F,right:M}=X(T);return{"--n-bezier":s,"--n-color":o[h("color",u)],"--n-close-icon-size":B,"--n-close-border-radius":W,"--n-close-color-hover":o[h("closeColorHover",u)],"--n-close-color-pressed":o[h("closeColorPressed",u)],"--n-close-icon-color":o[h("closeIconColor",u)],"--n-close-icon-color-hover":o[h("closeIconColorHover",u)],"--n-close-icon-color-pressed":o[h("closeIconColorPressed",u)],"--n-icon-color":o[h("iconColor",u)],"--n-border":o[h("border",u)],"--n-title-text-color":o[h("titleTextColor",u)],"--n-content-text-color":o[h("contentTextColor",u)],"--n-line-height":w,"--n-border-radius":z,"--n-font-size":I,"--n-title-font-weight":S,"--n-icon-size":H,"--n-icon-margin":T,"--n-icon-margin-rtl":k,"--n-close-size":j,"--n-close-margin":L,"--n-close-margin-rtl":V,"--n-padding":N,"--n-icon-margin-left":F,"--n-icon-margin-right":M}}),t=l?G("alert",y(()=>r.type[0]),f,r):void 0,d=J(!0),C=()=>{const{onAfterLeave:s,onAfterHide:o}=r;s&&s(),o&&o()};return{rtlEnabled:x,mergedClsPrefix:e,mergedBordered:i,visible:d,handleCloseClick:()=>{var s;Promise.resolve((s=r.onClose)===null||s===void 0?void 0:s.call(r)).then(o=>{o!==!1&&(d.value=!1)})},handleAfterLeave:()=>{C()},mergedTheme:g,cssVars:l?void 0:f,themeClass:t==null?void 0:t.themeClass,onRender:t==null?void 0:t.onRender}},render(){var r;return(r=this.onRender)===null||r===void 0||r.call(this),a(ae,{onAfterLeave:this.handleAfterLeave},{default:()=>{const{mergedClsPrefix:e,$slots:i}=this,l={class:[`${e}-alert`,this.themeClass,this.closable&&`${e}-alert--closable`,this.showIcon&&`${e}-alert--show-icon`,!this.title&&this.closable&&`${e}-alert--right-adjust`,this.rtlEnabled&&`${e}-alert--rtl`],style:this.cssVars,role:"alert"};return this.visible?a("div",Object.assign({},Y(this.$attrs,l)),this.closable&&a(Z,{clsPrefix:e,class:`${e}-alert__close`,onClick:this.handleCloseClick}),this.bordered&&a("div",{class:`${e}-alert__border`}),this.showIcon&&a("div",{class:`${e}-alert__icon`,"aria-hidden":"true"},ee(i.icon,()=>[a(oe,{clsPrefix:e},{default:()=>{switch(this.type){case"success":return a(se,null);case"info":return a(te,null);case"warning":return a(ne,null);case"error":return a(re,null);default:return null}}})])),a("div",{class:[`${e}-alert-body`,this.mergedBordered&&`${e}-alert-body--bordered`]},le(i.header,n=>{const g=n||this.title;return g?a("div",{class:`${e}-alert-body__title`},g):null}),i.default&&a("div",{class:`${e}-alert-body__content`},i))):null}})}}),ze=r=>(ve("data-v-693376d1"),r=r(),be(),r),Se={class:"color-warning font-500"},ye=ze(()=>E("span",{class:"color-error font-600"},"物理删除",-1)),we=$({__name:"delete-alert",setup(r){const e=ie(),i=ce(),l=e.name,n=y(()=>l==="job_task"?"删除前请检查待删除定时任务是存在通知配置或者工作流任务；":l==="retry_scene"?"删除前请检查待删除重试场景是存在通知配置或者重试任务；":l==="workflow_task"?"删除前请检查待删除工作流任务是存在通知配置；":l==="notify_recipient"?"删除前请检查通知配置是存在关联通知人；":null),g=y(()=>i.getDeleteAlert(l)!==!1),x=()=>(i.setDeleteAlert(l,!1),!0);return(f,t)=>{const d=Ie;return g.value?(de(),ue(d,{key:0,"show-icon":!1,type:"warning",closable:"",onClose:x},{default:he(()=>[E("div",Se,[R(" 📢 "+ge(n.value)+"该删除为 ",1),ye,R(" ，删除后不可恢复，必要时可以先导出备份 ")])]),_:1})):fe("",!0)}}}),Te=Ce(we,[["__scopeId","data-v-693376d1"]]);export{Te as _};
