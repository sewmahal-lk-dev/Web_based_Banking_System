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
const checks = [], failures = [];
for (const theme of ['dark','light']) {
    await evaluate(`localStorage.setItem('lankatrust-theme', '${theme}')`);
    for (const width of [1440,1366,1280,1024,768,390,320]) {
        await call('Emulation.setDeviceMetricsOverride',{width,height:960,deviceScaleFactor:1,mobile:false});
        for (const path of ['/','/login.jsp','/register.jsp']) {
            await navigate(path);
            const result = await evaluate(`({theme:document.documentElement.dataset.theme,title:document.title,overflow:document.documentElement.scrollWidth>innerWidth,button:document.querySelector('.theme-toggle')?.textContent.trim(),visibleTheme:document.querySelector('.theme-toggle')?.getBoundingClientRect().width>0,badPage:/HTTP Status 500/.test(document.body.innerText),wide:[...document.querySelectorAll('body *')].filter(e=>{const r=e.getBoundingClientRect();return r.width&& (r.right>innerWidth+1 || r.left < -1)&&!e.closest('.hero,.card-stage,.account-showcase')}).map(e=>e.className).slice(0,8),background:getComputedStyle(document.body).backgroundColor,labelColor:document.querySelector('.form-group label')?getComputedStyle(document.querySelector('.form-group label')).color:null})`);
            checks.push({theme,width,path,...result});
            if(theme==='dark' && path!=='/' && result.labelColor!=='rgb(248, 236, 217)') failures.push({theme,width,path,labelColor:result.labelColor});
            if(!result.visibleTheme || result.overflow || result.badPage || result.theme!==theme || result.button!==(theme==='dark'?'Light mode':'Dark mode')) failures.push(checks.at(-1));
            if ([1440,390].includes(width)) {
                const shot=await call('Page.captureScreenshot',{format:'png',captureBeyondViewport:false});
                fs.writeFileSync(`verification/cinematic-${theme}-${width}-${path==='/'?'home':path.slice(1,-4)}.png`,Buffer.from(shot.data,'base64'));
            }
            if(path==='/' && width<1320) {
                const menu=await evaluate(`(()=>{const b=document.querySelector('.menu-toggle');b.click();return b.getAttribute('aria-expanded')==='true'&&getComputedStyle(document.querySelector('#nav-links')).display!=='none'&&document.documentElement.scrollWidth<=innerWidth})()`);
                if(!menu) failures.push({theme,width,menu:false});
                await call('Input.dispatchKeyEvent',{type:'keyDown',key:'Escape'});
                assert.equal(await evaluate(`document.querySelector('.menu-toggle').getAttribute('aria-expanded')`),'false');
            }
        }
    }
}
await navigate('/');
await evaluate(`document.querySelector('.theme-toggle').click()`);
assert.equal(await evaluate(`localStorage.getItem('lankatrust-theme')`),'dark');
await navigate('/login.jsp');
assert.equal(await evaluate(`document.documentElement.dataset.theme`),'dark');
await evaluate(`document.querySelector('#showPassword').click()`);
assert.equal(await evaluate(`document.querySelector('input[name=password]').type`),'text');
await call('Page.reload'); await delay(500);
assert.equal(await evaluate(`document.documentElement.dataset.theme`),'dark');
const report={checks,failures,errors,unexpectedHttpErrors:responses.filter(r=>!r.url.endsWith('/colombo-lotus.jpg')&&!r.url.endsWith('/favicon.ico')),missingSkyline:responses.some(r=>r.url.endsWith('/colombo-lotus.jpg'))};
fs.writeFileSync('verification/cinematic-browser-results.json',JSON.stringify(report,null,2));
console.log(JSON.stringify({checks:checks.length,failures,errors,unexpectedHttpErrors:report.unexpectedHttpErrors,missingSkyline:report.missingSkyline},null,2));
ws.close();
if(failures.length||errors.length||report.unexpectedHttpErrors.length) process.exitCode=1;
