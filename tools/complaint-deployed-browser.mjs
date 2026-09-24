import fs from 'node:fs';
import crypto from 'node:crypto';
const base='http://localhost:8080/WebBasedBankingSystem';
const targets=await(await fetch('http://127.0.0.1:9226/json')).json();
const ws=new WebSocket(targets.find(t=>t.type==='page').webSocketDebuggerUrl);
await new Promise(r=>ws.addEventListener('open',r,{once:true}));
let seq=0;const pending=new Map(),results=[];
ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result);}});
const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++seq,{resolve,reject});ws.send(JSON.stringify({id:seq,method,params}));});
const ev=async expression=>{const r=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(r.exceptionDetails)throw new Error(JSON.stringify(r.exceptionDetails));return r.result.value;};
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
const nav=async route=>{await call('Page.navigate',{url:base+route});await sleep(700);for(let n=0;n<60;n++){if(await ev('document.readyState==="complete"'))return;await sleep(200);}};
const check=(ok,label)=>{if(!ok)throw new Error(label);results.push(label);console.log('PASS '+label);};
const shot=async name=>{await ev('document.fonts.ready');await sleep(150);fs.writeFileSync('verification/'+name+'.png',Buffer.from((await call('Page.captureScreenshot',{format:'png'})).data,'base64'));};
try{
 await call('Page.enable');await call('Page.reload',{ignoreCache:true});await sleep(700);await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});
 await nav('/customer/requests#support-workspace');
 if(await ev('location.pathname.endsWith("/login.jsp")')){
  const email='complaint-ui-'+Date.now()+'@example.invalid',password=crypto.randomBytes(20).toString('base64url')+'aA1!';
  await nav('/register.jsp');
  const register=await ev(`(async()=>{const f=document.querySelector('#registerForm');const values=${JSON.stringify({name:'Complaint UI Verification',email,password,confirmPassword:password,phone:'07'+String(Date.now()).slice(-8),accountType:'SAVINGS',city:'Colombo'})};for(const [key,value] of Object.entries(values))f.elements[key].value=value;const r=await fetch(f.action,{method:'POST',body:new URLSearchParams(new FormData(f))});return {url:r.url,ok:r.ok};})()`);
  check(register.ok&&register.url.includes('login'),'verification customer registered through real application');
  await nav('/login.jsp');
  await ev(`(()=>{const f=document.querySelector('form');f.email.value=${JSON.stringify(email)};f.password.value=${JSON.stringify(password)};f.requestSubmit();})()`);await sleep(800);
  await nav('/customer/requests#support-workspace');
 }
 check(await ev('document.body.classList.contains("workspace-app")&&location.pathname.endsWith("/customer/requests")'),'deployed customer requests workspace loaded with JavaScript');
 const buttons=await ev(`(()=>{const a=document.querySelector('[data-action-key="action-ticket"]'),b=document.querySelector('[data-action-key="action-complaint"]');const visible=e=>e&&e.checkVisibility()&&e.getBoundingClientRect().top<innerHeight;return {visible:visible(a)&&visible(b),same:a?.parentElement===b?.parentElement,label:b?.textContent.trim()};})()`);

 check(buttons.visible&&buttons.same&&buttons.label==='Submit Complaint','Submit Complaint visibly beside New support ticket');
 await shot('complaint-deployed-desktop');
 await ev(`document.querySelector('[data-action-key="action-complaint"]').click()`);await sleep(250);
 check(await ev(`(()=>{const f=document.querySelector('dialog[open] #new-complaint form');return !!f&&f.elements.type.value==='COMPLAINT'&&f.elements.subject&&f.elements.description&&[...f.elements.priority.options].map(o=>o.value).join(',')==='LOW,MEDIUM,HIGH';})()`),'complaint button opens subject description and LOW MEDIUM HIGH form');
 await shot('complaint-deployed-form');
 const subject='Complaint UI verification '+Date.now();
 await ev(`(()=>{const f=document.querySelector('dialog[open] #new-complaint form');f.subject.value=${JSON.stringify(subject)};f.description.value='Local deployment verification of the existing complaint workflow.';f.priority.value='HIGH';f.requestSubmit();})()`);await sleep(900);
 check(await ev(`document.body.innerText.includes('Your complaint was submitted successfully')`),'complaint submission shows success');
 const id=await ev(`(()=>{const t=[...document.querySelectorAll('.support-ticket')].find(t=>t.textContent.includes(${JSON.stringify(subject)}));return t?.id;})()`);
 check(!!id,'complaint stored in existing customer history');
 await nav('/customer/requests#'+id);
 check(await ev(`(()=>{const t=document.getElementById(${JSON.stringify(id)});return t?.checkVisibility()&&t.innerText.includes('COMPLAINT')&&t.textContent.includes('OPEN')&&t.textContent.includes('HIGH');})()`),'selected complaint visibly labelled COMPLAINT with OPEN and HIGH');
 await shot('complaint-deployed-history');
 await nav('/customer/requests#support-workspace');
 check(await ev(`document.querySelectorAll('#${id}').length===1`),'refresh retains one complaint entry');
 await call('Emulation.setDeviceMetricsOverride',{width:390,height:844,deviceScaleFactor:1,mobile:true});
 await nav('/customer/requests#support-workspace');
 check(await ev(`document.querySelector('[data-action-key="action-complaint"]').checkVisibility()&&document.documentElement.scrollWidth<=innerWidth+1`),'mobile complaint button visible without page overflow');
 await shot('complaint-deployed-mobile');
 await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});
 await ev(`document.documentElement.setAttribute('data-theme','dark')`);await shot('complaint-deployed-dark');
 await ev(`document.querySelector('[data-action-key="action-ticket"]').click()`);await sleep(150);
 check(await ev(`!!document.querySelector('dialog[open] #new-support-ticket form select[name="type"]')`),'normal support ticket form still opens');
 fs.writeFileSync('verification/complaint-deployed-browser.json',JSON.stringify({base,route:'/customer/requests#support-workspace',ticketId:id,subject,checks:results},null,2));
 console.log('ALL '+results.length+' DEPLOYED BROWSER CHECKS PASSED');
}finally{ws.close();}
