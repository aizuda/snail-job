import{bt as F,dT as M,ck as b,dU as u,bd as I,bR as i,be as S,bQ as V,bc as N,d as U,ai as t,c3 as D,cn as J,bO as K,aj as Q,bU as q,bf as G,bg as E,cJ as X,a as $,dV as Y,ca as a,bi as Z,r as oo,by as eo,c6 as ro,c5 as no,c4 as so,c7 as to}from"./index-DrjO7DLg.js";function lo(r){const{lineHeight:o,borderRadius:d,fontWeightStrong:C,baseColor:l,dividerColor:v,actionColor:y,textColor1:g,textColor2:s,closeColorHover:h,closeColorPressed:f,closeIconColor:m,closeIconColorHover:p,closeIconColorPressed:n,infoColor:e,successColor:x,warningColor:z,errorColor:T,fontSize:P}=r;return Object.assign(Object.assign({},M),{fontSize:P,lineHeight:o,titleFontWeight:C,borderRadius:d,border:`1px solid ${v}`,color:y,titleTextColor:g,iconColor:s,contentTextColor:s,closeBorderRadius:d,closeColorHover:h,closeColorPressed:f,closeIconColor:m,closeIconColorHover:p,closeIconColorPressed:n,borderInfo:`1px solid ${b(l,u(e,{alpha:.25}))}`,colorInfo:b(l,u(e,{alpha:.08})),titleTextColorInfo:g,iconColorInfo:e,contentTextColorInfo:s,closeColorHoverInfo:h,closeColorPressedInfo:f,closeIconColorInfo:m,closeIconColorHoverInfo:p,closeIconColorPressedInfo:n,borderSuccess:`1px solid ${b(l,u(x,{alpha:.25}))}`,colorSuccess:b(l,u(x,{alpha:.08})),titleTextColorSuccess:g,iconColorSuccess:x,contentTextColorSuccess:s,closeColorHoverSuccess:h,closeColorPressedSuccess:f,closeIconColorSuccess:m,closeIconColorHoverSuccess:p,closeIconColorPressedSuccess:n,borderWarning:`1px solid ${b(l,u(z,{alpha:.33}))}`,colorWarning:b(l,u(z,{alpha:.08})),titleTextColorWarning:g,iconColorWarning:z,contentTextColorWarning:s,closeColorHoverWarning:h,closeColorPressedWarning:f,closeIconColorWarning:m,closeIconColorHoverWarning:p,closeIconColorPressedWarning:n,borderError:`1px solid ${b(l,u(T,{alpha:.25}))}`,colorError:b(l,u(T,{alpha:.08})),titleTextColorError:g,iconColorError:T,contentTextColorError:s,closeColorHoverError:h,closeColorPressedError:f,closeIconColorError:m,closeIconColorHoverError:p,closeIconColorPressedError:n})}const io={common:F,self:lo},co=I("alert",`
 line-height: var(--n-line-height);
 border-radius: var(--n-border-radius);
 position: relative;
 transition: background-color .3s var(--n-bezier);
 background-color: var(--n-color);
 text-align: start;
 word-break: break-word;
`,[i("border",`
 border-radius: inherit;
 position: absolute;
 left: 0;
 right: 0;
 top: 0;
 bottom: 0;
 transition: border-color .3s var(--n-bezier);
 border: var(--n-border);
 pointer-events: none;
 `),S("closable",[I("alert-body",[i("title",`
 padding-right: 24px;
 `)])]),i("icon",{color:"var(--n-icon-color)"}),I("alert-body",{padding:"var(--n-padding)"},[i("title",{color:"var(--n-title-text-color)"}),i("content",{color:"var(--n-content-text-color)"})]),V({originalTransition:"transform .3s var(--n-bezier)",enterToProps:{transform:"scale(1)"},leaveToProps:{transform:"scale(0.9)"}}),i("icon",`
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
 `),i("close",`
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 position: absolute;
 right: 0;
 top: 0;
 margin: var(--n-close-margin);
 `),S("show-icon",[I("alert-body",{paddingLeft:"calc(var(--n-icon-margin-left) + var(--n-icon-size) + var(--n-icon-margin-right))"})]),S("right-adjust",[I("alert-body",{paddingRight:"calc(var(--n-close-size) + var(--n-padding) + 2px)"})]),I("alert-body",`
 border-radius: var(--n-border-radius);
 transition: border-color .3s var(--n-bezier);
 `,[i("title",`
 transition: color .3s var(--n-bezier);
 font-size: 16px;
 line-height: 19px;
 font-weight: var(--n-title-font-weight);
 `,[N("& +",[i("content",{marginTop:"9px"})])]),i("content",{transition:"color .3s var(--n-bezier)",fontSize:"var(--n-font-size)"})]),i("icon",{transition:"color .3s var(--n-bezier)"})]),ao=Object.assign(Object.assign({},E.props),{title:String,showIcon:{type:Boolean,default:!0},type:{type:String,default:"default"},bordered:{type:Boolean,default:!0},closable:Boolean,onClose:Function,onAfterLeave:Function,onAfterHide:Function}),ho=U({name:"Alert",inheritAttrs:!1,props:ao,slots:Object,setup(r){const{mergedClsPrefixRef:o,mergedBorderedRef:d,inlineThemeDisabled:C,mergedRtlRef:l}=G(r),v=E("Alert","-alert",co,io,r,o),y=X("Alert",l,o),g=$(()=>{const{common:{cubicBezierEaseInOut:n},self:e}=v.value,{fontSize:x,borderRadius:z,titleFontWeight:T,lineHeight:P,iconSize:H,iconMargin:R,iconMarginRtl:_,closeIconSize:W,closeBorderRadius:w,closeSize:A,closeMargin:B,closeMarginRtl:j,padding:k}=e,{type:c}=r,{left:L,right:O}=Y(R);return{"--n-bezier":n,"--n-color":e[a("color",c)],"--n-close-icon-size":W,"--n-close-border-radius":w,"--n-close-color-hover":e[a("closeColorHover",c)],"--n-close-color-pressed":e[a("closeColorPressed",c)],"--n-close-icon-color":e[a("closeIconColor",c)],"--n-close-icon-color-hover":e[a("closeIconColorHover",c)],"--n-close-icon-color-pressed":e[a("closeIconColorPressed",c)],"--n-icon-color":e[a("iconColor",c)],"--n-border":e[a("border",c)],"--n-title-text-color":e[a("titleTextColor",c)],"--n-content-text-color":e[a("contentTextColor",c)],"--n-line-height":P,"--n-border-radius":z,"--n-font-size":x,"--n-title-font-weight":T,"--n-icon-size":H,"--n-icon-margin":R,"--n-icon-margin-rtl":_,"--n-close-size":A,"--n-close-margin":B,"--n-close-margin-rtl":j,"--n-padding":k,"--n-icon-margin-left":L,"--n-icon-margin-right":O}}),s=C?Z("alert",$(()=>r.type[0]),g,r):void 0,h=oo(!0),f=()=>{const{onAfterLeave:n,onAfterHide:e}=r;n&&n(),e&&e()};return{rtlEnabled:y,mergedClsPrefix:o,mergedBordered:d,visible:h,handleCloseClick:()=>{var n;Promise.resolve((n=r.onClose)===null||n===void 0?void 0:n.call(r)).then(e=>{e!==!1&&(h.value=!1)})},handleAfterLeave:()=>{f()},mergedTheme:v,cssVars:C?void 0:g,themeClass:s?.themeClass,onRender:s?.onRender}},render(){var r;return(r=this.onRender)===null||r===void 0||r.call(this),t(q,{onAfterLeave:this.handleAfterLeave},{default:()=>{const{mergedClsPrefix:o,$slots:d}=this,C={class:[`${o}-alert`,this.themeClass,this.closable&&`${o}-alert--closable`,this.showIcon&&`${o}-alert--show-icon`,!this.title&&this.closable&&`${o}-alert--right-adjust`,this.rtlEnabled&&`${o}-alert--rtl`],style:this.cssVars,role:"alert"};return this.visible?t("div",Object.assign({},D(this.$attrs,C)),this.closable&&t(J,{clsPrefix:o,class:`${o}-alert__close`,onClick:this.handleCloseClick}),this.bordered&&t("div",{class:`${o}-alert__border`}),this.showIcon&&t("div",{class:`${o}-alert__icon`,"aria-hidden":"true"},K(d.icon,()=>[t(eo,{clsPrefix:o},{default:()=>{switch(this.type){case"success":return t(to,null);case"info":return t(so,null);case"warning":return t(no,null);case"error":return t(ro,null);default:return null}}})])),t("div",{class:[`${o}-alert-body`,this.mergedBordered&&`${o}-alert-body--bordered`]},Q(d.header,l=>{const v=l||this.title;return v?t("div",{class:`${o}-alert-body__title`},v):null}),d.default&&t("div",{class:`${o}-alert-body__content`},d))):null}})}});export{ho as _};
