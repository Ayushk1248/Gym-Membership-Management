// Same-origin API requests work on any host/port serving this app.
let liveData = [], livePlans = [], liveStaff = [];
let isPlanEdit = false, isStaffEdit = false;
const el = id => document.getElementById(id);
const value = id => el(id).value.trim();
const show = id => el(id).style.display = 'flex';
const hide = id => el(id).style.display = 'none';
function showError(error) {
    el('apiError').textContent = error instanceof Error ? error.message : 'Something went wrong. Please try again.';
    el('apiError').hidden = false;
}
function run(action) {
    el('apiError').hidden = true;
    return Promise.resolve().then(action).catch(showError);
}
async function api(path, method = 'GET', data) {
    const response = await fetch('/api/' + path, {
        method, headers: data ? {'Content-Type': 'application/json'} : {},
        body: data ? JSON.stringify(data) : undefined
    });
    const text = await response.text();
    let body; try { body = text ? JSON.parse(text) : null; } catch { body = null; }
    if (!response.ok) throw new Error(body?.detail || body?.message || `Request failed (${response.status}). Check the data and try again.`);
    return body;
}
function renderRows(id, rows, fields, update, remove) {
    const tbody = el(id); tbody.replaceChildren();
    rows.forEach(row => {
        const tr = document.createElement('tr');
        fields.forEach(field => { const td = document.createElement('td'); td.textContent = field(row) ?? ''; tr.appendChild(td); });
        const actions = document.createElement('td');
        for (const [label, action] of [['UPDATE', update], ['DELETE', remove]]) {
            const button = document.createElement('button'); button.type = 'button';
            button.className = 'btn-action btn-crud'; button.textContent = label;
            button.addEventListener('click', () => action(row)); actions.appendChild(button);
        }
        tr.appendChild(actions); tbody.appendChild(tr);
    });
}
function renderTable(rows) {
    renderRows('tableBody', rows, [r=>r.memberId,r=>r.fullName,r=>r.plan?.planName || 'No Plan',r=>r.status,r=>r.joinDate],
        r=>openEditModal(r.memberId,r.fullName,r.plan?.planId || '',r.status), r=>deleteRecord(r.memberId));
}
async function fetchMembers() { liveData = await api('members'); filterTable(); }
function filterTable() { const q=value('searchInput').toLowerCase(); renderTable(liveData.filter(r=>(r.fullName+' '+r.memberId).toLowerCase().includes(q))); }
function updatePlanOptions() {
    for (const id of ['newMemPlan','editMemPlan']) {
        const select=el(id), previous=select.value; select.replaceChildren();
        const empty=new Option(livePlans.length ? 'Select a plan' : 'Create a plan first', ''); select.add(empty);
        livePlans.forEach(p=>select.add(new Option(p.planName+' ('+p.planId+')',p.planId)));
        if (livePlans.some(p=>p.planId===previous)) select.value=previous;
    }
}
function openModal() { return run(async()=>{await fetchPlans(); show('dbModal');}); }
function closeModal() { hide('dbModal'); }
function saveRecord() { return run(async()=>{
    if(!value('newMemPlan')) throw new Error('Select an existing subscription plan.');
    await api('members','POST',{memberId:value('newMemId'),fullName:value('newMemName'),status:value('newMemStatus'),plan:{planId:value('newMemPlan')}});
    closeModal(); el('dbModal').querySelector('form').reset(); await fetchMembers();
}); }
function openEditModal(id,name,planId,status) { return run(async()=>{
    await fetchPlans(); el('editMemId').value=id; el('editMemName').value=name; el('editMemPlan').value=planId; el('editMemStatus').value=status; show('editModal');
}); }
function closeEditModal() { hide('editModal'); }
function submitUpdate() { return run(async()=>{
    if(!value('editMemPlan')) throw new Error('Select an existing subscription plan.');
    await api('members/'+encodeURIComponent(value('editMemId')),'PUT',{fullName:value('editMemName'),status:value('editMemStatus'),plan:{planId:value('editMemPlan')}});
    closeEditModal(); await fetchMembers();
}); }
function deleteRecord(id) { if(confirm('Delete member '+id+'?')) return run(async()=>{await api('members/'+encodeURIComponent(id),'DELETE');await fetchMembers();}); }
async function fetchPlans() { livePlans=await api('plans');filterPlans();updatePlanOptions(); }
function renderPlansTable(rows) { renderRows('planTableBody',rows,[r=>r.planId,r=>r.planName,r=>r.durationMonths,r=>'₹ '+r.price],r=>openPlanModal(r.planId,r.planName,r.durationMonths,r.price),r=>deletePlan(r.planId)); }
function filterPlans() {const q=value('searchPlans').toLowerCase();renderPlansTable(livePlans.filter(r=>(r.planName+' '+r.planId).toLowerCase().includes(q)));}
function openPlanModal(id='',name='',duration='',price='') {
    isPlanEdit=id!=='';el('planModalTitle').textContent=isPlanEdit?'UPDATE Plan':'INSERT Plan';
    el('planModal').querySelector('button[type="submit"]').textContent=isPlanEdit?'Update Plan':'Insert Data';
    el('planIdInput').value=id;el('planIdInput').readOnly=isPlanEdit;el('planNameInput').value=name;el('planDurationInput').value=duration;el('planPriceInput').value=price;show('planModal');
}
function closePlanModal(){hide('planModal');}
function savePlan(){return run(async()=>{
    const data={planId:value('planIdInput'),planName:value('planNameInput'),durationMonths:Number(value('planDurationInput')),price:Number(value('planPriceInput'))};
    if(data.durationMonths<=0 || data.price<0) throw new Error('Duration must be positive and price cannot be negative.');
    await api('plans'+(isPlanEdit?'/'+encodeURIComponent(data.planId):''),isPlanEdit?'PUT':'POST',data);
    closePlanModal();await fetchPlans();await fetchMembers();
});}
function deletePlan(id){if(confirm('Delete plan '+id+'? Plans used by members cannot be deleted.'))return run(async()=>{await api('plans/'+encodeURIComponent(id),'DELETE');await fetchPlans();});}
async function fetchStaff(){liveStaff=await api('staff');filterStaff();}
function renderStaffTable(rows){renderRows('staffTableBody',rows,[r=>r.staffId,r=>r.staffName,r=>r.specialization,r=>r.shiftTiming],r=>openStaffModal(r.staffId,r.staffName,r.specialization,r.shiftTiming),r=>deleteStaff(r.staffId));}
function filterStaff(){const q=value('searchStaff').toLowerCase();renderStaffTable(liveStaff.filter(r=>(r.staffName+' '+r.staffId).toLowerCase().includes(q)));}
function openStaffModal(id='',name='',spec='',shift=''){
    isStaffEdit=id!=='';el('staffModalTitle').textContent=isStaffEdit?'UPDATE Staff':'INSERT Staff';el('staffModal').querySelector('button[type="submit"]').textContent=isStaffEdit?'Update Staff':'Insert Data';el('staffIdInput').value=id;el('staffIdInput').readOnly=isStaffEdit;el('staffNameInput').value=name;el('staffSpecInput').value=spec;el('staffShiftInput').value=shift;show('staffModal');
}
function closeStaffModal(){hide('staffModal');}
function saveStaff(){return run(async()=>{
    const data={staffId:value('staffIdInput'),staffName:value('staffNameInput'),specialization:value('staffSpecInput'),shiftTiming:value('staffShiftInput')};
    await api('staff'+(isStaffEdit?'/'+encodeURIComponent(data.staffId):''),isStaffEdit?'PUT':'POST',data);closeStaffModal();await fetchStaff();
});}
function deleteStaff(id){if(confirm('Delete staff '+id+'?'))return run(async()=>{await api('staff/'+encodeURIComponent(id),'DELETE');await fetchStaff();});}
function switchTab(viewId,clickedBtn){
    document.querySelectorAll('.nav-btn').forEach(btn=>btn.classList.remove('active'));clickedBtn.classList.add('active');
    document.querySelectorAll('.view-section').forEach(view=>view.classList.remove('active'));el(viewId).classList.add('active');
    el('page-title').textContent={'view-dashboard':'Database Overview','view-members':'Member Directory (Members Table)','view-subscriptions':'Subscriptions (Plans Table)','view-trainers':'Staff Assignments (Staff Table)'}[viewId];
}
window.onload=()=>run(()=>Promise.all([fetchMembers(),fetchPlans(),fetchStaff()]));
