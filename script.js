// ==========================================
// 1. MEMBERS LOGIC
// ==========================================
let liveData = [];
function fetchMembers() {
    fetch('http://localhost:8080/api/members').then(res => res.json()).then(data => { liveData = data; renderTable(liveData); }).catch(e => console.error("API Error:", e));
}
function renderTable(data) {
    const tbody = document.getElementById('tableBody');
    if (!tbody) return; tbody.innerHTML = "";
    data.forEach(row => {
        let statusClass = row.status === 'Active' ? 'status-active' : 'status-expired';
        let planName = row.plan ? row.plan.planName : "No Plan";
        let planId = row.plan ? row.plan.planId : "PLN-1";
        tbody.innerHTML += `<tr>
            <td class="id-col">${row.memberId}</td><td style="font-weight: 500;">${row.fullName}</td>
            <td style="color: var(--text-muted);">${planName}</td><td><span class="status-badge ${statusClass}">${row.status}</span></td>
            <td style="color: var(--text-muted);">${row.joinDate}</td>
            <td><button class="btn-action btn-crud" onclick="openEditModal('${row.memberId}', '${row.fullName}', '${planId}', '${row.status}')">UPDATE</button>
            <button class="btn-action btn-crud" onclick="deleteRecord('${row.memberId}')">DELETE</button></td></tr>`;
    });
}
function filterTable() {
    let q = document.getElementById("searchInput").value.toLowerCase();
    renderTable(liveData.filter(user => user.fullName.toLowerCase().includes(q) || user.memberId.toLowerCase().includes(q)));
}
function openModal() { document.getElementById('dbModal').style.display = 'flex'; }
function closeModal() { document.getElementById('dbModal').style.display = 'none'; }
function saveRecord() { 
    const newMemberData = { memberId: document.getElementById('newMemId').value, fullName: document.getElementById('newMemName').value, status: document.getElementById('newMemStatus').value, joinDate: new Date().toISOString().split('T')[0], plan: { planId: document.getElementById('newMemPlan').value } };
    fetch('http://localhost:8080/api/members', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(newMemberData) }).then(() => { closeModal(); fetchMembers(); });
}
function openEditModal(id, name, planId, status) {
    document.getElementById('editMemId').value = id; document.getElementById('editMemName').value = name; document.getElementById('editMemPlan').value = planId; document.getElementById('editMemStatus').value = status; document.getElementById('editModal').style.display = 'flex';
}
function closeEditModal() { document.getElementById('editModal').style.display = 'none'; }
function submitUpdate() {
    const idToUpdate = document.getElementById('editMemId').value;
    const updatedData = { fullName: document.getElementById('editMemName').value, status: document.getElementById('editMemStatus').value, joinDate: new Date().toISOString().split('T')[0], plan: { planId: document.getElementById('editMemPlan').value } };
    fetch('http://localhost:8080/api/members/' + idToUpdate, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(updatedData) }).then(() => { closeEditModal(); fetchMembers(); });
}
function deleteRecord(id) {
    if(confirm(`DELETE FROM Members WHERE Member_ID = '${id}'?`)) { fetch('http://localhost:8080/api/members/' + id, { method: 'DELETE' }).then(() => fetchMembers()); }
}

// ==========================================
// 2. PLANS LOGIC
// ==========================================
let livePlans = [];
function fetchPlans() { fetch('http://localhost:8080/api/plans').then(res => res.json()).then(data => { livePlans = data; renderPlansTable(data); }); }
function renderPlansTable(data) {
    const tbody = document.getElementById('planTableBody');
    if (!tbody) return; tbody.innerHTML = "";
    data.forEach(row => {
        tbody.innerHTML += `<tr><td class="id-col">${row.planId}</td><td style="font-weight: 500;">${row.planName}</td><td style="color: var(--text-muted);">${row.durationMonths}</td><td style="color: var(--text-muted);">₹ ${row.price}</td>
            <td><button class="btn-action btn-crud" onclick="openPlanModal('${row.planId}', '${row.planName}', ${row.durationMonths}, ${row.price})">UPDATE</button><button class="btn-action btn-crud" onclick="deletePlan('${row.planId}')">DELETE</button></td></tr>`;
    });
}
let isPlanEdit = false;
function openPlanModal(id = '', name = '', duration = '', price = '') {
    isPlanEdit = id !== ''; document.getElementById('planModalTitle').textContent = isPlanEdit ? "UPDATE Plan" : "INSERT Plan";
    document.getElementById('planIdInput').value = id; document.getElementById('planIdInput').readOnly = isPlanEdit; document.getElementById('planNameInput').value = name; document.getElementById('planDurationInput').value = duration; document.getElementById('planPriceInput').value = price; document.getElementById('planModal').style.display = 'flex';
}
function closePlanModal() { document.getElementById('planModal').style.display = 'none'; }
function savePlan() {
    const planData = { planId: document.getElementById('planIdInput').value, planName: document.getElementById('planNameInput').value, durationMonths: document.getElementById('planDurationInput').value, price: document.getElementById('planPriceInput').value };
    const url = isPlanEdit ? 'http://localhost:8080/api/plans/' + planData.planId : 'http://localhost:8080/api/plans';
    fetch(url, { method: isPlanEdit ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(planData) }).then(() => { closePlanModal(); fetchPlans(); });
}
function deletePlan(id) { if(confirm(`WARNING: Deleting a Plan breaks connected Members! Delete ${id}?`)) { fetch('http://localhost:8080/api/plans/' + id, { method: 'DELETE' }).then(() => fetchPlans()); } }
function filterPlans() { let q = document.getElementById("searchPlans").value.toLowerCase(); renderPlansTable(livePlans.filter(p => p.planName.toLowerCase().includes(q) || p.planId.toLowerCase().includes(q))); }
// 3. STAFF LOGIC

let liveStaff = [];
function fetchStaff() { fetch('http://localhost:8080/api/staff').then(res => res.json()).then(data => { liveStaff = data; renderStaffTable(data); }); }
function renderStaffTable(data) {
    const tbody = document.getElementById('staffTableBody');
    if (!tbody) return; tbody.innerHTML = "";
    data.forEach(row => {
        tbody.innerHTML += `<tr><td class="id-col">${row.staffId}</td><td style="font-weight: 500;">${row.staffName}</td><td style="color: var(--text-muted);">${row.specialization}</td><td style="color: var(--text-muted);">${row.shiftTiming}</td>
            <td><button class="btn-action btn-crud" onclick="openStaffModal('${row.staffId}', '${row.staffName}', '${row.specialization}', '${row.shiftTiming}')">UPDATE</button><button class="btn-action btn-crud" onclick="deleteStaff('${row.staffId}')">DELETE</button></td></tr>`;
    });
}
let isStaffEdit = false;
function openStaffModal(id = '', name = '', spec = '', shift = '') {
    isStaffEdit = id !== ''; document.getElementById('staffModalTitle').textContent = isStaffEdit ? "UPDATE Staff" : "INSERT Staff";
    document.getElementById('staffIdInput').value = id; document.getElementById('staffIdInput').readOnly = isStaffEdit; document.getElementById('staffNameInput').value = name; document.getElementById('staffSpecInput').value = spec; document.getElementById('staffShiftInput').value = shift; document.getElementById('staffModal').style.display = 'flex';
}
function closeStaffModal() { document.getElementById('staffModal').style.display = 'none'; }
function saveStaff() {
    const staffData = { staffId: document.getElementById('staffIdInput').value, staffName: document.getElementById('staffNameInput').value, specialization: document.getElementById('staffSpecInput').value, shiftTiming: document.getElementById('staffShiftInput').value };
    const url = isStaffEdit ? 'http://localhost:8080/api/staff/' + staffData.staffId : 'http://localhost:8080/api/staff';
    fetch(url, { method: isStaffEdit ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(staffData) }).then(() => { closeStaffModal(); fetchStaff(); });
}
function deleteStaff(id) { if(confirm(`DELETE FROM Staff WHERE Staff_ID = '${id}'?`)) { fetch('http://localhost:8080/api/staff/' + id, { method: 'DELETE' }).then(() => fetchStaff()); } }
function filterStaff() { let q = document.getElementById("searchStaff").value.toLowerCase(); renderStaffTable(liveStaff.filter(s => s.staffName.toLowerCase().includes(q) || s.staffId.toLowerCase().includes(q))); }

// ==========================================
// 4. VIEW NAVIGATION & INITIALIZATION
// ==========================================
function switchTab(viewId, clickedBtn) {
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    clickedBtn.classList.add('active');
    document.querySelectorAll('.view-section').forEach(view => view.classList.remove('active'));
    document.getElementById(viewId).classList.add('active');
    const title = document.getElementById('page-title');
    if(viewId === 'view-dashboard') title.textContent = "Database Overview";
    if(viewId === 'view-members') title.textContent = "Member Directory (Members Table)";
    if(viewId === 'view-subscriptions') title.textContent = "Subscriptions (Plans Table)";
    if(viewId === 'view-trainers') title.textContent = "Staff Assignments (Staff Table)";
}

// Run this the second the page loads
window.onload = () => { 
    fetchMembers(); 
    fetchPlans();
    fetchStaff();
};