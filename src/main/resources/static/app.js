'use strict';

const money = new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' });
const dateTime = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' });
const $ = (selector) => document.querySelector(selector);

/** Escapes text before it is placed into HTML templates. */
function esc(value) {
    return String(value ?? '').replace(/[&<>"']/g, (c) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[c]);
}

function formatDuration(seconds) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return h > 0 ? `${h}h ${String(m).padStart(2, '0')}m` : `${m}m`;
}

function formatClock(seconds) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = seconds % 60;
    return [h, m, s].map((n) => String(n).padStart(2, '0')).join(':');
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: { 'Content-Type': 'application/json' },
        ...options,
        body: options.body ? JSON.stringify(options.body) : undefined
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const fieldErrors = data?.errors ? Object.entries(data.errors).map(([f, m]) => `${f} ${m}`).join(', ') : '';
        throw new Error(fieldErrors || data?.detail || `Request failed (${response.status})`);
    }
    return data;
}

async function guarded(errorElement, action) {
    errorElement.textContent = '';
    try {
        await action();
    } catch (error) {
        errorElement.textContent = error.message;
    }
}

/* ---------- Tabs ---------- */

const loaders = {};

function showTab(name) {
    document.querySelectorAll('[role=tab]').forEach((tab) =>
        tab.setAttribute('aria-selected', String(tab.dataset.tab === name)));
    document.querySelectorAll('[role=tabpanel]').forEach((panel) => { panel.hidden = panel.id !== `panel-${name}`; });
    history.replaceState(null, '', `#${name}`);
    loaders[name]?.();
}

document.querySelectorAll('[role=tab]').forEach((tab) =>
    tab.addEventListener('click', () => showTab(tab.dataset.tab)));

/* ---------- Overview ---------- */

loaders.overview = async () => {
    const [summary, stats, current, notes] = await Promise.all([
        api('/api/transactions/summary'),
        api('/api/time-entries/stats'),
        api('/api/time-entries/current'),
        api('/api/notes?size=5')
    ]);

    const balance = $('#ov-balance');
    balance.textContent = money.format(summary.balance);
    balance.className = summary.balance < 0 ? 'neg' : '';
    $('#ov-income').textContent = money.format(summary.totalIncome);
    $('#ov-expenses').textContent = money.format(summary.totalExpenses);
    $('#ov-score').textContent = stats.productivityScore == null ? '–' : `${stats.productivityScore}%`;

    const expenses = summary.categories.filter((c) => c.type === 'EXPENSE');
    const max = Math.max(...expenses.map((c) => c.total), 1);
    $('#ov-categories').innerHTML = expenses.length
        ? expenses.map((c) => `
            <div class="bar-row">
                <span>${esc(c.category)}</span>
                <div class="bar-track"><div class="bar-fill" style="width:${(c.total / max) * 100}%"></div></div>
                <span class="num">${money.format(c.total)}</span>
            </div>`).join('')
        : '<p class="empty">No expenses this month.</p>';

    renderTimer($('#ov-timer'), current);

    const pinned = notes.content.filter((n) => n.pinned);
    $('#ov-pinned').innerHTML = pinned.length
        ? pinned.map((n) => `<li>📌 ${esc(n.title)}</li>`).join('')
        : '<li class="muted">Nothing pinned.</li>';
};

/* ---------- Finance ---------- */

loaders.finance = async () => {
    const type = $('#tx-filter').value;
    const page = await api(`/api/transactions?size=50${type ? `&type=${type}` : ''}`);
    $('#tx-rows').innerHTML = page.content.length
        ? page.content.map((t) => `
            <tr>
                <td>${esc(t.occurredOn)}</td>
                <td>${esc(t.description)}</td>
                <td><span class="badge">${esc(t.category)}</span></td>
                <td class="num ${t.type === 'INCOME' ? 'pos' : 'neg'}">${t.type === 'INCOME' ? '+' : '−'}${money.format(t.amount)}</td>
                <td class="num"><button class="ghost" data-delete-tx="${t.id}" aria-label="Delete">✕</button></td>
            </tr>`).join('')
        : '<tr><td colspan="5" class="empty">No transactions yet.</td></tr>';
};

$('#tx-filter').addEventListener('change', loaders.finance);

$('#tx-form').occurredOn.valueAsDate = new Date();
$('#tx-form').addEventListener('submit', (event) => {
    event.preventDefault();
    const form = event.target;
    guarded($('#tx-error'), async () => {
        await api('/api/transactions', {
            method: 'POST',
            body: Object.fromEntries(new FormData(form))
        });
        form.reset();
        form.occurredOn.valueAsDate = new Date();
        await loaders.finance();
    });
});

$('#tx-rows').addEventListener('click', async (event) => {
    const id = event.target.dataset.deleteTx;
    if (id) {
        await api(`/api/transactions/${id}`, { method: 'DELETE' });
        await loaders.finance();
    }
});

/* ---------- Notes ---------- */

let activeTag = '';
let searchDebounce;

loaders.notes = async () => {
    const query = new URLSearchParams({ size: '50' });
    const text = $('#note-search').value.trim();
    if (text) query.set('q', text);
    if (activeTag) query.set('tag', activeTag);

    const [page, tags] = await Promise.all([api(`/api/notes?${query}`), api('/api/notes/tags')]);

    $('#note-tags').innerHTML = tags.map((t) =>
        `<button class="chip" data-tag="${esc(t.tag)}" aria-pressed="${t.tag === activeTag}">#${esc(t.tag)} · ${t.count}</button>`
    ).join('');

    $('#note-grid').innerHTML = page.content.length
        ? page.content.map((n) => `
            <article class="card note ${n.pinned ? 'pinned' : ''}">
                <h3>${n.pinned ? '📌 ' : ''}${esc(n.title)}</h3>
                <p>${esc(n.content)}</p>
                <div class="chips">${n.tags.map((t) => `<span class="badge">#${esc(t)}</span>`).join('')}</div>
                <div class="note-actions">
                    <button class="ghost" data-pin="${n.id}" data-pinned="${n.pinned}">${n.pinned ? 'Unpin' : 'Pin'}</button>
                    <button class="ghost" data-delete-note="${n.id}" aria-label="Delete">✕</button>
                </div>
            </article>`).join('')
        : '<p class="empty">No notes match.</p>';
};

$('#note-search').addEventListener('input', () => {
    clearTimeout(searchDebounce);
    searchDebounce = setTimeout(loaders.notes, 250);
});

$('#note-tags').addEventListener('click', (event) => {
    const tag = event.target.dataset.tag;
    if (tag !== undefined) {
        activeTag = activeTag === tag ? '' : tag;
        loaders.notes();
    }
});

$('#note-form').addEventListener('submit', (event) => {
    event.preventDefault();
    const form = event.target;
    guarded($('#note-error'), async () => {
        await api('/api/notes', {
            method: 'POST',
            body: {
                title: form.title.value,
                content: form.content.value,
                tags: form.tags.value.split(',').map((t) => t.trim()).filter(Boolean)
            }
        });
        form.reset();
        await loaders.notes();
    });
});

$('#note-grid').addEventListener('click', async (event) => {
    const { pin, pinned, deleteNote } = event.target.dataset;
    if (pin) {
        await api(`/api/notes/${pin}/pin`, { method: pinned === 'true' ? 'DELETE' : 'PUT' });
    } else if (deleteNote) {
        await api(`/api/notes/${deleteNote}`, { method: 'DELETE' });
    } else {
        return;
    }
    await loaders.notes();
});

/* ---------- Time ---------- */

let tick;

function renderTimer(container, entry) {
    clearInterval(tick);
    if (!entry) {
        container.innerHTML = '<span class="muted">No timer running.</span>';
        return;
    }
    const startedAt = Date.parse(entry.startedAt);
    container.innerHTML = `
        <div>
            <div class="elapsed">00:00:00</div>
            <span>${esc(entry.activity)}</span>
            <span class="badge ${entry.focus.toLowerCase()}">${esc(entry.focus.toLowerCase())}</span>
        </div>
        <button class="danger" data-stop="${entry.id}">Stop</button>`;
    const elapsed = container.querySelector('.elapsed');
    const update = () => { elapsed.textContent = formatClock(Math.max(0, Math.floor((Date.now() - startedAt) / 1000))); };
    update();
    tick = setInterval(update, 1000);
}

document.addEventListener('click', async (event) => {
    const id = event.target.dataset.stop;
    if (id) {
        await api(`/api/time-entries/${id}/stop`, { method: 'POST' });
        const current = document.querySelector('[role=tab][aria-selected=true]').dataset.tab;
        loaders[current]?.();
    }
});

loaders.time = async () => {
    const [current, stats, page] = await Promise.all([
        api('/api/time-entries/current'),
        api('/api/time-entries/stats'),
        api('/api/time-entries?size=15')
    ]);

    renderTimer($('#time-current'), current);

    const total = Math.max(stats.totalSeconds, 1);
    $('#time-stats').innerHTML = `
        <p><strong>${formatDuration(stats.totalSeconds)}</strong> tracked ·
           <strong>${stats.productivityScore ?? '–'}${stats.productivityScore == null ? '' : '%'}</strong> productive</p>
        <div class="bars">
            ${Object.entries(stats.secondsByFocus).map(([focus, seconds]) => `
                <div class="bar-row">
                    <span>${esc(focus.toLowerCase())}</span>
                    <div class="bar-track"><div class="bar-fill ${focus.toLowerCase()}" style="width:${(seconds / total) * 100}%"></div></div>
                    <span class="num">${formatDuration(seconds)}</span>
                </div>`).join('')}
        </div>
        <h2 class="spaced">Top activities</h2>
        <ul class="plain-list">
            ${stats.topActivities.map((a) => `<li>${esc(a.activity)} <span class="muted">· ${formatDuration(a.seconds)}</span></li>`).join('')
                || '<li class="muted">Nothing tracked yet.</li>'}
        </ul>`;

    $('#time-rows').innerHTML = page.content.length
        ? page.content.map((e) => `
            <tr>
                <td>${dateTime.format(new Date(e.startedAt))}</td>
                <td>${esc(e.activity)}</td>
                <td><span class="badge ${e.focus.toLowerCase()}">${esc(e.focus.toLowerCase())}</span></td>
                <td class="num">${e.running ? 'running' : formatDuration(e.durationSeconds)}</td>
                <td class="num"><button class="ghost" data-delete-entry="${e.id}" aria-label="Delete">✕</button></td>
            </tr>`).join('')
        : '<tr><td colspan="5" class="empty">No time tracked yet.</td></tr>';
};

$('#timer-form').addEventListener('submit', (event) => {
    event.preventDefault();
    const form = event.target;
    guarded($('#timer-error'), async () => {
        await api('/api/time-entries/start', { method: 'POST', body: Object.fromEntries(new FormData(form)) });
        form.reset();
        await loaders.time();
    });
});

$('#time-rows').addEventListener('click', async (event) => {
    const id = event.target.dataset.deleteEntry;
    if (id) {
        await api(`/api/time-entries/${id}`, { method: 'DELETE' });
        await loaders.time();
    }
});

showTab(location.hash.slice(1) in loaders ? location.hash.slice(1) : 'overview');
