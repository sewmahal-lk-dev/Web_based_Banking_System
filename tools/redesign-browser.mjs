import fs from 'node:fs';
const targets=await (await fetch('http://127.0.0.1:9225/json')).json();
const ws=new WebSocket(targets.find(t=>t.type==='page').webSocketDebuggerUrl);
await new Promise(r=>ws.addEventListener('open',r,{once:true}));
let id=0;const pending=new Map();
ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(m.id){const p=pending.get(m.id);pending.delete(m.id);m.error?p.reject(m.error):p.resolve(m.result)}});
const call=(method,params={})=>new Promise((resolve,reject)=>{pending.set(++id,{resolve,reject});ws.send(JSON.stringify({id,method,params}))});
const evaluate=async expression=>(await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result.value;
await call('Page.enable');await call('Network.enable');
const cookies=Object.fromEntries(fs.readFileSync('target/ui-sessions.txt','utf8').trim().split(/\r?\n/).map(s=>s.split('=')));
const failures=[];let checks=0;
for(const theme of ['light','dark']) for(const width of [1440,1280,768,390]){
 await call('Emulation.setDeviceMetricsOverride',{width,height:900,deviceScaleFactor:1,mobile:false});
 const routes=[['PUBLIC','/'],['PUBLIC','/login.jsp'],['PUBLIC','/register.jsp'],...['dashboard','accounts','transactions','transfer','payments','cards','loans','investments','requests','settings','products'].map(s=>['CUSTOMER','/customer/'+s]),...Object.keys(cookies).filter(r=>r!=='CUSTOMER').map(r=>[r,'/employee/dashboard'])];
 for(const [role,path] of routes){
  await call('Network.clearBrowserCookies');if(cookies[role])await call('Network.setCookie',{name:'JSESSIONID',value:cookies[role],url:'http://127.0.0.1:8765/bank/'});
  await call('Page.navigate',{url:'http://127.0.0.1:8765/bank'+path});
  await new Promise(r=>setTimeout(r,450));
  await evaluate('document.fonts.ready');
  await evaluate("localStorage.setItem('lankatrust-theme','"+theme+"');document.documentElement.dataset.theme='"+theme+"';document.documentElement.style.colorScheme='"+theme+"'");
  await evaluate("Promise.all([...document.images].map(i=>{i.loading='eager';return i.decode().catch(()=>{});} ))");
  const result=await evaluate(`({title:document.title,overflow:document.documentElement.scrollWidth>innerWidth+1,badImages:[...document.images].filter(i=>!i.complete||!i.naturalWidth).map(i=>i.src),badText:/HTTP Status 500|Aureus|Unable to load/.test(document.body.innerText),wide:[...document.querySelectorAll('body *')].filter(e=>e.getBoundingClientRect().right>innerWidth+2).slice(0,6).map(e=>e.className)})`);
  if(result.overflow||result.badImages.length||result.badText)failures.push({theme,width,role,path,...result});checks++;
  if((width===1440||width===390)&&['/','/login.jsp','/register.jsp','/customer/dashboard','/customer/transfer'].includes(path)||width===390&&role==='SYSTEM_ADMIN'){
   const shot=await call('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});fs.writeFileSync('target/redesign-'+theme+'-'+width+'-'+role+'-'+(path.replaceAll('/','-')||'home')+'.png',Buffer.from(shot.data,'base64'));
  }
  if(path==='/'&&width===390){await evaluate("document.querySelector('.menu-toggle').click()");const open=await evaluate("document.querySelector('.menu-toggle').getAttribute('aria-expanded')==='true' && getComputedStyle(document.querySelector('#nav-links')).display!=='none'");if(!open)failures.push({menu:'failed'});checks++;}
 }
}
fs.writeFileSync('target/redesign-browser-result.json',JSON.stringify({checks,failures},null,2));console.log(JSON.stringify({checks,failures},null,2));ws.close();
