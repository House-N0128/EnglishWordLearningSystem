// ========== API 工具库 ==========
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
    window.location.href = '/index.html';
}

function requireAuth(expectedRole) {
    return true;
}

// ========== 业务接口函数 ==========

async function getWordBooks() {
    return await apiGet('/api/wordbooks');
}

async function getWordBookDetail(wordBookId) {
    return await apiGet('/api/wordbooks/' + wordBookId);
}

async function getWordsByBook(wordBookId, page = 0, size = 20) {
    return await apiGet(`/api/words?wordBookId=${wordBookId}`);
}

async function getWordDetail(wordId) {
    return await apiGet('/api/words/' + wordId);
}

async function searchWord(keyword) {
    return await apiGet('/api/words/search?keyword=' + encodeURIComponent(keyword));
}

async function markWordLearned(wordId, wordBookId) {
    return await apiPost('/api/records/add', {
        wordId: wordId,
        learnedWordBookId: wordBookId
    });
}

async function getLearningProgress(wordBookId) {
    const res = await apiGet('/api/records/progress');
    if (res.code === 200 && res.data) {
        const bookProgress = res.data.find(p => p.wordBookId === wordBookId);
        return {
            code: 200,
            data: {
                learnedCount: bookProgress ? bookProgress.learnedWords : 0,
                learnedWordIds: []  // 后端暂未返回具体单词ID列表
            }
        };
    }
    return { code: 500, data: null };
}

async function getDailyStudyStats(days = 7) {
    return await apiGet('/api/learning/stats/daily?days=' + days);
}

async function collectWord(wordId) {
    return await apiPost('/api/collections/add', { wordId: wordId });
}

async function uncollectWord(wordId) {
    return await apiPost('/api/collections/remove', { wordId: wordId });
}

async function getCollectedWords() {
    return await apiGet('/api/collections');
}

async function isWordCollected(wordId) {
    return await apiGet('/api/collection/check/' + wordId);
}

async function updateUserInfo(userData) {
    return await apiPut('/api/user/info', userData);
}

async function changePassword(oldPassword, newPassword) {
    return await apiPost('/api/user/change-password', {
        oldPassword: oldPassword,
        newPassword: newPassword
    });
}