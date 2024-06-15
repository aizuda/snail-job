import{az as p,aA as e,aE as V,aB as C,aD as O,di as G,dj as H,d as E,aF as K,aI as L,dk as N,a as D,b7 as T,aP as W,cr as q,dl as J,ap as n,dm as Q,dn as U}from"./index-BztLELzb.js";function M(t,b="default",a=[]){const{children:i}=t;if(i!==null&&typeof i=="object"&&!Array.isArray(i)){const l=i[b];if(typeof l=="function")return l()}return a}const F="DESCRIPTION_ITEM_FLAG";function X(t){return typeof t=="object"&&t&&!Array.isArray(t)?t.type&&t.type[F]:!1}const Y=p([e("descriptions",{fontSize:"var(--n-font-size)"},[e("descriptions-separator",`
 display: inline-block;
 margin: 0 8px 0 2px;
 `),e("descriptions-table-wrapper",[e("descriptions-table",[e("descriptions-table-row",[e("descriptions-table-header",{padding:"var(--n-th-padding)"}),e("descriptions-table-content",{padding:"var(--n-td-padding)"})])])]),V("bordered",[e("descriptions-table-wrapper",[e("descriptions-table",[e("descriptions-table-row",[p("&:last-child",[e("descriptions-table-content",{paddingBottom:0})])])])])]),C("left-label-placement",[e("descriptions-table-content",[p("> *",{verticalAlign:"top"})])]),C("left-label-align",[p("th",{textAlign:"left"})]),C("center-label-align",[p("th",{textAlign:"center"})]),C("right-label-align",[p("th",{textAlign:"right"})]),C("bordered",[e("descriptions-table-wrapper",`
 border-radius: var(--n-border-radius);
 overflow: hidden;
 background: var(--n-merged-td-color);
 border: 1px solid var(--n-merged-border-color);
 `,[e("descriptions-table",[e("descriptions-table-row",[p("&:not(:last-child)",[e("descriptions-table-content",{borderBottom:"1px solid var(--n-merged-border-color)"}),e("descriptions-table-header",{borderBottom:"1px solid var(--n-merged-border-color)"})]),e("descriptions-table-header",`
 font-weight: 400;
 background-clip: padding-box;
 background-color: var(--n-merged-th-color);
 `,[p("&:not(:last-child)",{borderRight:"1px solid var(--n-merged-border-color)"})]),e("descriptions-table-content",[p("&:not(:last-child)",{borderRight:"1px solid var(--n-merged-border-color)"})])])])])]),e("descriptions-header",`
 font-weight: var(--n-th-font-weight);
 font-size: 18px;
 transition: color .3s var(--n-bezier);
 line-height: var(--n-line-height);
 margin-bottom: 16px;
 color: var(--n-title-text-color);
 `),e("descriptions-table-wrapper",`
 transition:
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[e("descriptions-table",`
 width: 100%;
 border-collapse: separate;
 border-spacing: 0;
 box-sizing: border-box;
 `,[e("descriptions-table-row",`
 box-sizing: border-box;
 transition: border-color .3s var(--n-bezier);
 `,[e("descriptions-table-header",`
 font-weight: var(--n-th-font-weight);
 line-height: var(--n-line-height);
 display: table-cell;
 box-sizing: border-box;
 color: var(--n-th-text-color);
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `),e("descriptions-table-content",`
 vertical-align: top;
 line-height: var(--n-line-height);
 display: table-cell;
 box-sizing: border-box;
 color: var(--n-td-text-color);
 transition:
 color .3s var(--n-bezier),
 background-color .3s var(--n-bezier),
 border-color .3s var(--n-bezier);
 `,[O("content",`
 transition: color .3s var(--n-bezier);
 display: inline-block;
 color: var(--n-td-text-color);
 `)]),O("label",`
 font-weight: var(--n-th-font-weight);
 transition: color .3s var(--n-bezier);
 display: inline-block;
 margin-right: 14px;
 color: var(--n-th-text-color);
 `)])])])]),e("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color);
 --n-merged-td-color: var(--n-td-color);
 --n-merged-border-color: var(--n-border-color);
 `),G(e("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color-modal);
 --n-merged-td-color: var(--n-td-color-modal);
 --n-merged-border-color: var(--n-border-color-modal);
 `)),H(e("descriptions-table-wrapper",`
 --n-merged-th-color: var(--n-th-color-popover);
 --n-merged-td-color: var(--n-td-color-popover);
 --n-merged-border-color: var(--n-border-color-popover);
 `))]),Z=Object.assign(Object.assign({},L.props),{title:String,column:{type:Number,default:3},columns:Number,labelPlacement:{type:String,default:"top"},labelAlign:{type:String,default:"left"},separator:{type:String,default:":"},size:{type:String,default:"medium"},bordered:Boolean,labelClass:String,labelStyle:[Object,String],contentClass:String,contentStyle:[Object,String]}),re=E({name:"Descriptions",props:Z,setup(t){const{mergedClsPrefixRef:b,inlineThemeDisabled:a}=K(t),i=L("Descriptions","-descriptions",Y,N,t,b),l=D(()=>{const{size:d,bordered:h}=t,{common:{cubicBezierEaseInOut:g},self:{titleTextColor:S,thColor:P,thColorModal:r,thColorPopover:z,thTextColor:v,thFontWeight:$,tdTextColor:_,tdColor:j,tdColorModal:R,tdColorPopover:o,borderColor:f,borderColorModal:A,borderColorPopover:c,borderRadius:m,lineHeight:y,[T("fontSize",d)]:w,[T(h?"thPaddingBordered":"thPadding",d)]:u,[T(h?"tdPaddingBordered":"tdPadding",d)]:x}}=i.value;return{"--n-title-text-color":S,"--n-th-padding":u,"--n-td-padding":x,"--n-font-size":w,"--n-bezier":g,"--n-th-font-weight":$,"--n-line-height":y,"--n-th-text-color":v,"--n-td-text-color":_,"--n-th-color":P,"--n-th-color-modal":r,"--n-th-color-popover":z,"--n-td-color":j,"--n-td-color-modal":R,"--n-td-color-popover":o,"--n-border-radius":m,"--n-border-color":f,"--n-border-color-modal":A,"--n-border-color-popover":c}}),s=a?W("descriptions",D(()=>{let d="";const{size:h,bordered:g}=t;return g&&(d+="a"),d+=h[0],d}),l,t):void 0;return{mergedClsPrefix:b,cssVars:a?void 0:l,themeClass:s==null?void 0:s.themeClass,onRender:s==null?void 0:s.onRender,compitableColumn:q(t,["columns","column"]),inlineThemeDisabled:a}},render(){const t=this.$slots.default,b=t?J(t()):[];b.length;const{contentClass:a,labelClass:i,compitableColumn:l,labelPlacement:s,labelAlign:d,size:h,bordered:g,title:S,cssVars:P,mergedClsPrefix:r,separator:z,onRender:v}=this;v==null||v();const $=b.filter(o=>X(o)),_={span:0,row:[],secondRow:[],rows:[]},R=$.reduce((o,f,A)=>{const c=f.props||{},m=$.length-1===A,y=["label"in c?c.label:M(f,"label")],w=[M(f)],u=c.span||1,x=o.span;o.span+=u;const I=c.labelStyle||c["label-style"]||this.labelStyle,k=c.contentStyle||c["content-style"]||this.contentStyle;if(s==="left")g?o.row.push(n("th",{class:[`${r}-descriptions-table-header`,i],colspan:1,style:I},y),n("td",{class:[`${r}-descriptions-table-content`,a],colspan:m?(l-x)*2+1:u*2-1,style:k},w)):o.row.push(n("td",{class:`${r}-descriptions-table-content`,colspan:m?(l-x)*2:u*2},n("span",{class:[`${r}-descriptions-table-content__label`,i],style:I},[...y,z&&n("span",{class:`${r}-descriptions-separator`},z)]),n("span",{class:[`${r}-descriptions-table-content__content`,a],style:k},w)));else{const B=m?(l-x)*2:u*2;o.row.push(n("th",{class:[`${r}-descriptions-table-header`,i],colspan:B,style:I},y)),o.secondRow.push(n("td",{class:[`${r}-descriptions-table-content`,a],colspan:B,style:k},w))}return(o.span>=l||m)&&(o.span=0,o.row.length&&(o.rows.push(o.row),o.row=[]),s!=="left"&&o.secondRow.length&&(o.rows.push(o.secondRow),o.secondRow=[])),o},_).rows.map(o=>n("tr",{class:`${r}-descriptions-table-row`},o));return n("div",{style:P,class:[`${r}-descriptions`,this.themeClass,`${r}-descriptions--${s}-label-placement`,`${r}-descriptions--${d}-label-align`,`${r}-descriptions--${h}-size`,g&&`${r}-descriptions--bordered`]},S||this.$slots.header?n("div",{class:`${r}-descriptions-header`},S||U(this,"header")):null,n("div",{class:`${r}-descriptions-table-wrapper`},n("table",{class:`${r}-descriptions-table`},n("tbody",null,s==="top"&&n("tr",{class:`${r}-descriptions-table-row`,style:{visibility:"collapse"}},Q(l*2,n("td",null))),R))))}}),ee={label:String,span:{type:Number,default:1},labelClass:String,labelStyle:[Object,String],contentClass:String,contentStyle:[Object,String]},te=E({name:"DescriptionsItem",[F]:!0,props:ee,render(){return null}});export{te as _,re as a};
