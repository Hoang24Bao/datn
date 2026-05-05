// ==================== CATEGORY MANAGEMENT ====================

let selectedFile = null;

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

function handleFileSelect(input) {
    if (input.files && input.files[0]) {
        selectedFile = input.files[0];
        const reader = new FileReader();
        reader.onload = function (e) {
            const previewImg = document.getElementById('previewImg');
            const previewDiv = document.getElementById('iconPreview');
            previewImg.src = e.target.result;
            previewDiv.style.display = 'block';
            document.getElementById('catIconUrl').value = selectedFile.name;
        };
        reader.readAsDataURL(selectedFile);
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
            loadAllCategoriesWithPagination(0);
        } else {
            Swal.fire('Lỗi', 'Không thể lưu chủ đề', 'error');
        }
    } catch (e) { console.error(e); }
}

async function loadAllCategoriesWithPagination(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('categorySearch')?.value || "";
    const level = document.getElementById('categoryFilterLevel')?.value || "";
    const isActive = document.getElementById('categoryFilterStatus')?.value || "";
    const pageSize = 5;

    try {
        let url = `/api/admin/categories/paging?page=${page}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (level) url += `&level=${level}`;
        if (isActive !== "") url += `&isActive=${isActive}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        if (!res.ok) throw new Error('Network response was not ok');

        const data = await res.json();
        const tbody = document.getElementById('categoryTableBody');

        const countBadge = document.getElementById('categoryCountBadge');
        if (countBadge) countBadge.innerText = `${data.totalElements || 0} chủ đề`;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5 text-muted">Không tìm thấy chủ đề nào</td></tr>`;
            if (paginationManager['categoryPagination']) paginationManager['categoryPagination'].reset();
            return;
        }

        tbody.innerHTML = data.content.map((cat, index) => {
            const isHidden = cat.isActive === false;
            const rowClass = isHidden ? 'row-hidden' : '';
            const statusBadge = isHidden ? '<span class="status-badge badge-hidden">Đã ẩn</span>' : '';
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
        document.getElementById('categoryTableBody').innerHTML = `<tr><td colspan="6" class="text-center text-danger">Lỗi nạp dữ liệu</td></tr>`;
    }
}

async function deleteCategory(id) {
    const result = await Swal.fire({
        title: 'Xác nhận thay đổi?',
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
        if (lessonsRes.ok) lessons = await lessonsRes.json();

        mainTitle.innerText = `Chỉnh sửa Chủ đề: ${category.categoryName}`;

        mainContent.innerHTML = `
            <div class="category-detail-container" data-category-id="${id}">
                <button class="back-to-list" onclick="goBackToCategories()">
                    <i class="fas fa-arrow-left"></i> Quay lại danh sách
                </button>
                <div class="category-detail-header">
                    <img src="${category.iconUrl || 'https://placehold.co/100x100?text=No+Image'}"
                         alt="${category.categoryName}" class="category-detail-avatar" id="detailAvatar"
                         onerror="this.src='https://placehold.co/100x100?text=No+Image'">
                    <div class="category-detail-title" style="flex: 1;">
                        <input type="text" id="editCategoryName" class="form-control form-control-lg"
                               value="${category.categoryName}" style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">
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
                            <h4><i class="fas fa-image"></i> URL Ảnh đại diện</h4>
                            <div class="input-group">
                                <input type="text" id="editIconUrl" class="form-control" value="${category.iconUrl || ''}"
                                       placeholder="Nhập URL ảnh" onchange="updateAvatarPreview()">
                                <button class="btn btn-orange" type="button" onclick="document.getElementById('editFileInput').click()">
                                    <i class="fas fa-upload"></i> Upload
                                </button>
                            </div>
                            <input type="file" id="editFileInput" style="display: none;" accept="image/*" onchange="handleEditFileSelect(this, ${id})">
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
                <div class="lessons-list">
                    <h3><i class="fas fa-list"></i> Danh sách bài học</h3>
                    ${lessons.length === 0 ?
                        '<p class="text-muted text-center py-4">Chưa có bài học nào trong chủ đề này</p>' :
                        `<table class="lessons-table">
                            <thead><tr><th>STT</th><th>ID</th><th style="padding-left: 30px;">Tên bài học</th><th style="text-align: center;">Số từ vựng</th></tr></thead>
                            <tbody>
                                ${lessons.map((lesson, idx) => `
                                    <tr data-lesson-id="${lesson.id}">
                                        <td>${idx + 1}</td>
                                        <td><span class="id-badge">#${lesson.id}</span></td>
                                        <td style="padding-left: 30px;"><strong>${lesson.lessonName}</strong></td>
                                        <td style="text-align: center;"><span class="level-badge">${lesson.totalVocab || 0} từ</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>`
                    }
                </div>
            </div>
        `;
        localStorage.setItem('activeAdminTab', 'categories');
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể tải thông tin chi tiết', 'error');
    }
}

function updateAvatarPreview() {
    const url = document.getElementById('editIconUrl').value;
    const avatar = document.getElementById('detailAvatar');
    if (url) avatar.src = url;
}

async function handleEditFileSelect(input, categoryId) {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const formData = new FormData();
        formData.append("file", file);
        formData.append("categoryId", categoryId);

        Swal.fire({ title: 'Đang upload...', didOpen: () => Swal.showLoading() });

        try {
            const token = localStorage.getItem('token');
            const res = await fetch('/api/admin/categories/upload-icon', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            if (res.ok) {
                const data = await res.json();
                document.getElementById('editIconUrl').value = data.iconUrl;
                document.getElementById('detailAvatar').src = data.iconUrl;
                Swal.fire('Thành công', 'Đã upload ảnh mới', 'success');
            } else {
                Swal.fire('Lỗi', 'Không thể upload ảnh', 'error');
            }
        } catch (e) {
            Swal.fire('Lỗi', 'Không thể upload ảnh', 'error');
        }
    }
}

async function saveCategoryChanges(id) {
    const token = localStorage.getItem('token');
    const updatedData = {
        categoryName: document.getElementById('editCategoryName').value,
        jlptLevel: document.getElementById('editJlptLevel').value,
        iconUrl: document.getElementById('editIconUrl').value,
        isActive: document.getElementById('editIsActive').value === 'true'
    };

    if (!updatedData.categoryName) {
        Swal.fire('Lỗi', 'Tên chủ đề không được để trống', 'error');
        return;
    }

    try {
        const res = await fetch(`/api/admin/categories/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        });

        if (res.ok) {
            Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã cập nhật chủ đề', timer: 1000, showConfirmButton: false });
            goBackToCategories();
        } else {
            const error = await res.text();
            Swal.fire('Lỗi', error, 'error');
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể cập nhật', 'error');
    }
}

function goBackToCategories() {
    const navItems = document.querySelectorAll('.nav-item');
    for (let item of navItems) {
        if (item.innerText.trim() === 'Chủ đề') {
            loadSection('categories', item, new Event('click'));
            break;
        }
    }
}