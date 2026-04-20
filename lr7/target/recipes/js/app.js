'use strict';

const API = 'recipes';

let allRecipes = [];
let editingId  = null;

const grid        = document.getElementById('recipeGrid');
const searchInput = document.getElementById('searchInput');
const formModal   = document.getElementById('formModal');
const viewModal   = document.getElementById('viewModal');
const recipeForm  = document.getElementById('recipeForm');
const modalTitle  = document.getElementById('modalTitle');
const toast       = document.getElementById('toast');

document.addEventListener('DOMContentLoaded', loadRecipes);

function loadRecipes() {
    grid.innerHTML = '<div class="loading">Загрузка...</div>';
    fetch(API)
        .then(r => r.json())
        .then(data => {
            allRecipes = data;
            renderGrid(data);
        })
        .catch(() => showToast('Ошибка загрузки рецептов', 'error'));
}

let searchTimer = null;
searchInput.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
        const q = searchInput.value.trim();
        if (q === '') {
            renderGrid(allRecipes);
            return;
        }
        fetch(`${API}?search=${encodeURIComponent(q)}`)
            .then(r => r.json())
            .then(data => renderGrid(data))
            .catch(() => showToast('Ошибка поиска', 'error'));
    }, 300);
});

function renderGrid(recipes) {
    if (!recipes.length) {
        grid.innerHTML = '<div class="empty-state"><div class="icon">🍽️</div><p>Рецепты не найдены</p></div>';
        return;
    }
    grid.innerHTML = recipes.map(r => {
        const imgHtml = r.imagePath
            ? `<div class="card-img"><img src="${r.imagePath}" alt="${escHtml(r.name)}"></div>`
            : `<div class="card-img card-img-empty">🍽️</div>`;
        return `
        <div class="recipe-card" onclick="openView(${r.id})">
            ${imgHtml}
            <div class="card-body">
                <div class="recipe-card-header">
                    <h3>${escHtml(r.name)}</h3>
                    ${diffBadge(r.difficulty)}
                </div>
                <div class="recipe-meta">
                    <span>⏱ ${r.cookTime} мин</span>
                </div>
                <div class="recipe-ingredients">${escHtml(r.ingredients)}</div>
                <div class="recipe-actions" onclick="event.stopPropagation()">
                    <button class="btn-icon" title="Редактировать" onclick="openEdit(${r.id})">✏️</button>
                    <button class="btn-icon" title="Удалить" onclick="deleteRecipe(${r.id})">🗑️</button>
                </div>
            </div>
        </div>`;
    }).join('');
}

function diffBadge(d) {
    const map = { 'простой': 'badge-easy', 'средний': 'badge-medium', 'комплексный': 'badge-hard' };
    return `<span class="badge ${map[d] || ''}">${escHtml(d)}</span>`;
}

function openView(id) {
    fetch(`${API}?id=${id}`)
        .then(r => r.json())
        .then(r => {
            document.getElementById('viewTitle').textContent        = r.name;
            document.getElementById('viewBadge').innerHTML          = diffBadge(r.difficulty);
            document.getElementById('viewCookTime').textContent     = r.cookTime + ' минут';
            document.getElementById('viewIngredients').textContent  = r.ingredients;
            document.getElementById('viewSteps').textContent        = r.steps;

            const viewImg = document.getElementById('viewImg');
            if (r.imagePath) {
                viewImg.src = r.imagePath;
                viewImg.style.display = 'block';
            } else {
                viewImg.style.display = 'none';
            }

            viewModal.classList.add('open');
        })
        .catch(() => showToast('Ошибка загрузки рецепта', 'error'));
}

function openAdd() {
    editingId = null;
    modalTitle.textContent = 'Добавить рецепт';
    recipeForm.reset();
    document.getElementById('imagePreview').style.display = 'none';
    document.getElementById('existingImage').value = '';
    formModal.classList.add('open');
}

function openEdit(id) {
    fetch(`${API}?id=${id}`)
        .then(r => r.json())
        .then(r => {
            editingId = r.id;
            modalTitle.textContent = 'Редактировать рецепт';
            document.getElementById('fName').value        = r.name;
            document.getElementById('fIngredients').value = r.ingredients;
            document.getElementById('fDifficulty').value  = r.difficulty;
            document.getElementById('fSteps').value       = r.steps;
            document.getElementById('fCookTime').value    = r.cookTime;
            document.getElementById('existingImage').value = r.imagePath || '';

            const preview = document.getElementById('imagePreview');
            if (r.imagePath) {
                preview.src = r.imagePath;
                preview.style.display = 'block';
            } else {
                preview.style.display = 'none';
            }

            document.getElementById('fImage').value = '';
            formModal.classList.add('open');
        })
        .catch(() => showToast('Ошибка загрузки рецепта', 'error'));
}

// предпросмотр выбранного фото
document.getElementById('fImage').addEventListener('change', function() {
    const file = this.files[0];
    const preview = document.getElementById('imagePreview');
    if (file) {
        preview.src = URL.createObjectURL(file);
        preview.style.display = 'block';
    } else {
        preview.style.display = 'none';
    }
});

recipeForm.addEventListener('submit', function(e) {
    e.preventDefault();

    // используем FormData чтобы отправить файл
    const formData = new FormData();
    formData.append('name',        document.getElementById('fName').value.trim());
    formData.append('ingredients', document.getElementById('fIngredients').value.trim());
    formData.append('difficulty',  document.getElementById('fDifficulty').value);
    formData.append('steps',       document.getElementById('fSteps').value.trim());
    formData.append('cookTime',    document.getElementById('fCookTime').value);

    const imageFile = document.getElementById('fImage').files[0];
    if (imageFile) {
        formData.append('image', imageFile);
    }

    let method = 'POST';
    if (editingId !== null) {
        method = 'PUT';
        formData.append('id', editingId);
        formData.append('existingImage', document.getElementById('existingImage').value);
    }

    fetch(API, { method, body: formData })
        .then(r => r.json())
        .then(data => {
            if (data.error) { showToast(data.error, 'error'); return; }
            closeModal('formModal');
            showToast(editingId ? 'Рецепт обновлён' : 'Рецепт добавлен', 'success');
            loadRecipes();
        })
        .catch(() => showToast('Ошибка сохранения', 'error'));
});

function deleteRecipe(id) {
    if (!confirm('Удалить этот рецепт?')) return;
    fetch(`${API}?id=${id}`, { method: 'DELETE' })
        .then(r => r.json())
        .then(data => {
            if (data.error) { showToast(data.error, 'error'); return; }
            showToast('Рецепт удалён', 'success');
            loadRecipes();
        })
        .catch(() => showToast('Ошибка удаления', 'error'));
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
        if (e.target === overlay) overlay.classList.remove('open');
    });
});

let toastTimer = null;
function showToast(msg, type = 'success') {
    toast.textContent = msg;
    toast.className   = `show ${type}`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.className = '', 3000);
}

function escHtml(s) {
    const d = document.createElement('div');
    d.appendChild(document.createTextNode(String(s)));
    return d.innerHTML;
}
