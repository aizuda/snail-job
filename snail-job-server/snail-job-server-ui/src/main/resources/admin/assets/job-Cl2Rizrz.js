import{aM as e}from"./index-BxI_8ir5.js";function r(t){return e({url:"/job/page/list",method:"get",params:t})}function u(t){return e({url:"/job/list",method:"get",params:t})}function n(t){return e({url:`/job/${t}`,method:"get"})}function s(t){return e({url:"/job/task/list",method:"get",params:t})}function a(t){return e({url:"/job/task/tree/list",method:"get",params:t})}function i(t){return e({url:"/job",method:"post",data:t})}function b(t){return e({url:"/job",method:"put",data:t})}function c(t){return e({url:"/job/status",method:"put",data:t})}function f(t){return e({url:"/job/ids",method:"delete",data:t})}function h(t){return e({url:`/job/trigger/${t}`,method:"post"})}function l(t){return e({url:"/job/job-name/list",method:"get",params:t})}export{i as a,b,r as c,f as d,h as e,l as f,c as g,u as h,n as i,s as j,a as k};