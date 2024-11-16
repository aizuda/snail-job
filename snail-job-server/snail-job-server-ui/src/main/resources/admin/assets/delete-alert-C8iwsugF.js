import{a_ as O,dX as D,bC as v,dY as b,b3 as x,bu as c,bp as w,dq as q,bo as G,b7 as A,d as E,b4 as K,cF as U,a as S,dZ as X,bv as g,ba as Y,r as Z,al as i,de as J,bG as Q,cq as ee,bb as oe,dT as re,dU as ne,dV as te,dS as se,am as le,cC as ie,k as ae,y as ce,o as de,c as ue,w as ge,e as R,g as $,t as he,ai as fe,l as ve}from"./index--PhRNylI.js";function be(n){const{lineHeight:e,borderRadius:a,fontWeightStrong:l,baseColor:t,dividerColor:h,actionColor:I,textColor1:f,textColor2:r,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,infoColor:o,successColor:_,warningColor:z,errorColor:y,fontSize:T}=n;return Object.assign(Object.assign({},D),{fontSize:T,lineHeight:e,titleFontWeight:l,borderRadius:a,border:`1px solid ${h}`,color:I,titleTextColor:f,iconColor:r,contentTextColor:r,closeBorderRadius:a,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,borderInfo:`1px solid ${v(t,b(o,{alpha:.25}))}`,colorInfo:v(t,b(o,{alpha:.08})),titleTextColorInfo:f,iconColorInfo:o,contentTextColorInfo:r,closeColorHoverInfo:d,closeColorPressedInfo:C,closeIconColorInfo:p,closeIconColorHoverInfo:m,closeIconColorPressedInfo:s,borderSuccess:`1px solid ${v(t,b(_,{alpha:.25}))}`,colorSuccess:v(t,b(_,{alpha:.08})),titleTextColorSuccess:f,iconColorSuccess:_,contentTextColorSuccess:r,closeColorHoverSuccess:d,closeColorPressedSuccess:C,closeIconColorSuccess:p,closeIconColorHoverSuccess:m,closeIconColorPressedSuccess:s,borderWarning:`1px solid ${v(t,b(z,{alpha:.33}))}`,colorWarning:v(t,b(z,{alpha:.08})),titleTextColorWarning:f,iconColorWarning:z,contentTextColorWarning:r,closeColorHoverWarning:d,closeColorPressedWarning:C,closeIconColorWarning:p,closeIconColorHoverWarning:m,closeIconColorPressedWarning:s,borderError:`1px solid ${v(t,b(y,{alpha:.25}))}`,colorError:v(t,b(y,{alpha:.08})),titleTextColorError:f,iconColorError:y,contentTextColorError:r,closeColorHoverError:d,closeColorPressedError:C,closeIconColorError:p,closeIconColorHoverError:m,closeIconColorPressedError:s})}const Ce={name:"Alert",common:O,self:be},pe=x("alert",`
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
 `),w("closable",[x("alert-body",[c("title",`
 padding-right: 24px;
 `)])]),c("icon",{color:"var(--n-icon-color)"}),x("alert-body",{padding:"var(--n-padding)"},[c("title",{color:"var(--n-title-text-color)"}),c("content",{color:"var(--n-content-text-color)"})]),q({originalTransition:"transform .3s var(--n-bezier)",enterToProps:{transform:"scale(1)"},leaveToProps:{transform:"scale(0.9)"}}),c("icon",`
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
 `),w("show-icon",[x("alert-body",{paddingLeft:"calc(var(--n-icon-margin-left) + var(--n-icon-size) + var(--n-icon-margin-right))"})]),w("right-adjust",[x("alert-body",{paddingRight:"calc(var(--n-close-size) + var(--n-padding) + 2px)"})]),x("alert-body",`
 border-radius: var(--n-border-radius);
 transition: border-color .3s var(--n-bezier);
 `,[c("title",`
 transition: color .3s var(--n-bezier);
 font-size: 16px;
 line-height: 19px;
 font-weight: var(--n-title-font-weight);
 `,[G("& +",[c("content",{marginTop:"9px"})])]),c("content",{transition:"color .3s var(--n-bezier)",fontSize:"var(--n-font-size)"})]),c("icon",{transition:"color .3s var(--n-bezier)"})]),me=Object.assign(Object.assign({},A.props),{title:String,showIcon:{type:Boolean,default:!0},type:{type:String,default:"default"},bordered:{type:Boolean,default:!0},closable:Boolean,onClose:Function,onAfterLeave:Function,onAfterHide:Function}),xe=E({name:"Alert",inheritAttrs:!1,props:me,setup(n){const{mergedClsPrefixRef:e,mergedBorderedRef:a,inlineThemeDisabled:l,mergedRtlRef:t}=K(n),h=A("Alert","-alert",pe,Ce,n,e),I=U("Alert",t,e),f=S(()=>{const{common:{cubicBezierEaseInOut:s},self:o}=h.value,{fontSize:_,borderRadius:z,titleFontWeight:y,lineHeight:T,iconSize:H,iconMargin:P,iconMarginRtl:k,closeIconSize:B,closeBorderRadius:W,closeSize:j,closeMargin:L,closeMarginRtl:V,padding:F}=o,{type:u}=n,{left:N,right:M}=X(P);return{"--n-bezier":s,"--n-color":o[g("color",u)],"--n-close-icon-size":B,"--n-close-border-radius":W,"--n-close-color-hover":o[g("closeColorHover",u)],"--n-close-color-pressed":o[g("closeColorPressed",u)],"--n-close-icon-color":o[g("closeIconColor",u)],"--n-close-icon-color-hover":o[g("closeIconColorHover",u)],"--n-close-icon-color-pressed":o[g("closeIconColorPressed",u)],"--n-icon-color":o[g("iconColor",u)],"--n-border":o[g("border",u)],"--n-title-text-color":o[g("titleTextColor",u)],"--n-content-text-color":o[g("contentTextColor",u)],"--n-line-height":T,"--n-border-radius":z,"--n-font-size":_,"--n-title-font-weight":y,"--n-icon-size":H,"--n-icon-margin":P,"--n-icon-margin-rtl":k,"--n-close-size":j,"--n-close-margin":L,"--n-close-margin-rtl":V,"--n-padding":F,"--n-icon-margin-left":N,"--n-icon-margin-right":M}}),r=l?Y("alert",S(()=>n.type[0]),f,n):void 0,d=Z(!0),C=()=>{const{onAfterLeave:s,onAfterHide:o}=n;s&&s(),o&&o()};return{rtlEnabled:I,mergedClsPrefix:e,mergedBordered:a,visible:d,handleCloseClick:()=>{var s;Promise.resolve((s=n.onClose)===null||s===void 0?void 0:s.call(n)).then(o=>{o!==!1&&(d.value=!1)})},handleAfterLeave:()=>{C()},mergedTheme:h,cssVars:l?void 0:f,themeClass:r==null?void 0:r.themeClass,onRender:r==null?void 0:r.onRender}},render(){var n;return(n=this.onRender)===null||n===void 0||n.call(this),i(ie,{onAfterLeave:this.handleAfterLeave},{default:()=>{const{mergedClsPrefix:e,$slots:a}=this,l={class:[`${e}-alert`,this.themeClass,this.closable&&`${e}-alert--closable`,this.showIcon&&`${e}-alert--show-icon`,!this.title&&this.closable&&`${e}-alert--right-adjust`,this.rtlEnabled&&`${e}-alert--rtl`],style:this.cssVars,role:"alert"};return this.visible?i("div",Object.assign({},J(this.$attrs,l)),this.closable&&i(Q,{clsPrefix:e,class:`${e}-alert__close`,onClick:this.handleCloseClick}),this.bordered&&i("div",{class:`${e}-alert__border`}),this.showIcon&&i("div",{class:`${e}-alert__icon`,"aria-hidden":"true"},ee(a.icon,()=>[i(oe,{clsPrefix:e},{default:()=>{switch(this.type){case"success":return i(se,null);case"info":return i(te,null);case"warning":return i(ne,null);case"error":return i(re,null);default:return null}}})])),i("div",{class:[`${e}-alert-body`,this.mergedBordered&&`${e}-alert-body--bordered`]},le(a.header,t=>{const h=t||this.title;return h?i("div",{class:`${e}-alert-body__title`},h):null}),a.default&&i("div",{class:`${e}-alert-body__content`},a))):null}})}}),Ie={class:"color-warning font-500"},_e=E({__name:"delete-alert",setup(n){const e=ae(),a=ce(),l=e.name,t=S(()=>l==="job_task"?"删除前请检查待删除定时任务是存在通知配置或者工作流任务；":l==="retry_scene"?"删除前请检查待删除重试场景是存在通知配置或者重试任务；":l==="workflow_task"?"删除前请检查待删除工作流任务是存在通知配置；":l==="notify_recipient"?"删除前请检查通知配置是存在关联通知人；":null),h=S(()=>a.getDeleteAlert(l)!==!1),I=()=>(a.setDeleteAlert(l,!1),!0);return(f,r)=>{const d=xe;return h.value?(de(),ue(d,{key:0,"show-icon":!1,type:"warning",closable:"",onClose:I},{default:ge(()=>[R("div",Ie,[$(" 📢 "+he(t.value)+"该删除为 ",1),r[0]||(r[0]=R("span",{class:"color-error font-600"},"物理删除",-1)),r[1]||(r[1]=$(" ，删除后不可恢复，必要时可以先导出备份 "))])]),_:1})):fe("",!0)}}}),ye=ve(_e,[["__scopeId","data-v-693376d1"]]);export{ye as _};
