// ========== 移动端 API 工具库 ==========
// 修改此地址指向你的后端服务器
var BASE_URL = 'http://10.115.171.176:8080';

function apiUrl(path) { return BASE_URL + path; }

function authHeaders() {
    var userId = localStorage.getItem('userId') || '';
    var role = localStorage.getItem('userRole') || '';
    return {
        'Content-Type': 'application/json',
        'X-User-Id': userId,
        'X-User-Role': role
    };
}

async function apiGet(path) {
    var res = await fetch(apiUrl(path), { headers: authHeaders() });
    return res.json();
}

async function apiPost(path, body) {
    var res = await fetch(apiUrl(path), {
        method: 'POST',
        headers: authHeaders(),
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiPut(path, body) {
    var res = await fetch(apiUrl(path), {
        method: 'PUT',
        headers: authHeaders(),
        body: JSON.stringify(body)
    });
    return res.json();
}

async function apiDelete(path, body) {
    var opts = { method: 'DELETE', headers: authHeaders() };
    if (body) { opts.body = JSON.stringify(body); }
    var res = await fetch(apiUrl(path), opts);
    return res.json();
}

function setAuth(userId, role) {
    localStorage.setItem('userId', userId);
    localStorage.setItem('userRole', role);
}

function clearAuth() {
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    window.location.href = '/mobile/普通用户/登录.html';
}

function requireAuth(expectedRole) {
    var userId = localStorage.getItem('userId');
    var role = localStorage.getItem('userRole');
    if (!userId || !role) {
        window.location.href = '/mobile/普通用户/登录.html';
        return false;
    }
    if (expectedRole && role !== expectedRole) {
        window.location.href = '/mobile/普通用户/登录.html';
        return false;
    }
    return true;
}
