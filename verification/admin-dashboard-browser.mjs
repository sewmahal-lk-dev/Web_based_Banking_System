import fs from 'node:fs';
const tabs=await(await fetch('http://127.0.0.1:9225/json')).json();const ws=new WebSocket(tabs.find(t=>t.type==='page').webSocketDebuggerUrl);await new Promise(r=>ws.addEventListener('open',r,{once:true}));let id=0;const pending=new Map(),errors=[];
ws.onmessage=e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result);}else if(m.method==='Runtime.exceptionThrown')errors.push(m.params.exceptionDetails);};
const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++id,{resolve,reject});ws.send(JSON.stringify({id,method,params}));});
const ev=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value;};
const sleep=ms=>new Promise(r=>setTimeout(r,ms));const ready=async()=>{await sleep(1000);for(let i=0;i<100;i++){if(await ev('document.readyState==="complete"'))return;await sleep(150);}};
const nav=async path=>{await call('Page.navigate',{url:'http://127.0.0.1:8774/bank'+path});await ready();};
let checks=0;const check=(v,label)=>{if(!v)throw Error(label);checks++;console.log('PASS '+label);};
await call('Page.enable');await call('Runtime.enable');await call('Network.enable');await call('Network.setCacheDisabled',{cacheDisabled:true});await call('Network.setCookie',{name:'JSESSIONID',value:fs.readFileSync('target/admin-dashboard-session.txt','utf8').trim(),url:'http://127.0.0.1:8774/bank/'});

await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1100,deviceScaleFactor:1,mobile:false});await nav('/employee/dashboard');
check(await ev("!!document.querySelector('.operations-home .admin-overview')"),'Admin sections integrated into dashboard workspace');
check(await ev("document.querySelectorAll('.admin-recent-notifications .notify-item').length===5"),'five actual recent notifications');
check(await ev("document.querySelectorAll('.admin-activity-list li').length===5"),'five actual administrative activities');
check(await ev("document.querySelectorAll('.workspace-metric').length===4"),'all four original count cards retained');
check(await ev("!!document.querySelector('.staff-welcome')&&document.querySelector('.control-actions-heading').textContent.includes('Start a task')"),'welcome and Start a task retained');
for(const group of ['Customers','Employees','Products','Audit-log']){
 await ev(`document.querySelector('.operations-sidebar a[href="#${group}"]').click()`);
 check(await ev(`!document.querySelector('[data-workspace="${group}"]').hidden`),'existing workspace opens '+group);
 check(await ev("document.querySelector('.operations-home').hidden"),'overview hidden outside home');
}
await nav('/employee/dashboard');
const before=await ev("Number(document.querySelector('.notify-badge').textContent)");
await ev("document.querySelector('.admin-recent-notifications .notify-item.is-unread button').click()");await ready();
check(await ev("location.hash==='#Customers'&&!document.querySelector('[data-workspace=Customers]').hidden"),'recent notification marks read and opens Customers');
await nav('/employee/dashboard');check(await ev(`Number(document.querySelector('.notify-badge').textContent)===${before-1}`),'bell updates after reading');
check(await ev("!!document.querySelector('.admin-recent-notifications .is-read')"),'recent list displays read state');
await ev("document.querySelector('.admin-recent-activity .panel-heading a').click()");await ready();
check(await ev("!document.querySelector('[data-workspace=Audit-log]').hidden"),'activity View All opens existing Audit Log workspace');
await nav('/employee/dashboard');await ev("document.querySelector('.admin-recent-notifications .panel-heading a').click()");await ready();
check(await ev("location.pathname.endsWith('/employee/notifications')"),'notification View All opens existing notification list');
for(const theme of ['dark','light']){
 await ev(`localStorage.setItem('lankatrust-theme','${theme}')`);
 for(const width of [1440,768,390,320]){
  await call('Emulation.setDeviceMetricsOverride',{width,height:1100,deviceScaleFactor:1,mobile:false});await nav('/employee/dashboard');
  check(await ev(`document.documentElement.dataset.theme==='${theme}'`),theme+' theme '+width);
  check(await ev('document.documentElement.scrollWidth<=innerWidth'),theme+' no horizontal overflow '+width);
  check(await ev("document.querySelector('.admin-overview').getBoundingClientRect().width>0"),theme+' overview visible '+width);
  check(await ev("(()=>{const f=document.querySelector('.admin-recent-notifications .notify-item');return f.querySelector('button').getBoundingClientRect().width>=f.getBoundingClientRect().width-2&&getComputedStyle(f.querySelector('button')).backgroundImage==='none';})()"),theme+' full-width notification rows '+width);
  if(width===1440||width===390){await ev("document.querySelector('.admin-overview').scrollIntoView({block:'start'})");await sleep(200);fs.writeFileSync(`verification/admin-dashboard-${theme}-${width}.png`,Buffer.from((await call('Page.captureScreenshot',{format:'png'})).data,'base64'));}
 }
}
check(errors.length===0,'no browser JavaScript exceptions');
fs.writeFileSync('verification/admin-dashboard-browser.json',JSON.stringify({checks,passed:true},null,2));await call('Browser.close');ws.close();
