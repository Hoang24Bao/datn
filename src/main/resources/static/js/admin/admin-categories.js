// ===== CATEGORY ===== //
async function loadAllCategoriesWithPagination(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('categorySearch')?.value || "";
    const level = document.getElementById('categoryFilterLevel')?.value || "";
    const isActive = document.getElementById('categoryFilterStatus')?.value || "";
    const pageSize = paginationManager['categoryPagination'].pageSize;

    try {
        let url = `/api/admin/categories/paging?page=${page}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (level) url += `&level=${level}`;
        if (isActive !== "") url += `&isActive=${isActive}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });

        if (!res.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await res.json();
        const tbody = document.getElementById('categoryTableBody');

        const countBadge = document.getElementById('categoryCountBadge');
        if (countBadge) {
            countBadge.innerText = `${data.totalElements || 0} chủ đề`;
        }

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5 text-muted">Không tìm thấy chủ đề nào</td</tr>`;
            if (paginationManager['categoryPagination']) {
                paginationManager['categoryPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = data.content.map((cat, index) => {
            const isHidden = cat.isActive === false;
            const rowClass = isHidden ? 'row-hidden' : '';
            const statusBadge = isHidden ? '<span class="status-badge badge-hidden">Đã ẩn</span>' : '';
            const iconUrlWithTimestamp = cat.iconUrl ? addTimestampToUrl(cat.iconUrl) : '';
            const actionBtn = isHidden
                ? `<button class="btn-action btn-restore me-1" title="Khôi phục" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash-restore"></i></button>`
                : `<button class="btn-action btn-trash me-1" title="Ẩn" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash"></i></button>`;

            return `
            <tr class="${rowClass}">
                <td style="text-align: center;">${(page * pageSize) + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${cat.id}</span></td>
                <td style="padding-left: 40px;">
                    <div class="d-flex align-items-center gap-2">
                        ${cat.iconUrl ? `<img src="${cat.iconUrl}" style="width: 40px; height: 40px; object-fit: cover; border-radius: 8px;">` : ''}
                        <strong style="color:var(--teal-dark)">${cat.categoryName}</strong>
                    </div>
                    <div class="mt-1">${statusBadge}</div>
                </td>
                <td style="text-align: center;">${cat.totalLessons || 0} bài</td>
                <td style="text-align: center;"><span class="level-badge">${cat.jlptLevel || 'N5'}</span></td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="editCategory(${cat.id})"><i class="fas fa-edit"></i></button>
                    ${actionBtn}
                </td>
            </tr>`;
        }).join('');

        if (paginationManager['categoryPagination']) {
            paginationManager['categoryPagination'].render(data.totalPages, data.number);
        }
    } catch (e) {
        console.error("Lỗi tải chủ đề:", e);
        document.getElementById('categoryTableBody').innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi nạp dữ liệu</td</tr>`;
    }
}

async function loadAllCategoriesClientSide(page, pageSize) {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch('/api/admin/categories', { headers: { 'Authorization': 'Bearer ' + token } });
        if (!res.ok) return;
        let categories = await res.json();
        categories.sort((a, b) => a.id - b.id);

        const totalPages = Math.ceil(categories.length / pageSize);
        const start = page * pageSize;
        const end = start + pageSize;
        const pageData = categories.slice(start, end);

        const tbody = document.getElementById('categoryTableBody');

        if (pageData.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-5 text-muted">Chưa có chủ đề nào</td></tr>`;
            if (paginationManager['categoryPagination']) {
                paginationManager['categoryPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = pageData.map((cat, index) => {
            const isHidden = cat.isActive === false;
            const rowClass = isHidden ? 'row-hidden' : '';
            const statusBadge = isHidden ? '<span class="status-badge badge-hidden">Đã ẩn</span>' : '';
            const actionBtn = isHidden
                ? `<button class="btn-action btn-restore me-1" title="Khôi phục" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash-restore"></i></button>`
                : `<button class="btn-action btn-trash me-1" title="Ẩn" onclick="deleteCategory(${cat.id})"><i class="fas fa-trash"></i></button>`;

            return `
            <tr class="${rowClass}">
                <td style="text-align: center;">${start + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${cat.id}</span></td>
                <td style="padding-left: 40px;">
                    <strong style="color:var(--teal-dark)">${cat.categoryName}</strong>
                    <div class="mt-1">${statusBadge}</div>
                </td>
                <td style="text-align: center;">${cat.totalLessons || 0} bài</td>
                <td style="text-align: center;"><span class="level-badge">${cat.jlptLevel || 'N5'}</span></td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="editCategory(${cat.id})"><i class="fas fa-edit"></i></button>
                    ${actionBtn}
                </td>
            </tr>`;
        }).join('');

        if (paginationManager['categoryPagination']) {
            paginationManager['categoryPagination'].render(totalPages, page);
        }
    } catch (e) {
        console.error(e);
    }
}

function openAddCategoryModal() {
    document.getElementById('categoryForm').reset();
    new bootstrap.Modal(document.getElementById('categoryModal')).show();
}

document.getElementById('catIconUrl')?.addEventListener('input', function (e) {
    const url = e.target.value;
    const previewDiv = document.getElementById('iconPreview');
    const previewImg = document.getElementById('previewImg');

    if (url && url.trim() !== "") {
        previewImg.src = url.startsWith('http') ? url : '/' + url;
        previewDiv.style.display = 'block';
    } else {
        previewDiv.style.display = 'none';
    }
});

let selectedFile = null;
let selectedThumbnailFile = null;

function handleFileSelect(input) {
    if (input.files && input.files[0]) {
        selectedFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('previewImg').src = e.target.result;
            document.getElementById('iconPreview').style.display = 'block';
            document.getElementById('catIconUrl').value = selectedFile.name;
        };
        reader.readAsDataURL(selectedFile);
    }
}

function handleThumbnailSelect(input) {
    if (input.files && input.files[0]) {
        selectedThumbnailFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('thumbnailPreviewImg').src = e.target.result;
            document.getElementById('thumbnailPreview').style.display = 'block';
            document.getElementById('catThumbnailUrl').value = selectedThumbnailFile.name;
        };
        reader.readAsDataURL(selectedThumbnailFile);
    }
}

async function submitCategory() {
    const name = document.getElementById('catName').value.trim();
    const level = document.getElementById('catLevel').value;
    const token = localStorage.getItem('token');

    if (!name) return Swal.fire('Lỗi', 'Vui lòng nhập tên chủ đề!', 'error');

    const formData = new FormData();
    formData.append("categoryName", name);
    formData.append("jlptLevel", level);

    if (selectedFile) {
        formData.append("file", selectedFile);
    } else {
        formData.append("iconUrl", document.getElementById('catIconUrl').value);
    }

    if (selectedThumbnailFile) {
        formData.append("thumbnailFile", selectedThumbnailFile);
    }

    try {
        Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });

        const response = await fetch('/api/admin/categories/add-with-file', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token },
            body: formData
        });

        if (response.ok) {
            bootstrap.Modal.getInstance(document.getElementById('categoryModal')).hide();
            await Swal.fire('Thành công', 'Chủ đề và ảnh đã được lưu!', 'success');
            selectedFile = null;
            selectedThumbnailFile = null;
            document.getElementById('thumbnailPreview').style.display = 'none';
            document.getElementById('catThumbnailUrl').value = '';
            loadAllCategoriesWithPagination(0);
        } else {
            const err = await response.text();
            Swal.fire('Lỗi', err || 'Không thể lưu chủ đề', 'error');
        }
    } catch (e) { console.error(e); }
}

async function deleteCategory(id) {
    const result = await Swal.fire({
        title: 'Xác nhận ẩn?',
        text: "Hành động này sẽ thay đổi trạng thái hiển thị của chủ đề đối với người dùng.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#06BBCC',
        cancelButtonColor: '#ef4444',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`/api/admin/categories/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token }
            });

            if (res.ok) {
                const message = await res.text();
                await Swal.fire({
                    icon: 'success',
                    title: 'Thành công',
                    text: message,
                    timer: 1000,
                    showConfirmButton: false
                });
                const currentPage = paginationManager['categoryPagination']?.currentPage || 0;
                loadAllCategoriesWithPagination(currentPage);
            }
        } catch (e) { console.error(e); }
    }
}

async function editCategory(id) {
    pendingIconFile = null;
    pendingThumbnailFile = null;
    const token = localStorage.getItem('token');
    const mainContent = document.getElementById('main-content');
    const mainTitle = document.getElementById('page-main-title');

    try {
        const res = await fetch(`/api/admin/categories/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) throw new Error('Không thể tải thông tin chủ đề');

        const category = await res.json();

        const lessonsRes = await fetch(`/api/admin/lessons/by-category/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        let lessons = [];
        if (lessonsRes.ok) {
            lessons = await lessonsRes.json();
        }

        let scenes = [];
        try {
            const scenesRes = await fetch(`/api/admin/interactive/scenes/${id}`, {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (scenesRes.ok) {
                scenes = await scenesRes.json();
            }
        } catch (sceneError) {
            console.error('Lỗi tải scenes:', sceneError);
            scenes = [];
        }

        mainTitle.innerText = `Chỉnh sửa Chủ đề: ${category.categoryName}`;

        mainContent.innerHTML = `
            <div class="category-detail-container" data-category-id="${id}">
                <button class="back-to-list" onclick="goBackToCategories()">
                    <i class="fas fa-arrow-left"></i> Quay lại danh sách
                </button>

                <div class="category-detail-header">
                    <img src="${category.iconUrl ? addTimestampToUrl(category.iconUrl) : 'https://placehold.co/100x100?text=No+Image'}"
                 alt="${category.categoryName}"
                 class="category-detail-avatar"
                 id="detailAvatar"
                 onerror="this.src='https://placehold.co/100x100?text=No+Image'">
                    <div class="category-detail-title" style="flex: 1;">
                        <input type="text" id="editCategoryName" class="form-control form-control-lg"
                               value="${escapeHtml(category.categoryName)}"
                               style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">
                        <p><i class="fas fa-tag"></i> Slug: <span id="slugPreview">${category.slug || ''}</span></p>
                    </div>
                </div>

                <form id="editCategoryForm">
                    <div class="info-grid">
                        <div class="info-card">
                            <h4><i class="fas fa-chart-line"></i> Cấp độ JLPT</h4>
                            <select id="editJlptLevel" class="form-select">
                                <option value="N5" ${category.jlptLevel === 'N5' ? 'selected' : ''}>N5 (Cơ bản)</option>
                                <option value="N4" ${category.jlptLevel === 'N4' ? 'selected' : ''}>N4 (Sơ cấp)</option>
                                <option value="N3" ${category.jlptLevel === 'N3' ? 'selected' : ''}>N3 (Trung cấp)</option>
                                <option value="N2" ${category.jlptLevel === 'N2' ? 'selected' : ''}>N2 (Thượng cấp)</option>
                                <option value="N1" ${category.jlptLevel === 'N1' ? 'selected' : ''}>N1 (Cao cấp)</option>
                            </select>
                        </div>
                        <div class="info-card">
                            <h4><i class="fas fa-image"></i> URL Ảnh đại diện (Icon)</h4>
                            <div class="input-group">
                                <input type="text" id="editIconUrl" class="form-control" value="${category.iconUrl || ''}"
                                       placeholder="Nhập URL ảnh" onchange="updateAvatarPreview()">
                                <button class="btn btn-orange" type="button" onclick="document.getElementById('editFileInput').click()">
                                    <i class="fas fa-upload"></i> Upload
                                </button>
                            </div>
                            <input type="file" id="editFileInput" style="display: none;" accept="image/*" onchange="handleEditFileSelect(this, ${id})">
                            <small class="text-muted">Ảnh hiển thị ở trang chủ và danh sách chủ đề</small>
                        </div>
                        <div class="info-card">
                            <h4><i class="fas fa-image"></i> URL Ảnh Thumbnail (Banner)</h4>
                            <div class="input-group">
                                <input type="text" id="editThumbnailUrl" class="form-control" value="${category.thumbnailUrl || ''}"
                                       placeholder="Nhập URL ảnh thumbnail" onchange="updateThumbnailPreview()">
                                <button class="btn btn-orange" type="button" onclick="document.getElementById('editThumbnailFileInput').click()">
                                    <i class="fas fa-upload"></i> Upload
                                </button>
                            </div>
                            <input type="file" id="editThumbnailFileInput" style="display: none;" accept="image/*" onchange="handleEditThumbnailSelect(this, ${id})">
                            <div id="thumbnailPreviewContainer" class="mt-2 ${category.thumbnailUrl ? '' : 'd-none'}">
            <img id="detailThumbnail" src="${category.thumbnailUrl ? addTimestampToUrl(category.thumbnailUrl) : ''}"
                 style="width: 100%; max-height: 150px; object-fit: cover; border-radius: 10px;"
                 onerror="this.src='https://placehold.co/400x150?text=No+Thumbnail'">
                                <small class="text-muted d-block mt-1">Ảnh banner hiển thị ở đầu trang bài học</small>
                            </div>
                        </div>
                        <div class="info-card">
                            <h4><i class="fas fa-toggle-on"></i> Trạng thái</h4>
                            <select id="editIsActive" class="form-select">
                                <option value="true" ${category.isActive ? 'selected' : ''}>Đang hiển thị</option>
                                <option value="false" ${!category.isActive ? 'selected' : ''}>Đã ẩn</option>
                            </select>
                        </div>
                    </div>
                </form>

                <div class="d-flex gap-2 mb-4">
                    <button class="btn-custom btn-teal" onclick="saveCategoryChanges(${id})">
                        <i class="fas fa-save me-1"></i> Lưu thay đổi
                    </button>
                </div>

                <!-- Tab Navigation -->
                <div class="tab-navigation-wrapper mb-4">
                    <ul class="nav nav-tabs nav-justified" id="categoryTab" role="tablist">
                        <li class="nav-item" role="presentation">
                            <button class="nav-link active" id="lessons-tab" data-bs-toggle="tab" data-bs-target="#lessons" type="button" role="tab">
                                <i class="fas fa-book"></i> Bài học
                            </button>
                        </li>
                        <li class="nav-item" role="presentation">
                            <button class="nav-link" id="scenes-tab" data-bs-toggle="tab" data-bs-target="#scenes" type="button" role="tab">
                                <i class="fas fa-image"></i> Cảnh tương tác
                            </button>
                        </li>
                    </ul>
                </div>

                <!-- Tab Content -->
                <div class="tab-content" id="categoryTabContent">
                    <!-- Tab Bài học -->
                    <div class="tab-pane fade show active" id="lessons" role="tabpanel">
                        <div class="lessons-list">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                <h3 style="margin: 0;"><i class="fas fa-book-reader"></i> Danh sách bài học</h3>
                                <button class="btn-home" onclick="openAddLessonInCateModal(${id})">
                                    <i class="fas fa-plus" style="margin-right:5px"></i> Thêm bài học
                                </button>
                            </div>
                            ${lessons.length === 0 ?
                                '<p class="text-muted text-center py-4">Chưa có bài học nào trong chủ đề này</p>' :
                                `<table class="lessons-table">
                                    <thead>
                                        <tr>
                                            <th>STT</th>
                                            <th>ID</th>
                                            <th style="padding-left: 30px;">Tên bài học</th>
                                            <th style="text-align: center;">Số từ vựng</th>
                                            <th style="text-align: center;">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${lessons.map((lesson, idx) => {
                                            const isHidden = lesson.isActive === false;
                                            const rowClass = isHidden ? 'row-hidden' : '';
                                            const actionBtn = isHidden
                                                ? `<button class="btn-action btn-restore" title="Khôi phục" onclick="deleteLesson(${lesson.id})">
                                                    <i class="fas fa-trash-restore"></i>
                                                   </button>`
                                                : `<button class="btn-action btn-trash" title="Ẩn bài học" onclick="deleteLesson(${lesson.id})">
                                                    <i class="fas fa-trash"></i>
                                                   </button>`;

                                            return `
                                            <tr class="${rowClass}" data-lesson-id="${lesson.id}">
                                                <td style="text-align: center;">${idx + 1}</td>
                                                <td style="text-align: center;"><span class="id-badge">#${lesson.id}</span></td>
                                                <td style="padding-left: 30px;"><strong>${escapeHtml(lesson.lessonName)}</strong></td>
                                                <td style="text-align: center;"><span class="level-badge">${lesson.totalVocab || 0} từ</span></td>
                                                <td style="text-align: center;">
                                                    <button class="btn-action btn-view me-1" title="Xem & Sửa" onclick="editLesson(${lesson.id})">
                                                        <i class="fas fa-edit"></i>
                                                    </button>
                                                    ${actionBtn}
                                                </td>
                                            </tr>`;
                                        }).join('')}
                                    </tbody>
                                  </table>`
                            }
                        </div>
                    </div>

                    <!-- Tab Cảnh tương tác -->
                    <div class="tab-pane fade" id="scenes" role="tabpanel">
                        <div class="scenes-list">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                <h3 style="margin: 0;"><i class="fas fa-image me-2" style="color: var(--teal);"></i>Danh sách cảnh tương tác</h3>
                                <button class="btn-home" onclick="openCreateSceneModalFromCategory()">
                                    <i class="fas fa-plus" style="margin-right:5px"></i> Thêm cảnh
                                </button>
                            </div>

                            <div id="scenesListContainer">
                                <!-- Danh sách cảnh sẽ hiển thị ở đây -->
                                ${scenes.length === 0 ?
                                    `<div class="text-center py-5 text-muted">
                                        <i class="fas fa-image fa-3x mb-3 opacity-25"></i>
                                        <p>Chưa có cảnh tương tác nào trong chủ đề này</p>
                                        <button class="btn btn-primary" onclick="openCreateSceneModalFromCategory()">Tạo cảnh đầu tiên</button>
                                    </div>` :
                                    scenes.map((scene, idx) => `
                                        <div class="card mb-3" data-scene-id="${scene.id}">
                                            <div class="row g-0">
                                                <div class="col-md-3">
                                                    <img src="${scene.imageUrl}" class="img-fluid rounded-start"
                                                         style="height: 150px; width: 100%; object-fit: cover;">
                                                </div>
                                                <div class="col-md-7">
                                                    <div class="card-body">
                                                        <h6 class="card-title fw-bold">Cảnh ${idx + 1}: ${scene.description || 'Không có mô tả'}</h6>
                                                        <div class="mt-2">
                                                            ${scene.points?.map(p => `<span class="badge bg-warning me-1">📌 ${p.vocab?.expression || '?'}</span>`).join('') || '<span class="text-muted">Chưa có điểm tương tác</span>'}
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="col-md-2 d-flex align-items-center justify-content-end pe-3 gap-2">
                                                    <button class="btn btn-sm btn-warning" onclick="editSceneFromCategory(${scene.id})" title="Sửa cảnh">
                                                        <i class="fas fa-edit"></i> Sửa
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-danger" onclick="deleteSceneFromCategory(${scene.id})" title="Xóa cảnh">
                                                        <i class="fas fa-trash"></i> Xóa
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    `).join('')
                                }
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        localStorage.setItem('activeAdminTab', 'categories');

        currentCategoryIdForScene = id;

        if (scenes.length > 0) {
            loadScenesListForCategory();
        }

    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể tải thông tin chi tiết', 'error');
    }
}

// Xem trước ảnh từ URL
function previewImageFromUrl() {
    const url = document.getElementById('sceneImageUrl').value.trim();
    const previewArea = document.getElementById('sceneImagePreviewArea');
    const previewImg = document.getElementById('scenePreviewImg');
    const processBtn = document.getElementById('processSceneBtn');

    if (url) {
        previewImg.src = url;
        previewArea.style.display = 'block';
        processBtn.disabled = false;
        processBtn.setAttribute('data-image-url', url);
        processBtn.setAttribute('data-source', 'url');
    } else {
        Swal.fire('Chú ý', 'Vui lòng nhập URL ảnh', 'warning');
    }
}

// Xem trước ảnh từ file upload
function previewSceneImageFile() {
    const file = document.getElementById('sceneImageFile').files[0];
    const previewArea = document.getElementById('sceneImagePreviewArea');
    const previewImg = document.getElementById('scenePreviewImg');
    const processBtn = document.getElementById('processSceneBtn');
    const urlInput = document.getElementById('sceneImageUrl');

    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            previewImg.src = e.target.result;
            previewArea.style.display = 'block';
            processBtn.disabled = false;
            processBtn.setAttribute('data-file', 'true');
            processBtn.setAttribute('data-source', 'file');
            if (urlInput) urlInput.value = '';
        };
        reader.readAsDataURL(file);
    }
}

// Xóa preview ảnh trong scene modal
function clearSceneImagePreview() {
    const previewArea = document.getElementById('sceneImagePreviewArea');
    const previewImg = document.getElementById('scenePreviewImg');
    const fileInput = document.getElementById('sceneImageFile');
    const urlInput = document.getElementById('sceneImageUrl');
    const processBtn = document.getElementById('processSceneBtn');

    if (previewArea) previewArea.style.display = 'none';
    if (previewImg) previewImg.src = '';
    if (fileInput) fileInput.value = '';
    if (urlInput) urlInput.value = '';
    if (processBtn) {
        processBtn.disabled = true;
        processBtn.removeAttribute('data-image-url');
        processBtn.removeAttribute('data-file');
        processBtn.removeAttribute('data-source');
    }
}

// Xử lý ảnh
async function processSceneImage() {
    const processBtn = document.getElementById('processSceneBtn');
    const source = processBtn.getAttribute('data-source');
    const token = localStorage.getItem('token');

    Swal.fire({ title: 'Đang xử lý ảnh...', didOpen: () => Swal.showLoading() });

    try {
        if (source === 'file') {
            const file = document.getElementById('sceneImageFile').files[0];
            if (!file) throw new Error('Không tìm thấy file ảnh');

            const formData = new FormData();
            formData.append('file', file);

            const res = await fetch('/api/admin/interactive/upload-scene-image', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });
            const data = await res.json();

            if (!res.ok) throw new Error(data.error || 'Upload thất bại');
            currentImageUrl = data.imageUrl;
        } else {
            const url = document.getElementById('sceneImageUrl').value.trim();
            if (!url) throw new Error('Vui lòng nhập URL ảnh');
            currentImageUrl = url;
        }

        document.getElementById('sceneDisplay').src = currentImageUrl;
        document.getElementById('step1Container').style.display = 'none';
        document.getElementById('step2Container').style.display = 'block';
        initDrawingEvents();
        Swal.close();

    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', e.message || 'Không thể xử lý ảnh', 'error');
    }
}


async function openCreateSceneModalFromCategory() {
    const container = document.querySelector('.category-detail-container');
    const categoryId = container ? container.getAttribute('data-category-id') : null;

    if (!categoryId) {
        Swal.fire('Lỗi', 'Không xác định được chủ đề!', 'error');
        return;
    }

    currentCategoryIdForScene = parseInt(categoryId);
    currentSceneCategoryId = parseInt(categoryId);

    sceneHotspots = [];
    selectedVocabIdForScene = null;
    currentImageUrl = null;

    document.getElementById('step1Container').style.display = 'block';
    document.getElementById('step2Container').style.display = 'none';
    document.getElementById('sceneDesc').value = '';
    document.getElementById('sceneImageFile').value = '';
    document.getElementById('sceneImageUrl').value = '';
    document.getElementById('sceneImagePreviewArea').style.display = 'none';
    document.getElementById('scenePreviewImg').src = '';
    document.getElementById('processSceneBtn').disabled = true;
    document.getElementById('processSceneBtn').removeAttribute('data-image-url');
    document.getElementById('processSceneBtn').removeAttribute('data-file');
    document.getElementById('processSceneBtn').removeAttribute('data-source');
    document.getElementById('hotspotsContainer').innerHTML = '';
    document.getElementById('hotspotListContainer').innerHTML = '<div class="text-muted text-center p-3">Chưa có vùng nào</div>';

    await loadSceneVocabListForCategory();

    const modalElement = document.getElementById('createSceneModal');
    if (modalElement) {
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    } else {
        Swal.fire('Lỗi', 'Không tìm thấy modal tạo cảnh!', 'error');
    }
}

function cancelCreateScene() {
    sceneHotspots = [];
    currentImageUrl = null;
    selectedVocabIdForScene = null;

    const modal = bootstrap.Modal.getInstance(document.getElementById('createSceneModal'));
    if (modal) modal.hide();
}

// Load danh sách từ vựng cho Category
async function loadSceneVocabListForCategory() {
    const token = localStorage.getItem('token');
    const container = document.getElementById('vocabListContainer');

    if (!container) return;

    container.innerHTML = '<div class="text-center p-3">Đang tải từ vựng...</div>';

    try {
        const res = await fetch(`/api/admin/vocab/by-category/${currentCategoryIdForScene}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            throw new Error('Không thể tải từ vựng');
        }

        sceneVocabList = await res.json();

        if (sceneVocabList.length === 0) {
            container.innerHTML = '<div class="alert alert-warning m-2">Chưa có từ vựng nào trong chủ đề này. Hãy thêm từ vựng vào các bài học trước!</div>';
        } else {
            container.innerHTML = sceneVocabList.map(v => `
                <div class="vocab-item p-2 border-bottom" data-vocab-id="${v.id}"
                     onclick="selectVocabForScene(${v.id})" style="cursor: pointer;">
                    <strong>${escapeHtml(v.expression)}</strong>
                    <div class="small text-muted">${escapeHtml(v.meaning)}</div>
                    <small class="text-info">📖 ${escapeHtml(v.lessonName)}</small>
                </div>
            `).join('');
        }
    } catch(e) {
        console.error(e);
        container.innerHTML = '<div class="alert alert-danger m-2">Lỗi tải từ vựng</div>';
    }
}

// Sửa cảnh từ Category
async function editSceneFromCategory(sceneId) {
    currentEditSceneId = sceneId;
    editSceneHotspots = [];
    editSelectedVocabId = null;

    const token = localStorage.getItem('token');

    Swal.fire({ title: 'Đang tải dữ liệu...', didOpen: () => Swal.showLoading() });

    try {
        const res = await fetch(`/api/admin/interactive/scene/${sceneId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const data = await res.json();

        if (res.ok) {
            document.getElementById('editSceneDesc').value = data.description || '';
            document.getElementById('editSceneDisplay').src = data.imageUrl;

            await loadEditVocabListForCategory();

            if (data.points && data.points.length > 0) {
                editSceneHotspots = data.points.map(point => ({
                    id: point.id,
                    vocabId: point.vocabId,
                    vocabName: point.vocab?.expression,
                    coordX: point.coordX,
                    coordY: point.coordY,
                    width: point.width,
                    height: point.height,
                    isExisting: true
                }));
                renderEditHotspots();
                updateEditHotspotList();
            }

            Swal.close();
            const modal = new bootstrap.Modal(document.getElementById('editSceneModal'));
            modal.show();

            setTimeout(() => {
                initEditDrawingEvents();
            }, 500);
        } else {
            Swal.fire('Lỗi', data.error || 'Không thể tải dữ liệu', 'error');
        }
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể tải dữ liệu', 'error');
    }
}

// Load danh sách từ vựng cho modal sửa
async function loadEditVocabListForCategory() {
    const token = localStorage.getItem('token');
    const container = document.getElementById('editVocabListContainer');

    if (!container) return;

    container.innerHTML = '<div class="text-center p-3">Đang tải từ vựng...</div>';

    try {
        const res = await fetch(`/api/admin/vocab/by-category/${currentCategoryIdForScene}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        editSceneVocabList = await res.json();

        if (editSceneVocabList.length === 0) {
            container.innerHTML = '<div class="alert alert-warning m-2">Chưa có từ vựng nào trong chủ đề này. Hãy thêm từ vựng vào các bài học trước!</div>';
        } else {
            container.innerHTML = editSceneVocabList.map(v => `
                <div class="vocab-item p-2 border-bottom" data-vocab-id="${v.id}"
                     onclick="selectEditVocab(${v.id})" style="cursor: pointer;">
                    <strong>${escapeHtml(v.expression)}</strong>
                    <div class="small text-muted">${escapeHtml(v.meaning)}</div>
                    <small class="text-info">📖 ${escapeHtml(v.lessonName)}</small>
                </div>
            `).join('');
        }
    } catch(e) {
        console.error(e);
        container.innerHTML = '<div class="alert alert-danger m-2">Lỗi tải từ vựng</div>';
    }
}

// Xóa cảnh từ Category
async function deleteSceneFromCategory(sceneId) {
    const result = await Swal.fire({
        title: 'Xác nhận xóa?',
        text: 'Cảnh này sẽ bị xóa vĩnh viễn cùng tất cả điểm tương tác!',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        const token = localStorage.getItem('token');
        const categoryId = currentCategoryIdForScene;

        try {
            const res = await fetch(`/api/admin/interactive/scene/${sceneId}`, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token }
            });

            if (res.ok) {
                Swal.fire('Thành công', 'Đã xóa cảnh!', 'success');
                if (categoryId) {
                    await loadScenesListForCategory();
                }
            } else {
                const error = await res.text();
                Swal.fire('Lỗi', error || 'Không thể xóa', 'error');
            }
        } catch(e) {
            console.error(e);
            Swal.fire('Lỗi', 'Không thể xóa cảnh', 'error');
        }
    }
}

// Load danh sách cảnh cho Category
async function loadScenesListForCategory() {
    const categoryId = currentCategoryIdForScene;
    const token = localStorage.getItem('token');
    const container = document.getElementById('scenesListContainer');

    if (!container) return;
    if (!categoryId) return;

    try {
        const res = await fetch(`/api/admin/interactive/scenes/${categoryId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const scenes = await res.json();

        if (!scenes || scenes.length === 0) {
            container.innerHTML = `
                <div class="text-center py-5 text-muted">
                    <i class="fas fa-image fa-3x mb-3 opacity-25"></i>
                    <p>Chưa có cảnh tương tác nào trong chủ đề này</p>
                    <button class="btn btn-primary" onclick="openCreateSceneModalFromCategory()">Tạo cảnh đầu tiên</button>
                </div>
            `;
            return;
        }

        container.innerHTML = scenes.map((scene, idx) => `
            <div class="card mb-3" data-scene-id="${scene.id}">
                <div class="row g-0">
                    <div class="col-md-3">
                        <img src="${scene.imageUrl}" class="img-fluid rounded-start"
                             style="height: 150px; width: 100%; object-fit: cover;">
                    </div>
                    <div class="col-md-7">
                        <div class="card-body">
                            <h6 class="card-title fw-bold">Cảnh ${idx + 1}: ${escapeHtml(scene.description || 'Không có mô tả')}</h6>
                            <div class="mt-2">
                                ${scene.points?.map(p => `<span class="badge bg-warning me-1">📌 ${escapeHtml(p.vocab?.expression || '?')}</span>`).join('') || '<span class="text-muted">Chưa có điểm tương tác</span>'}
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2 d-flex align-items-center justify-content-end pe-3 gap-2">
                        <button class="btn btn-sm btn-warning" onclick="editSceneFromCategory(${scene.id})" title="Sửa cảnh">
                            <i class="fas fa-edit"></i> Sửa
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteSceneFromCategory(${scene.id})" title="Xóa cảnh">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch(e) {
        console.error(e);
        container.innerHTML = '<div class="alert alert-danger">Lỗi tải dữ liệu</div>';
    }
}

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
            const pointRes = await fetch('/api/admin/interactive/point', {
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

            if (!pointRes.ok) {
                console.error('Failed to create point:', await pointRes.text());
            }
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('createSceneModal'));
        if (modal) modal.hide();

        Swal.fire('Thành công', 'Đã tạo cảnh tương tác!', 'success');

        sceneHotspots = [];
        currentImageUrl = null;
        selectedVocabIdForScene = null;

        if (categoryId) {
            editCategory(categoryId);
        }

    } catch(e) {
        console.error('Error:', e);
        Swal.fire('Lỗi', e.message || 'Không thể lưu cảnh', 'error');
    }
}


async function updateScene() {
    const token = localStorage.getItem('token');
    const description = document.getElementById('editSceneDesc').value;
    const categoryId = currentCategoryIdForScene;

    Swal.fire({ title: 'Đang cập nhật...', didOpen: () => Swal.showLoading() });

    try {
        await fetch('/api/admin/interactive/scene/update', {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                sceneId: currentEditSceneId,
                description: description
            })
        });

        await fetch(`/api/admin/interactive/scene/${currentEditSceneId}/points`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        for (const point of editSceneHotspots) {
            await fetch('/api/admin/interactive/point', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    sceneId: currentEditSceneId,
                    vocabId: point.vocabId,
                    coordX: parseFloat(point.coordX),
                    coordY: parseFloat(point.coordY),
                    width: parseFloat(point.width),
                    height: parseFloat(point.height)
                })
            });
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('editSceneModal'));
        if (modal) modal.hide();

        Swal.fire('Thành công', 'Đã cập nhật cảnh tương tác!', 'success');

        if (categoryId) {
            await loadScenesListForCategory();
        }

    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', e.message || 'Không thể cập nhật', 'error');
    }
}


function openAddLessonModal() {
    document.getElementById('newLessonName').value = '';
    document.getElementById('newLessonLevel').value = '';
    document.getElementById('newLessonCategoryId').innerHTML = '<option value="">-- Vui lòng chọn cấp độ trước --</option>';
    document.getElementById('newLessonCategoryId').disabled = true;
    document.getElementById('newLessonStatus').value = 'true';
    document.getElementById('newLessonNoCateMsg').style.display = 'none';
    new bootstrap.Modal(document.getElementById('lessonModal')).show();
}


async function onLessonLevelChange() {
    const level = document.getElementById('newLessonLevel').value;
    const catSelect = document.getElementById('newLessonCategoryId');
    const loadingEl = document.getElementById('newLessonCategoryLoading');
    const noCateMsg = document.getElementById('newLessonNoCateMsg');
    const token = localStorage.getItem('token');

    noCateMsg.style.display = 'none';
    catSelect.disabled = true;
    catSelect.innerHTML = '<option value="">-- Vui lòng chọn cấp độ trước --</option>';

    if (!level) return;

    loadingEl.style.display = 'block';
    try {
        const res = await fetch(`/api/admin/categories/filter?level=${level}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const categories = await res.json();
        loadingEl.style.display = 'none';

        if (categories && categories.length > 0) {
            catSelect.innerHTML = '<option value="">-- Chọn chủ đề --</option>' +
                categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');
            catSelect.disabled = false;
        } else {
            catSelect.innerHTML = '<option value="">-- Không có chủ đề --</option>';
            noCateMsg.style.display = 'block';
        }
    } catch (e) {
        loadingEl.style.display = 'none';
        console.error(e);
    }
}

// Submit thêm bài học mới
async function submitNewLesson() {
    const token = localStorage.getItem('token');
    const name = document.getElementById('newLessonName').value.trim();
    const categoryId = document.getElementById('newLessonCategoryId').value;
    const isActive = document.getElementById('newLessonStatus').value;

    if (!name) {
        return Swal.fire('Chú ý', 'Vui lòng nhập tên bài học!', 'warning');
    }
    if (!categoryId) {
        return Swal.fire('Chú ý', 'Vui lòng chọn chủ đề!', 'warning');
    }

    try {
        const res = await fetch('/api/admin/lessons', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ lessonName: name, categoryId: categoryId, isActive: isActive })
        });

        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('lessonModal')).hide();
            await Swal.fire({
                icon: 'success', title: 'Thành công',
                text: 'Đã thêm bài học mới!', timer: 1200, showConfirmButton: false
            });
            if (typeof loadAllLessonsWithPagination === 'function') loadAllLessonsWithPagination(0);
        } else {
            const err = await res.text();
            Swal.fire({ icon: 'warning', title: 'Không thể thêm', text: err, confirmButtonColor: '#06BBCC' });
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể kết nối server', 'error');
    }
}

// Submit thêm bài học từ Category
async function submitAddLessonFromCategory(categoryId) {
    const lessonName = document.getElementById('lessonName').value.trim();
    const lessonSlug = document.getElementById('lessonSlug').value.trim();
    const isActive = document.getElementById('lessonIsActive').value === 'true';

    if (!lessonName) {
        document.getElementById('lessonName').classList.add('is-invalid');
        Swal.fire('Lỗi', 'Vui lòng nhập tên bài học', 'warning');
        return;
    }
    document.getElementById('lessonName').classList.remove('is-invalid');

    const token = localStorage.getItem('token');

    Swal.fire({
        title: 'Đang xử lý...',
        text: 'Vui lòng chờ',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    try {
        const response = await fetch('/api/admin/lessons', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({
                lessonName: lessonName,
                slug: lessonSlug,
                categoryId: categoryId,
                isActive: isActive
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Không thể thêm bài học');
        }

        const newLesson = await response.json();

        const modal = bootstrap.Modal.getInstance(document.getElementById('addLessonFromCategoryModal'));
        modal.hide();

        Swal.fire({
            title: 'Thành công!',
            text: `Đã thêm bài học "${lessonName}"`,
            icon: 'success',
            timer: 1500,
            showConfirmButton: false
        });

        await editCategory(categoryId);

    } catch (error) {
        console.error('Error:', error);
        Swal.fire({
            title: 'Lỗi!',
            text: error.message || 'Không thể thêm bài học',
            icon: 'error'
        });
    }
}

// Cập nhật preview thumbnail
function updateThumbnailPreview() {
    const url = document.getElementById('editThumbnailUrl').value;
    const thumbnailImg = document.getElementById('detailThumbnail');
    const container = document.getElementById('thumbnailPreviewContainer');

    if (url && url.trim() !== "") {
        const timestamp = new Date().getTime();
        thumbnailImg.src = url + '?v=' + timestamp;
        container.classList.remove('d-none');
    } else {
        container.classList.add('d-none');
    }
}

async function handleEditThumbnailSelect(input, categoryId) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const formData = new FormData();
        formData.append("thumbnailFile", file);
        formData.append("categoryId", categoryId);

        Swal.fire({ title: 'Đang upload thumbnail...', didOpen: () => Swal.showLoading() });

        try {
            const token = localStorage.getItem('token');
            const res = await fetch('/api/admin/categories/upload-thumbnail', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                document.getElementById('editThumbnailUrl').value = data.thumbnailUrl;
                document.getElementById('detailThumbnail').src = data.thumbnailUrl;
                document.getElementById('thumbnailPreviewContainer').classList.remove('d-none');
                Swal.fire('Thành công', 'Đã upload ảnh thumbnail mới', 'success');
            } else {
                Swal.fire('Lỗi', 'Không thể upload thumbnail', 'error');
            }
        } catch (e) {
            Swal.fire('Lỗi', 'Không thể upload thumbnail', 'error');
        }
    }
}

// Cập nhật preview ảnh
function updateAvatarPreview() {
    const url = document.getElementById('editIconUrl').value;
    const avatar = document.getElementById('detailAvatar');
    if (url) {
        avatar.src = url;
    }
}

let pendingIconFile = null;
let pendingThumbnailFile = null;
function handleEditFileSelect(input, categoryId) {
    if (input.files && input.files[0]) {
        pendingIconFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {

            document.getElementById('detailAvatar').src = e.target.result;
        };
        reader.readAsDataURL(pendingIconFile);
    }
}

// Xử lý chọn thumbnail
function handleEditThumbnailSelect(input, categoryId) {
    if (input.files && input.files[0]) {
        pendingThumbnailFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {
            document.getElementById('detailThumbnail').src = e.target.result;
            document.getElementById('thumbnailPreviewContainer').classList.remove('d-none');
            document.getElementById('thumbnailPreviewContainer').classList.remove('d-none');
        };
        reader.readAsDataURL(pendingThumbnailFile);
    }
}


async function saveCategoryChanges(id) {
    const token = localStorage.getItem('token');

    Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });

    let iconUrl = document.getElementById('editIconUrl').value;
    let thumbnailUrl = document.getElementById('editThumbnailUrl').value;

    const hasNewIcon = pendingIconFile instanceof File;
    const hasNewThumbnail = pendingThumbnailFile instanceof File;

    try {
        if (hasNewIcon) {
            const formData = new FormData();
            formData.append("file", pendingIconFile);
            formData.append("categoryId", id);

            const res = await fetch('/api/admin/categories/upload-icon', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                iconUrl = data.iconUrl;
                pendingIconFile = null;
            } else {
                Swal.fire('Lỗi', 'Không thể upload icon', 'error');
                return;
            }
        }

        if (hasNewThumbnail) {
            const formData = new FormData();
            formData.append("thumbnailFile", pendingThumbnailFile);
            formData.append("categoryId", id);

            const res = await fetch('/api/admin/categories/upload-thumbnail', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                thumbnailUrl = data.thumbnailUrl;
                pendingThumbnailFile = null;
            } else {
                Swal.fire('Lỗi', 'Không thể upload thumbnail', 'error');
                return;
            }
        }

        const updatedData = {
            categoryName: document.getElementById('editCategoryName').value,
            jlptLevel: document.getElementById('editJlptLevel').value,
            iconUrl: iconUrl,
            thumbnailUrl: thumbnailUrl,
            isActive: document.getElementById('editIsActive').value === 'true'
        };

        if (!updatedData.categoryName) {
            Swal.fire('Lỗi', 'Tên chủ đề không được để trống', 'error');
            return;
        }

        const res = await fetch(`/api/admin/categories/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        });

        if (res.ok) {
            document.getElementById('editIconUrl').style.backgroundColor = '';
            document.getElementById('editIconUrl').style.borderColor = '';
            document.getElementById('editThumbnailUrl').style.backgroundColor = '';
            document.getElementById('editThumbnailUrl').style.borderColor = '';

            Swal.fire({
                icon: 'success',
                title: 'Thành công',
                text: 'Đã cập nhật chủ đề',
                timer: 1500,
                showConfirmButton: false
            });

            setTimeout(() => {
                editCategory(id);
            }, 1500);
        } else {
            const error = await res.text();
            Swal.fire('Lỗi', error, 'error');
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể cập nhật', 'error');
    }
}

let _currentCatId = null;

function openAddLessonInCateModal(catId) {
    _currentCatId = catId;
    document.getElementById('inCateLessonName').value = '';
    document.getElementById('inCateLessonStatus').value = 'true';
    new bootstrap.Modal(document.getElementById('addLessonInCateModal')).show();
}

async function submitLessonInCate() {
    const token = localStorage.getItem('token');
    const name = document.getElementById('inCateLessonName').value.trim();
    const isActive = document.getElementById('inCateLessonStatus').value;

    if (!name) {
        return Swal.fire('Chú ý', 'Vui lòng nhập tên bài học!', 'warning');
    }
    if (!_currentCatId) {
        return Swal.fire('Lỗi', 'Không xác định được chủ đề!', 'error');
    }

    try {
        const res = await fetch(`/api/admin/lessons/in-category/${_currentCatId}`, {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ lessonName: name, isActive: isActive })
        });

        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('addLessonInCateModal')).hide();
            await Swal.fire({
                icon: 'success', title: 'Thành công',
                text: 'Đã thêm bài học!', timer: 1200, showConfirmButton: false
            });
            editCategory(_currentCatId);
        } else {
            const err = await res.text();
            Swal.fire({ icon: 'warning', title: 'Không thể thêm', text: err, confirmButtonColor: '#06BBCC' });
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể kết nối server', 'error');
    }
}

// Quay lại danh sách chủ đề
function goBackToCategories() {
    const navItems = document.querySelectorAll('.nav-item');
    for (let item of navItems) {
        if (item.innerText.trim() === 'Chủ đề') {
            const currentPage = paginationManager['categoryPagination']?.currentPage || 0;
            loadSection('categories', item, new Event('click'));
            setTimeout(() => {
                loadAllCategoriesWithPagination(currentPage);
            }, 100);
            break;
        }
    }
}

