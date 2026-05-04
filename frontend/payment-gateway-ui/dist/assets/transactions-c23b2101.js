import{i as a}from"./index-7af3744a.js";const t={list:async(t=1,s=10)=>(await a.get("/api/v1/transactions",{params:{page:t,pageSize:s}})).data,getById:async t=>(await a.get(`/api/v1/transactions/${t}`)).data,refund:async(t,s,n)=>(await a.post("/api/v1/refunds",{transactionId:t,amount:s,reason:n})).data};export{t};
//# sourceMappingURL=transactions-c23b2101.js.map
