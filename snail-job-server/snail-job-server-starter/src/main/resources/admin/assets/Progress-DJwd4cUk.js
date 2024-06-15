import{az as P,aA as o,aB as b,d as z,a as x,dI as C,ap as r,aU as W,dJ as q,dK as O,dL as T,dM as A,aF as M,aI as L,dN as _,b7 as I,aP as X}from"./index-BztLELzb.js";const j=P([o("progress",{display:"inline-block"},[o("progress-icon",`
 color: var(--n-icon-color);
 transition: color .3s var(--n-bezier);
 `),b("line",`
 width: 100%;
 display: block;
 `,[o("progress-content",`
 display: flex;
 align-items: center;
 `,[o("progress-graph",{flex:1})]),o("progress-custom-content",{marginLeft:"14px"}),o("progress-icon",`
 width: 30px;
 padding-left: 14px;
 height: var(--n-icon-size-line);
 line-height: var(--n-icon-size-line);
 font-size: var(--n-icon-size-line);
 `,[b("as-text",`
 color: var(--n-text-color-line-outer);
 text-align: center;
 width: 40px;
 font-size: var(--n-font-size);
 padding-left: 4px;
 transition: color .3s var(--n-bezier);
 `)])]),b("circle, dashboard",{width:"120px"},[o("progress-custom-content",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 justify-content: center;
 `),o("progress-text",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 color: inherit;
 font-size: var(--n-font-size-circle);
 color: var(--n-text-color-circle);
 font-weight: var(--n-font-weight-circle);
 transition: color .3s var(--n-bezier);
 white-space: nowrap;
 `),o("progress-icon",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 color: var(--n-icon-color);
 font-size: var(--n-icon-size-circle);
 `)]),b("multiple-circle",`
 width: 200px;
 color: inherit;
 `,[o("progress-text",`
 font-weight: var(--n-font-weight-circle);
 color: var(--n-text-color-circle);
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 justify-content: center;
 transition: color .3s var(--n-bezier);
 `)]),o("progress-content",{position:"relative"}),o("progress-graph",{position:"relative"},[o("progress-graph-circle",[P("svg",{verticalAlign:"bottom"}),o("progress-graph-circle-fill",`
 stroke: var(--n-fill-color);
 transition:
 opacity .3s var(--n-bezier),
 stroke .3s var(--n-bezier),
 stroke-dasharray .3s var(--n-bezier);
 `,[b("empty",{opacity:0})]),o("progress-graph-circle-rail",`
 transition: stroke .3s var(--n-bezier);
 overflow: hidden;
 stroke: var(--n-rail-color);
 `)]),o("progress-graph-line",[b("indicator-inside",[o("progress-graph-line-rail",`
 height: 16px;
 line-height: 16px;
 border-radius: 10px;
 `,[o("progress-graph-line-fill",`
 height: inherit;
 border-radius: 10px;
 `),o("progress-graph-line-indicator",`
 background: #0000;
 white-space: nowrap;
 text-align: right;
 margin-left: 14px;
 margin-right: 14px;
 height: inherit;
 font-size: 12px;
 color: var(--n-text-color-line-inner);
 transition: color .3s var(--n-bezier);
 `)])]),b("indicator-inside-label",`
 height: 16px;
 display: flex;
 align-items: center;
 `,[o("progress-graph-line-rail",`
 flex: 1;
 transition: background-color .3s var(--n-bezier);
 `),o("progress-graph-line-indicator",`
 background: var(--n-fill-color);
 font-size: 12px;
 transform: translateZ(0);
 display: flex;
 vertical-align: middle;
 height: 16px;
 line-height: 16px;
 padding: 0 10px;
 border-radius: 10px;
 position: absolute;
 white-space: nowrap;
 color: var(--n-text-color-line-inner);
 transition:
 right .2s var(--n-bezier),
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier);
 `)]),o("progress-graph-line-rail",`
 position: relative;
 overflow: hidden;
 height: var(--n-rail-height);
 border-radius: 5px;
 background-color: var(--n-rail-color);
 transition: background-color .3s var(--n-bezier);
 `,[o("progress-graph-line-fill",`
 background: var(--n-fill-color);
 position: relative;
 border-radius: 5px;
 height: inherit;
 width: 100%;
 max-width: 0%;
 transition:
 background-color .3s var(--n-bezier),
 max-width .2s var(--n-bezier);
 `,[b("processing",[P("&::after",`
 content: "";
 background-image: var(--n-line-bg-processing);
 animation: progress-processing-animation 2s var(--n-bezier) infinite;
 `)])])])])])]),P("@keyframes progress-processing-animation",`
 0% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 100%;
 opacity: 1;
 }
 66% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 0;
 opacity: 0;
 }
 100% {
 position: absolute;
 left: 0;
 top: 0;
 bottom: 0;
 right: 0;
 opacity: 0;
 }
 `)]),G={success:r(q,null),error:r(O,null),warning:r(T,null),info:r(A,null)},Y=z({name:"ProgressLine",props:{clsPrefix:{type:String,required:!0},percentage:{type:Number,default:0},railColor:String,railStyle:[String,Object],fillColor:String,status:{type:String,required:!0},indicatorPlacement:{type:String,required:!0},indicatorTextColor:String,unit:{type:String,default:"%"},processing:{type:Boolean,required:!0},showIndicator:{type:Boolean,required:!0},height:[String,Number],railBorderRadius:[String,Number],fillBorderRadius:[String,Number]},setup(e,{slots:p}){const u=x(()=>C(e.height)),a=x(()=>e.railBorderRadius!==void 0?C(e.railBorderRadius):e.height!==void 0?C(e.height,{c:.5}):""),i=x(()=>e.fillBorderRadius!==void 0?C(e.fillBorderRadius):e.railBorderRadius!==void 0?C(e.railBorderRadius):e.height!==void 0?C(e.height,{c:.5}):"");return()=>{const{indicatorPlacement:n,railColor:h,railStyle:l,percentage:d,unit:c,indicatorTextColor:f,status:g,showIndicator:y,fillColor:t,processing:v,clsPrefix:s}=e;return r("div",{class:`${s}-progress-content`,role:"none"},r("div",{class:`${s}-progress-graph`,"aria-hidden":!0},r("div",{class:[`${s}-progress-graph-line`,{[`${s}-progress-graph-line--indicator-${n}`]:!0}]},r("div",{class:`${s}-progress-graph-line-rail`,style:[{backgroundColor:h,height:u.value,borderRadius:a.value},l]},r("div",{class:[`${s}-progress-graph-line-fill`,v&&`${s}-progress-graph-line-fill--processing`],style:{maxWidth:`${e.percentage}%`,backgroundColor:t,height:u.value,lineHeight:u.value,borderRadius:i.value}},n==="inside"?r("div",{class:`${s}-progress-graph-line-indicator`,style:{color:f}},p.default?p.default():`${d}${c}`):null)))),y&&n==="outside"?r("div",null,p.default?r("div",{class:`${s}-progress-custom-content`,style:{color:f},role:"none"},p.default()):g==="default"?r("div",{role:"none",class:`${s}-progress-icon ${s}-progress-icon--as-text`,style:{color:f}},d,c):r("div",{class:`${s}-progress-icon`,"aria-hidden":!0},r(W,{clsPrefix:s},{default:()=>G[g]}))):null)}}}),H={success:r(q,null),error:r(O,null),warning:r(T,null),info:r(A,null)},V=z({name:"ProgressCircle",props:{clsPrefix:{type:String,required:!0},status:{type:String,required:!0},strokeWidth:{type:Number,required:!0},fillColor:String,railColor:String,railStyle:[String,Object],percentage:{type:Number,default:0},offsetDegree:{type:Number,default:0},showIndicator:{type:Boolean,required:!0},indicatorTextColor:String,unit:String,viewBoxWidth:{type:Number,required:!0},gapDegree:{type:Number,required:!0},gapOffsetDegree:{type:Number,default:0}},setup(e,{slots:p}){function u(a,i,n){const{gapDegree:h,viewBoxWidth:l,strokeWidth:d}=e,c=50,f=0,g=c,y=0,t=2*c,v=50+d/2,s=`M ${v},${v} m ${f},${g}
      a ${c},${c} 0 1 1 ${y},${-t}
      a ${c},${c} 0 1 1 ${-y},${t}`,$=Math.PI*2*c,S={stroke:n,strokeDasharray:`${a/100*($-h)}px ${l*8}px`,strokeDashoffset:`-${h/2}px`,transformOrigin:i?"center":void 0,transform:i?`rotate(${i}deg)`:void 0};return{pathString:s,pathStyle:S}}return()=>{const{fillColor:a,railColor:i,strokeWidth:n,offsetDegree:h,status:l,percentage:d,showIndicator:c,indicatorTextColor:f,unit:g,gapOffsetDegree:y,clsPrefix:t}=e,{pathString:v,pathStyle:s}=u(100,0,i),{pathString:$,pathStyle:S}=u(d,h,a),m=100+n;return r("div",{class:`${t}-progress-content`,role:"none"},r("div",{class:`${t}-progress-graph`,"aria-hidden":!0},r("div",{class:`${t}-progress-graph-circle`,style:{transform:y?`rotate(${y}deg)`:void 0}},r("svg",{viewBox:`0 0 ${m} ${m}`},r("g",null,r("path",{class:`${t}-progress-graph-circle-rail`,d:v,"stroke-width":n,"stroke-linecap":"round",fill:"none",style:s})),r("g",null,r("path",{class:[`${t}-progress-graph-circle-fill`,d===0&&`${t}-progress-graph-circle-fill--empty`],d:$,"stroke-width":n,"stroke-linecap":"round",fill:"none",style:S}))))),c?r("div",null,p.default?r("div",{class:`${t}-progress-custom-content`,role:"none"},p.default()):l!=="default"?r("div",{class:`${t}-progress-icon`,"aria-hidden":!0},r(W,{clsPrefix:t},{default:()=>H[l]})):r("div",{class:`${t}-progress-text`,style:{color:f},role:"none"},r("span",{class:`${t}-progress-text__percentage`},d),r("span",{class:`${t}-progress-text__unit`},g))):null)}}});function N(e,p,u=100){return`m ${u/2} ${u/2-e} a ${e} ${e} 0 1 1 0 ${2*e} a ${e} ${e} 0 1 1 0 -${2*e}`}const E=z({name:"ProgressMultipleCircle",props:{clsPrefix:{type:String,required:!0},viewBoxWidth:{type:Number,required:!0},percentage:{type:Array,default:[0]},strokeWidth:{type:Number,required:!0},circleGap:{type:Number,required:!0},showIndicator:{type:Boolean,required:!0},fillColor:{type:Array,default:()=>[]},railColor:{type:Array,default:()=>[]},railStyle:{type:Array,default:()=>[]}},setup(e,{slots:p}){const u=x(()=>e.percentage.map((i,n)=>`${Math.PI*i/100*(e.viewBoxWidth/2-e.strokeWidth/2*(1+2*n)-e.circleGap*n)*2}, ${e.viewBoxWidth*8}`));return()=>{const{viewBoxWidth:a,strokeWidth:i,circleGap:n,showIndicator:h,fillColor:l,railColor:d,railStyle:c,percentage:f,clsPrefix:g}=e;return r("div",{class:`${g}-progress-content`,role:"none"},r("div",{class:`${g}-progress-graph`,"aria-hidden":!0},r("div",{class:`${g}-progress-graph-circle`},r("svg",{viewBox:`0 0 ${a} ${a}`},f.map((y,t)=>r("g",{key:t},r("path",{class:`${g}-progress-graph-circle-rail`,d:N(a/2-i/2*(1+2*t)-n*t,i,a),"stroke-width":i,"stroke-linecap":"round",fill:"none",style:[{strokeDashoffset:0,stroke:d[t]},c[t]]}),r("path",{class:[`${g}-progress-graph-circle-fill`,y===0&&`${g}-progress-graph-circle-fill--empty`],d:N(a/2-i/2*(1+2*t)-n*t,i,a),"stroke-width":i,"stroke-linecap":"round",fill:"none",style:{strokeDasharray:u.value[t],strokeDashoffset:0,stroke:l[t]}})))))),h&&p.default?r("div",null,r("div",{class:`${g}-progress-text`},p.default())):null)}}}),F=Object.assign(Object.assign({},L.props),{processing:Boolean,type:{type:String,default:"line"},gapDegree:Number,gapOffsetDegree:Number,status:{type:String,default:"default"},railColor:[String,Array],railStyle:[String,Array],color:[String,Array],viewBoxWidth:{type:Number,default:100},strokeWidth:{type:Number,default:7},percentage:[Number,Array],unit:{type:String,default:"%"},showIndicator:{type:Boolean,default:!0},indicatorPosition:{type:String,default:"outside"},indicatorPlacement:{type:String,default:"outside"},indicatorTextColor:String,circleGap:{type:Number,default:1},height:Number,borderRadius:[String,Number],fillBorderRadius:[String,Number],offsetDegree:Number}),J=z({name:"Progress",props:F,setup(e){const p=x(()=>e.indicatorPlacement||e.indicatorPosition),u=x(()=>{if(e.gapDegree||e.gapDegree===0)return e.gapDegree;if(e.type==="dashboard")return 75}),{mergedClsPrefixRef:a,inlineThemeDisabled:i}=M(e),n=L("Progress","-progress",j,_,e,a),h=x(()=>{const{status:d}=e,{common:{cubicBezierEaseInOut:c},self:{fontSize:f,fontSizeCircle:g,railColor:y,railHeight:t,iconSizeCircle:v,iconSizeLine:s,textColorCircle:$,textColorLineInner:S,textColorLineOuter:m,lineBgProcessing:k,fontWeightCircle:B,[I("iconColor",d)]:R,[I("fillColor",d)]:w}}=n.value;return{"--n-bezier":c,"--n-fill-color":w,"--n-font-size":f,"--n-font-size-circle":g,"--n-font-weight-circle":B,"--n-icon-color":R,"--n-icon-size-circle":v,"--n-icon-size-line":s,"--n-line-bg-processing":k,"--n-rail-color":y,"--n-rail-height":t,"--n-text-color-circle":$,"--n-text-color-line-inner":S,"--n-text-color-line-outer":m}}),l=i?X("progress",x(()=>e.status[0]),h,e):void 0;return{mergedClsPrefix:a,mergedIndicatorPlacement:p,gapDeg:u,cssVars:i?void 0:h,themeClass:l==null?void 0:l.themeClass,onRender:l==null?void 0:l.onRender}},render(){const{type:e,cssVars:p,indicatorTextColor:u,showIndicator:a,status:i,railColor:n,railStyle:h,color:l,percentage:d,viewBoxWidth:c,strokeWidth:f,mergedIndicatorPlacement:g,unit:y,borderRadius:t,fillBorderRadius:v,height:s,processing:$,circleGap:S,mergedClsPrefix:m,gapDeg:k,gapOffsetDegree:B,themeClass:R,$slots:w,onRender:D}=this;return D==null||D(),r("div",{class:[R,`${m}-progress`,`${m}-progress--${e}`,`${m}-progress--${i}`],style:p,"aria-valuemax":100,"aria-valuemin":0,"aria-valuenow":d,role:e==="circle"||e==="line"||e==="dashboard"?"progressbar":"none"},e==="circle"||e==="dashboard"?r(V,{clsPrefix:m,status:i,showIndicator:a,indicatorTextColor:u,railColor:n,fillColor:l,railStyle:h,offsetDegree:this.offsetDegree,percentage:d,viewBoxWidth:c,strokeWidth:f,gapDegree:k===void 0?e==="dashboard"?75:0:k,gapOffsetDegree:B,unit:y},w):e==="line"?r(Y,{clsPrefix:m,status:i,showIndicator:a,indicatorTextColor:u,railColor:n,fillColor:l,railStyle:h,percentage:d,processing:$,indicatorPlacement:g,unit:y,fillBorderRadius:v,railBorderRadius:t,height:s},w):e==="multiple-circle"?r(E,{clsPrefix:m,strokeWidth:f,railColor:n,fillColor:l,railStyle:h,viewBoxWidth:c,percentage:d,showIndicator:a,circleGap:S},w):null)}});export{J as _};
