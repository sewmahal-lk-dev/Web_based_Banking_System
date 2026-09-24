import fs from 'node:fs';
const tabs=await(await fetch('http://127.0.0.1:9225/json')).json();const ws=new WebSocket(tabs.find(t=>t.type==='page').webSocketDebuggerUrl);await new Promise(r=>ws.addEventListener('open',r,{once:true}));let id=0;const pending=new Map(),errors=[];
ws.onmessage=e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result);}else if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails);};
const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++id,{resolve,reject});ws.send(JSON.stringify({id,method,params}));});
const ev=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value;};
const sleep=ms=>new Promise(r=>setTimeout(r,ms));const ready=async()=>{await sleep(1000);for(let i=0;i<100;i++){if(await ev('document.readyState==="complete"'))return;await sleep(150);}};
const nav=async path=>{await call('Page.navigate',{url:'http://127.0.0.1:8772/bank'+path});await ready();};
let checks=0;const check=(v,label)=>{if(!v)throw Error(label);checks++;console.log('PASS '+label);};
await call('Page.enable');await call('Runtime.enable');await call('Network.enable');await call('Network.setCacheDisabled',{cacheDisabled:true});await call('Network.setCookie',{name:'JSESSIONID',value:fs.readFileSync('target/report-session.txt','utf8').trim(),url:'http://127.0.0.1:8772/bank/'});
await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});await nav('/employee/dashboard');
check(await ev("!!document.querySelector('.operations-sidebar a[href$=\"/admin/reports\"]')"),'admin Reports sidebar link');check(await ev("[...document.querySelectorAll('.quick-action-tile')].filter(e=>e.textContent.includes('Generate report')).length===1"),'one Generate report quick action');
await ev("document.querySelector('.quick-action-tile[href$=\"/admin/reports\"]').click()");await ready();check(await ev("!!document.querySelector('#report-filters')"),'quick action opens reports');check(await ev("document.querySelector('.operations-sidebar a[aria-current]').textContent==='Reports'"),'Reports navigation active');
for(const type of ['CUSTOMER','EMPLOYEE','PRODUCT','AUDIT']){
 await ev(`(()=>{const s=document.querySelector('#report-type');s.value='${type}';s.dispatchEvent(new Event('change'));if(s.value==='AUDIT')document.querySelector('[name=action]').value='REPORT_GENERATED';})()`);
 check(await ev(`document.querySelector('[name=role]').disabled===${type!=='EMPLOYEE'}&&document.querySelector('[name=productType]').disabled===${type!=='PRODUCT'}&&document.querySelector('[name=action]').disabled===${type!=='AUDIT'}`),type+' applicable filters only');
 await ev("document.querySelector('#report-filters').requestSubmit()");await ready();check(await ev("!!document.querySelector('.report-preview table')&&!document.querySelector('.notice.error')"),type+' browser generation');
 check(await ev("document.querySelectorAll('.report-preview tbody tr').length<=25"),type+' bounded preview');
}
for(const theme of ['dark','light']){
 await ev(`localStorage.setItem('lankatrust-theme','${theme}')`);
 for(const width of [1440,768,390,320]){await call('Emulation.setDeviceMetricsOverride',{width,height:1000,deviceScaleFactor:1,mobile:false});await nav('/admin/reports');check(await ev(`document.documentElement.dataset.theme==='${theme}'`),theme+' persistence '+width);check(await ev('document.documentElement.scrollWidth<=innerWidth'),theme+' no overflow '+width);if(width===1440||width===390)fs.writeFileSync(`verification/admin-reports-${theme}-${width}.png`,Buffer.from((await call('Page.captureScreenshot',{format:'png'})).data,'base64'));}
}
await ev("document.querySelector('.theme-toggle').click()");check(await ev("document.documentElement.dataset.theme==='dark'"),'theme toggle');await ev("document.querySelector('.sidebar-toggle').click()");check(await ev("document.body.classList.contains('nav-open')"),'mobile sidebar');
check(errors.length===0,'no JS errors');fs.writeFileSync('verification/admin-report-browser.json',JSON.stringify({checks,passed:true},null,2));await call('Browser.close');ws.close();
