import fs from 'node:fs';
import path from 'node:path';
import {spawn,spawnSync} from 'node:child_process';

const base=process.argv[2];
const original=spawnSync('node',['target/task3-tools/notification-browser.mjs',base],{stdio:'inherit',windowsHide:true,timeout:100000});
if(original.status!==0)throw new Error('Existing notification browser checks failed');
const profile=path.resolve('target/task3-chrome-'+Date.now());fs.mkdirSync(profile,{recursive:true});
const chrome=process.env.NOTIFICATION_CHROME_PORT?null:spawn('C:/Program Files/Google/Chrome/Application/chrome.exe',['--headless=new','--disable-gpu','--no-first-run','--remote-debugging-port=0',`--user-data-dir=${profile}`,'about:blank'],{stdio:'ignore',windowsHide:true});
let ws,serial=0,checks=0;const failures=[],errors=[],pending=new Map();
const sleep=ms=>new Promise(r=>setTimeout(r,ms));
const check=(ok,label,detail)=>{checks++;if(!ok)failures.push({label,detail});};
let current='startup';
try {
 const portFile=path.join(profile,'DevToolsActivePort');if(chrome)for(let i=0;i<100&&!fs.existsSync(portFile);i++)await sleep(100);
 const port=process.env.NOTIFICATION_CHROME_PORT||fs.readFileSync(portFile,'utf8').split('\n')[0];
 const targets=await(await fetch(`http://127.0.0.1:${port}/json`)).json();ws=new WebSocket(targets.find(p=>p.type==='page').webSocketDebuggerUrl);await new Promise(r=>ws.addEventListener('open',r,{once:true}));
 ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result);}else if(m.method==='Runtime.exceptionThrown')errors.push({page:current,error:m.params.exceptionDetails});else if(m.method==='Runtime.consoleAPICalled'&&m.params.type==='error')errors.push({page:current,error:m.params.args});});
 const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++serial,{resolve,reject});ws.send(JSON.stringify({id:serial,method,params}));});
 const ev=async expression=>{const result=await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});if(result.exceptionDetails)throw new Error(JSON.stringify(result.exceptionDetails));return result.result.value;};
 await call('Page.enable');await call('Runtime.enable');await call('Network.enable');
 const nav=async route=>{current=route;await call('Page.navigate',{url:base+route});await sleep(100);for(let i=0;i<150;i++){try{if(await ev('document.readyState==="complete"')){await ev('document.fonts.ready');return;}}catch{}await sleep(60);}throw new Error('Navigation timed out: '+route);};
 const waitPage=async()=>{await sleep(250);for(let i=0;i<100;i++){try{if(await ev('document.readyState==="complete"'))return;}catch{}await sleep(50);}};
 const identity=async(id,role)=>nav(`/fixture?id=${id}&role=${role}`);
 const shot=async name=>{await sleep(350);fs.writeFileSync(`verification/task3-${name}.png`,Buffer.from((await call('Page.captureScreenshot',{format:'png',captureBeyondViewport:false})).data,'base64'));};
 const key=async(key,code=key,vk)=>{await call('Input.dispatchKeyEvent',{type:'keyDown',key,code,windowsVirtualKeyCode:vk});await call('Input.dispatchKeyEvent',{type:'keyUp',key,code,windowsVirtualKeyCode:vk});};
 const size=async width=>call('Emulation.setDeviceMetricsOverride',{width,height:900,deviceScaleFactor:1,mobile:false});
 const roles=['CUSTOMER_SERVICE_OFFICER','LOAN_OFFICER','CARD_SERVICES_OFFICER','INVESTMENT_OFFICER','COMPLIANCE_RISK_OFFICER','SYSTEM_ADMIN'];
 const pages=[['PUBLIC',0,'/'],['PUBLIC',0,'/login.jsp'],['PUBLIC',0,'/register.jsp'],...['dashboard','accounts','transactions','transfer','payments','payment-plans','cards','loans','investments','requests','tickets','settings','products','notifications'].map(s=>['CUSTOMER',1,'/customer/'+s]),...roles.map((r,i)=>[r,i+1,'/employee/dashboard']),['SYSTEM_ADMIN',6,'/employee/notifications']];
 for(const theme of ['light','dark'])for(const width of [1440,1024,768,390]){
  await size(width);let lastRole='';
  for(const [role,id,route] of pages){
   if(role!==lastRole){if(role==='PUBLIC')await call('Network.clearBrowserCookies');else await identity(id,role);lastRole=role;}
   await ev(`localStorage.setItem('lankatrust-theme','${theme}')`);await nav(route);
   const result=await ev(`({title:document.title,overflow:document.documentElement.scrollWidth>innerWidth+2,error:/HTTP Status 500|Unable to load|JasperException|temporarily unavailable/.test(document.body.innerText),design:!!document.querySelector('link[href$="design-system.css"]'),wide:[...document.querySelectorAll('body *')].filter(e=>{const r=e.getBoundingClientRect();return r.width&&r.right>innerWidth+2&&!e.closest('.ui-table-scroll,.table-wrapper,.table-container');}).slice(0,6).map(e=>e.className)})`);
   check(!result.error&&result.design,`${theme} ${width} ${role} ${route}: rendered`,result);
   check(!result.overflow,`${theme} ${width} ${role} ${route}: viewport fits`,result);
   if([1440,390].includes(width)&&['/','/login.jsp','/customer/dashboard','/customer/tickets','/customer/notifications'].includes(route))await shot(`${theme}-${width}-${route.replaceAll('/','-')||'home'}`);
   if(width===1440&&role==='SYSTEM_ADMIN'&&route==='/employee/dashboard')await shot(`${theme}-admin`);
   if(width===390&&role==='CUSTOMER_SERVICE_OFFICER')await shot(`${theme}-mobile-staff`);
   if([1440,390].includes(width)&&route==='/customer/transactions'){
    check(await ev('!!document.querySelector("table caption")&&document.querySelector(".ui-table-scroll").tabIndex===0'),'transaction table has caption and keyboard scroll region');await shot(`${theme}-${width}-transactions`);
   }
   if(width===1440&&route==='/customer/tickets'){
    await ev('document.querySelector(".ticket-conversation").scrollIntoView({block:"start"})');await shot(`${theme}-conversation`);
    check(await ev('!!document.querySelector("[data-sender=customer]")&&!!document.querySelector("[data-sender=staff]")&&!!document.querySelector("[data-sender=system]")'),'support distinguishes customer staff and timeline events');
   }
   if(width===1440&&role==='CARD_SERVICES_OFFICER'){
    await ev('(()=>{const t=document.querySelector(".support-ticket");if(t){t.querySelector("details").open=true;t.scrollIntoView({block:"start"})}})()');await shot(`${theme}-assigned-ticket`);
   }
   if(width===390&&route==='/customer/dashboard'){
    await ev('document.querySelector(".sidebar-toggle").click()');check(await ev('document.body.classList.contains("nav-open")&&!document.querySelector(".customer-sidebar").inert'),'mobile navigation opens');
    await key('Escape','Escape',27);check(await ev('!document.body.classList.contains("nav-open")&&document.activeElement.matches(".sidebar-toggle")'),'mobile Escape closes and restores focus');
    await ev('document.querySelector(".notify-bell").click()');check(await ev('(()=>{const r=document.querySelector(".notify-popup").getBoundingClientRect();return r.left>=0&&r.right<=innerWidth})()'),'mobile notification popup fits');await key('Escape','Escape',27);
   }
  }
 }
 await size(1440);await call('Network.clearBrowserCookies');await nav('/login.jsp');
 await ev(`(()=>{const f=document.querySelector('form');f.email.value='one@example.invalid';f.password.value='Fixture-Login-Only-2026!';f.requestSubmit();})()`);await waitPage();check(await ev('location.pathname==="/customer/dashboard"'),'real Chrome password login');
 await ev('document.querySelector("#balanceToggle").click()');check(await ev('document.querySelector("#balanceToggle").getAttribute("aria-pressed")==="true"'),'balance privacy toggle');
 await ev('document.querySelector(".theme-toggle").click()');const savedTheme=await ev('document.documentElement.dataset.theme');await nav('/customer/accounts');check(await ev('document.documentElement.dataset.theme')===savedTheme,'theme persists between routes');
 await ev('document.querySelector(".sidebar-toggle").click()');check(await ev('document.body.classList.contains("sidebar-collapsed")'),'desktop sidebar collapses');await ev('document.querySelector(".sidebar-toggle").click()');
 await nav('/customer/tickets');
 await ev(`(()=>{const f=document.querySelector('input[value="ticket"]').form;f.subject.value='UI verification: payment enquiry';f.description.value='Please review this demonstration payment enquiry.';f.requestSubmit();})()`);await waitPage();
 check(await ev('!!document.querySelector(".ui-toast")&&!!document.querySelector(".notice[role=status]")'),'server success produces toast and retained inline feedback');await shot('success-toast');
 const ticket=await ev(`[...document.querySelectorAll('.support-ticket')].find(t=>t.textContent.includes('UI verification: payment enquiry')).id`);
 await ev(`(()=>{const t=document.getElementById('${ticket}');t.querySelector('details').open=true;t.querySelector('input[value="ticket-close"]').form.querySelector('button').click();})()`);
 check(await ev('document.querySelector(".ui-dialog").open&&document.activeElement.textContent==="Keep editing"'),'close-ticket confirmation opens with safe initial focus');await shot('confirmation');
 check(await ev('(()=>{const r=document.querySelector(".ui-dialog").getBoundingClientRect();return Math.abs(r.x+r.width/2-document.documentElement.clientWidth/2)<2&&Math.abs(r.y+r.height/2-innerHeight/2)<2})()'),'confirmation centered in viewport');
 await key('Tab','Tab',9);check(await ev('document.querySelector(".ui-dialog").contains(document.activeElement)'),'dialog keyboard focus stays inside');
 await key('Escape','Escape',27);check(await ev('!document.querySelector(".ui-dialog").open'),'Escape cancels confirmation');
 check(await ev(`!!document.getElementById('${ticket}').querySelector('.ticket-reply')`),'cancelled confirmation preserves ticket');
 await ev(`document.getElementById('${ticket}').querySelector('input[value="ticket-close"]').form.querySelector('button').click()`);await ev('document.querySelector(".ui-dialog .ui-danger").click()');await waitPage();
 check(await ev(`!document.getElementById('${ticket}').querySelector('.ticket-reply')&&document.getElementById('${ticket}').textContent.includes('CLOSED')`),'confirmed close submits original form and CSRF');
 await nav('/customer/payments');
 await ev(`(()=>{const f=document.querySelector('form[action$="/customer/payments"]');f.recipient.value='CEB / Electricity';f.billReference.value='UI-CHECK';f.amount.value='999999999';f.requestSubmit();})()`);await waitPage();
 check(await ev('!!document.querySelector(".ui-toast.is-error")'),'server payment error produces persistent toast');await shot('error-toast');
 await sleep(9200);check(await ev('!!document.querySelector(".ui-toast.is-error")'),'error toast does not expire');await ev('document.querySelector(".ui-toast button").click()');check(await ev('!document.querySelector(".ui-toast")&&!!document.querySelector(".message.error")'),'dismissal retains critical inline error');
 await call('Emulation.setEmulatedMedia',{features:[{name:'prefers-reduced-motion',value:'reduce'}]});await nav('/customer/dashboard');
 check(await ev('getComputedStyle(document.querySelector(".dashboard-content")).animationName==="none"'),'reduced motion disables entry animation');
 await ev('document.querySelector(".notify-bell").click()');check(await ev('getComputedStyle(document.querySelector(".notify-popup")).animationName==="none"'),'reduced motion disables notification animation');
 await call('Emulation.setEmulatedMedia',{features:[]});
 await nav('/logout');check(await ev('location.pathname==="/login.jsp"'),'real browser logout');
 check(errors.length===0,'no uncaught JavaScript exceptions or console errors',errors);
 console.log(`TASK3 BROWSER: ${checks-failures.length}/${checks} checks passed`);
 if(failures.length)console.log(JSON.stringify(failures,null,2));
} catch(error) {failures.push({label:'Browser harness',error:String(error),page:current});console.error(error);}
finally {fs.writeFileSync('verification/task3-browser-results.json',JSON.stringify({checks,passed:checks-failures.length,failures,consoleErrors:errors,sizes:[1440,1024,768,390]},null,2));ws?.close();chrome?.kill();}
if(failures.length)process.exitCode=1;
