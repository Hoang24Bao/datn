// ===== LESSON ===== //
async function loadAllLessonsWithPagination(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('lessonSearch')?.value || "";
    const categoryId = document.getElementById('lessonFilterCategory')?.value || "";
    const level = document.getElementById('lessonFilterLevel')?.value || "";
    const pageSize = paginationManager['lessonPagination'].pageSize;

    try {
        let url = `/api/admin/lessons/paging?page=${page}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (categoryId) url += `&categoryId=${categoryId}`;
        if (level) url += `&level=${level}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });

        if (!res.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await res.json();
        const tbody = document.getElementById('lessonTableBody');

        const countBadge = document.getElementById('lessonCountBadge');
        if (countBadge) {
            countBadge.innerText = `${data.totalElements || 0} bài học`;
        }

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5 text-muted">Không tìm thấy bài học nào</td</tr>`;
            if (paginationManager['lessonPagination']) {
                paginationManager['lessonPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = data.content.map((les, index) => {
            const totalVocab = les.totalVocab || 0;
            const lessonName = les.lessonName || '';
            const categoryName = les.categoryName || 'Chưa phân loại';
            const isActive = les.isActive === true;

            const rowClass = !isActive ? 'row-hidden' : '';

            const actionBtn = !isActive
        ? `<button class="btn-action btn-restore" title="Khôi phục" onclick="deleteLesson(${les.id})">
            <i class="fas fa-trash-restore"></i>
            </button>`
        : `<button class="btn-action btn-trash" title="Ẩn" onclick="deleteLesson(${les.id})">
            <i class="fas fa-trash"></i>
            </button>`;

            return `
            <tr class="${rowClass}">
                <td style="text-align: center;">${(page * pageSize) + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${les.id}</span></td>
                <td><strong>${lessonName}</strong></td>
                <td><strong style="color:var(--teal-dark)">${categoryName}</strong></td>
                <td style="text-align: center;"><span class="level-badge">${totalVocab} từ</span></td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="editLesson(${les.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    ${actionBtn}
                </td>
            </tr>`;
        }).join('');

        if (paginationManager['lessonPagination']) {
            paginationManager['lessonPagination'].render(data.totalPages, data.number);
        }
    } catch (e) {
        console.error("Lỗi tải bài học:", e);
        document.getElementById('lessonTableBody').innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi nạp dữ liệu</td</tr>`;
    }
}

async function loadLessonCategoryFilter() {
    const token = localStorage.getItem('token');
    const level = document.getElementById('lessonFilterLevel')?.value || "";

    try {
        let url = '/api/admin/categories';
        if (level) {
            url = `/api/admin/categories/filter?level=${level}`;
        }

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const categories = await res.json();
        const select = document.getElementById('lessonFilterCategory');
        if (select) {
            select.innerHTML = '<option value="">Tất cả chủ đề</option>' +
                categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');
        }
    } catch (e) {
        console.error("Lỗi tải danh sách chủ đề:", e);
    }
}

async function onLessonFilterLevelChange() {
    const token = localStorage.getItem('token');
    const level = document.getElementById('lessonFilterLevel').value;
    const categorySelect = document.getElementById('lessonFilterCategory');

    if (!level) {
        categorySelect.innerHTML = '<option value="">Tất cả chủ đề</option>';
    } else {
        try {
            const res = await fetch(`/api/admin/categories/filter?level=${level}`, {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (res.ok) {
                const categories = await res.json();
                categorySelect.innerHTML = '<option value="">Tất cả chủ đề</option>' +
                    categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');
            }
        } catch (e) {
            console.error("Lỗi tải chủ đề theo cấp độ:", e);
        }
    }
    loadAllLessonsWithPagination(0);
}

async function loadAllLessonsClientSide(page, pageSize) {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch('/api/admin/lessons', { headers: { 'Authorization': 'Bearer ' + token } });
        const lessons = await res.json();

        const totalPages = Math.ceil(lessons.length / pageSize);
        const start = page * pageSize;
        const end = start + pageSize;
        const pageData = lessons.slice(start, end);

        const tbody = document.getElementById('lessonTableBody');

        if (pageData.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5 text-muted">Chưa có bài học nào</td></tr>`;
            if (paginationManager['lessonPagination']) {
                paginationManager['lessonPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = pageData.map((les, index) => `
            <tr>
                <td style="text-align: center;">${start + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${les.id}</span></td>
                <td><strong>${les.lessonName}</strong></td>
                <td><strong style="color:var(--teal-dark)">${les.categoryName || 'Chưa phân loại'}</strong></td>
                <td style="text-align: center;"><span class="level-badge">${les.totalVocab || 0} từ</span></td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" onclick="editLesson(${les.id})"><i class="fas fa-edit"></i></button>
                    <button class="btn-action btn-trash" onclick="deleteLesson(${les.id})"><i class="fas fa-trash"></i></button>
                </td>
            </tr>
        `).join('');

        if (paginationManager['lessonPagination']) {
            paginationManager['lessonPagination'].render(totalPages, page);
        }
    } catch (e) {
        console.error(e);
    }
}



function openQuickAddLesson() {
    const catId = document.getElementById('vCategoryId').value;
    openAddLessonModal();
}



// ========== CẢNH TƯƠNG TÁC ==========

let currentSceneLessonId = null;
let currentSceneCategoryId = null;
let currentCategoryIdForScene = null;
let currentImageUrl = null;
let currentSceneId = null;
let sceneVocabList = [];
let sceneHotspots = [];
let isDrawing = false;
let startX, startY;
let selectionBox = null;
let selectedVocabIdForScene = null;

async function openCreateSceneModal() {
    const lessonId = document.querySelector('.lesson-detail-container')?.getAttribute('data-lesson-id');
    currentSceneLessonId = lessonId;
    if (!lessonId) return;

    const token = localStorage.getItem('token');
    const lessonRes = await fetch(`/api/admin/lessons/${lessonId}`, {
        headers: { 'Authorization': 'Bearer ' + token }
    });
    const lesson = await lessonRes.json();
    currentSceneCategoryId = lesson.categoryId;

    sceneHotspots = [];
    selectedVocabIdForScene = null;

    await loadSceneVocabList();

    document.getElementById('step1Container').style.display = 'block';
    document.getElementById('step2Container').style.display = 'none';
    document.getElementById('sceneDesc').value = '';
    document.getElementById('sceneImage').value = '';
    document.getElementById('imagePreviewArea').style.display = 'none';
    document.getElementById('uploadSceneBtn').disabled = true;
    document.getElementById('hotspotsContainer').innerHTML = '';
    document.getElementById('hotspotListContainer').innerHTML = '<div class="text-muted text-center p-3">Chưa có vùng nào</div>';

    new bootstrap.Modal(document.getElementById('createSceneModal')).show();
}

async function loadSceneVocabList() {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`/api/admin/vocab/by-lesson/${currentSceneLessonId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        sceneVocabList = await res.json();

        const container = document.getElementById('vocabListContainer');
        if (sceneVocabList.length === 0) {
            container.innerHTML = '<div class="alert alert-warning m-2">Chưa có từ vựng nào. Hãy thêm từ vựng vào bài học trước!</div>';
        } else {
            container.innerHTML = sceneVocabList.map(v => `
                <div class="vocab-item p-2 border-bottom" data-vocab-id="${v.id}"
                     onclick="selectVocabForScene(${v.id})" style="cursor: pointer;">
                    <strong>${v.expression}</strong>
                    <div class="small text-muted">${v.meaning}</div>
                </div>
            `).join('');
        }
    } catch(e) { console.error(e); }
}

function selectVocabForScene(vocabId) {
    selectedVocabIdForScene = vocabId;
    document.querySelectorAll('#vocabListContainer .vocab-item').forEach(item => {
        if (item.dataset.vocabId == vocabId) {
            item.classList.add('selected');
        } else {
            item.classList.remove('selected');
        }
    });
}

function previewSceneImage() {
    const file = document.getElementById('sceneImage').files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('imagePreview').src = e.target.result;
            document.getElementById('imagePreviewArea').style.display = 'block';
            document.getElementById('uploadSceneBtn').disabled = false;
        };
        reader.readAsDataURL(file);
    }
}

// Upload ảnh lên Cloudinary
async function uploadSceneImage() {
    const file = document.getElementById('sceneImage').files[0];
    const token = localStorage.getItem('token');

    if (!file) {
        Swal.fire('Lỗi', 'Vui lòng chọn file ảnh!', 'error');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    Swal.fire({ title: 'Đang upload ảnh...', didOpen: () => Swal.showLoading() });

    try {
        const res = await fetch('/api/admin/interactive/upload-scene-image', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token },
            body: formData
        });
        const data = await res.json();

        if (res.ok) {
            currentImageUrl = data.imageUrl;

            document.getElementById('sceneDisplay').src = currentImageUrl;
            document.getElementById('step1Container').style.display = 'none';
            document.getElementById('step2Container').style.display = 'block';
            initDrawingEvents();
            Swal.close();
        } else {
            Swal.fire('Lỗi', data.error || 'Upload thất bại', 'error');
        }
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể upload ảnh', 'error');
    }
}

function initDrawingEvents() {
    const wrapper = document.getElementById('sceneWrapper');
    if (!wrapper) return;

    wrapper.removeEventListener('mousedown', startDraw);
    window.removeEventListener('mousemove', onDraw);
    window.removeEventListener('mouseup', endDraw);

    wrapper.addEventListener('mousedown', startDraw);
    window.addEventListener('mousemove', onDraw);
    window.addEventListener('mouseup', endDraw);
}

function startDraw(e) {
    const wrapper = document.getElementById('sceneWrapper');
    if (!wrapper.contains(e.target)) return;
    if (!selectedVocabIdForScene) {
        Swal.fire('Chú ý', 'Hãy chọn một từ vựng từ danh sách bên phải trước khi kéo vùng!', 'warning');
        return;
    }
    if (e.button !== 0) return;

    e.preventDefault();

    const img = document.getElementById('sceneDisplay');
    const rect = img.getBoundingClientRect();

    isDrawing = true;
    startX = e.clientX - rect.left;
    startY = e.clientY - rect.top;

    if (selectionBox) selectionBox.remove();
    selectionBox = document.createElement('div');
    selectionBox.className = 'selection-box';
    selectionBox.style.cssText = `
        position: absolute;
        border: 2px dashed #fb873f;
        background: rgba(251, 135, 63, 0.2);
        pointer-events: none;
        border-radius: 4px;
        left: ${startX}px;
        top: ${startY}px;
        width: 0px;
        height: 0px;
    `;
    wrapper.appendChild(selectionBox);
}

function onDraw(e) {
    if (!isDrawing || !selectionBox) return;

    const img = document.getElementById('sceneDisplay');
    const rect = img.getBoundingClientRect();

    let currentX = Math.max(0, Math.min(e.clientX - rect.left, rect.width));
    let currentY = Math.max(0, Math.min(e.clientY - rect.top, rect.height));

    selectionBox.style.width = Math.abs(currentX - startX) + 'px';
    selectionBox.style.height = Math.abs(currentY - startY) + 'px';
    selectionBox.style.left = Math.min(currentX, startX) + 'px';
    selectionBox.style.top = Math.min(currentY, startY) + 'px';
}

function endDraw(e) {
    if (!isDrawing || !selectionBox) return;
    isDrawing = false;

    const img = document.getElementById('sceneDisplay');
    const rect = img.getBoundingClientRect();

    const width = parseFloat(selectionBox.style.width);
    const height = parseFloat(selectionBox.style.height);
    const left = parseFloat(selectionBox.style.left);
    const top = parseFloat(selectionBox.style.top);

    if (width > 10 && height > 10) {
        const xPercent = ((left + width/2) / rect.width * 100).toFixed(2);
        const yPercent = ((top + height/2) / rect.height * 100).toFixed(2);
        const wPercent = (width / rect.width * 100).toFixed(2);
        const hPercent = (height / rect.height * 100).toFixed(2);

        addHotspot(xPercent, yPercent, wPercent, hPercent);
    }

    selectionBox.remove();
    selectionBox = null;
}

function addHotspot(x, y, w, h) {
    const vocab = sceneVocabList.find(v => v.id === selectedVocabIdForScene);
    const hotspot = {
        id: Date.now(),
        vocabId: selectedVocabIdForScene,
        vocabName: vocab?.expression,
        coordX: x,
        coordY: y,
        width: w,
        height: h
    };
    sceneHotspots.push(hotspot);

    const container = document.getElementById('hotspotsContainer');
    const hotspotDiv = document.createElement('div');
    hotspotDiv.className = 'hotspot-editor';
    hotspotDiv.style.left = `${x}%`;
    hotspotDiv.style.top = `${y}%`;
    hotspotDiv.style.width = `${w}%`;
    hotspotDiv.style.height = `${h}%`;
    hotspotDiv.style.transform = 'translate(-50%, -50%)';
    hotspotDiv.id = `hotspot-${hotspot.id}`;
    container.appendChild(hotspotDiv);

    updateHotspotList();
}

function updateHotspotList() {
    const container = document.getElementById('hotspotListContainer');
    if (sceneHotspots.length === 0) {
        container.innerHTML = '<div class="text-muted text-center p-3">Chưa có vùng nào</div>';
        return;
    }

    container.innerHTML = sceneHotspots.map((h, idx) => `
        <div class="d-flex justify-content-between align-items-center p-2 mb-2 border rounded">
            <span>
                <strong>${h.vocabName}</strong>
                <br><small class="text-muted">${h.coordX}%, ${h.coordY}%</small>
            </span>
            <button class="btn btn-sm btn-danger" onclick="removeHotspot(${idx})">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `).join('');
}

function removeHotspot(index) {
    const removed = sceneHotspots.splice(index, 1)[0];
    document.getElementById(`hotspot-${removed.id}`)?.remove();
    updateHotspotList();
}

// Lưu scene và points khi hoàn thành
async function saveScene() {
    if (sceneHotspots.length === 0) {
        Swal.fire('Chú ý', 'Hãy tạo ít nhất một vùng tương tác!', 'warning');
        return;
    }

    const token = localStorage.getItem('token');
    const description = document.getElementById('sceneDesc').value;
    const categoryId = currentCategoryIdForScene || currentSceneCategoryId;

    if (!categoryId) {
        Swal.fire('Lỗi', 'Không xác định được chủ đề!', 'error');
        return;
    }

    if (!currentImageUrl) {
        Swal.fire('Lỗi', 'Chưa upload ảnh! Vui lòng chọn ảnh trước.', 'error');
        return;
    }

    Swal.fire({ title: 'Đang lưu...', didOpen: () => Swal.showLoading() });

    try {
        const requestBody = {
            imageUrl: currentImageUrl,
            categoryId: categoryId,
            description: description || ''
        };

        const sceneRes = await fetch('/api/admin/interactive/scene-from-url', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });

        const responseText = await sceneRes.text();
        let sceneData;
        try {
            sceneData = JSON.parse(responseText);
        } catch (e) {
            throw new Error('Server trả về dữ liệu không hợp lệ: ' + responseText);
        }

        if (!sceneRes.ok) {
            throw new Error(sceneData.error || 'Không thể tạo scene');
        }

        if (!sceneData.sceneId) {
            throw new Error('Server không trả về sceneId');
        }

        const sceneId = sceneData.sceneId;

        for (const point of sceneHotspots) {
            await fetch('/api/admin/interactive/point', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    sceneId: sceneId,
                    vocabId: point.vocabId,
                    coordX: parseFloat(point.coordX),
                    coordY: parseFloat(point.coordY),
                    width: parseFloat(point.width),
                    height: parseFloat(point.height)
                })
            });
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('createSceneModal'));
        if (modal) modal.hide();

        Swal.fire('Thành công', 'Đã tạo cảnh tương tác!', 'success');

        sceneHotspots = [];
        currentImageUrl = null;
        selectedVocabIdForScene = null;

        await loadScenesListForCategory();

    } catch(e) {
        console.error('Error:', e);
        Swal.fire('Lỗi', e.message || 'Không thể lưu cảnh', 'error');
    }
}


