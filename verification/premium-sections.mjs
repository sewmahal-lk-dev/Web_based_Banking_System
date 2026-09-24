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
    await call('Page.navigate',{url:'http://127.0.0.1:8766/bank'+path});
    for(let i=0;i<80;i++) { await delay(100); if(await evaluate(`document.readyState === 'complete' && location.pathname === ${JSON.stringify('/bank'+path)}`)) break; }
    await evaluate('document.fonts.ready');
    await delay(800);
}
await call('Page.enable'); await call('Runtime.enable'); await call('Network.enable');
await navigate('/');

await call('Emulation.setDeviceMetricsOverride',{width:1440,height:960,deviceScaleFactor:1,mobile:false});
for(const theme of ['dark','light']) {
 await evaluate(`localStorage.setItem('lankatrust-theme','${theme}')`);
 await navigate('/');
 const colors=await evaluate(`([...document.querySelectorAll('main > section')].map(s=>({id:s.id,bg:getComputedStyle(s).backgroundColor,text:getComputedStyle(s.querySelector('h2')||s.querySelector('h1')).color})))`);
 console.log(theme,JSON.stringify(colors));
 await evaluate(`document.querySelector('#personal').scrollIntoView()`);
 await delay(250);
 const shot=await call('Page.captureScreenshot',{format:'png'});
 fs.writeFileSync(`verification/premium-${theme}-sections.png`,Buffer.from(shot.data,'base64'));
}
ws.close();
