// ========== API 工具库 ==========

function authHeaders() {
    var userId = localStorage.getItem('userId') || '';
    var role = localStorage.getItem('userRole') || '';
    return {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
        'X-User-Role': role
    };
}

async function apiGet(url) {
    var res = await fetch(url, { headers: authHeaders() });
    return res.json();
}

async function apiPost(url, body) {
    var res = await fetch(url, {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiPut(url, body) {
    var res = await fetch(url, {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiDelete(url, body) {
    var opts = { method: 'DELETE', headers: authHeaders() };
    if (body) { opts.body = JSON.stringify(body); }
    var res = await fetch(url, opts);
    return res.json();
}

function setAuth(userId, role) {
    localStorage.setItem('userId', userId);
    localStorage.setItem('userRole', role);
}

function clearAuth() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    window.location.href = '/index.html';
}

function requireAuth(expectedRole) {
    var userId = localStorage.getItem('userId');
    var role = localStorage.getItem('userRole');
    if (!userId || !role) {
        window.location.href = '/index.html';
        return false;
    }
    if (expectedRole && role !== expectedRole) {
        window.location.href = '/index.html';
        return false;
    }
    return true;
}
