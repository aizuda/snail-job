import{d as z,aj as r,bz as W,dQ as j,dR as q,dS as O,dT as T,a as x,dU as w,b8 as P,b9 as s,ba as C,bb as _,bc as G,dV as A,bM as N,be as M}from"./index-C0p55rrf.js";const L={success:r(T,null),error:r(O,null),warning:r(q,null),info:r(j,null)},X=z({name:"ProgressCircle",props:{clsPrefix:{type:String,required:!0},status:{type:String,required:!0},strokeWidth:{type:Number,required:!0},fillColor:[String,Object],railColor:String,railStyle:[String,Object],percentage:{type:Number,default:0},offsetDegree:{type:Number,default:0},showIndicator:{type:Boolean,required:!0},indicatorTextColor:String,unit:String,viewBoxWidth:{type:Number,required:!0},gapDegree:{type:Number,required:!0},gapOffsetDegree:{type:Number,default:0}},setup(e,{slots:p}){function u(n,o,t,l){const{gapDegree:c,viewBoxWidth:f,strokeWidth:y}=e,d=50,g=0,h=d,i=0,a=2*d,$=50+y/2,S=`M ${$},${$} m ${g},${h}
      a ${d},${d} 0 1 1 ${i},-100
      a ${d},${d} 0 1 1 0,${a}`,v=Math.PI*2*d,m={stroke:l==="rail"?t:typeof e.fillColor=="object"?"url(#gradient)":t,strokeDasharray:`${n/100*(v-c)}px ${f*8}px`,strokeDashoffset:`-${c/2}px`,transformOrigin:o?"center":void 0,transform:o?`rotate(${o}deg)`:void 0};return{pathString:S,pathStyle:m}}const b=()=>{const n=typeof e.fillColor=="object",o=n?e.fillColor.stops[0]:"",t=n?e.fillColor.stops[1]:"";return n&&r("defs",null,r("linearGradient",{id:"gradient",x1:"0%",y1:"100%",x2:"100%",y2:"0%"},r("stop",{offset:"0%","stop-color":o}),r("stop",{offset:"100%","stop-color":t})))};return()=>{const{fillColor:n,railColor:o,strokeWidth:t,offsetDegree:l,status:c,percentage:f,showIndicator:y,indicatorTextColor:d,unit:g,gapOffsetDegree:h,clsPrefix:i}=e,{pathString:a,pathStyle:$}=u(100,0,o,"rail"),{pathString:S,pathStyle:v}=u(f,l,n,"fill"),m=100+t;return r("div",{class:`${i}-progress-content`,role:"none"},r("div",{class:`${i}-progress-graph`,"aria-hidden":!0},r("div",{class:`${i}-progress-graph-circle`,style:{transform:h?`rotate(${h}deg)`:void 0}},r("svg",{viewBox:`0 0 ${m} ${m}`},b(),r("g",null,r("path",{class:`${i}-progress-graph-circle-rail`,d:a,"stroke-width":t,"stroke-linecap":"round",fill:"none",style:$})),r("g",null,r("path",{class:[`${i}-progress-graph-circle-fill`,f===0&&`${i}-progress-graph-circle-fill--empty`],d:S,"stroke-width":t,"stroke-linecap":"round",fill:"none",style:v}))))),y?r("div",null,p.default?r("div",{class:`${i}-progress-custom-content`,role:"none"},p.default()):c!=="default"?r("div",{class:`${i}-progress-icon`,"aria-hidden":!0},r(W,{clsPrefix:i},{default:()=>L[c]})):r("div",{class:`${i}-progress-text`,style:{color:d},role:"none"},r("span",{class:`${i}-progress-text__percentage`},f),r("span",{class:`${i}-progress-text__unit`},g))):null)}}}),Y={success:r(T,null),error:r(O,null),warning:r(q,null),info:r(j,null)},H=z({name:"ProgressLine",props:{clsPrefix:{type:String,required:!0},percentage:{type:Number,default:0},railColor:String,railStyle:[String,Object],fillColor:[String,Object],status:{type:String,required:!0},indicatorPlacement:{type:String,required:!0},indicatorTextColor:String,unit:{type:String,default:"%"},processing:{type:Boolean,required:!0},showIndicator:{type:Boolean,required:!0},height:[String,Number],railBorderRadius:[String,Number],fillBorderRadius:[String,Number]},setup(e,{slots:p}){const u=x(()=>w(e.height)),b=x(()=>{var t,l;return typeof e.fillColor=="object"?`linear-gradient(to right, ${(t=e.fillColor)===null||t===void 0?void 0:t.stops[0]} , ${(l=e.fillColor)===null||l===void 0?void 0:l.stops[1]})`:e.fillColor}),n=x(()=>e.railBorderRadius!==void 0?w(e.railBorderRadius):e.height!==void 0?w(e.height,{c:.5}):""),o=x(()=>e.fillBorderRadius!==void 0?w(e.fillBorderRadius):e.railBorderRadius!==void 0?w(e.railBorderRadius):e.height!==void 0?w(e.height,{c:.5}):"");return()=>{const{indicatorPlacement:t,railColor:l,railStyle:c,percentage:f,unit:y,indicatorTextColor:d,status:g,showIndicator:h,processing:i,clsPrefix:a}=e;return r("div",{class:`${a}-progress-content`,role:"none"},r("div",{class:`${a}-progress-graph`,"aria-hidden":!0},r("div",{class:[`${a}-progress-graph-line`,{[`${a}-progress-graph-line--indicator-${t}`]:!0}]},r("div",{class:`${a}-progress-graph-line-rail`,style:[{backgroundColor:l,height:u.value,borderRadius:n.value},c]},r("div",{class:[`${a}-progress-graph-line-fill`,i&&`${a}-progress-graph-line-fill--processing`],style:{maxWidth:`${e.percentage}%`,background:b.value,height:u.value,lineHeight:u.value,borderRadius:o.value}},t==="inside"?r("div",{class:`${a}-progress-graph-line-indicator`,style:{color:d}},p.default?p.default():`${f}${y}`):null)))),h&&t==="outside"?r("div",null,p.default?r("div",{class:`${a}-progress-custom-content`,style:{color:d},role:"none"},p.default()):g==="default"?r("div",{role:"none",class:`${a}-progress-icon ${a}-progress-icon--as-text`,style:{color:d}},f,y):r("div",{class:`${a}-progress-icon`,"aria-hidden":!0},r(W,{clsPrefix:a},{default:()=>Y[g]}))):null)}}});function I(e,p,u=100){return`m ${u/2} ${u/2-e} a ${e} ${e} 0 1 1 0 ${2*e} a ${e} ${e} 0 1 1 0 -${2*e}`}const V=z({name:"ProgressMultipleCircle",props:{clsPrefix:{type:String,required:!0},viewBoxWidth:{type:Number,required:!0},percentage:{type:Array,default:[0]},strokeWidth:{type:Number,required:!0},circleGap:{type:Number,required:!0},showIndicator:{type:Boolean,required:!0},fillColor:{type:Array,default:()=>[]},railColor:{type:Array,default:()=>[]},railStyle:{type:Array,default:()=>[]}},setup(e,{slots:p}){const u=x(()=>e.percentage.map((o,t)=>`${Math.PI*o/100*(e.viewBoxWidth/2-e.strokeWidth/2*(1+2*t)-e.circleGap*t)*2}, ${e.viewBoxWidth*8}`)),b=(n,o)=>{const t=e.fillColor[o],l=typeof t=="object"?t.stops[0]:"",c=typeof t=="object"?t.stops[1]:"";return typeof e.fillColor[o]=="object"&&r("linearGradient",{id:`gradient-${o}`,x1:"100%",y1:"0%",x2:"0%",y2:"100%"},r("stop",{offset:"0%","stop-color":l}),r("stop",{offset:"100%","stop-color":c}))};return()=>{const{viewBoxWidth:n,strokeWidth:o,circleGap:t,showIndicator:l,fillColor:c,railColor:f,railStyle:y,percentage:d,clsPrefix:g}=e;return r("div",{class:`${g}-progress-content`,role:"none"},r("div",{class:`${g}-progress-graph`,"aria-hidden":!0},r("div",{class:`${g}-progress-graph-circle`},r("svg",{viewBox:`0 0 ${n} ${n}`},r("defs",null,d.map((h,i)=>b(h,i))),d.map((h,i)=>r("g",{key:i},r("path",{class:`${g}-progress-graph-circle-rail`,d:I(n/2-o/2*(1+2*i)-t*i,o,n),"stroke-width":o,"stroke-linecap":"round",fill:"none",style:[{strokeDashoffset:0,stroke:f[i]},y[i]]}),r("path",{class:[`${g}-progress-graph-circle-fill`,h===0&&`${g}-progress-graph-circle-fill--empty`],d:I(n/2-o/2*(1+2*i)-t*i,o,n),"stroke-width":o,"stroke-linecap":"round",fill:"none",style:{strokeDasharray:u.value[i],strokeDashoffset:0,stroke:typeof c[i]=="object"?`url(#gradient-${i})`:c[i]}})))))),l&&p.default?r("div",null,r("div",{class:`${g}-progress-text`},p.default())):null)}}}),E=P([s("progress",{display:"inline-block"},[s("progress-icon",`
 color: var(--n-icon-color);
 transition: color .3s var(--n-bezier);
 `),C("line",`
 width: 100%;
 display: block;
 `,[s("progress-content",`
 display: flex;
 align-items: center;
 `,[s("progress-graph",{flex:1})]),s("progress-custom-content",{marginLeft:"14px"}),s("progress-icon",`
 width: 30px;
 padding-left: 14px;
 height: var(--n-icon-size-line);
 line-height: var(--n-icon-size-line);
 font-size: var(--n-icon-size-line);
 `,[C("as-text",`
 color: var(--n-text-color-line-outer);
 text-align: center;
 width: 40px;
 font-size: var(--n-font-size);
 padding-left: 4px;
 transition: color .3s var(--n-bezier);
 `)])]),C("circle, dashboard",{width:"120px"},[s("progress-custom-content",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 justify-content: center;
 `),s("progress-text",`
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
 `),s("progress-icon",`
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translateX(-50%) translateY(-50%);
 display: flex;
 align-items: center;
 color: var(--n-icon-color);
 font-size: var(--n-icon-size-circle);
 `)]),C("multiple-circle",`
 width: 200px;
 color: inherit;
 `,[s("progress-text",`
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
 `)]),s("progress-content",{position:"relative"}),s("progress-graph",{position:"relative"},[s("progress-graph-circle",[P("svg",{verticalAlign:"bottom"}),s("progress-graph-circle-fill",`
 stroke: var(--n-fill-color);
 transition:
 opacity .3s var(--n-bezier),
 stroke .3s var(--n-bezier),
 stroke-dasharray .3s var(--n-bezier);
 `,[C("empty",{opacity:0})]),s("progress-graph-circle-rail",`
 transition: stroke .3s var(--n-bezier);
 overflow: hidden;
 stroke: var(--n-rail-color);
 `)]),s("progress-graph-line",[C("indicator-inside",[s("progress-graph-line-rail",`
 height: 16px;
 line-height: 16px;
 border-radius: 10px;
 `,[s("progress-graph-line-fill",`
 height: inherit;
 border-radius: 10px;
 `),s("progress-graph-line-indicator",`
 background: #0000;
 white-space: nowrap;
 text-align: right;
 margin-left: 14px;
 margin-right: 14px;
 height: inherit;
 font-size: 12px;
 color: var(--n-text-color-line-inner);
 transition: color .3s var(--n-bezier);
 `)])]),C("indicator-inside-label",`
 height: 16px;
 display: flex;
 align-items: center;
 `,[s("progress-graph-line-rail",`
 flex: 1;
 transition: background-color .3s var(--n-bezier);
 `),s("progress-graph-line-indicator",`
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
 `)]),s("progress-graph-line-rail",`
 position: relative;
 overflow: hidden;
 height: var(--n-rail-height);
 border-radius: 5px;
 background-color: var(--n-rail-color);
 transition: background-color .3s var(--n-bezier);
 `,[s("progress-graph-line-fill",`
 background: var(--n-fill-color);
 position: relative;
 border-radius: 5px;
 height: inherit;
 width: 100%;
 max-width: 0%;
 transition:
 background-color .3s var(--n-bezier),
 max-width .2s var(--n-bezier);
 `,[C("processing",[P("&::after",`
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
 `)]),F=Object.assign(Object.assign({},G.props),{processing:Boolean,type:{type:String,default:"line"},gapDegree:Number,gapOffsetDegree:Number,status:{type:String,default:"default"},railColor:[String,Array],railStyle:[String,Array],color:[String,Array,Object],viewBoxWidth:{type:Number,default:100},strokeWidth:{type:Number,default:7},percentage:[Number,Array],unit:{type:String,default:"%"},showIndicator:{type:Boolean,default:!0},indicatorPosition:{type:String,default:"outside"},indicatorPlacement:{type:String,default:"outside"},indicatorTextColor:String,circleGap:{type:Number,default:1},height:Number,borderRadius:[String,Number],fillBorderRadius:[String,Number],offsetDegree:Number}),Q=z({name:"Progress",props:F,setup(e){const p=x(()=>e.indicatorPlacement||e.indicatorPosition),u=x(()=>{if(e.gapDegree||e.gapDegree===0)return e.gapDegree;if(e.type==="dashboard")return 75}),{mergedClsPrefixRef:b,inlineThemeDisabled:n}=_(e),o=G("Progress","-progress",E,A,e,b),t=x(()=>{const{status:c}=e,{common:{cubicBezierEaseInOut:f},self:{fontSize:y,fontSizeCircle:d,railColor:g,railHeight:h,iconSizeCircle:i,iconSizeLine:a,textColorCircle:$,textColorLineInner:S,textColorLineOuter:v,lineBgProcessing:m,fontWeightCircle:B,[N("iconColor",c)]:R,[N("fillColor",c)]:k}}=o.value;return{"--n-bezier":f,"--n-fill-color":k,"--n-font-size":y,"--n-font-size-circle":d,"--n-font-weight-circle":B,"--n-icon-color":R,"--n-icon-size-circle":i,"--n-icon-size-line":a,"--n-line-bg-processing":m,"--n-rail-color":g,"--n-rail-height":h,"--n-text-color-circle":$,"--n-text-color-line-inner":S,"--n-text-color-line-outer":v}}),l=n?M("progress",x(()=>e.status[0]),t,e):void 0;return{mergedClsPrefix:b,mergedIndicatorPlacement:p,gapDeg:u,cssVars:n?void 0:t,themeClass:l==null?void 0:l.themeClass,onRender:l==null?void 0:l.onRender}},render(){const{type:e,cssVars:p,indicatorTextColor:u,showIndicator:b,status:n,railColor:o,railStyle:t,color:l,percentage:c,viewBoxWidth:f,strokeWidth:y,mergedIndicatorPlacement:d,unit:g,borderRadius:h,fillBorderRadius:i,height:a,processing:$,circleGap:S,mergedClsPrefix:v,gapDeg:m,gapOffsetDegree:B,themeClass:R,$slots:k,onRender:D}=this;return D==null||D(),r("div",{class:[R,`${v}-progress`,`${v}-progress--${e}`,`${v}-progress--${n}`],style:p,"aria-valuemax":100,"aria-valuemin":0,"aria-valuenow":c,role:e==="circle"||e==="line"||e==="dashboard"?"progressbar":"none"},e==="circle"||e==="dashboard"?r(X,{clsPrefix:v,status:n,showIndicator:b,indicatorTextColor:u,railColor:o,fillColor:l,railStyle:t,offsetDegree:this.offsetDegree,percentage:c,viewBoxWidth:f,strokeWidth:y,gapDegree:m===void 0?e==="dashboard"?75:0:m,gapOffsetDegree:B,unit:g},k):e==="line"?r(H,{clsPrefix:v,status:n,showIndicator:b,indicatorTextColor:u,railColor:o,fillColor:l,railStyle:t,percentage:c,processing:$,indicatorPlacement:d,unit:g,fillBorderRadius:i,railBorderRadius:h,height:a},k):e==="multiple-circle"?r(V,{clsPrefix:v,strokeWidth:y,railColor:o,fillColor:l,railStyle:t,viewBoxWidth:f,percentage:c,showIndicator:b,circleGap:S},k):null)}});export{Q as _};
