import{b4 as V,dR as D,bH as b,dS as v,b5 as x,bv as c,bq as w,dE as q,bp as K,d as H,aj as i,dg as Q,bN as G,cp as J,ak as U,cF as X,b8 as Y,bb as $,cI as Z,a as S,dT as ee,bw as g,be as oe,r as re,b6 as ne,dN as te,dM as se,dL as le,dO as ie,k as ae,y as ce,c as de,Q as ue,o as ge,w as he,e as R,g as E,t as fe,l as be}from"./index-mUIrqFZO.js";function ve(n){const{lineHeight:e,borderRadius:a,fontWeightStrong:l,baseColor:t,dividerColor:h,actionColor:I,textColor1:f,textColor2:r,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,infoColor:o,successColor:_,warningColor:z,errorColor:y,fontSize:T}=n;return Object.assign(Object.assign({},D),{fontSize:T,lineHeight:e,titleFontWeight:l,borderRadius:a,border:`1px solid ${h}`,color:I,titleTextColor:f,iconColor:r,contentTextColor:r,closeBorderRadius:a,closeColorHover:d,closeColorPressed:C,closeIconColor:p,closeIconColorHover:m,closeIconColorPressed:s,borderInfo:`1px solid ${b(t,v(o,{alpha:.25}))}`,colorInfo:b(t,v(o,{alpha:.08})),titleTextColorInfo:f,iconColorInfo:o,contentTextColorInfo:r,closeColorHoverInfo:d,closeColorPressedInfo:C,closeIconColorInfo:p,closeIconColorHoverInfo:m,closeIconColorPressedInfo:s,borderSuccess:`1px solid ${b(t,v(_,{alpha:.25}))}`,colorSuccess:b(t,v(_,{alpha:.08})),titleTextColorSuccess:f,iconColorSuccess:_,contentTextColorSuccess:r,closeColorHoverSuccess:d,closeColorPressedSuccess:C,closeIconColorSuccess:p,closeIconColorHoverSuccess:m,closeIconColorPressedSuccess:s,borderWarning:`1px solid ${b(t,v(z,{alpha:.33}))}`,colorWarning:b(t,v(z,{alpha:.08})),titleTextColorWarning:f,iconColorWarning:z,contentTextColorWarning:r,closeColorHoverWarning:d,closeColorPressedWarning:C,closeIconColorWarning:p,closeIconColorHoverWarning:m,closeIconColorPressedWarning:s,borderError:`1px solid ${b(t,v(y,{alpha:.25}))}`,colorError:b(t,v(y,{alpha:.08})),titleTextColorError:f,iconColorError:y,contentTextColorError:r,closeColorHoverError:d,closeColorPressedError:C,closeIconColorError:p,closeIconColorHoverError:m,closeIconColorPressedError:s})}const Ce={common:V,self:ve},pe=x("alert",`
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
 `,[K("& +",[c("content",{marginTop:"9px"})])]),c("content",{transition:"color .3s var(--n-bezier)",fontSize:"var(--n-font-size)"})]),c("icon",{transition:"color .3s var(--n-bezier)"})]),me=Object.assign(Object.assign({},$.props),{title:String,showIcon:{type:Boolean,default:!0},type:{type:String,default:"default"},bordered:{type:Boolean,default:!0},closable:Boolean,onClose:Function,onAfterLeave:Function,onAfterHide:Function}),xe=H({name:"Alert",inheritAttrs:!1,props:me,slots:Object,setup(n){const{mergedClsPrefixRef:e,mergedBorderedRef:a,inlineThemeDisabled:l,mergedRtlRef:t}=Y(n),h=$("Alert","-alert",pe,Ce,n,e),I=Z("Alert",t,e),f=S(()=>{const{common:{cubicBezierEaseInOut:s},self:o}=h.value,{fontSize:_,borderRadius:z,titleFontWeight:y,lineHeight:T,iconSize:k,iconMargin:P,iconMarginRtl:A,closeIconSize:B,closeBorderRadius:W,closeSize:j,closeMargin:L,closeMarginRtl:N,padding:O}=o,{type:u}=n,{left:F,right:M}=ee(P);return{"--n-bezier":s,"--n-color":o[g("color",u)],"--n-close-icon-size":B,"--n-close-border-radius":W,"--n-close-color-hover":o[g("closeColorHover",u)],"--n-close-color-pressed":o[g("closeColorPressed",u)],"--n-close-icon-color":o[g("closeIconColor",u)],"--n-close-icon-color-hover":o[g("closeIconColorHover",u)],"--n-close-icon-color-pressed":o[g("closeIconColorPressed",u)],"--n-icon-color":o[g("iconColor",u)],"--n-border":o[g("border",u)],"--n-title-text-color":o[g("titleTextColor",u)],"--n-content-text-color":o[g("contentTextColor",u)],"--n-line-height":T,"--n-border-radius":z,"--n-font-size":_,"--n-title-font-weight":y,"--n-icon-size":k,"--n-icon-margin":P,"--n-icon-margin-rtl":A,"--n-close-size":j,"--n-close-margin":L,"--n-close-margin-rtl":N,"--n-padding":O,"--n-icon-margin-left":F,"--n-icon-margin-right":M}}),r=l?oe("alert",S(()=>n.type[0]),f,n):void 0,d=re(!0),C=()=>{const{onAfterLeave:s,onAfterHide:o}=n;s&&s(),o&&o()};return{rtlEnabled:I,mergedClsPrefix:e,mergedBordered:a,visible:d,handleCloseClick:()=>{var s;Promise.resolve((s=n.onClose)===null||s===void 0?void 0:s.call(n)).then(o=>{o!==!1&&(d.value=!1)})},handleAfterLeave:()=>{C()},mergedTheme:h,cssVars:l?void 0:f,themeClass:r==null?void 0:r.themeClass,onRender:r==null?void 0:r.onRender}},render(){var n;return(n=this.onRender)===null||n===void 0||n.call(this),i(X,{onAfterLeave:this.handleAfterLeave},{default:()=>{const{mergedClsPrefix:e,$slots:a}=this,l={class:[`${e}-alert`,this.themeClass,this.closable&&`${e}-alert--closable`,this.showIcon&&`${e}-alert--show-icon`,!this.title&&this.closable&&`${e}-alert--right-adjust`,this.rtlEnabled&&`${e}-alert--rtl`],style:this.cssVars,role:"alert"};return this.visible?i("div",Object.assign({},Q(this.$attrs,l)),this.closable&&i(G,{clsPrefix:e,class:`${e}-alert__close`,onClick:this.handleCloseClick}),this.bordered&&i("div",{class:`${e}-alert__border`}),this.showIcon&&i("div",{class:`${e}-alert__icon`,"aria-hidden":"true"},J(a.icon,()=>[i(ne,{clsPrefix:e},{default:()=>{switch(this.type){case"success":return i(ie,null);case"info":return i(le,null);case"warning":return i(se,null);case"error":return i(te,null);default:return null}}})])),i("div",{class:[`${e}-alert-body`,this.mergedBordered&&`${e}-alert-body--bordered`]},U(a.header,t=>{const h=t||this.title;return h?i("div",{class:`${e}-alert-body__title`},h):null}),a.default&&i("div",{class:`${e}-alert-body__content`},a))):null}})}}),Ie={class:"color-warning font-500"},_e=H({__name:"delete-alert",setup(n){const e=ae(),a=ce(),l=e.name,t=S(()=>l==="job_task"?"删除前请检查待删除定时任务是存在通知配置或者工作流任务；":l==="retry_scene"?"删除前请检查待删除重试场景是存在通知配置或者重试任务；":l==="workflow_task"?"删除前请检查待删除工作流任务是存在通知配置；":l==="notify_recipient"?"删除前请检查通知配置是存在关联通知人；":null),h=S(()=>a.getDeleteAlert(l)!==!1),I=()=>(a.setDeleteAlert(l,!1),!0);return(f,r)=>{const d=xe;return h.value?(ge(),de(d,{key:0,"show-icon":!1,type:"warning",closable:"",onClose:I},{default:he(()=>[R("div",Ie,[E(" 📢 "+fe(t.value)+"该删除为 ",1),r[0]||(r[0]=R("span",{class:"color-error font-600"},"物理删除",-1)),r[1]||(r[1]=E(" ，删除后不可恢复，必要时可以先导出备份 "))])]),_:1})):ue("",!0)}}}),ye=be(_e,[["__scopeId","data-v-693376d1"]]);export{ye as _};
