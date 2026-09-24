import fs from 'node:fs';
import assert from 'node:assert/strict';
const targets = await (await fetch('http://127.0.0.1:9226/json')).json();
const ws = new WebSocket(targets.find(t => t.type === 'page').webSocketDebuggerUrl);
await new Promise(r => ws.addEventListener('open', r, {once:true}));
let id = 0;
const pending = new Map(), errors = [], responses = [];
ws.addEventListener('message', e => {
    const m = JSON.parse(e.data);
    if (m.id) { const p = pending.get(m.id); pending.delete(m.id); m.error ? p.reject(m.error) : p.resolve(m.result); }
    if (m.method === 'Runtime.exceptionThrown') errors.push(m.params.exceptionDetails.text);
    if (m.method === 'Network.responseReceived' && m.params.response.status >= 400) responses.push({url:m.params.response.url,status:m.params.response.status});
});
const call = (method, params={}) => new Promise((resolve,reject) => { pending.set(++id,{resolve,reject}); ws.send(JSON.stringify({id,method,params})); });
const evaluate = async expression => {
    const result = await call('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true});
    if (result.exceptionDetails) throw Error(JSON.stringify(result.exceptionDetails));
    return result.result.value;
};
const delay = ms => new Promise(r => setTimeout(r,ms));
async function navigate(path) {
    await call('Page.navigate',{url:'http://localhost:8080/WebBasedBankingSystem'+path});
    for(let i=0;i<80;i++) { await delay(100); if(await evaluate(`document.readyState === 'complete' && location.pathname === ${JSON.stringify('/WebBasedBankingSystem'+path)}`)) break; }
    await evaluate('document.fonts.ready');
    await delay(800);
}
await call('Page.enable'); await call('Runtime.enable'); await call('Network.enable'); await call('Network.setCacheDisabled',{cacheDisabled:true});
await navigate('/');

const results=[];
await call('Emulation.setDeviceMetricsOverride',{width:1440,height:1000,deviceScaleFactor:1,mobile:false});
for(const theme of ['dark','light']) {
 await evaluate(`localStorage.setItem('lankatrust-theme','${theme}')`);
 await navigate('/');
 await delay(700);
 const first=await evaluate(`({card:getComputedStyle(document.querySelector('.premium-card')).transform,orbit:getComputedStyle(document.querySelector('.visual-orbit')).transform,animations:document.getAnimations().map(a=>({name:a.animationName,state:a.playState})),features:document.querySelectorAll('.trust-feature').length})`);
 await delay(500);
 const second=await evaluate(`({card:getComputedStyle(document.querySelector('.premium-card')).transform,orbit:getComputedStyle(document.querySelector('.visual-orbit')).transform})`);
 assert.notEqual(first.card,second.card);assert.notEqual(first.orbit,second.orbit);assert.equal(first.features,4);
 results.push({theme,movingCard:true,movingOrbit:true,...first});
 const shot=await call('Page.captureScreenshot',{format:'png'});fs.writeFileSync(`verification/cinematic-${theme}-hero-final.png`,Buffer.from(shot.data,'base64'));
 await evaluate(`document.querySelector('#personal').scrollIntoView()`);await delay(1100);
 assert.equal(await evaluate(`document.querySelector('.service-row>a').classList.contains('is-visible')`),true);
 const sections=await call('Page.captureScreenshot',{format:'png'});fs.writeFileSync(`verification/cinematic-${theme}-sections-final.png`,Buffer.from(sections.data,'base64'));
}
await call('Emulation.setEmulatedMedia',{features:[{name:'prefers-reduced-motion',value:'reduce'}]});
await navigate('/');
assert.equal(await evaluate(`document.getAnimations().filter(a=>a.playState==='running').length`),0);
assert.equal(await evaluate(`[...document.querySelectorAll('.motion-ready')].some(e=>getComputedStyle(e).opacity==='0')`),false);
await call('Emulation.setEmulatedMedia',{features:[{name:'prefers-reduced-motion',value:'no-preference'}]});
console.log(JSON.stringify({results,reducedMotion:true,scrollReveal:true,errors},null,2));
fs.writeFileSync('verification/cinematic-motion-results.json',JSON.stringify({results,reducedMotion:true,scrollReveal:true,errors},null,2));
ws.close();
