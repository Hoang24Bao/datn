// ==================== VOCABULARY MANAGEMENT ====================

let bulkModeActive = false;
let selectedVocabIds = new Set();

async function loadVocabWithPagination(page) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('vocabSearch')?.value || "";
    const cateId = document.getElementById('filterCate')?.value || "";
    const level = document.getElementById('filterLevel')?.value || "";
    const pageSize = 20;

    try {
        let url = `/api/admin/vocab/all?page=${page}&size=${pageSize}&search=${encodeURIComponent(search)}&cateId=${cateId}&level=${level}`;
        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const data = await res.json();

        const tbody = document.getElementById('vocabTableBody');

        const countBadge = document.getElementById('vocabCountBadge');
        if (countBadge) countBadge.innerText = `${data.totalElements || 0} từ vựng`;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">Không tìm thấy từ vựng nào</td</tr>`;
            if (paginationManager['vocabPagination']) paginationManager['vocabPagination'].reset();
            return;
        }

        tbody.innerHTML = data.content.map((v, index) => {
            const sttNumber = (page * pageSize) + index + 1;
            return `
            <tr>
                ${bulkModeActive ?
                    `<td class="checkbox-cell" style="text-align: center;">
                        <input type="checkbox" class="select-checkbox row-select-vocab" value="${v.id}"
                               onchange="updateVocabSelection(this)">
                      </td>
                     <td class="stt-cell" style="display: none; text-align: center;">${sttNumber}</td>` :
                    `<td style="text-align: center;">${sttNumber}</td>`
                }
                <td style="text-align:center;"><span class="id-badge">#${v.id}</span></td>
                <td><strong style="font-size: 18px; color: var(--text-dark);">${escapeHtml(v.expression)}</strong></td>
                <td><span style="color: var(--teal); font-weight: 600;">${escapeHtml(v.kana)}</span></td>
                <td><small style="font-size: 15px;">${escapeHtml(v.romaji)}</small></td>
                <td>${escapeHtml(v.meaning)}</td>
                <td style="text-align:center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="editVocab(${v.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action btn-trash" title="Xóa" onclick="deleteVocab(${v.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                  </td>
            </tr>`;
        }).join('');

        if (paginationManager['vocabPagination']) {
            paginationManager['vocabPagination'].render(data.totalPages, data.number);
        }
    } catch (e) {
        console.error(e);
        document.getElementById('vocabTableBody').innerHTML = `<tr><td colspan="8" class="text-center text-danger">Lỗi nạp dữ liệu</td</tr>`;
    }
}

async function deleteVocab(id) {
    const result = await Swal.fire({
        title: 'Xác nhận xóa?',
        text: "Bạn có chắc muốn xóa từ vựng này?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#06BBCC',
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`/api/admin/vocab/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (res.ok) {
                Swal.fire('Thành công', 'Đã xóa từ vựng', 'success');
                const currentPage = paginationManager['vocabPagination']?.currentPage || 0;
                loadVocabWithPagination(currentPage);
            } else {
                Swal.fire('Lỗi', 'Không thể xóa từ vựng', 'error');
            }
        } catch (e) { console.error(e); }
    }
}

async function updateCategoryFilter() {
    const token = localStorage.getItem('token');
    const level = document.getElementById('filterLevel').value;
    const cateSelect = document.getElementById('filterCate');

    if (!level) {
        cateSelect.innerHTML = '<option value="">Tất cả chủ đề</option>';
        loadVocabWithPagination(0);
        return;
    }
    try {
        const res = await fetch(`/api/admin/categories/filter?level=${level}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.ok) {
            const categories = await res.json();
            let options = '<option value="">Tất cả chủ đề</option>';
            options += categories.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)}</option>`).join('');
            cateSelect.innerHTML = options;
        }
    } catch (e) { console.error(e); }
    loadVocabWithPagination(0);
}

async function openAddVocabModal() {
    const token = localStorage.getItem('token');
    document.getElementById('vocabForm').reset();
    document.getElementById('quickAddLessonContainer').style.display = 'none';

    try {
        const res = await fetch('/api/admin/categories', { headers: { 'Authorization': 'Bearer ' + token } });
        const categories = await res.json();

        const catSelect = document.getElementById('vCategoryId');
        catSelect.innerHTML = '<option value="">-- Chọn chủ đề --</option>' +
            categories.map(c => `<option value="${c.id}">${escapeHtml(c.categoryName)}</option>`).join('');

        document.getElementById('vLessonId').innerHTML = '<option value="">-- Vui lòng chọn chủ đề trước --</option>';
        new bootstrap.Modal(document.getElementById('vocabModal')).show();
    } catch (e) {
        Swal.fire('Lỗi', 'Không thể khởi tạo modal', 'error');
    }
}

async function submitVocab() {
    const token = localStorage.getItem('token');
    const data = {
        expression: document.getElementById('vExpression').value,
        kana: document.getElementById('vKana').value,
        romaji: document.getElementById('vRomaji').value,
        meaning: document.getElementById('vMeaning').value,
        wordType: document.getElementById('vWordType').value,
        example: document.getElementById('vExample').value,
        exampleVi: document.getElementById('vExampleVi').value,
        imageUrl: document.getElementById('vImageUrl').value,
        audioUrl: document.getElementById('vAudioUrl').value,
        lessonId: document.getElementById('vLessonId').value
    };

    if (!data.expression || !data.meaning || !data.lessonId) {
        return Swal.fire('Chú ý', 'Vui lòng điền các trường bắt buộc!', 'warning');
    }

    try {
        const res = await fetch('/api/admin/vocab/add', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('vocabModal')).hide();
            Swal.fire('Thành công', 'Đã thêm từ vựng mới!', 'success');
            loadVocabWithPagination(0);
        } else {
            Swal.fire('Lỗi', 'Không thể lưu từ vựng', 'error');
        }
    } catch (e) { console.error(e); }
}

async function onVocabCategoryChange() {
    const catId = document.getElementById('vCategoryId').value;
    const lessonSelect = document.getElementById('vLessonId');
    const quickAddContainer = document.getElementById('quickAddLessonContainer');
    const token = localStorage.getItem('token');

    if (!catId) {
        lessonSelect.innerHTML = '<option value="">-- Vui lòng chọn chủ đề trước --</option>';
        quickAddContainer.style.display = 'none';
        return;
    }

    try {
        const res = await fetch(`/api/admin/lessons/by-category/${catId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const lessons = await res.json();

        if (lessons && lessons.length > 0) {
            lessonSelect.innerHTML = lessons.map(l => `<option value="${l.id}">${escapeHtml(l.lessonName)}</option>`).join('');
            quickAddContainer.style.display = 'none';
            lessonSelect.disabled = false;
        } else {
            lessonSelect.innerHTML = '<option value="">(Trống)</option>';
            lessonSelect.disabled = true;
            quickAddContainer.style.display = 'block';
        }
    } catch (e) { console.error(e); }
}

function openQuickAddLesson() {
    const catId = document.getElementById('vCategoryId').value;
    const catName = document.getElementById('vCategoryId').options[document.getElementById('vCategoryId').selectedIndex].text;

    const lessonCatSelect = document.getElementById('lessonCategorySelect');
    if (lessonCatSelect) lessonCatSelect.value = catId;

    const lessonModal = new bootstrap.Modal(document.getElementById('lessonModal'));
    lessonModal.show();
    console.log(`Đang tạo bài học cho chủ đề: ${catName}`);
}

// ==================== BULK MODE FUNCTIONS ====================

function toggleBulkMode() {
    bulkModeActive = !bulkModeActive;
    const toggleBtn = document.getElementById('toggleBulkModeBtn');
    const selectAllCheckbox = document.getElementById('selectAllVocab');
    const tableSection = document.querySelector('.table-vocab');

    if (bulkModeActive) {
        toggleBtn.innerHTML = '<i class="fas fa-times"></i> Thoát';
        toggleBtn.classList.add('active');
        tableSection.classList.add('bulk-mode-active');
        if (selectAllCheckbox) selectAllCheckbox.style.display = 'inline-block';
        selectedVocabIds.clear();
        updateBulkBar();
        const currentPage = paginationManager['vocabPagination']?.currentPage || 0;
        loadVocabWithPagination(currentPage);
    } else {
        toggleBtn.innerHTML = '<i class="fas fa-check-square"></i> Chọn nhiều';
        toggleBtn.classList.remove('active');
        tableSection.classList.remove('bulk-mode-active');
        if (selectAllCheckbox) {
            selectAllCheckbox.style.display = 'none';
            selectAllCheckbox.checked = false;
        }
        selectedVocabIds.clear();
        hideBulkBar();
        const currentPage = paginationManager['vocabPagination']?.currentPage || 0;
        loadVocabWithPagination(currentPage);
    }
}

function updateBulkBar() {
    let bar = document.getElementById('vocab-bulk-bar');
    if (!bar) {
        bar = document.createElement('div');
        bar.className = 'bulk-action-bar';
        bar.id = 'vocab-bulk-bar';
        bar.innerHTML = `
            <span>📌 Đã chọn: <span class="selected-count" id="vocab-selected-count">0</span> từ</span>
            <button class="btn-bulk btn-bulk-delete" onclick="bulkDeleteVocab()">
                <i class="fas fa-trash-alt"></i> Xóa
            </button>
            <button class="btn-bulk btn-bulk-cancel" onclick="clearVocabSelection()">
                <i class="fas fa-times"></i> Hủy chọn
            </button>
        `;
        document.body.appendChild(bar);
    }

    const countSpan = document.getElementById('vocab-selected-count');
    if (countSpan) countSpan.innerText = selectedVocabIds.size;

    if (selectedVocabIds.size > 0) {
        bar.classList.add('show');
    } else {
        bar.classList.remove('show');
    }
}

function hideBulkBar() {
    const bar = document.getElementById('vocab-bulk-bar');
    if (bar) bar.classList.remove('show');
}

function clearVocabSelection() {
    const checkboxes = document.querySelectorAll('#vocabTableBody .row-select-vocab');
    checkboxes.forEach(cb => {
        cb.checked = false;
        const id = parseInt(cb.value);
        selectedVocabIds.delete(id);
    });
    updateBulkBar();

    const selectAll = document.getElementById('selectAllVocab');
    if (selectAll) selectAll.checked = false;
}

function toggleSelectAllVocab() {
    const selectAll = document.getElementById('selectAllVocab');
    const checkboxes = document.querySelectorAll('#vocabTableBody .row-select-vocab');

    checkboxes.forEach(cb => {
        cb.checked = selectAll.checked;
        const id = parseInt(cb.value);
        if (selectAll.checked) {
            selectedVocabIds.add(id);
        } else {
            selectedVocabIds.delete(id);
        }
    });
    updateBulkBar();
}

function updateVocabSelection(checkbox) {
    const id = parseInt(checkbox.value);
    if (checkbox.checked) {
        selectedVocabIds.add(id);
    } else {
        selectedVocabIds.delete(id);
    }
    updateBulkBar();

    const selectAll = document.getElementById('selectAllVocab');
    if (selectAll) {
        const totalCheckboxes = document.querySelectorAll('#vocabTableBody .row-select-vocab').length;
        const checkedCheckboxes = document.querySelectorAll('#vocabTableBody .row-select-vocab:checked').length;
        selectAll.checked = totalCheckboxes > 0 && totalCheckboxes === checkedCheckboxes;
        selectAll.indeterminate = checkedCheckboxes > 0 && checkedCheckboxes < totalCheckboxes;
    }
}

async function bulkDeleteVocab() {
    if (selectedVocabIds.size === 0) return;

    const result = await Swal.fire({
        title: 'Xác nhận xóa',
        text: `Bạn có chắc muốn xóa ${selectedVocabIds.size} từ vựng đã chọn?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        const token = localStorage.getItem('token');
        let successCount = 0;

        for (const id of selectedVocabIds) {
            try {
                const res = await fetch(`/api/admin/vocab/${id}`, {
                    method: 'DELETE',
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                if (res.ok) successCount++;
            } catch (e) { console.error(e); }
        }

        Swal.fire('Thành công', `Đã xóa ${successCount} từ vựng`, 'success');
        clearVocabSelection();
        if (bulkModeActive) toggleBulkMode();
        const currentPage = paginationManager['vocabPagination']?.currentPage || 0;
        loadVocabWithPagination(currentPage);
    }
}

async function editVocab(id) {
    Swal.fire({
        icon: 'info',
        title: 'Thông báo',
        text: `Chức năng sửa từ vựng ID: ${id} đang phát triển`,
        timer: 1500,
        showConfirmButton: false
    });
}