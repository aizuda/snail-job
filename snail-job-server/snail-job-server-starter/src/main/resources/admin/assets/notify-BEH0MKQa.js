import{bm as e}from"./index-KP_OAqI5.js";function o(t){return e({url:"/notify-config/list",method:"get",params:t})}function f(t){return e({url:"/notify-config",method:"post",data:t})}function r(t){return e({url:"/notify-config",method:"put",data:t})}function c(t){return e({url:"/notify-config/ids",method:"delete",data:t})}function u(t,i){return e({url:`/notify-config/${t}/status/${i}`,method:"put"})}function s(t){return e({url:"/notify-recipient/page/list",method:"get",params:t})}function d(){return e({url:"/notify-recipient/list",method:"get"})}function h(t){return e({url:"/notify-recipient",method:"post",data:t})}function y(t){return e({url:"/notify-recipient",method:"put",data:t})}function p(t){return e({url:"/notify-recipient/ids",method:"delete",data:t})}export{y as a,s as b,p as c,d,f as e,h as f,r as g,o as h,c as i,u as j};