// ==================== USER MANAGEMENT ====================

async function loadAllUsersWithFilter(page = 0) {
    const token = localStorage.getItem('token');
    const search = document.getElementById('userSearch')?.value || "";
    const levelId = document.getElementById('userFilterLevel')?.value || "";
    const active = document.getElementById('userFilterStatus')?.value || "";
    const timeFilter = document.getElementById('userFilterTime')?.value || "";
    const pageSize = 5;

    try {
        let url = `/api/admin/users/all-paging?page=${page}&size=${pageSize}&search=${encodeURIComponent(search)}&levelId=${levelId}&active=${active}&timeFilter=${timeFilter}`;

        const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
        const data = await res.json();

        const tbody = document.getElementById('fullUserTable');
        document.getElementById('userCountBadge').innerText = `${data.totalElements} người dùng`;

        if (!data.content || data.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-5 text-muted">Không tìm thấy học viên phù hợp</td></tr>`;
            if (paginationManager['userPagination']) {
                paginationManager['userPagination'].reset();
            }
            return;
        }

        tbody.innerHTML = data.content.map((user, index) => {
            const isLocked = user.active === false;
            return `
            <tr class="${isLocked ? 'row-hidden' : ''}">
                <td style="text-align: center;">${(page * pageSize) + index + 1}</td>
                <td style="text-align: center;"><span class="id-badge">#${user.id}</span></td>
                <td>
                    <div class="user-cell">
                        <img src="${getAvatarPath(user.avatarUrl, user.fullname)}">
                        <div>
                            <div class="name">${user.fullname}</div>
                            <div class="small text-muted">@${user.userName}</div>
                        </div>
                    </div>
                </td>
                <td>${user.email}</td>
                <td style="text-align: center;"><span class="level-badge">${getLevelName(user.levelId)}</span></td>
                <td style="white-space: nowrap; text-align: center;">
                    <span class="status-pill ${user.active ? 'active-pill' : 'locked-pill'}">
                        ${user.active ? 'Hoạt động' : 'Bị khóa'}
                    </span>
                </td>
                <td style="text-align: center; white-space: nowrap;">
                    <button class="btn-action btn-view me-1" title="Xem chi tiết" onclick="viewUser(${user.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    ${isLocked
                        ? `<button class="btn-action btn-restore" onclick="deleteUser(${user.id}, false)"><i class="fas fa-user-check"></i></button>`
                        : `<button class="btn-action btn-trash" onclick="deleteUser(${user.id}, true)"><i class="fas fa-user-slash"></i></button>`
                    }
                </td>
            </tr>`;
        }).join('');

        if (paginationManager['userPagination']) {
            paginationManager['userPagination'].render(data.totalPages, data.number);
        }
    } catch (e) {
        console.error("Lỗi tải người dùng:", e);
    }
}

async function viewUser(id) {
    const token = localStorage.getItem('token');
    try {
        const res = await fetch(`/api/admin/users/${id}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        const user = await res.json();

        document.getElementById('viewUserAvatar').src = getAvatarPath(user.avatarUrl, user.fullname);
        document.getElementById('viewFullnameTitle').innerText = user.fullname;
        document.getElementById('viewId').value = `#${user.id}`;
        document.getElementById('viewUsername').value = user.userName;
        document.getElementById('viewEmail').value = user.email || 'Chưa cập nhật';
        document.getElementById('viewLevel').value = getLevelName(user.levelId);
        document.getElementById('viewPoints').value = `${user.totalPoints || 0} điểm`;

        const date = new Date(user.created);
        document.getElementById('viewCreated').value = date.toLocaleDateString('vi-VN');

        const badgeContainer = document.getElementById('viewUserStatusBadge');
        if (user.active) {
            badgeContainer.innerHTML = '<span class="badge bg-success"><i class="fas fa-check-circle me-1"></i>Đang hoạt động</span>';
        } else {
            badgeContainer.innerHTML = '<span class="badge bg-danger"><i class="fas fa-lock me-1"></i>Đã bị khóa</span>';
        }

        new bootstrap.Modal(document.getElementById('userViewModal')).show();
    } catch (e) {
        console.error(e);
        Swal.fire('Lỗi', 'Không thể lấy thông tin chi tiết học viên', 'error');
    }
}

async function deleteUser(id, isLocking) {
    const actionText = isLocking ? "khóa" : "mở khóa";

    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: isLocking ? "Học viên này sẽ không thể đăng nhập!" : "Học viên sẽ có thể truy cập lại hệ thống.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#06BBCC',
        cancelButtonColor: '#ef4444',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        const token = localStorage.getItem('token');
        try {
            const res = await fetch(`/api/admin/users/${id}`, {
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
                const currentPage = paginationManager['userPagination']?.currentPage || 0;
                loadAllUsersWithFilter(currentPage);
            }
        } catch (e) {
            console.error(e);
            Swal.fire('Lỗi', 'Không thể thay đổi trạng thái người dùng', 'error');
        }
    }
}