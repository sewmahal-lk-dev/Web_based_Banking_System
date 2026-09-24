import fs from 'node:fs';
const tabs=await(await fetch('http://127.0.0.1:9225/json')).json();const ws=new WebSocket(tabs.find(t=>t.type==='page').webSocketDebuggerUrl);await new Promise(r=>ws.addEventListener('open',r,{once:true}));let id=0;const pending=new Map(),errors=[];
ws.onmessage=e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result);}else if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails);};
const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++id,{resolve,reject});ws.send(JSON.stringify({id,method,params}));});
const ev=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value;};
const sleep=ms=>new Promise(r=>setTimeout(r,ms));const nav=async path=>{await call('Page.navigate',{url:'http://127.0.0.1:8773/bank'+path});await sleep(1000);for(let i=0;i<100;i++){if(await ev('document.readyState==="complete"'))return;await sleep(150);}};
let checks=0;const check=(v,label)=>{if(!v)throw Error(label);checks++;console.log('PASS '+label);};
const [cookie,reference]=fs.readFileSync('target/receipt-session.txt','utf8').trim().split(/\r?\n/);
await call('Page.enable');await call('Runtime.enable');await call('Network.enable');await call('Network.setCacheDisabled',{cacheDisabled:true});await call('Network.setCookie',{name:'JSESSIONID',value:cookie,url:'http://127.0.0.1:8773/bank/'});
await nav('/customer/transfer?receipt='+reference);
for(const theme of ['dark','light']){
 await ev(`localStorage.setItem('lankatrust-theme','${theme}')`);
 for(const width of [1440,390]){
  await call('Emulation.setDeviceMetricsOverride',{width,height:1000,deviceScaleFactor:1,mobile:false});await nav('/customer/transfer?receipt='+reference);
  check(await ev(`(()=>{const a=document.querySelector('.message.success a[href*="/transfer/receipt"]');return a&&a.getBoundingClientRect().height>0&&a.href.endsWith('${reference}')})()`),theme+' visible exact receipt button '+width);
  check(await ev('document.documentElement.scrollWidth<=innerWidth'),theme+' transfer no overflow '+width);
  fs.writeFileSync(`verification/receipt-success-${theme}-${width}.png`,Buffer.from((await call('Page.captureScreenshot',{format:'png'})).data,'base64'));
  await nav('/customer/transactions');check(await ev(`document.querySelectorAll('a[href*="/transfer/receipt?reference="]').length===1`),'history eligible outgoing receipt only '+theme+' '+width);
  check(await ev('document.documentElement.scrollWidth<=innerWidth'),'history no overflow '+theme+' '+width);
 }
}
const response=await ev(`fetch('/bank/customer/transfer/receipt?reference=${reference}').then(async r=>({status:r.status,type:r.headers.get('content-type'),magic:new TextDecoder().decode((await r.arrayBuffer()).slice(0,5))}))`);check(response.status===200&&response.type==='application/pdf'&&response.magic==='%PDF-','browser receipt download returns PDF');
check(errors.length===0,'no JS errors');fs.writeFileSync('verification/transfer-receipt-browser.json',JSON.stringify({checks,passed:true},null,2));await call('Browser.close');ws.close();
