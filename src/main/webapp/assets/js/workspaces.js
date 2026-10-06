/* Progressive app workspaces. The server remains responsible for data, access and actions. */
'use strict';
(() => {
  const body=document.body;
  const $=(selector,root=document)=>root.querySelector(selector);
  const $$=(selector,root=document)=>[...root.querySelectorAll(selector)];
  const el=(tag,cls,text)=>{const node=document.createElement(tag);if(cls)node.className=cls;if(text!==undefined)node.textContent=text;return node;};
  const icons={dashboard:'M3 11 12 3l9 8M5 10v10h5v-6h4v6h5V10',accounts:'M3 9h18M4 20h16M6 10v7m6-7v7m6-7v7M3 7l9-4 9 4',transfer:'M4 7h15m-4-4 4 4-4 4M20 17H5m4-4-4 4 4 4',payments:'M6 3h12v18l-3-2-3 2-3-2-3 2V3m3 5h6m-6 4h6',cards:'M3 5h18v14H3V5m0 5h18M6 15h4',loans:'M4 20V10l8-7 8 7v10H4m5 0v-7h6v7',investments:'M4 19V5m0 14h16M7 14l4-4 4 2 5-7m-5 0h5v5',support:'M4 4h16v13H9l-5 4V4m4 5h8m-8 4h5',people:'M8 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8M2 21v-3a6 6 0 0 1 12 0v3m2-17a4 4 0 0 1 0 8m2 3a5 5 0 0 1 4 5',audit:'M6 3h12v18H6V3m3 5h6m-6 4h6m-6 4h4',search:'M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14m5 12 6 6',plus:'M12 4v16M4 12h16',close:'M6 6l12 12M18 6 6 18',notification:'M6 8a6 6 0 0 1 12 0v6l2 3H4l2-3V8m4 12h4',copy:'M8 8h12v13H8V8M16 8V3H3v13h5'};
  function icon(name){const svg=document.createElementNS('http://www.w3.org/2000/svg','svg');svg.setAttribute('viewBox','0 0 24 24');svg.setAttribute('class','ui-icon');svg.setAttribute('aria-hidden','true');const path=document.createElementNS(svg.namespaceURI,'path');path.setAttribute('d',icons[name]||icons.accounts);svg.append(path);return svg;}
  function button(text,fn,cls='workspace-button'){const b=el('button',cls,text);b.type='button';b.addEventListener('click',fn);return b;}
  const labelOf=node=>node?.textContent.replace(/\s+/g,' ').trim()||'';
  const slug=text=>text.toLowerCase().replace(/[^a-z0-9]+/g,'-').replace(/^-|-$/g,'');
  const iconFor=text=>/loan/i.test(text)?'loans':/investment/i.test(text)?'investments':/card/i.test(text)?'cards':/support|ticket|request|complaint/i.test(text)?'support':/employee|customer/i.test(text)?'people':/audit|repay/i.test(text)?'audit':/cash|pay/i.test(text)?'payments':'accounts';

  if(body.classList.contains('public-site')){
    const nav=$('.nav');const access=el('div','public-access');
    $$('.nav-links a',nav).filter(a=>/login\.jsp|register\.jsp/.test(a.href)).forEach(a=>access.append(a.cloneNode(true)));nav.insertBefore(access,$('.menu-toggle',nav));
    if('IntersectionObserver' in window&&!matchMedia('(prefers-reduced-motion: reduce)').matches){const observer=new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting){entry.target.classList.add('is-revealed');observer.unobserve(entry.target);}}),{threshold:.08});$$('.public-site main>.section').forEach(s=>{s.classList.add('workspace-reveal');observer.observe(s);});}
    return;
  }
  const staff=body.classList.contains('staff-portal');
  const customer=body.classList.contains('customer-portal');
  if(!staff&&!customer&&!body.classList.contains('notifications-page'))return;
  if(typeof HTMLDialogElement==='undefined')return; // The original server pages remain usable.
  const main=$('main'),content=$('.dashboard-content,.content',main)||main;
  const contextPath=($('a[href*="/logout"]')?.getAttribute('href')||'').replace(/\/logout$/,'');
  const actions=new Map(),workspaces=new Map(),collections=[];
  let currentWorkspace='',actionSequence=0,activeAction=null,activeRecord=null,previousActionHash='';
  let support=null;
  const originalControls=[];

  function makeDialog(cls,title){const d=el('dialog','workspace-dialog '+cls);const head=el('header','workspace-dialog-header');const heading=el('h2','',title);heading.id='dialog-title-'+(++actionSequence);d.setAttribute('aria-labelledby',heading.id);const close=button('',()=>d.close(),'dialog-close');close.append(icon('close'));close.setAttribute('aria-label','Close '+title);head.append(heading,close);const region=el('div','workspace-dialog-body services-content');d.append(head,region);body.append(d);return {dialog:d,heading,region,close};}
  const actionView=makeDialog('action-dialog','Banking action');
  const detailView=makeDialog('record-drawer','Record details');
  const commandView=makeDialog('command-dialog','Find a banking action');

  function restoreAction(){if(!activeAction)return;activeAction.placeholder.after(activeAction.node);activeAction.node.hidden=true;activeAction=null;}
  actionView.dialog.addEventListener('close',()=>{if(actionView.dialog.open)return;restoreAction();if(location.hash.startsWith('#action-'))history.replaceState(null,'',location.pathname+location.search+previousActionHash);});
  detailView.dialog.addEventListener('close',()=>{if(detailView.dialog.open)return;if(activeRecord){activeRecord.store.append(activeRecord.record);activeRecord=null;}});
  function openAction(key){const action=actions.get(key);if(!action)return false;
    if(activeAction?.key===key)return true;
    restoreAction();activeAction=action;action.node.hidden=false;actionView.heading.textContent=action.title;actionView.region.replaceChildren();
    if(action.context)actionView.region.append(el('p','action-context',action.context));
    const error=$('.notice.error,.message.error,.error-box',action.node)||$('.notice.error,.message.error,.error-box',action.placeholder.parentElement);
    if(error){const feedback=el('p','action-error',labelOf(error));feedback.setAttribute('role','alert');actionView.region.append(feedback);}
    actionView.region.append(action.node);if(!actionView.dialog.open)actionView.dialog.showModal();
    requestAnimationFrame(()=>{const input=$('input:not([type=hidden]):not([type=checkbox]),select,textarea',action.node);(input||actionView.close).focus();});return true;
  }
  function navigate(hash){if(hash.startsWith('#action-'))previousActionHash=location.hash.startsWith('#action-')?'':location.hash;history.pushState(null,'',hash);route(true);}
  function registerAction(node,title,key,context=''){
    if(node.dataset.actionReady)return node.dataset.actionReady;
    key=key||'action-'+slug(title)+'-'+(++actionSequence);
    while(actions.has(key))key+='-'+(++actionSequence);
    const placeholder=document.createComment('Original '+title+' location');node.before(placeholder);node.hidden=true;node.dataset.actionReady=key;
    const action={node,title,key,placeholder,context,workspace:''};actions.set(key,action);originalControls.push(node);
    const launch=button(title,()=>navigate('#'+key),'workspace-button action-launch');launch.dataset.actionKey=key;launch.prepend(icon(/new|apply|request|create/i.test(title)?'plus':iconFor(title)));placeholder.before(launch);action.launch=launch;
    return key;
  }

  // Individual records become searchable summaries; the original details/actions move into a drawer.
  function fields(record){return $$('dl>div',record).map(div=>({label:labelOf($('dt',div)),value:labelOf($('dd',div))}));}
  function collectRecords(panel,title){
    const records=$$('.record',panel).filter(r=>!r.closest('.support-ticket')&&!r.closest('#cash-transactions')&&!r.closest('#card-closure')&&r.closest('.panel')===panel);
    if(!records.length)return null;
    const store=el('div','record-store');store.hidden=true;panel.append(store);
    const index=records.map((record,i)=>{const info=fields(record);return {record,info,text:info.map(f=>f.value).join(' ').toLowerCase(),state:info.find(f=>/^status$/i.test(f.label))?.value||'',id:i};});
    const area=el('div','record-browser');const toolbar=el('div','collection-toolbar');const search=el('input');search.type='search';search.placeholder='Search '+title.toLowerCase();search.setAttribute('aria-label','Search loaded '+title.toLowerCase());
    const states=el('select');states.setAttribute('aria-label','Filter '+title+' by status');states.append(new Option('All statuses',''));
    [...new Set(index.map(r=>r.state).filter(Boolean))].sort().forEach(s=>states.append(new Option(s.replaceAll('_',' '),s)));toolbar.append(search,states);
    const customerSorting=staff&&body.dataset.role==='SYSTEM_ADMIN'&&title==='Customers';
    let sort=null;
    if(customerSorting){
      toolbar.classList.add('customer-sort-toolbar');
      const label=el('label','customer-sort-control','Sort By');sort=el('select');sort.setAttribute('aria-label','Sort customers');
      [['newest','Newest First'],['oldest','Oldest First'],['name-asc','Name A-Z'],['name-desc','Name Z-A'],['id-desc','Customer ID High-Low'],['id-asc','Customer ID Low-High']].forEach(([value,text])=>sort.append(new Option(text,value)));
      label.append(sort);toolbar.append(label);
    }
    function compareCustomers(a,b){
      const aId=Number(a.record.dataset.customerId),bId=Number(b.record.dataset.customerId);
      if(sort.value==='id-desc')return bId-aId;
      if(sort.value==='id-asc')return aId-bId;
      if(sort.value.startsWith('name-')){
        const aName=a.info.find(f=>f.label==='name')?.value||'',bName=b.info.find(f=>f.label==='name')?.value||'';
        const order=aName.localeCompare(bName,undefined,{sensitivity:'base'});
        return (sort.value==='name-desc'?-order:order)||aId-bId;
      }
      // Match MySQL timestamp ordering, with missing legacy dates before known dates.
      const aDate=a.record.dataset.customerCreated===''?-Infinity:Number(a.record.dataset.customerCreated);
      const bDate=b.record.dataset.customerCreated===''?-Infinity:Number(b.record.dataset.customerCreated);
      const order=(aDate===bDate?0:aDate<bDate?-1:1)||(aId-bId);
      return sort.value==='oldest'?order:-order;
    }
    const region=el('div','workspace-table-region');region.tabIndex=0;region.setAttribute('role','region');region.setAttribute('aria-label',title+' records, scroll for more columns');
    const cardRecords=customer&&body.dataset.workspacePage==='cards';if(cardRecords){$('h2',panel).textContent='My cards / applications';area.classList.add('card-record-browser');}
    const table=el('table','workspace-table');const caption=el('caption','sr-only',title+' currently loaded records');const thead=el('thead'),header=el('tr');
    ['Record','Details','Status','Action'].forEach(text=>{const th=el('th','',text);th.scope='col';header.append(th);});thead.append(header);const tbody=el('tbody');table.append(caption,thead,tbody);region.append(table);
    const pagination=el('div','collection-pagination');const count=el('span');count.setAttribute('aria-live','polite');let page=0;
    const prev=button('Previous',()=>{page--;render();},'ui-ghost');const next=button('Next',()=>{page++;render();},'ui-ghost');pagination.append(count,prev,next);area.append(toolbar,region,pagination);store.before(area);
    records.forEach(record=>store.append(record));
    function render(){const query=search.value.trim().toLowerCase();const filtered=index.filter(r=>(!states.value||r.state===states.value)&&(!query||r.text.includes(query)));if(customerSorting)filtered.sort(compareCustomers);page=Math.max(0,Math.min(page,Math.ceil(filtered.length/6)-1));tbody.replaceChildren();
      filtered.slice(page*6,page*6+6).forEach(item=>{const row=el('tr');const primary=item.info.find(f=>cardRecords?/^card type$/i.test(f.label):/^(name|customer name|product name|recipient|subject)$/i.test(f.label))||item.info[0];const secondary=item.info.filter(f=>cardRecords?/^(card number|daily limit)$/i.test(f.label):f!==primary&&!/^status$/i.test(f.label)).slice(0,2);const identity=el('td');identity.append(el('strong','',primary?.value||'Record '+(item.id+1)));identity.append(el('small','',primary?.label||title));const info=el('td');secondary.forEach(f=>info.append(el('small','',f.label+': '+(cardRecords&&f.label==='daily limit'?'LKR ':'')+f.value)));const status=el('td');const badge=el('span','status-badge',item.state||'Recorded');badge.dataset.state=item.state;status.append(badge);const action=el('td');const open=button('View / manage',()=>{
        if(detailView.dialog.open)detailView.dialog.close();activeRecord={record:item.record,store};detailView.heading.textContent=title+' / '+(primary?.value||'Record');detailView.region.replaceChildren(item.record);detailView.dialog.showModal();detailView.close.focus();
      },'record-open ui-ghost');action.append(open);row.append(identity,info,status,action);tbody.append(row);});
      if(!filtered.length){const row=el('tr'),cell=el('td','collection-empty','No matching records. Try another search or status.');cell.colSpan=4;row.append(cell);tbody.append(row);}
      count.textContent=filtered.length?`${page*6+1}–${Math.min(page*6+6,filtered.length)} of ${filtered.length} loaded records`:'0 matching records';prev.disabled=page===0;next.disabled=(page+1)*6>=filtered.length;
    }
    search.addEventListener('input',()=>{page=0;render();});states.addEventListener('change',()=>{page=0;render();});sort?.addEventListener('change',()=>{page=0;render();});render();
    const collection={panel,title,index,filter:state=>{if(state&&![...states.options].some(option=>option.value===state))states.append(new Option(state.replaceAll('_',' '),state));states.value=state;page=0;render();},render};collections.push(collection);return collection;
  }

  // Card applications use only the existing type field; account and limits are assigned by the server.
  const cardApplication=$('.card-application');
  if(cardApplication){
    const form=$('form',cardApplication),select=$('select[name=type]',form),label=select.closest('label');
    const section=el('section','card-application-section card-type-section');const heading=el('h3','','02 / Card details');heading.id='card-type-heading';
    const choices=el('div','card-type-choices');choices.setAttribute('role','group');choices.setAttribute('aria-labelledby',heading.id);
    label.firstChild.textContent='Card type (required) ';label.classList.add('card-native-type');select.required=true;
    const choiceButtons=[];
    [...select.options].filter(option=>['DEBIT','CREDIT'].includes(option.value)).forEach(option=>{
      const choice=button('',()=>{select.value=option.value;select.dispatchEvent(new Event('change',{bubbles:true}));},'card-type-choice');choice.dataset.cardType=option.value;choice.append(icon('cards'),el('strong','',option.value==='DEBIT'?'Debit Card':'Credit Card'),el('span','',option.value==='DEBIT'?'Linked to your account':'Credit facility'),el('small','card-selection-label','Select'));choices.append(choice);choiceButtons.push(choice);
    });
    function reflectCardType(){choiceButtons.forEach(choice=>{const selected=choice.dataset.cardType===select.value;choice.setAttribute('aria-pressed',String(selected));$('.card-selection-label',choice).textContent=selected?'Selected':'Select';});$$('[data-card-kind]',cardApplication).forEach(node=>node.hidden=node.dataset.cardKind!==select.value);}
    label.before(section);section.append(heading,choices,label);select.addEventListener('change',reflectCardType);reflectCardType();
    const terms=$('.card-application-terms',cardApplication);section.after(terms);
    const submit=$('button:not([type=button])',form);submit.textContent='Submit Application';const footer=el('div','card-application-footer');submit.before(footer);footer.append(button('Cancel',()=>actionView.dialog.close(),'ui-ghost'),submit);
    const accountNote=$('form>p',cardApplication);if(accountNote)accountNote.textContent='Review your card type and linked account before submitting.';
    if($('.card-account-unavailable',cardApplication)){submit.disabled=true;submit.setAttribute('aria-describedby','card-account-heading');}
  }

  // Actions use the original DOM nodes, retaining native validation, listeners and form contracts.
  const shortTitles={'ticket':'New support ticket','ticket-reply':'Reply to ticket','ticket-assign':'Assign department','ticket-status':'Update ticket status','ticket-update':'Edit ticket','schedule':'Schedule payment','cash-deposit':'Cash deposit','cash-withdraw':'Cash withdrawal','limit':'Change card limit','request':'New service request','profile':'Edit profile','password':'Change password','employee-save':'Edit employee','product-save':'Edit product'};
  $$('form').filter(form=>form.method.toLowerCase()==='post'&&!form.closest('.notification-center,.notifications-page,.admin-overview')).forEach(form=>{
    const action=$('[name=action]',form)?.value||'';const record=form.closest('.record,.support-ticket');const title=shortTitles[action]||labelOf($('button:not([type=button]),input[type=submit]',form))||'Banking action';
    // One-click confirmations already handled by banking-ui.js do not need a second action dialog.
    const controls=$$('input:not([type=hidden]),select,textarea',form);
    if(!controls.length)return;
    let node=form,key,display=title;
    if(form.closest('#employee-management')){node=form.closest('#employee-management');key='action-new-employee';display='New employee';}
    else if(form.closest('#product-management')){node=form.closest('#product-management');key='action-new-product';display='New product';}
    else if(action==='apply'){node=form.closest('.panel');key='action-apply';display=labelOf($('h2',node));}
    else if(action==='ticket'){node=form.closest('.panel');const complaint=node.id==='new-complaint';key=complaint?'action-complaint':'action-ticket';if(complaint)display='Submit Complaint';}
    else if(action==='request'){key='action-request';}
    else if(action==='schedule'){key='action-schedule';}
    else if(action.startsWith('cash-'))key='action-'+action;
    else if(/\/customer\/transfer$/.test(form.action)){key='action-transfer';display='Transfer money';}
    else if(/\/customer\/payments$/.test(form.action)){key='action-pay';display='Pay a bill';}
    else if(record){const id=$('[name=id]',form)?.value||(++actionSequence);key='action-'+(action||'edit')+'-'+id;}
    if(controls.length>9&&!key?.startsWith('action-new-'))return;
    const context=record?labelOf($('h3',record))||fields(record).slice(0,3).map(f=>f.label+': '+f.value).join(' · '):'';
    registerAction(node,display,key,context);
  });

  // Keep both ticket entry points visible together after forms move into dialogs.
  if(customer&&actions.has('action-complaint')){
    $$('a[href="#new-complaint"]').forEach(link=>{link.href='#action-complaint';link.addEventListener('click',event=>{event.preventDefault();navigate('#action-complaint');});});
  }

  function setupSupport(center){
    const tickets=$$('.support-ticket',center);if(!tickets.length)return null;
    const layout=el('div','ticket-workbench'),rail=el('aside','ticket-rail'),pane=el('div','ticket-detail-pane');
    const title=el('h3','',staff?'Support queue':'My tickets');const search=el('input');search.type='search';search.placeholder='Find a ticket';search.setAttribute('aria-label','Search loaded tickets');const filters=el('div','ticket-filters');filters.setAttribute('aria-label','Filter tickets by status');
    const list=el('div','ticket-selector-list');const back=button('All tickets',()=>{layout.classList.remove('show-ticket');back.hidden=true;list.querySelector('[aria-current=true]')?.focus();},'ticket-back ui-ghost');back.prepend(icon('support'));back.hidden=true;pane.append(back);
    let state='',selected=null;
    const items=tickets.map(ticket=>{
      const status=$('.ticket-assignment .status-badge',ticket)?.textContent.trim()||labelOf($('.ticket-progress .current',ticket)).replaceAll(' ','_');
      const heading=(customer&&$('.support-reference',ticket)?.textContent.includes('COMPLAINT')?'COMPLAINT / ':'')+labelOf($('h3',ticket));const row=button('',()=>select(ticket,true),'ticket-selector');row.dataset.ticketId=ticket.id;row.append(el('span','ticket-selector-title',heading),el('span','ticket-selector-status',status.replaceAll('_',' ')));row.setAttribute('aria-controls',ticket.id);list.append(row);pane.append(ticket);ticket.hidden=true;
      const department=$$('.ticket-assignment>div',ticket).find(field=>/Assigned Department/i.test(labelOf($('dt',field))));
      const statusLine=el('p','ticket-statusline',status.replaceAll('_',' ')+' · '+labelOf($('dd',department||el('div'))));$('h3',ticket).after(statusLine);
      const commandbar=el('div','ticket-commandbar');$$('[data-action-key]',ticket).filter(launch=>/^action-ticket-(reply|assign|status)-/.test(launch.dataset.actionKey)).forEach(launch=>commandbar.append(launch));statusLine.after(commandbar);
      const details=$('dl.ticket-assignment',ticket);if(details){const metadata=el('details','ticket-metadata');metadata.append(el('summary','','Ticket details / assignment'));details.before(metadata);metadata.append(details);}
      // Staff conversation details open in the selected pane; options stay separate.
      const conversation=$('.ticket-conversation',ticket);if(conversation?.parentElement.tagName==='DETAILS')conversation.parentElement.open=true;
      return {ticket,row,status,heading,text:labelOf(ticket).toLowerCase()};
    });
    ['','OPEN','ASSIGNED','IN_PROGRESS','ESCALATED','RESOLVED','CLOSED'].forEach(value=>{const chip=button(value?value.replaceAll('_',' '):'All',()=>{state=value;render();},'filter-chip');chip.dataset.state=value;filters.append(chip);});
    const empty=el('p','ticket-no-match','No matching tickets.');list.append(empty);
    function render(){const q=search.value.trim().toLowerCase();let count=0;items.forEach(item=>{const visible=(!state||item.status===state)&&(!q||item.text.includes(q));item.row.hidden=!visible;if(visible)count++;});empty.hidden=count>0;$$('button',filters).forEach(chip=>chip.setAttribute('aria-pressed',String(chip.dataset.state===state)));}
    function select(ticket,update){if(update){navigate('#'+ticket.id);return;}selected=ticket;items.forEach(item=>{item.ticket.hidden=item.ticket!==ticket;item.row.setAttribute('aria-current',item.ticket===ticket?'true':'false');});layout.classList.add('show-ticket');back.hidden=!matchMedia('(max-width: 800px)').matches;pane.scrollTop=0;}
    search.addEventListener('input',render);rail.append(title,search,filters,list);if(actions.has('action-ticket'))rail.append(button('New ticket',()=>navigate('#action-ticket'),'workspace-button rail-new-ticket'));layout.append(rail,pane);center.append(layout);render();
    const mobile=matchMedia('(max-width: 800px)');
    function fit(){if(layout.getBoundingClientRect().width)layout.style.height=Math.max(330,innerHeight-layout.getBoundingClientRect().top-28)+'px';}
    mobile.addEventListener('change',()=>{back.hidden=!mobile.matches||!selected;if(!mobile.matches&&!selected)select(tickets[0],false);fit();});window.addEventListener('resize',fit);
    if(!mobile.matches)select(tickets[0],false);
    return {center,items,select,layout,fit};
  }
  const supportCenter=$('.support-center');if(supportCenter)support=setupSupport(supportCenter);

  function makeWorkspace(id,title,iconName,nodes){const section=el('section','bank-workspace');section.dataset.workspace=id;section.setAttribute('aria-label',title);section.hidden=true;nodes[0]?.before(section);nodes.forEach(node=>section.append(node));workspaces.set(id,{section,title,icon:iconName});return section;}
  function selectWorkspace(id,focus=false){const workspace=workspaces.get(id);if(!workspace)return;currentWorkspace=id;for(const [key,item] of workspaces)item.section.hidden=key!==id;
    $$('[data-workspace-link]').forEach(link=>{if(link.dataset.workspaceLink===id)link.setAttribute('aria-current','page');else link.removeAttribute('aria-current');});
    const workspaceHeading=$('.topbar h1');if(staff&&workspaceHeading)workspaceHeading.textContent=workspace.title;
    if(focus){window.scrollTo({top:0,behavior:'instant'});workspace.section.tabIndex=-1;workspace.section.focus({preventScroll:true});}
    if(id==='assigned-support-tickets'||id==='support-workspace')requestAnimationFrame(()=>support?.fit());
  }
  function tile(title,description,target,iconName){const a=el('a','quick-action-tile');a.href=target;a.append(icon(iconName),el('span','',title),el('small','',description),el('span','tile-arrow','↗'));if(target.startsWith('#'))a.addEventListener('click',event=>{event.preventDefault();navigate(target);});return a;}

  if(staff){
    const panels=$$('.panel',content).filter(p=>p.parentElement===content&&!p.dataset.actionReady);
    const groupPanels=panels.filter(p=>p.hasAttribute('data-record-group'));
    const audit=groupPanels.find(p=>/audit/i.test(p.dataset.recordGroup));
    for(const panel of groupPanels){const title=panel.dataset.recordGroup;const nodes=[panel];if(audit===panel&&$('#audit-search'))nodes.unshift($('#audit-search'));makeWorkspace(panel.id,title,iconFor(title),nodes);collectRecords(panel,title);}
    if($('#cash-transactions'))makeWorkspace('cash-transactions','Cash transactions','payments',[$('#cash-transactions')]);
    if($('#card-closure'))makeWorkspace('card-closure','Find / close a card','cards',[$('#card-closure')]);
    if(supportCenter)makeWorkspace('assigned-support-tickets','Support tickets','support',[supportCenter]);
    // Place create actions with their matching management workspace.
    for(const [key,id] of [['action-new-employee','Employees'],['action-new-product','Products']]){const action=actions.get(key),workspace=workspaces.get(id);if(action&&workspace){workspace.section.prepend(action.launch);action.workspace=id;}}
    const nav=$('.operations-sidebar nav');nav?.replaceChildren();
    const dashboard=el('section','bank-workspace operations-home');dashboard.dataset.workspace='workspace-home';dashboard.setAttribute('aria-label','Dashboard');const welcome=$('.staff-welcome');if(welcome)dashboard.append(welcome);else dashboard.append(el('h2','','Your banking workspace'));
    const actionsHeading=el('div','control-actions-heading');actionsHeading.append(el('h3','','Start a task'),el('span','','Your role, your workspace'));dashboard.append(actionsHeading);
    const quick=el('div','quick-action-grid');
    const preferred=body.dataset.role==='SYSTEM_ADMIN'?[['New employee','#action-new-employee','people'],['New product','#action-new-product','plus']]:body.dataset.role==='CUSTOMER_SERVICE_OFFICER'?[['Cash deposit','#action-cash-deposit','payments'],['Cash withdrawal','#action-cash-withdraw','transfer']]:[];
    preferred.forEach(([name,hash,i])=>{if(actions.has(hash.slice(1)))quick.append(tile(name,'Open secure form',hash,i));});
    for(const [id,workspace] of workspaces){
      const grouped=collections.find(collection=>collection.panel.id===id);
      if(grouped&&['Loans','Cards','Investments'].includes(id)){
        const activeState='ACTIVE';
        for(const [name,state] of [['Pending '+id.toLowerCase(),'PENDING'],['Active '+id.toLowerCase(),activeState]])quick.append(tile(name,grouped.index.filter(item=>item.state===state).length+' records currently loaded','#'+id+'~'+state,workspace.icon));
      }else quick.append(tile(workspace.title,id==='assigned-support-tickets'?'Conversations & updates':'Open workspace','#'+id,workspace.icon));
    }
    if(body.dataset.role==='SYSTEM_ADMIN')quick.append(tile('Generate report','Preview and download administrative reports',contextPath+'/admin/reports','audit'));
    dashboard.append(quick);
    const summaries=el('div','workspace-metrics');for(const collection of collections){const card=el('a','workspace-metric');card.href='#'+collection.panel.id;card.append(el('span','',collection.title),el('strong','',String(collection.index.length)),el('small','','Records currently loaded'));card.addEventListener('click',e=>{e.preventDefault();navigate(card.hash);});summaries.append(card);}dashboard.append(summaries);
    if(body.dataset.role==='SYSTEM_ADMIN'){const overview=$('.admin-overview',content);if(overview)dashboard.append(overview);}
    content.prepend(dashboard);workspaces.set('workspace-home',{section:dashboard,title:'Dashboard',icon:'dashboard'});
    [['workspace-home',workspaces.get('workspace-home')],...[...workspaces].filter(([id])=>id!=='workspace-home')].forEach(([id,workspace])=>{const a=el('a');a.href='#'+id;a.dataset.workspaceLink=id;a.title=workspace.title;a.append(icon(workspace.icon),el('span','nav-label',workspace.title));a.addEventListener('click',event=>{event.preventDefault();navigate(a.hash);});nav?.append(a);});
    if(body.dataset.role==='SYSTEM_ADMIN'){const reports=el('a');reports.href=contextPath+'/admin/reports';reports.append(icon('audit'),el('span','nav-label','Reports'));nav?.append(reports);}
    const notifications=el('a');notifications.href=contextPath+'/employee/notifications';notifications.append(icon('notification'),el('span','nav-label','Notifications'));nav?.append(notifications);
    $$('.staff-navigation,.workspace-summary',content).forEach(node=>node.hidden=true);
    // Non-workspace explanatory/footer material does not create an unrelated scroll trail.
    [...content.children].forEach(node=>{if(!node.matches('.bank-workspace,.notice,.ui-toast-stack'))node.hidden=true;});
    currentWorkspace='workspace-home';
  } else if(customer){
    if(body.classList.contains('overview-page')){
      const recent=$$('.activity-panel .transaction');recent.slice(3).forEach(row=>row.hidden=true);
      $$('.account-directory-row').slice(2).forEach(row=>row.hidden=true);
      const nextStep=$('.next-step');if(nextStep){const links=el('div','dashboard-footer-links');$$('a',nextStep).forEach(a=>links.append(a));$('.dashboard-footer')?.append(links);nextStep.remove();}
      $$('.transaction').forEach(row=>{const info=$('.transaction-info',row),details=$$('span',info).slice(1);if(details.length){const extra=el('details','transaction-extra');extra.append(el('summary','','Transaction details'));details[0].before(extra);details.forEach(node=>extra.append(node));}});
    } else {
      const panels=$$('.panel',content).filter(p=>!p.closest('.support-ticket')&&!p.dataset.actionReady&&!p.closest('.record-store'));
      panels.forEach(panel=>collectRecords(panel,labelOf($('h2',panel))||'Records'));
      const title=$('.topbar h1')?.textContent.trim()||'Your workspace';const header=el('div','customer-workspace-header');header.append(el('span','workspace-kicker','LANKATRUST / PERSONAL BANKING'),el('h2','',title));
      const launchers=el('div','workspace-launchers');for(const key of ['action-transfer','action-pay','action-schedule','action-apply','action-request','action-ticket','action-complaint']){const action=actions.get(key);if(action)launchers.append(action.launch);}
      if(launchers.children.length){header.append(launchers);content.prepend(header);}
      // Scheduled payments and current payment action become explicit local tabs.
      if(actions.has('action-pay')){
        const payment=$('.payment-layout',content);const scheduled=$$('.panel',content).find(p=>$('h2',p)?.textContent.includes('Scheduled bill payments'));
        if(payment&&scheduled){makeWorkspace('pay-bill','Pay a bill','payments',[payment]);makeWorkspace('scheduled-payments','Scheduled payments','payments',[scheduled]);const tabs=el('nav','workspace-tabs');tabs.setAttribute('aria-label','Payment workspaces');for(const [id,item] of workspaces){const a=el('a','',item.title);a.href='#'+id;a.dataset.workspaceLink=id;a.addEventListener('click',e=>{e.preventDefault();navigate(a.hash);});tabs.append(a);}header.after(tabs);currentWorkspace='pay-bill';}
      }
      if(body.dataset.workspacePage==='requests'&&supportCenter){
        const nodes=[...content.children].filter(node=>node.matches('.panel')&&node!==supportCenter&&!node.dataset.actionReady);
        makeWorkspace('service-requests','Service requests','support',nodes);makeWorkspace('support-workspace','Support tickets','support',[supportCenter]);
        const tabs=el('nav','workspace-tabs');tabs.setAttribute('aria-label','Customer service workspaces');for(const [id,item] of workspaces){const a=el('a','',item.title);a.href='#'+id;a.dataset.workspaceLink=id;a.addEventListener('click',e=>{e.preventDefault();navigate(a.hash);});tabs.append(a);}header.after(tabs);currentWorkspace='service-requests';for(const key of ['action-ticket','action-complaint'])if(actions.has(key))actions.get(key).workspace='support-workspace';
      }
      $$('.workspace-summary',content).forEach(node=>node.hidden=true);
    }
  }

  // Make form action launchers compact inside record details and selected tickets.
  for(const action of actions.values()){
    const workspace=action.launch.closest('[data-workspace]');if(workspace)action.workspace=workspace.dataset.workspace;
    if(action.launch.closest('.record,.support-ticket'))action.launch.classList.add('compact-action');
  }
  // Resolve copied account numbers only where the full value is already displayed.
  $$('.account-row,dl>div').forEach(row=>{const label=labelOf($('dt,span',row));const value=$('dd,strong',row);if(value&&/^(account|account number|account_number)$/i.test(label)&&/^\d{3,}$/.test(labelOf(value))){const copy=button('Copy',async()=>{try{await navigator.clipboard.writeText(labelOf(value));copy.textContent='Copied';setTimeout(()=>copy.textContent='Copy',1800);}catch{copy.textContent='Select number to copy';}},'copy-account');copy.prepend(icon('copy'));row.append(copy);}});

  const commandActions=[];
  if(customer){const links=[['Transfer money','transfer#action-transfer','transfer'],['Pay a bill','payments#action-pay','payments'],['Scheduled payments','payments#scheduled-payments','payments'],['Payment history','transactions','audit'],['Apply for a loan','loans#action-apply','loans'],['My loans','loans','loans'],['Manage cards','cards','cards'],['New investment','investments#action-apply','investments'],['My investments','investments','investments'],['New support ticket','tickets#action-ticket','support'],['Submit Complaint','requests#action-complaint','support'],['My tickets','tickets','support'],['Service requests','requests','support'],['Accounts','accounts','accounts'],['Browse banking products','products','accounts']];links.forEach(([name,path,i])=>commandActions.push({name,href:contextPath+'/customer/'+path,icon:i}));}
  if(staff){for(const [id,w] of workspaces)commandActions.push({name:w.title,href:'#'+id,icon:w.icon});for(const action of actions.values())if(!action.context&&['action-new-employee','action-new-product','action-cash-deposit','action-cash-withdraw'].includes(action.key))commandActions.push({name:action.title,href:'#'+action.key,icon:iconFor(action.title)});$$('.operations-home .quick-action-tile').filter(a=>a.hash.includes('~')).forEach(a=>commandActions.push({name:labelOf($('span:not(.tile-arrow)',a)),href:a.hash,icon:iconFor(a.hash)}));}
  const query=el('input','command-input');query.type='search';query.placeholder='Try “payment”, “loan” or “support”';query.setAttribute('aria-label','Find an authorized banking action');const results=el('div','command-results');commandView.region.append(query,results);const hint=el('p','command-hint','Search available actions. Use Tab or arrow keys to choose, Enter to open, Escape to close.');commandView.region.append(hint);
  function searchActions(){results.replaceChildren();const found=commandActions.filter(a=>a.name.toLowerCase().includes(query.value.toLowerCase().trim()));found.forEach(action=>{const a=el('a','command-result');a.href=action.href;a.append(icon(action.icon),el('span','',action.name),el('small','','Open →'));a.addEventListener('click',event=>{commandView.dialog.close();if(action.href.startsWith('#')){event.preventDefault();navigate(action.href);}});results.append(a);});if(!found.length)results.append(el('p','command-empty','No matching action. Try another term.'));}
  query.addEventListener('input',searchActions);commandView.dialog.addEventListener('keydown',event=>{if(!['ArrowDown','ArrowUp'].includes(event.key))return;const links=$$('a',results);if(!links.length)return;event.preventDefault();const index=links.indexOf(document.activeElement);links[(index+(event.key==='ArrowDown'?1:-1)+links.length)%links.length].focus();});
  function openCommands(){query.value='';searchActions();commandView.dialog.showModal();query.focus();}
  if(commandActions.length){const launch=button('',openCommands,'action-search');launch.setAttribute('aria-label','Find a banking action (Ctrl K)');launch.append(icon('search'),el('span','','Find a banking action'),el('kbd','','Ctrl K'));$('.topbar')?.append(launch);document.addEventListener('keydown',event=>{if((event.ctrlKey||event.metaKey)&&event.key.toLowerCase()==='k'&&!$$('dialog[open]').length){event.preventDefault();openCommands();}});}

  function route(focus=false){let hash=decodeURIComponent(location.hash.slice(1));if(hash==='new-complaint'||(hash==='new-support-ticket'&&new URLSearchParams(location.search).get('type')==='COMPLAINT'))hash='action-complaint';if(!hash.startsWith('action-')&&actionView.dialog.open)actionView.dialog.close();
    if(hash.startsWith('action-')&&actions.has(hash)){const action=actions.get(hash);if(action.workspace)selectWorkspace(action.workspace);else if(staff)selectWorkspace(currentWorkspace||'workspace-home');openAction(hash);return;}
    const ticket=support?.items.find(item=>item.ticket.id===hash||String(new URLSearchParams(location.search).get('ticket'))===item.ticket.id.replace(/^(support-)?ticket-/,''));
    if(ticket){if(staff)selectWorkspace('assigned-support-tickets',focus);else if(workspaces.has('support-workspace'))selectWorkspace('support-workspace',focus);support.select(ticket.ticket,false);return;}
    const [workspaceKey,filterState]=hash.split('~');
    if(workspaces.has(workspaceKey)){selectWorkspace(workspaceKey,focus);collections.find(collection=>collection.panel.id===workspaceKey)?.filter(filterState||'');}
    else if(staff){const params=new URLSearchParams(location.search);selectWorkspace(params.has('q')&&workspaces.has('Audit-log')?'Audit-log':params.has('findCard')?'card-closure':'workspace-home',focus);}
    else if(workspaces.size)selectWorkspace(currentWorkspace||[...workspaces.keys()][0],focus);
  }
  window.addEventListener('hashchange',()=>route(true));window.addEventListener('popstate',()=>route(true));
  // Remember only view keys across ordinary server POST/redirect cycles. Never store field values.
  const stateKey='lankatrust-return-view';
  document.addEventListener('submit',event=>{const form=event.target;if(!(form instanceof HTMLFormElement)||event.defaultPrevented)return;
    const submit=event.submitter;if(submit){submit.classList.add('is-submitting');submit.setAttribute('aria-busy','true');}
    if(form.method.toLowerCase()==='post'&&!form.closest('.notification-center')){try{sessionStorage.setItem(stateKey,JSON.stringify({path:location.pathname,workspace:currentWorkspace,action:activeAction?.key||'',hash:location.hash,time:Date.now()}));}catch{}}
  });
  window.addEventListener('pageshow',()=>$$('.is-submitting').forEach(b=>{b.classList.remove('is-submitting');b.removeAttribute('aria-busy');}));
  let returnView;try{returnView=JSON.parse(sessionStorage.getItem(stateKey));sessionStorage.removeItem(stateKey);}catch{}
  if(returnView&&returnView.path===location.pathname&&Date.now()-returnView.time<120000&&(!location.hash||location.hash===returnView.hash)){const error=$('.notice.error,.message.error,.error-box');const hash=error&&actions.has(returnView.action)?'#'+returnView.action:returnView.workspace?'#'+returnView.workspace:returnView.hash.startsWith('#action-')?'':returnView.hash;history.replaceState(null,'',location.pathname+location.search+hash);}
  body.classList.add('workspace-app');route();requestAnimationFrame(()=>support?.fit());
})();
