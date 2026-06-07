// ===== VOCABULARY ===== //
async function loadVocabWithPagination(page) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('vocabSearch')?.value || "";
    const cateId = document.getElementById('filterCate')?.value || "";
    const level = document.getElementById('filterLevel')?.value || "";
    const pageSize = paginationManager['vocabPagination'].pageSize;

    try {
        let url = `/api/admin/vocab/all?page=${page}&size=${pageSize}&search=${encodeURIComponent(search)}&cateId=${cateId}&level=${level}`;
        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const data = await res.json();

        const tbody = document.getElementById('vocabTableBody');

        const countBadge = document.getElementById('vocabCountBadge');
        if (countBadge) {
            countBadge.innerText = `${data.totalElements || 0} từ vựng`;
        }

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">Không tìm thấy từ vựng nào</td</tr>`;
            if (paginationManager['vocabPagination']) {
                paginationManager['vocabPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = data.content.map((v, index) => {
            const sttNumber = (page * pageSize) + index + 1;
            const isActive = v.isActive !== undefined ? v.isActive : true;
            const rowClass = !isActive ? 'row-hidden' : '';
            const actionBtn = !isActive
        ? `<button class="btn-action btn-restore" title="Khôi phục" onclick="deleteVocab(${v.id})">
            <i class="fas fa-trash-restore"></i>
           </button>`
        : `<button class="btn-action btn-trash" title="Ẩn" onclick="deleteVocab(${v.id})">
            <i class="fas fa-trash"></i>
           </button>`;

            return `
    <tr class="${rowClass}">
        ${bulkModeActive ?
            `<td class="checkbox-cell" style="text-align: center;">
                <input type="checkbox" class="select-checkbox row-select-vocab" value="${v.id}"
                       onchange="updateVocabSelection(this)">
              </td>
             <td class="stt-cell" style="display: none; text-align: center;">${sttNumber}</td>` :
            `<td style="text-align: center;">${sttNumber}</td>`
        }
        <td style="text-align:center;"><span class="id-badge">#${v.id}</span></td>
        <td><strong style="font-size: 18px; color: var(--text-dark);">${v.expression}</strong></td>
        <td><span style="color: var(--teal); font-weight: 600;">${v.kana}</span></td>
        <td><small style="font-size: 15px;">${v.romaji}</small></td>
        <td>${v.meaning}</td>
        <td style="text-align:center;">
            <button class="btn-action btn-edit me-1" title="Sửa" onclick="editVocab(${v.id})">
                <i class="fas fa-edit"></i>
            </button>
            ${actionBtn}
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

// Ẩn/Hiện từ vựng
async function deleteVocab(id) {
    const token = localStorage.getItem('token');

    try {
        const checkRes = await fetch(`/api/admin/vocab/${id}/check-usage`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (checkRes.ok) {
            const usageData = await checkRes.json();
            if (usageData.hasInteractivePoints) {
                const scenesList = usageData.scenes.map(s =>
                    `<li><strong>${escapeHtml(s.sceneName)}</strong> - Bài học: ${escapeHtml(s.lessonName)}</li>`
                ).join('');

                const result = await Swal.fire({
                    title: 'Không thể ẩn từ vựng này!',
                    html: `
                        <div style="text-align: left;">
                            <p>Từ vựng <strong>${escapeHtml(usageData.expression)}</strong> đang được sử dụng trong:</p>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                ${scenesList}
                            </ul>
                            <p class="text-danger mt-2" style="color: #dc2626;">
                                <i class="fas fa-exclamation-triangle"></i>
                                Vui lòng xóa hoặc sửa các cảnh tương tác này trước khi ẩn từ vựng.
                            </p>
                        </div>
                    `,
                    icon: 'warning',
                    confirmButtonText: 'Đến trang Cảnh tương tác',
                    showCancelButton: true,
                    cancelButtonText: 'Hủy'
                });

                if (result.isConfirmed && usageData.firstLessonId) {
                    editLesson(usageData.firstLessonId);
                    setTimeout(() => {
                        const scenesTab = document.querySelector('#scenes-tab');
                        if (scenesTab) {
                            const tab = new bootstrap.Tab(scenesTab);
                            tab.show();
                        }
                    }, 500);
                }
                return;
            }
        }

        const result = await Swal.fire({
            title: 'Xác nhận thay đổi?',
            text: "Hành động này sẽ thay đổi trạng thái hiển thị của từ vựng đối với người dùng.",
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#06BBCC',
            cancelButtonColor: '#ef4444',
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Hủy'
        });

        if (result.isConfirmed) {
            const res = await fetch(`/api/admin/vocab/${id}/toggle-status`, {
                method: 'PATCH',
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
                const currentPage = paginationManager['vocabPagination']?.currentPage || 0;
                loadVocabWithPagination(currentPage);
            } else {
                const error = await res.text();
                Swal.fire('Lỗi', error, 'error');
            }
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể kiểm tra ràng buộc', 'error');
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
            options += categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');
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
        // 1. Load danh sách Chủ đề
        const res = await fetch('/api/admin/categories/active', { headers: { 'Authorization': 'Bearer ' + token } });
        const categories = await res.json();

        const catSelect = document.getElementById('vCategoryId');
        catSelect.innerHTML = '<option value="">-- Chọn chủ đề --</option>' +
            categories.map(c => `<option value="${c.id}">${c.categoryName}</option>`).join('');

        // 2. Reset ô bài học
        document.getElementById('vLessonId').innerHTML = '<option value="">-- Vui lòng chọn chủ đề trước --</option>';

        new bootstrap.Modal(document.getElementById('vocabModal')).show();
    } catch (e) {
        Swal.fire('Lỗi', 'Không thể khởi tạo modal', 'error');
    }
}

async function submitVocab() {
    const token = localStorage.getItem('token');

    const expression = document.getElementById('vExpression').value;
    const kana = document.getElementById('vKana').value;
    const romaji = document.getElementById('vRomaji').value;
    const meaning = document.getElementById('vMeaning').value;
    const wordType = document.getElementById('vWordType').value;
    const example = document.getElementById('vExample').value;
    const exampleVi = document.getElementById('vExampleVi').value;
    const lessonId = document.getElementById('vLessonId').value;

    if (!expression || !meaning) {
        return Swal.fire('Chú ý', 'Vui lòng điền các trường bắt buộc!', 'warning');
    }

    Swal.fire({
        title: 'Đang xử lý...',
        text: 'Vui lòng chờ trong giây lát',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        let imageUrl = null;
        const imageFile = document.getElementById('vocabImageFile').files[0];

        // 1. Upload ảnh nếu có
        if (imageFile && romaji) {
            const formData = new FormData();
            formData.append('file', imageFile);
            formData.append('romaji', romaji);

            const imageResponse = await fetch('/api/admin/upload/image', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            const imageData = await imageResponse.json();
            if (imageResponse.ok) {
                imageUrl = imageData.imageUrl;
            } else {
                throw new Error(imageData.message || 'Upload ảnh thất bại');
            }
        }

        // 2. Lấy audio URL từ field
        let audioUrl = document.getElementById('vAudioUrl').value;

        // 3. Lưu từ vựng vào database
        const data = {
            expression: expression,
            kana: kana,
            romaji: romaji,
            meaning: meaning,
            wordType: wordType,
            example: example,
            exampleVi: exampleVi,
            imageUrl: imageUrl,
            audioUrl: audioUrl,
            lessonId: lessonId
        };


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
            document.getElementById('vocabForm').reset();
            document.getElementById('imagePreviewContainer').style.display = 'none';
            document.getElementById('audioPreview').style.display = 'none';
            document.getElementById('vImageUrl').value = '';
            document.getElementById('vAudioUrl').value = '';
            document.getElementById('clearAudioBtn').style.display = 'none';

            Swal.fire('Thành công', 'Đã thêm từ vựng mới!', 'success');
            loadVocabWithPagination(0);
        } else {
            const error = await res.text();
            Swal.fire('Lỗi', error, 'error');
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', e.message || 'Không thể lưu từ vựng', 'error');
    }
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
            lessonSelect.innerHTML = lessons.map(l => `<option value="${l.id}">${l.lessonName}</option>`).join('');
            quickAddContainer.style.display = 'none';
            lessonSelect.disabled = false;
        } else {
            lessonSelect.innerHTML = '<option value="">(Trống)</option>';
            lessonSelect.disabled = true;
            quickAddContainer.style.display = 'block';
        }
    } catch (e) { console.error(e); }
}

// ===== PREVIEW ẢNH  =====
document.getElementById('vocabImageFile').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        if (!file.type.startsWith('image/')) {
            Swal.fire('Lỗi', 'Chỉ chấp nhận file ảnh!', 'error');
            this.value = '';
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            Swal.fire('Lỗi', 'File ảnh không được vượt quá 5MB!', 'error');
            this.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = function(event) {
            const previewImg = document.getElementById('imagePreview');
            const previewContainer = document.getElementById('imagePreviewContainer');
            previewImg.src = event.target.result;
            previewContainer.style.display = 'block';

            document.getElementById('vImageUrl').value = '';
        };
        reader.readAsDataURL(file);
    } else {
        document.getElementById('imagePreviewContainer').style.display = 'none';
        document.getElementById('vImageUrl').value = '';
    }
});

// Xóa ảnh đã chọn
function clearVocabImage() {
    document.getElementById('vocabImageFile').value = '';
    document.getElementById('vImageUrl').value = '';
    document.getElementById('imagePreviewContainer').style.display = 'none';
    document.getElementById('imagePreview').src = '';
}


// Sinh audio từ Romaji
async function generateVocabAudio() {
    const romaji = document.getElementById('vRomaji').value.trim();

    if (!romaji) {
        Swal.fire('Chú ý', 'Vui lòng nhập Romaji trước khi sinh audio!', 'warning');
        return;
    }

    const token = localStorage.getItem('token');

    Swal.fire({
        title: 'Đang sinh audio...',
        text: `Đang xử lý: "${romaji}"`,
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        const response = await fetch('/api/admin/vocab/generate-audio', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                romaji: romaji,
                vocabId: 0
            })
        });

        const data = await response.json();

        if (response.ok) {
            const audioUrl = data.audioUrl;
            document.getElementById('vAudioUrl').value = audioUrl;

            const audioPlayer = document.getElementById('audioPreview');
            const audioSource = document.getElementById('audioSource');
            audioSource.src = audioUrl;
            audioPlayer.load();
            audioPlayer.style.display = 'block';
            document.getElementById('clearAudioBtn').style.display = 'inline-block';

            Swal.fire('Thành công', 'Đã sinh audio thành công!', 'success');
        } else {
            Swal.fire('Lỗi', data.error || 'Sinh audio thất bại', 'error');
        }
    } catch (error) {
        console.error('Audio generation error:', error);
        Swal.fire('Lỗi', 'Không thể sinh audio', 'error');
    }
}

// Preview audio
function previewAudio() {
    const audioUrl = document.getElementById('vAudioUrl').value;
    if (audioUrl) {
        const audioPlayer = document.getElementById('audioPreview');
        const audioSource = document.getElementById('audioSource');
        audioSource.src = audioUrl;
        audioPlayer.load();
        audioPlayer.style.display = 'block';
    } else {
        Swal.fire('Chú ý', 'Chưa có audio URL!', 'warning');
    }
}

// Xóa audio
function clearVocabAudio() {
    document.getElementById('vAudioUrl').value = '';
    document.getElementById('audioPreview').style.display = 'none';
    document.getElementById('audioSource').src = '';
    document.getElementById('clearAudioBtn').style.display = 'none';
}


// ===== VOCABULARY DETAIL ===== //
// Hàm xem/sửa chi tiết từ vựng
async function editVocab(id) {
    const token = localStorage.getItem('token');
    const mainContent = document.getElementById('main-content');
    const mainTitle = document.getElementById('page-main-title');

    try {
        const res = await fetch(`/api/admin/vocab/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) throw new Error('Không thể tải thông tin từ vựng');

        const vocab = await res.json();

        const lessonsRes = await fetch(`/api/admin/lessons`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        let lessons = [];
        if (lessonsRes.ok) {
            lessons = await lessonsRes.json();
        }

        const categoriesRes = await fetch(`/api/admin/categories`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        let categories = [];
        if (categoriesRes.ok) {
            categories = await categoriesRes.json();
        }

        mainTitle.innerText = `Chi tiết từ vựng: ${vocab.expression || ''}`;

        mainContent.innerHTML = `
            <div class="vocab-detail-container">
                <button class="back-to-list" onclick="goBackToVocabulary()">
                    <i class="fas fa-arrow-left"></i> Quay lại danh sách
                </button>

                <div class="vocab-detail-header">
                    <div class="vocab-detail-title" style="flex: 1;">
                        <input type="text" id="editVocabExpression" class="form-control form-control-lg"
                               value="${escapeHtml(vocab.expression || '')}"
                               style="font-size: 28px; font-weight: 700; margin-bottom: 8px;">
                        <div class="vocab-sub-info mt-2">
                            <span class="id-badge">#${vocab.id}</span>
                            <span class="level-badge ml-2">${vocab.wordType || 'Chưa phân loại'}</span>
                        </div>
                    </div>
                </div>

                <form id="editVocabForm">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="info-card">
                                <h4><i class="fas fa-language"></i> Thông tin cơ bản</h4>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Cách đọc (Kana)</label>
                                    <input type="text" id="editVocabKana" class="form-control"
                                           value="${escapeHtml(vocab.kana || '')}" placeholder="VD: たべる">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Romaji</label>
                                    <input type="text" id="editVocabRomaji" class="form-control"
                                           value="${escapeHtml(vocab.romaji || '')}" placeholder="VD: taberu">
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Ý nghĩa (Tiếng Việt) <span class="text-danger">*</span></label>
                                    <input type="text" id="editVocabMeaning" class="form-control"
                                           value="${escapeHtml(vocab.meaning || '')}" placeholder="VD: Ăn" required>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Loại từ</label>
                                    <select id="editVocabWordType" class="form-select">
                                        <option value="N" ${vocab.wordType === 'N' ? 'selected' : ''}>Danh từ</option>
                                        <option value="V" ${vocab.wordType === 'V' ? 'selected' : ''}>Động từ</option>
                                        <option value="A" ${vocab.wordType === 'A' ? 'selected' : ''}>Tính từ</option>
                                        <option value="Adv" ${vocab.wordType === 'Adv' ? 'selected' : ''}>Trạng từ</option>
                                    </select>
                                </div>
                            </div>

                            <div class="info-card mt-3">
                                <h4><i class="fas fa-file-alt"></i> Ví dụ</h4>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Câu ví dụ (Nhật)</label>
                                    <textarea id="editVocabExample" class="form-control" rows="3">${escapeHtml(vocab.example || '')}</textarea>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label fw-600">Dịch ví dụ (Việt)</label>
                                    <textarea id="editVocabExampleVi" class="form-control" rows="3">${escapeHtml(vocab.exampleVi || '')}</textarea>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="info-card">
                                <h4><i class="fas fa-image"></i> Hình ảnh</h4>
                                <div class="text-center mb-3">
                                    ${vocab.imageUrl ?
                                        `<img src="${vocab.imageUrl}" id="detailVocabImage"
                                              style="max-width: 100%; max-height: 250px; border-radius: 12px; object-fit: contain; margin-bottom: 10px;">` :
                                        `<div class="text-muted text-center p-4 border rounded">Chưa có ảnh</div>`
                                    }
                                </div>
                                <div class="input-group">
                                    <input type="file" id="editVocabImageFile" class="form-control" accept="image/*">
                                    <input type="hidden" id="editVocabImageUrl" value="${vocab.imageUrl || ''}">
                                </div>
                                <small class="text-muted">Chọn ảnh mới để thay đổi (để trống nếu giữ nguyên)</small>
                            </div>

                            <div class="info-card mt-3">
                                <h4><i class="fas fa-music"></i> Audio phát âm</h4>
                                <div class="input-group mb-2">
                                    <input type="text" id="editVocabAudioUrl" class="form-control"
                                           value="${escapeHtml(vocab.audioUrl || '')}" placeholder="URL audio">
                                    <button type="button" class="btn btn-primary" onclick="generateEditVocabAudio()">
                                        <i class="fas fa-music"></i> Tự sinh
                                    </button>
                                    <button type="button" class="btn btn-secondary" onclick="previewEditAudio()">
                                        <i class="fas fa-play"></i> Preview
                                    </button>
                                </div>
                                <audio id="editAudioPreview" controls style="display: none; margin-top: 10px; width: 100%;">
                                    <source id="editAudioSource" src="">
                                </audio>
                            </div>

                            <div class="info-card mt-3">
                                <h4><i class="fas fa-toggle-on"></i> Trạng thái</h4>
                                <select id="editVocabIsActive" class="form-select">
                                    <option value="true" ${vocab.isActive !== false ? 'selected' : ''}>Đang hiển thị</option>
                                    <option value="false" ${vocab.isActive === false ? 'selected' : ''}>Đã ẩn</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </form>

                <div class="d-flex gap-2 mt-4 justify-content-end">
                    <button class="btn-custom btn-red" onclick="goBackToVocabulary()">
                        <i class="fas fa-times me-1"></i> Hủy
                    </button>
                    <button class="btn-custom btn-teal" onclick="saveVocabChanges(${id})">
                        <i class="fas fa-save me-1"></i> Lưu thay đổi
                    </button>
                </div>
            </div>
        `;

        document.getElementById('editVocabImageFile').addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(event) {
                    let imgElement = document.getElementById('detailVocabImage');
                    if (!imgElement) {
                        const container = document.querySelector('.info-card .text-center');
                        if (container) {
                            container.innerHTML = `<img id="detailVocabImage" src="${event.target.result}"
                                                       style="max-width: 100%; max-height: 250px; border-radius: 12px; object-fit: contain; margin-bottom: 10px;">`;
                        }
                    } else {
                        imgElement.src = event.target.result;
                    }
                };
                reader.readAsDataURL(file);
            }
        });

        localStorage.setItem('activeAdminTab', 'vocabulary');

    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể tải thông tin chi tiết', 'error');
    }
}


async function generateEditVocabAudio() {
    const romaji = document.getElementById('editVocabRomaji').value.trim();

    if (!romaji) {
        Swal.fire('Chú ý', 'Vui lòng nhập Romaji trước khi sinh audio!', 'warning');
        return;
    }

    const token = localStorage.getItem('token');

    Swal.fire({
        title: 'Đang sinh audio...',
        text: `Đang xử lý: "${romaji}"`,
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        const response = await fetch('/api/admin/vocab/generate-audio', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                romaji: romaji,
                vocabId: 0
            })
        });

        const data = await response.json();

        if (response.ok) {
            const audioUrl = data.audioUrl;
            document.getElementById('editVocabAudioUrl').value = audioUrl;

            const audioPlayer = document.getElementById('editAudioPreview');
            const audioSource = document.getElementById('editAudioSource');
            audioSource.src = audioUrl;
            audioPlayer.load();
            audioPlayer.style.display = 'block';

            Swal.fire({
                title: 'Thành công',
                text: 'Đã sinh audio thành công!',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            });
        } else {
            Swal.fire('Lỗi', data.error || 'Sinh audio thất bại', 'error');
        }
    } catch (error) {
        console.error('Audio generation error:', error);
        Swal.fire('Lỗi', 'Không thể sinh audio', 'error');
    }
}


function previewEditAudio() {
    const audioUrl = document.getElementById('editVocabAudioUrl').value;
    if (audioUrl) {
        const audioPlayer = document.getElementById('editAudioPreview');
        const audioSource = document.getElementById('editAudioSource');
        audioSource.src = audioUrl;
        audioPlayer.load();
        audioPlayer.style.display = 'block';
    } else {
        Swal.fire('Chú ý', 'Chưa có audio URL!', 'warning');
    }
}

// Lưu thay đổi từ vựng
async function saveVocabChanges(id) {
    const token = localStorage.getItem('token');

    const expression = document.getElementById('editVocabExpression').value;
    const kana = document.getElementById('editVocabKana').value;
    const romaji = document.getElementById('editVocabRomaji').value;
    const meaning = document.getElementById('editVocabMeaning').value;
    const wordType = document.getElementById('editVocabWordType').value;
    const example = document.getElementById('editVocabExample').value;
    const exampleVi = document.getElementById('editVocabExampleVi').value;
    const audioUrl = document.getElementById('editVocabAudioUrl').value;
    const isActive = document.getElementById('editVocabIsActive').value === 'true';

    if (!expression || !meaning) {
        Swal.fire('Lỗi', 'Từ vựng và ý nghĩa không được để trống!', 'error');
        return;
    }

    Swal.fire({
        title: 'Đang xử lý...',
        text: 'Vui lòng chờ trong giây lát',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        let imageUrl = document.getElementById('editVocabImageUrl').value;
        const imageFile = document.getElementById('editVocabImageFile').files[0];

        if (imageFile) {
            const formData = new FormData();
            formData.append('file', imageFile);
            formData.append('romaji', romaji);

            const imageResponse = await fetch('/api/admin/upload/image', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            const imageData = await imageResponse.json();
            if (imageResponse.ok) {
                imageUrl = imageData.imageUrl;
            } else {
                throw new Error(imageData.message || 'Upload ảnh thất bại');
            }
        }

        const data = {
            id: id,
            expression: expression,
            kana: kana,
            romaji: romaji,
            meaning: meaning,
            wordType: wordType,
            example: example,
            exampleVi: exampleVi,
            imageUrl: imageUrl,
            audioUrl: audioUrl,
            isActive: isActive
        };

        const res = await fetch(`/api/admin/vocab/${id}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            Swal.fire({
                icon: 'success',
                title: 'Thành công',
                text: 'Đã cập nhật từ vựng!',
                timer: 1500,
                showConfirmButton: false
            });

            setTimeout(() => {
                editVocab(id);
            }, 1500);
        } else {
            const error = await res.text();
            Swal.fire('Lỗi', error, 'error');
        }
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', e.message || 'Không thể cập nhật từ vựng', 'error');
    }
}

function goBackToVocabulary() {
    const navItems = document.querySelectorAll('.nav-item');
    for (let item of navItems) {
        if (item.innerText.trim() === 'Từ vựng') {
            loadSection('vocabulary', item, new Event('click'));
            break;
        }
    }
}



// ======== QUẢN LÝ TEST ========

async function loadTestCategoryFilter() {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch('/api/admin/tests/categories', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const categories = await res.json();
        const select = document.getElementById('testFilterCategory');
        if (select) {
            select.innerHTML = '<option value="">Tất cả chủ đề</option>' +
                categories.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
        }

        const createSelect = document.getElementById('testCategoryId');
        if (createSelect) {
            createSelect.innerHTML = '<option value="">-- Chọn chủ đề --</option>' +
                categories.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
        }
    } catch(e) {
        console.error(e);
    }
}


async function loadTestsWithFilters(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('testSearch')?.value || "";
    const categoryId = document.getElementById('testFilterCategory')?.value || "";
    const isActive = document.getElementById('testFilterStatus')?.value || "";
    const pageSize = paginationManager['testsPagination']?.pageSize || 15;

    try {
        let url = `/api/admin/tests/paging?page=${page}&size=${pageSize}`;
        if (search) url += `&search=${encodeURIComponent(search)}`;
        if (categoryId) url += `&categoryId=${categoryId}`;
        if (isActive !== "") url += `&isActive=${isActive}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const data = await res.json();

        const tbody = document.getElementById('testsTableBody');
        const countBadge = document.getElementById('testCountBadge');

        if (countBadge) {
            countBadge.innerText = `${data.totalElements || 0} bài test`;
        }

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="10" class="text-center py-5 text-muted">Không tìm thấy bài test nào</td></tr>`;
            if (paginationManager['testsPagination']) {
                paginationManager['testsPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = data.content.map((test, index) => {
            const rowClass = !test.isActive ? 'row-hidden' : '';
            const statusBadge = test.isActive
                ? '<span class="status-pill active-pill">Hoạt động</span>'
                : '<span class="status-pill locked-pill">Đã ẩn</span>';

            return `
            <tr class="${rowClass}">
                <td style="text-align: center;">${(page * pageSize) + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${test.id}</span></td>
                <td><strong>${escapeHtml(test.title)}</strong></td>
                <td><span style="color: var(--teal-dark);">${escapeHtml(test.categoryName || 'N/A')}</span></td>
                <td style="text-align: center;">${test.durationMinutes} phút</td>
                <td style="text-align: center;">${test.maxScore}</td>
                <td style="text-align: center;">${test.passScore}</td>
                <td style="text-align: center;">${test.questionCount || 0}</td>
                <td style="text-align: center;">${statusBadge}</td>
                <td style="text-align: center;">
                    <button class="btn-action btn-edit me-1" title="Sửa" onclick="openEditTestModal(${test.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action btn-trash" title="Xóa" onclick="deleteTest(${test.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>`;
        }).join('');

        if (paginationManager['testsPagination']) {
            paginationManager['testsPagination'].render(data.totalPages, data.number);
        }
    } catch(e) {
        console.error(e);
        document.getElementById('testsTableBody').innerHTML = `<tr><td colspan="10" class="text-center text-danger">Lỗi tải dữ liệu</td></tr>`;
    }
}

// Mở modal tạo test
function openCreateTestModal() {
    document.getElementById('createTestForm').reset();
    document.getElementById('testDuration').value = 15;
    document.getElementById('testQuestionCount').value = 10;
    document.getElementById('testMaxScore').value = 100;
    document.getElementById('testPassScore').value = 60;
    document.getElementById('testQuestionType').value = 'mixed';
    new bootstrap.Modal(document.getElementById('createTestModal')).show();
}

// Tạo test mới
async function createTest() {
    const token = localStorage.getItem('token');
    const categoryId = document.getElementById('testCategoryId').value;
    const title = document.getElementById('testTitle').value.trim();
    const durationMinutes = parseInt(document.getElementById('testDuration').value);
    const questionCount = parseInt(document.getElementById('testQuestionCount').value);
    const maxScore = parseInt(document.getElementById('testMaxScore').value);
    const passScore = parseFloat(document.getElementById('testPassScore').value);
    const questionType = document.getElementById('testQuestionType').value;

    if (!categoryId || !title) {
        Swal.fire('Lỗi', 'Vui lòng điền đầy đủ thông tin!', 'error');
        return;
    }

    Swal.fire({ title: 'Đang tạo test...', didOpen: () => Swal.showLoading() });

    try {
        const res = await fetch('/api/admin/tests/create', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                categoryId: parseInt(categoryId),
                title: title,
                durationMinutes: durationMinutes,
                questionCount: questionCount,
                maxScore: maxScore,
                passScore: passScore,
                questionType: questionType
            })
        });

        const data = await res.json();

        if (data.success) {
            bootstrap.Modal.getInstance(document.getElementById('createTestModal')).hide();
            Swal.fire('Thành công', 'Đã tạo bài test mới!', 'success');
            loadTestsWithFilters(0);
        } else {
            Swal.fire('Lỗi', data.error || 'Không thể tạo test', 'error');
        }
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể kết nối server', 'error');
    }
}

// Mở modal sửa test
async function openEditTestModal(testId) {
    const token = localStorage.getItem('token');

    try {
        const res = await fetch(`/api/admin/tests/${testId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const test = await res.json();

        document.getElementById('editTestId').value = test.id;
        document.getElementById('editTestTitle').value = test.title;
        document.getElementById('editTestDuration').value = test.durationMinutes;
        document.getElementById('editTestMaxScore').value = test.maxScore;
        document.getElementById('editTestPassScore').value = test.passScore;
        document.getElementById('editTestIsActive').value = test.isActive ? 'true' : 'false';

        displayQuestionsInModal(test.questions || []);

        new bootstrap.Modal(document.getElementById('editTestModal')).show();
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể tải thông tin test', 'error');
    }
}


function displayQuestionsInModal(questions) {
    const container = document.getElementById('editQuestionsContainer');
    if (!container) return;

    if (!questions || questions.length === 0) {
        container.innerHTML = '<div class="alert alert-info">Chưa có câu hỏi nào cho bài test này. Câu hỏi sẽ được tự động tạo khi tạo test.</div>';
        return;
    }

    container.innerHTML = `
        <style>
            .question-item {
                background: #fff;
                border: 1px solid #e9ecef;
                border-radius: 12px;
                margin-bottom: 20px;
                padding: 20px;
                transition: all 0.2s;
            }
            .question-item:hover {
                box-shadow: 0 2px 8px rgba(0,0,0,0.05);
                border-color: #06BBCC;
            }
            .question-header {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 15px;
                padding-bottom: 10px;
                border-bottom: 2px solid #f0f0f0;
            }
            .question-number {
                background: #06BBCC;
                color: white;
                width: 32px;
                height: 32px;
                border-radius: 50%;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                font-weight: bold;
                font-size: 14px;
            }
            .question-text {
                font-weight: 600;
                font-size: 16px;
                color: #2c3e50;
                flex: 1;
            }
            .options-list {
                list-style: none;
                padding-left: 0;
                margin: 15px 0;
                display: flex;
                flex-direction: column;
                gap: 10px;
            }
            .option-item {
                padding: 10px 15px;
                border-radius: 8px;
                display: flex;
                align-items: center;
                gap: 12px;
                border: 1px solid #e9ecef;
            }
            .option-item.correct {
                background: #d1fae5;
                border-color: #10b981;
            }
            .option-prefix {
                font-weight: bold;
                width: 30px;
                color: #6c757d;
            }
            .option-item.correct .option-prefix {
                color: #059669;
            }
            .option-text {
                flex: 1;
                color: #495057;
            }
            .option-item.correct .option-text {
                color: #059669;
                font-weight: 500;
            }
            @media (max-width: 768px) {
                .question-item {
                    padding: 15px;
                }
                .question-text {
                    font-size: 14px;
                }
                .option-item {
                    padding: 8px 12px;
                    font-size: 13px;
                }
            }
        </style>
        <div class="questions-list">
            ${questions.map((q, idx) => {
                let options = q.options;
                if (typeof options === 'string') {
                    try {
                        options = JSON.parse(options);
                    } catch(e) {
                        options = [];
                    }
                }
                const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F'];

                return `
                    <div class="question-item">
                        <div class="question-header">
                            <span class="question-number">${idx + 1}</span>
                            <span class="question-text">${escapeHtml(q.questionText || '')}</span>
                        </div>

                        <ul class="options-list">
                            ${Array.isArray(options) && options.length > 0 ?
                                options.map((opt, optIdx) => `
                                    <li class="option-item ${q.correctAnswer === opt ? 'correct' : ''}">
                                        <span class="option-prefix">${optionLetters[optIdx]}.</span>
                                        <span class="option-text">${escapeHtml(opt)}</span>
                                    </li>
                                `).join('')
                                : '<li class="text-muted">Không có đáp án</li>'
                            }
                        </ul>
                    </div>
                `;
            }).join('')}
        </div>
    `;
}

// Cập nhật test
async function updateTest() {
    const token = localStorage.getItem('token');
    const testId = document.getElementById('editTestId').value;
    const title = document.getElementById('editTestTitle').value.trim();
    const durationMinutes = parseInt(document.getElementById('editTestDuration').value);
    const maxScore = parseInt(document.getElementById('editTestMaxScore').value);
    const passScore = parseFloat(document.getElementById('editTestPassScore').value);
    const isActive = document.getElementById('editTestIsActive').value === 'true';

    if (!title) {
        Swal.fire('Lỗi', 'Tiêu đề không được để trống!', 'error');
        return;
    }

    Swal.fire({ title: 'Đang cập nhật...', didOpen: () => Swal.showLoading() });

    try {
        const res = await fetch(`/api/admin/tests/${testId}`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title: title,
                durationMinutes: durationMinutes,
                maxScore: maxScore,
                passScore: passScore,
                isActive: isActive
            })
        });

        const data = await res.json();

        if (data.success) {
            bootstrap.Modal.getInstance(document.getElementById('editTestModal')).hide();
            Swal.fire('Thành công', 'Đã cập nhật bài test!', 'success');
            loadTestsWithFilters(0);
        } else {
            Swal.fire('Lỗi', data.error || 'Không thể cập nhật', 'error');
        }
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể kết nối server', 'error');
    }
}



 // deleteTest
async function deleteTest(testId) {
    const token = localStorage.getItem('token');

    try {
        const res = await fetch(`/api/admin/tests/${testId}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const test = await res.json();

        const isCurrentlyActive = test.isActive;
        const action = isCurrentlyActive ? 'khóa' : 'mở khóa';
        const confirmText = isCurrentlyActive ? 'Bài test sẽ bị khóa và không hiển thị nữa!' : 'Bài test sẽ được mở khóa và hiển thị lại!';

        const result = await Swal.fire({
            title: `Xác nhận ${action}?`,
            text: confirmText,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#06BBCC',
            cancelButtonColor: '#ef4444',
            confirmButtonText: action === 'khóa' ? 'Khóa' : 'Mở khóa',
            cancelButtonText: 'Hủy'
        });

        if (result.isConfirmed) {
            Swal.fire({ title: `Đang ${action}...`, didOpen: () => Swal.showLoading() });

            try {
                const updateRes = await fetch(`/api/admin/tests/${testId}`, {
                    method: 'PUT',
                    headers: {
                        'Authorization': 'Bearer ' + token,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        title: test.title,
                        durationMinutes: test.durationMinutes,
                        maxScore: test.maxScore,
                        passScore: test.passScore,
                        isActive: !isCurrentlyActive
                    })
                });

                const data = await updateRes.json();

                if (data.success) {
                    Swal.fire('Thành công', `Đã ${action} bài test!`, 'success');
                    loadTestsWithFilters(0);
                } else {
                    Swal.fire('Lỗi', data.error || `Không thể ${action} test`, 'error');
                }
            } catch(e) {
                console.error(e);
                Swal.fire('Lỗi', 'Không thể kết nối server', 'error');
            }
        }
    } catch(e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể lấy thông tin test', 'error');
    }
}
