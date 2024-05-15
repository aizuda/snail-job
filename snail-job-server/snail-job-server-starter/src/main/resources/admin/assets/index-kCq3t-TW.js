import{d as x,s as I,a as N,o as L,c as z,w as s,f as o,e as r,h as e,$ as t,t as i,g as m,I as D,N as S}from"./index-DQvTTbM-.js";import{_ as T,a as V}from"./DescriptionsItem-Bd4R5YlG.js";import{_ as B}from"./Space-B5Upi9VN.js";const C="snail-job",U="module",$="1.0.0",A="A flexible, reliable, and fast platform for distributed task retry and distributed task scheduling.",J="Apache-2.0",M="https://gitee.com/aizuda/snail-job",H={githubUrl:"https://github.com/aizuda/snail-job.git",giteeUrl:"https://gitee.com/aizuda/snail-job.git"},O={url:"https://gitee.com/aizuda/snail-job/issues"},W=["Job","Retry","Snail Job","Vue3 admin ","vue-admin-template","Vite5","TypeScript","naive-ui","naive-ui-admin","ant-design-vue v4","UnoCSS"],E={node:">=18.12.0",pnpm:">=8.7.0"},R={build:"vite build --mode prod","build:test":"vite build --mode test",cleanup:"sa cleanup",commit:"sa git-commit",dev:"vite --mode test","dev:prod":"vite --mode prod","gen-route":"sa gen-route",lint:"eslint . --fix",prepare:"simple-git-hooks",preview:"vite preview",release:"sa release",typecheck:"vue-tsc --noEmit --skipLibCheck","update-pkg":"sa update-pkg"},q={"@better-scroll/core":"2.5.1","@codemirror/lang-javascript":"^6.2.2","@codemirror/lang-json":"^6.0.1","@codemirror/theme-one-dark":"^6.1.2","@iconify/vue":"4.1.2","@sa/axios":"workspace:*","@sa/color":"workspace:*","@sa/cron-input":"workspace:*","@sa/hooks":"workspace:*","@sa/materials":"workspace:*","@sa/utils":"workspace:*","@vueuse/core":"10.9.0",clipboard:"2.0.11",dayjs:"1.11.11",echarts:"5.5.0","lodash-es":"4.17.21","naive-ui":"2.38.2",nprogress:"0.2.0",pinia:"2.1.7","ts-md5":"1.3.1",vue:"3.4.26","vue-codemirror6":"^1.3.0","vue-draggable-plus":"0.4.0","vue-i18n":"9.13.1","vue-router":"4.3.2"},F={"@elegant-router/vue":"0.3.6","@iconify/json":"2.2.207","@sa/scripts":"workspace:*","@sa/uno-preset":"workspace:*","@soybeanjs/eslint-config":"1.3.4","@types/lodash-es":"4.17.12","@types/node":"20.12.10","@types/nprogress":"0.2.3","@unocss/eslint-config":"0.59.4","@unocss/preset-icons":"0.59.4","@unocss/preset-uno":"0.59.4","@unocss/transformer-directives":"0.59.4","@unocss/transformer-variant-group":"0.59.4","@unocss/vite":"0.59.4","@vitejs/plugin-vue":"5.0.4","@vitejs/plugin-vue-jsx":"3.1.0",eslint:"9.2.0","eslint-plugin-vue":"9.25.0","lint-staged":"15.2.2",sass:"1.76.0","simple-git-hooks":"2.11.1",tsx:"4.9.3",typescript:"5.4.5","unplugin-icons":"0.19.0","unplugin-vue-components":"0.27.0",vite:"5.2.11","vite-plugin-progress":"0.0.7","vite-plugin-svg-icons":"2.0.1","vite-plugin-vue-devtools":"7.1.3","vue-eslint-parser":"9.4.2","vue-tsc":"2.0.16"},G="https://www.easyretry.com/pages/78ba75/",p={name:C,type:U,version:$,description:A,license:J,homepage:M,repository:H,bugs:O,keywords:W,engines:E,scripts:R,dependencies:q,devDependencies:F,"simple-git-hooks":{"commit-msg":"pnpm sa git-commit-verify","pre-commit":"pnpm typecheck && pnpm lint-staged"},"lint-staged":{"*":"eslint --fix"},website:G},K=["innerHTML"],P=["href"],Q=["href"],X=["href"],Y=["href"],oe=x({name:"about",__name:"index",setup(Z){const g=I(),b=N(()=>g.isMobile?1:2),{name:f,version:_,dependencies:v,devDependencies:h}=p;function l(n){const[u,c]=n;return{name:u,version:c}}const k={name:f,version:_,dependencies:Object.entries(v).map(n=>l(n)),devDependencies:Object.entries(h).map(n=>l(n))},y="2024-05-15 09:59:51";return(n,u)=>{const c=D,a=T,d=S,j=V,w=B;return L(),z(w,{vertical:"",size:16},{default:s(()=>[o(c,{title:e(t)("page.about.title"),bordered:!1,size:"small",segmented:"",class:"card-wrapper"},{default:s(()=>[r("p",{innerHTML:e(t)("page.about.introduction")},null,8,K)]),_:1},8,["title"]),o(c,{title:e(t)("page.about.projectInfo.title"),bordered:!1,size:"small",segmented:"",class:"card-wrapper"},{default:s(()=>[o(j,{"label-placement":"left",bordered:"",size:"small",column:b.value},{default:s(()=>[o(a,{label:e(t)("page.about.projectInfo.officialWebsite")},{default:s(()=>[r("a",{class:"text-primary",href:e(p).website,target:"_blank",rel:"noopener noreferrer"},i(e(t)("page.about.projectInfo.officialWebsite")),9,P)]),_:1},8,["label"]),o(a,{label:e(t)("page.about.projectInfo.version")},{default:s(()=>[o(d,{type:"primary"},{default:s(()=>[m(i(k.version),1)]),_:1})]),_:1},8,["label"]),o(a,{label:e(t)("page.about.projectInfo.githubLink")},{default:s(()=>[r("a",{class:"text-primary",href:e(p).repository.githubUrl,target:"_blank",rel:"noopener noreferrer"},i(e(t)("page.about.projectInfo.githubLink")),9,Q)]),_:1},8,["label"]),o(a,{label:e(t)("page.about.projectInfo.giteeLink")},{default:s(()=>[r("a",{class:"text-primary",href:e(p).repository.giteeUrl,target:"_blank",rel:"noopener noreferrer"},i(e(t)("page.about.projectInfo.giteeLink")),9,X)]),_:1},8,["label"]),o(a,{label:e(t)("page.about.projectInfo.previewLink")},{default:s(()=>[r("a",{class:"text-primary",href:e(p).website,target:"_blank",rel:"noopener noreferrer"},i(e(t)("page.about.projectInfo.previewLink")),9,Y)]),_:1},8,["label"]),o(a,{label:e(t)("page.about.projectInfo.latestBuildTime")},{default:s(()=>[o(d,{type:"primary"},{default:s(()=>[m(i(e(y)),1)]),_:1})]),_:1},8,["label"])]),_:1},8,["column"])]),_:1},8,["title"])]),_:1})}}});export{oe as default};