const $ = (id) => document.getElementById(id);
let folders = [];
let browseCurrent = "";
let browseParent = "";
let statusTimer = null;

// ---- 設定（対象フォルダ） ----
async function loadFolders() {
    const data = await (await fetch("/api/settings")).json();
    folders = data.folders || [];
    renderFolders();
}
async function saveFolders() {
    const res = await fetch("/api/settings", {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ folders })
    });
    folders = (await res.json()).folders || [];
    renderFolders();
}
function renderFolders() {
    const list = $("folderList");
    list.innerHTML = "";
    $("noFolders").hidden = folders.length > 0;
    folders.forEach((path, i) => {
        const li = document.createElement("li");
        const code = document.createElement("code");
        code.textContent = path;
        const btn = document.createElement("button");
        btn.textContent = "削除";
        btn.onclick = () => { folders.splice(i, 1); saveFolders(); };
        li.append(code, btn);
        list.appendChild(li);
    });
}

// ---- 索引状態 ----
async function loadStatus() {
    try {
        const s = await (await fetch("/api/index/status")).json();
        renderStatus(s);
        if (s.indexing) {
            if (!statusTimer) statusTimer = setInterval(loadStatus, 1500);
            $("reindexBtn").disabled = true;
        } else {
            if (statusTimer) { clearInterval(statusTimer); statusTimer = null; }
            $("reindexBtn").disabled = false;
        }
    } catch { /* ignore */ }
}
function renderStatus(s) {
    const el = $("indexStatus");
    if (s.indexing) {
        el.innerHTML = `<span class="dot busy"></span>索引作成中…（${s.docCount} 件）`;
        return;
    }
    const last = s.lastIndexedEpochMs ? "最終索引 " + fmtDate(s.lastIndexedEpochMs) : "未索引";
    el.innerHTML = `<span class="dot ready"></span>索引済み ${s.docCount} 件 ・ ${last}`;
}
async function reindex() {
    const res = await fetch("/api/index/reindex", { method: "POST" });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.error || "再索引を開始できませんでした");
        return;
    }
    loadStatus();
}

// ---- ディレクトリブラウザ ----
async function browse(path) {
    const url = path ? `/api/browse?path=${encodeURIComponent(path)}` : "/api/browse";
    const res = await fetch(url);
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        alert(err.error || "フォルダを開けませんでした");
        return;
    }
    const data = await res.json();
    browseCurrent = data.current || "";
    browseParent = data.parent || "";
    $("modalCurrent").textContent = browseCurrent || "ドライブを選択してください";
    $("selectBtn").disabled = !browseCurrent;
    $("upBtn").disabled = !browseCurrent;

    const dirList = $("dirList");
    dirList.innerHTML = "";
    const entries = data.entries || [];
    if (entries.length === 0) {
        const li = document.createElement("li");
        li.className = "empty";
        li.textContent = "（サブフォルダはありません）";
        dirList.appendChild(li);
    }
    entries.forEach((entry) => {
        const li = document.createElement("li");
        li.textContent = "📁 " + entry.name;
        li.onclick = () => browse(entry.path);
        dirList.appendChild(li);
    });
}
function openModal() { $("overlay").classList.add("open"); browse(""); }
function closeModal() { $("overlay").classList.remove("open"); }

// ---- 検索 ----
async function runSearch() {
    const q = $("searchInput").value.trim();
    if (!q) { $("searchStatus").textContent = "検索語を入力してください。"; $("results").innerHTML = ""; return; }
    $("searchStatus").textContent = "検索中…";
    const res = await fetch(`/api/search?q=${encodeURIComponent(q)}`);
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        $("searchStatus").textContent = err.error || "検索に失敗しました";
        return;
    }
    renderResults(await res.json());
}
function renderResults(data) {
    const hits = data.hits || [];
    let status = `${data.total} 件ヒット`;
    if (data.total >= data.limit) status += `（上限 ${data.limit} 件で打ち切り）`;
    $("searchStatus").innerHTML = `<span class="badge">${status}</span>`;
    const list = $("results");
    list.innerHTML = "";
    if (hits.length === 0) {
        if (data.total === 0) $("searchStatus").innerHTML += " — 該当なし（索引が空の場合は先に再索引してください）";
        return;
    }
    hits.forEach((h) => {
        const li = document.createElement("li");
        const name = document.createElement("div");
        name.className = "name";
        name.textContent = h.fileName;
        const snippet = document.createElement("div");
        snippet.className = "snippet";
        snippet.innerHTML = h.snippet || "";   // サーバ側でエスケープ済み（<mark> のみ）
        const meta = document.createElement("div");
        meta.className = "meta";
        meta.textContent = `${h.parentPath} ・ ${fmtSize(h.sizeBytes)} ・ ${fmtDate(h.lastModifiedEpochMs)}`;
        li.append(name, snippet, meta);
        list.appendChild(li);
    });
}

// ---- ユーティリティ ----
function fmtSize(bytes) {
    if (bytes < 1024) return bytes + " B";
    const units = ["KB", "MB", "GB", "TB"];
    let v = bytes / 1024, i = 0;
    while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
    return v.toFixed(1) + " " + units[i];
}
function fmtDate(ms) {
    if (!ms) return "-";
    const d = new Date(ms);
    const p = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

// ---- イベント ----
$("addFolderBtn").onclick = openModal;
$("cancelBtn").onclick = closeModal;
$("upBtn").onclick = () => browse(browseParent);
$("selectBtn").onclick = () => {
    if (browseCurrent && !folders.includes(browseCurrent)) { folders.push(browseCurrent); saveFolders(); }
    closeModal();
};
$("overlay").onclick = (e) => { if (e.target === $("overlay")) closeModal(); };
$("reindexBtn").onclick = reindex;
$("searchBtn").onclick = runSearch;
$("searchInput").addEventListener("keydown", (e) => { if (e.key === "Enter") runSearch(); });

loadFolders();
loadStatus();
