const API = '/api';
let selectedSubjectId = null;

// ── TOAST ───────────────────────────────────────────────────────
function showToast(msg) {
    let t = document.getElementById('toast');
    if (!t) {
        t = document.createElement('div');
        t.id = 'toast';
        t.className = 'toast';
        document.body.appendChild(t);
    }
    t.textContent = msg;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 2500);
}

// ── FETCH HELPERS ────────────────────────────────────────────────
async function get(url) {
    const res = await fetch(API + url);
    return res.json();
}

async function post(url, body) {
    const res = await fetch(API + url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

async function del(url) {
    return fetch(API + url, { method: 'DELETE' });
}

async function patch(url) {
    return fetch(API + url, { method: 'PATCH' });
}

// ── FORMAT HELPERS ───────────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return 'No date set';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function daysUntil(dateStr) {
    if (!dateStr) return null;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    const exam = new Date(dateStr + 'T00:00:00');
    return Math.ceil((exam - today) / 86400000);
}

// ── RENDER ───────────────────────────────────────────────────────
async function render() {
    const [subjects, stats] = await Promise.all([
        get('/subjects'),
        get('/stats')
    ]);

    renderSidebar(subjects);
    renderStats(stats);
    renderProgress(subjects);

    const sel = subjects.find(s => s.id === selectedSubjectId);
    renderTopics(sel);

    const pill = document.getElementById('countdown-pill');
    pill.textContent = stats.nextExamDays >= 0
        ? 'Next exam in ' + stats.nextExamDays + ' days'
        : 'No exams set';
}

function renderSidebar(subjects) {
    const list = document.getElementById('subj-list');
    if (subjects.length === 0) {
        list.innerHTML = '<div class="empty-hint">No subjects yet</div>';
        return;
    }
    list.innerHTML = subjects.map(s => {
        const days = daysUntil(s.examDate);
        return `
        <div class="subj-card ${s.id === selectedSubjectId ? 'selected' : ''}"
             onclick="selectSubject('${s.id}')">
            <div class="sdot" style="background:${s.color}"></div>
            <div class="sinfo">
                <div class="sname">${s.name}</div>
                <div class="sdate">${formatDate(s.examDate)}</div>
            </div>
            <button class="sremove" onclick="event.stopPropagation(); removeSubject('${s.id}')">×</button>
        </div>`;
    }).join('');
}

function renderStats(stats) {
    document.getElementById('stat-subjects').textContent = stats.subjectCount;
    document.getElementById('stat-progress').textContent = stats.overallProgress + '%';
    document.getElementById('stat-days').textContent =
        stats.nextExamDays >= 0 ? stats.nextExamDays + 'd' : '—';
}

function renderTopics(subject) {
    const title = document.getElementById('topic-panel-title');
    const badge = document.getElementById('exam-badge');
    const area = document.getElementById('topic-area');

    if (!subject) {
        title.textContent = 'Select a subject';
        badge.innerHTML = '';
        area.innerHTML = '<div class="no-sel">👈 Pick a subject from the left to manage its topics</div>';
        return;
    }

    title.textContent = subject.name + ' — Topics';

    const days = daysUntil(subject.examDate);
    if (days !== null) {
        let cls = 'eb-ok', label = 'Exam in ' + days + 'd';
        if (days < 0) { cls = 'eb-past'; label = 'Exam passed'; }
        else if (days === 0) { cls = 'eb-soon'; label = 'Exam today!'; }
        else if (days <= 7) { cls = 'eb-soon'; }
        badge.innerHTML = `<span class="exam-badge ${cls}">${label}</span>`;
    } else {
        badge.innerHTML = '';
    }

    const topicRows = (subject.topics || []).map(t => {
        const cls = { PENDING: 'ts-pending', TODAY: 'ts-today', DONE: 'ts-done' }[t.status];
        const lbl = { PENDING: 'Pending', TODAY: 'Today', DONE: 'Done' }[t.status];
        return `
        <div class="topic-row">
            <div class="topic-dot" style="background:${subject.color}"></div>
            <span class="topic-name">${t.name}</span>
            <button class="topic-status ${cls}"
                onclick="cycleTopic('${subject.id}','${t.id}')">${lbl}</button>
            <button class="topic-remove"
                onclick="removeTopic('${subject.id}','${t.id}')">×</button>
        </div>`;
    }).join('');

    area.innerHTML = (subject.topics.length === 0
        ? '<div class="no-sel">No topics yet — add one below</div>'
        : topicRows)
        + `<div class="topic-add">
            <input id="topic-inp" placeholder="Add topic (e.g. Calculus, Waves...)"
                   onkeydown="if(event.key==='Enter') addTopic('${subject.id}')"/>
            <button onclick="addTopic('${subject.id}')">Add</button>
           </div>`;
}

function renderProgress(subjects) {
    const area = document.getElementById('progress-area');
    if (subjects.length === 0) {
        area.innerHTML = '<div class="no-sel">Add subjects to see progress</div>';
        return;
    }
    area.innerHTML = subjects.map(s => {
        const total = s.topics ? s.topics.length : 0;
        const done = s.topics ? s.topics.filter(t => t.status === 'DONE').length : 0;
        const pct = total === 0 ? 0 : Math.round((done / total) * 100);
        return `
        <div class="prog-item">
            <span class="prog-name" title="${s.name}">${s.name}</span>
            <div class="prog-bg">
                <div class="prog-fill" style="width:${pct}%;background:${s.color}"></div>
            </div>
            <span class="prog-pct">${pct}%</span>
        </div>`;
    }).join('');
}

// ── ACTIONS ──────────────────────────────────────────────────────
async function addSubject() {
    const name = document.getElementById('inp-name').value.trim();
    const date = document.getElementById('inp-date').value;
    if (!name) { showToast('Please enter a subject name'); return; }
    const subject = await post('/subjects', { name, examDate: date || null });
    selectedSubjectId = subject.id;
    document.getElementById('inp-name').value = '';
    document.getElementById('inp-date').value = '';
    showToast(name + ' added!');
    render();
}

function selectSubject(id) {
    selectedSubjectId = id;
    render();
}

async function removeSubject(id) {
    await del('/subjects/' + id);
    if (selectedSubjectId === id) selectedSubjectId = null;
    showToast('Subject removed');
    render();
}

async function addTopic(subjectId) {
    const inp = document.getElementById('topic-inp');
    if (!inp) return;
    const name = inp.value.trim();
    if (!name) { showToast('Please enter a topic name'); return; }
    await post('/subjects/' + subjectId + '/topics', { name });
    showToast('Topic added!');
    render();
}

async function removeTopic(subjectId, topicId) {
    await del('/subjects/' + subjectId + '/topics/' + topicId);
    showToast('Topic removed');
    render();
}

async function cycleTopic(subjectId, topicId) {
    await patch('/subjects/' + subjectId + '/topics/' + topicId + '/cycle');
    render();
}

// ── INIT ──────────────────────────────────────────────────────────
render();
