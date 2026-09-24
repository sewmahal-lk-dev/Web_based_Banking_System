'use strict';
(() => {
  const role=document.querySelector('.ops-role');
  if(role)role.textContent='SYSTEM ADMINISTRATOR';
  const type=document.querySelector('#report-type');
  function reflect() {
    document.querySelectorAll('[data-report-types]').forEach(label=>{
      const enabled=label.dataset.reportTypes.split(' ').includes(type.value);
      label.hidden=!enabled;
      label.querySelectorAll('input,select').forEach(input=>input.disabled=!enabled);
    });
    const blocked=document.querySelector('[name=status] option[value=BLOCKED]');
    blocked.hidden=blocked.disabled=type.value!=='CUSTOMER';
    if(blocked.disabled&&blocked.selected)blocked.parentElement.value='';
  }
  type.addEventListener('change',reflect);reflect();
})();
