// ==================== LESSON MANAGEMENT ====================

async function loadAllLessonsWithPagination(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('lessonSearch')?.value || "";
    const categoryId = document.getElementById('lessonFilterCategory')?.value || "";
    const level = document.getElementById('lessonFilterLevel')?.value || "";
    const pageSize = 10;

    try {
        let url = `/api/admin/lessons/paging?page=${page}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (categoryId) url += `&categoryId=${categoryId}`;
        if (level) url += `&level=${level}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        if (!res.ok) throw new Error('Network response was not ok');

        const data = await res.json();
        const tbody = document.getElementById('lessonTableBody');

        const countBadge = document.getElementById('lessonCountBadge');
        if (countBadge) countBadge.innerText = `${data.totalElements || 0} bài học`;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-5 text-muted">Không tìm thấy bài học nào</td</tr>`;
            if (paginationManager['lessonPagination']) paginationManager['lessonPagination'].reset();
            return;
        }

        tbody.innerHTML = data.content.map((les, index) => {
            const totalVocab = les.totalVocab || 0;
            const lessonName = les.lessonName || '';
            const categoryName = les.categoryName || 'Chưa phân loại';
            return `
            <tr>
                <td style="text-align: center;">${(page * pageSize) + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${les.id}</span></td>
                <td><strong>${escapeHtml(lessonName)}</strong></td>
                <td><strong style="color:var(--teal-dark)">${escapeHtml(categoryName)}</strong></td>
                <td style="text-align: center;"><span class="level-badge">${totalVocab} từ</span></td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="editLesson(${les.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action btn-trash" title="Xóa" onclick="deleteLesson(${les.id})">
                        <i class="fas fa-trash"></i>
                    </button>
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
        if (level) url = `/api/admin/categories/filter?level=${level}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const categories = await res.json();
        const select = document.getElementById('lessonFilterCategory');
        if (select) {
            select.innerHTML = '<option value="">Tất cả chủ đề</option>' +
                categories.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)}</option>`).join('');
        }
    } catch (e) {
        console.error("Lỗi tải danh sách chủ đề:", e);
    }
}

async function onLessonLevelChange() {
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
                    categories.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)}</option>`).join('');
            }
        } catch (e) {
            console.error("Lỗi tải chủ đề theo cấp độ:", e);
        }
    }
    loadAllLessonsWithPagination(0);
}

async function openAddLessonModal() {
    const token = localStorage.getItem('token');
    document.getElementById('lessonForm').reset();
    try {
        const res = await fetch('/api/admin/categories', { headers: { 'Authorization': 'Bearer ' + token } });
        const categories = await res.json();
        const select = document.getElementById('lessonCategorySelect');
        select.innerHTML = categories.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)} (${c.jlptLevel})</option>`).join('');
        new bootstrap.Modal(document.getElementById('lessonModal')).show();
    } catch (e) {
        Swal.fire('Lỗi', 'Không thể tải chủ đề', 'error');
    }
}

async function submitLesson() {
    const name = document.getElementById('lessonName').value.trim();
    const catId = document.getElementById('lessonCategorySelect').value;
    const token = localStorage.getItem('token');

    if (!name) return Swal.fire('Lỗi', 'Tên không được trống', 'error');

    try {
        const res = await fetch('/api/admin/lessons', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json' },
            body: JSON.stringify({ lessonName: name, categoryId: catId })
        });
        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('lessonModal')).hide();
            Swal.fire('Thành công', 'Đã thêm bài học!', 'success');
            loadAllLessonsWithPagination(0);
            loadLessonCategoryFilter();
        } else {
            const error = await res.text();
            Swal.fire('Lỗi', error, 'error');
        }
    } catch (e) {
        console.error(e);
    }
}

// Fallback function cho lessons (nếu cần)
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
            tbody.innerHTML = `<td><td colspan="6" class="text-center py-5 text-muted">Chưa có bài học nào</td</tr>`;
            if (paginationManager['lessonPagination']) paginationManager['lessonPagination'].reset();
            return;
        }

        tbody.innerHTML = pageData.map((les, index) => `
            <tr>
                <td style="text-align: center;">${start + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${les.id}</span></td>
                <td><strong>${escapeHtml(les.lessonName)}</strong></td>
                <td><strong style="color:var(--teal-dark)">${escapeHtml(les.categoryName || 'Chưa phân loại')}</strong></td>
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