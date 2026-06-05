// Global state management
let students = [];
let deleteTargetId = null;
let selectedMarksheetStudentId = null;

// ==========================================================================
// SESSION CHECK & THEME INSTANTIATION
// ==========================================================================
document.addEventListener('DOMContentLoaded', async () => {
    // 1. Session Validation: Redirect immediately if unauthenticated
    await checkAuth();

    // 2. Initialize Dark/Light Mode Theme
    initTheme();

    // 3. Setup Layout Toggle Listeners
    setupSidebarToggle();

    // 4. Fetch initial dashboard and student data
    await refreshData();

    // 5. Register Form, Search and Filter Listeners
    setupEventListeners();
});

async function checkAuth() {
    try {
        const response = await fetch('login');
        const data = await response.json();
        if (!data.loggedIn) {
            window.location.href = 'login.html';
        }
    } catch (err) {
        console.error("Auth check failed, redirecting:", err);
        window.location.href = 'login.html';
    }
}

// ==========================================================================
// THEME & SIDEBAR LAYOUT MANAGER
// ==========================================================================
function initTheme() {
    const savedTheme = localStorage.getItem('sgs_theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeToggleIcons(savedTheme);

    const themeToggleBtn = document.getElementById('themeToggleBtn');
    themeToggleBtn.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('sgs_theme', newTheme);
        updateThemeToggleIcons(newTheme);
        showToast(`Theme switched to ${newTheme} mode`, 'success');
    });
}

function updateThemeToggleIcons(theme) {
    const sunIcon = document.getElementById('sunIcon');
    const moonIcon = document.getElementById('moonIcon');
    if (theme === 'dark') {
        sunIcon.style.display = 'block';
        moonIcon.style.display = 'none';
    } else {
        sunIcon.style.display = 'none';
        moonIcon.style.display = 'block';
    }
}

function setupSidebarToggle() {
    const sidebar = document.getElementById('sidebar');
    const menuToggleBtn = document.getElementById('menuToggleBtn');
    const mobileCloseBtn = document.getElementById('mobileCloseBtn');

    menuToggleBtn.addEventListener('click', () => {
        sidebar.classList.add('active');
    });

    mobileCloseBtn.addEventListener('click', () => {
        sidebar.classList.remove('active');
    });

    // Close sidebar on window resize if larger than breakpoint
    window.addEventListener('resize', () => {
        if (window.innerWidth > 768) {
            sidebar.classList.remove('active');
        }
    });
}

// ==========================================================================
// API CLIENT ACTIONS (GET, POST, DELETE)
// ==========================================================================
async function refreshData() {
    await Promise.all([
        fetchStudents(),
        fetchDashboardMetrics()
    ]);
}

async function fetchStudents() {
    try {
        const response = await fetch('students');
        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        students = await response.json();
        renderStudentTable(students);
        renderLeaderboard(students);
        populateMarksheetSelector(students);
        ensureDefaultStudentTools();
    } catch (err) {
        showToast("Error retrieving student listings.", "error");
        console.error("fetchStudents error:", err);
    }
}

async function fetchDashboardMetrics() {
    try {
        const response = await fetch('dashboard');
        if (response.status === 401) {
            window.location.href = 'login.html';
            return;
        }
        const data = await response.json();
        updateDashboardUI(data);
    } catch (err) {
        showToast("Error fetching dashboard statistics.", "error");
        console.error("fetchDashboard error:", err);
    }
}

function updateDashboardUI(metrics) {
    document.getElementById('totalStudentsCount').textContent = metrics.totalStudents;
    document.getElementById('classAverageValue').textContent = metrics.classAverage + '%';
    document.getElementById('topGradeValue').textContent = metrics.topGrade;
    document.getElementById('heroFocusText').textContent = getClassFocusText(metrics);

    document.getElementById('countA').textContent = metrics.gradeA;
    document.getElementById('countB').textContent = metrics.gradeB;
    document.getElementById('countC').textContent = metrics.gradeC;
    document.getElementById('countD').textContent = metrics.gradeD;
    document.getElementById('countF').textContent = metrics.gradeF;
}

function getClassFocusText(metrics) {
    if (metrics.totalStudents === 0) return 'Add first record';
    if (metrics.gradeF > 0) return `${metrics.gradeF} urgent recovery plan${metrics.gradeF > 1 ? 's' : ''}`;
    if (metrics.gradeD > 0) return `${metrics.gradeD} student${metrics.gradeD > 1 ? 's' : ''} near pass lift`;
    if (metrics.classAverage < 75) return 'Build B-grade consistency';
    return 'Push high scorers to mastery';
}

// ==========================================================================
// DYNAMIC DOM DATA TABLE RENDERING
// ==========================================================================
function renderStudentTable(studentsList) {
    const tbody = document.getElementById('studentTableBody');
    tbody.innerHTML = '';

    if (studentsList.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="10" class="text-center" style="color: var(--text-secondary); padding: 40px;">
                    No records found matching search or filter criteria.
                </td>
            </tr>
        `;
        return;
    }

    studentsList.forEach(student => {
        const tr = document.createElement('tr');
        
        // Map grade class for custom colorful badges
        const gradeClass = `badge-${student.grade.toLowerCase()}`;
        
        tr.innerHTML = `
            <td><strong>#${student.id}</strong></td>
            <td>${escapeHtml(student.name)}</td>
            <td>${student.subject1}</td>
            <td>${student.subject2}</td>
            <td>${student.subject3}</td>
            <td>${student.subject4}</td>
            <td><strong>${student.average.toFixed(2)}</strong></td>
            <td><span class="dist-badge ${gradeClass}">${student.grade}</span></td>
            <td><span class="performance-msg">${escapeHtml(student.performanceMessage)}</span></td>
            <td>
                <div class="row-actions">
                <button class="btn btn-secondary btn-icon btn-roadmap" onclick="renderRoadmap(${student.id})" title="Build Improvement Roadmap">
                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 17l6-6 4 4 7-8"></path>
                        <path d="M14 7h6v6"></path>
                    </svg>
                </button>
                <button class="btn btn-secondary btn-icon btn-marksheet" onclick="generateMarksheet(${student.id})" title="Generate Marksheet">
                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="8" y1="13" x2="16" y2="13"></line>
                        <line x1="8" y1="17" x2="16" y2="17"></line>
                    </svg>
                </button>
                <button class="btn btn-secondary btn-icon" onclick="openDeleteModal(${student.id}, '${escapeQuote(student.name)}')" title="Delete Student Record">
                    <svg viewBox="0 0 24 24" width="16" height="16" stroke="var(--danger)" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                        <line x1="10" y1="11" x2="10" y2="17"></line>
                        <line x1="14" y1="11" x2="14" y2="17"></line>
                    </svg>
                </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// ==========================================================================
// LEADERBOARD AND MARKSHEET GENERATOR
// ==========================================================================
function getRankedStudents(list = students) {
    return [...list].sort((a, b) => {
        if (b.average !== a.average) return b.average - a.average;
        return a.name.localeCompare(b.name);
    });
}

function renderLeaderboard(list) {
    const leaderboardList = document.getElementById('leaderboardList');
    const rankedStudents = getRankedStudents(list);

    if (rankedStudents.length === 0) {
        leaderboardList.innerHTML = '<div class="empty-tool-state">No students available for ranking.</div>';
        return;
    }

    leaderboardList.innerHTML = rankedStudents.map((student, index) => {
        const rank = index + 1;
        const subjectInsight = getSubjectInsights(student);
        const rankClass = rank <= 3 ? `rank-${rank}` : '';

        return `
            <button class="leaderboard-row ${rankClass}" onclick="selectStudentTools(${student.id})">
                <span class="rank-badge">${rank}</span>
                <span class="leaderboard-main">
                    <strong>${escapeHtml(student.name)}</strong>
                    <small>${escapeHtml(subjectInsight.best.name)} leads at ${subjectInsight.best.score}/100</small>
                </span>
                <span class="leaderboard-score">
                    <strong>${student.average.toFixed(2)}%</strong>
                    <small>Grade ${student.grade}</small>
                </span>
            </button>
        `;
    }).join('');
}

function populateMarksheetSelector(list) {
    const select = document.getElementById('marksheetStudentSelect');
    const previousValue = selectedMarksheetStudentId ? String(selectedMarksheetStudentId) : select.value;

    select.innerHTML = '<option value="">Select Student</option>';
    getRankedStudents(list).forEach(student => {
        const option = document.createElement('option');
        option.value = student.id;
        option.textContent = `${student.name} - ${student.average.toFixed(2)}%`;
        select.appendChild(option);
    });

    if ([...select.options].some(option => option.value === previousValue)) {
        select.value = previousValue;
    }
}

function ensureDefaultStudentTools() {
    if (students.length === 0) {
        selectedMarksheetStudentId = null;
        document.getElementById('marksheetPreview').innerHTML = '<div class="empty-tool-state">Add a student to generate a marksheet.</div>';
        return;
    }

    const stillExists = students.some(student => student.id === selectedMarksheetStudentId);
    const defaultStudent = stillExists
        ? students.find(student => student.id === selectedMarksheetStudentId)
        : getRankedStudents(students)[0];

    if (defaultStudent) {
        renderRoadmap(defaultStudent.id);
        generateMarksheet(defaultStudent.id, false);
    }
}

function selectStudentTools(studentId) {
    renderRoadmap(studentId);
    generateMarksheet(studentId);
}

function generateMarksheet(studentId, shouldNotify = true) {
    const student = students.find(item => item.id === Number(studentId));
    if (!student) return;

    selectedMarksheetStudentId = student.id;
    const select = document.getElementById('marksheetStudentSelect');
    select.value = String(student.id);

    const rank = getRankedStudents(students).findIndex(item => item.id === student.id) + 1;
    const subjectInsight = getSubjectInsights(student);
    const nextTarget = getNextTarget(student.average);
    const roadmapSteps = buildRoadmapSteps(student, subjectInsight.weakest, subjectInsight.secondWeakest, nextTarget, Math.max(3, Math.ceil(Math.max(0, nextTarget.score - student.average) / 3)));

    document.getElementById('marksheetPreview').innerHTML = `
        <article class="printable-marksheet" id="printableMarksheet">
            <header class="marksheet-title">
                <div>
                    <span>Student Grading System</span>
                    <h2>Academic Marksheet</h2>
                </div>
                <div class="marksheet-grade">${student.grade}</div>
            </header>
            <section class="marksheet-meta">
                <div><span>Name</span><strong>${escapeHtml(student.name)}</strong></div>
                <div><span>Student ID</span><strong>#${student.id}</strong></div>
                <div><span>Class Rank</span><strong>${rank} of ${students.length}</strong></div>
                <div><span>Average</span><strong>${student.average.toFixed(2)}%</strong></div>
            </section>
            <table class="marksheet-table">
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Marks</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    ${getSubjectRows(student)}
                </tbody>
            </table>
            <section class="marksheet-advice">
                <div>
                    <span>Strongest subject</span>
                    <strong>${subjectInsight.best.name}: ${subjectInsight.best.score}/100</strong>
                </div>
                <div>
                    <span>Improve first</span>
                    <strong>${subjectInsight.weakest.name}: ${subjectInsight.weakest.score}/100</strong>
                </div>
                <div>
                    <span>Next target</span>
                    <strong>${nextTarget.label}</strong>
                </div>
            </section>
            <section class="marksheet-roadmap">
                <h3>Roadmap to Improve Marks</h3>
                <ol>
                    ${roadmapSteps.map(step => `<li>${escapeHtml(step)}</li>`).join('')}
                </ol>
            </section>
            <footer class="marksheet-footer">
                <span>Generated by SGS Portal</span>
                <span>Authorized Signature</span>
            </footer>
        </article>
    `;

    if (shouldNotify) showToast(`Generated marksheet for ${student.name}.`, 'success');
}

function getSubjectRows(student) {
    return [
        { name: 'Subject 1', score: student.subject1 },
        { name: 'Subject 2', score: student.subject2 },
        { name: 'Subject 3', score: student.subject3 },
        { name: 'Subject 4', score: student.subject4 }
    ].map(subject => {
        const status = subject.score >= 75 ? 'Strong' : subject.score >= 50 ? 'Practice' : 'Urgent';
        return `
            <tr>
                <td>${subject.name}</td>
                <td>${subject.score}/100</td>
                <td>${status}</td>
            </tr>
        `;
    }).join('');
}

function getSubjectInsights(student) {
    const subjectScores = [
        { name: 'Subject 1', score: student.subject1 },
        { name: 'Subject 2', score: student.subject2 },
        { name: 'Subject 3', score: student.subject3 },
        { name: 'Subject 4', score: student.subject4 }
    ].sort((a, b) => a.score - b.score);

    return {
        weakest: subjectScores[0],
        secondWeakest: subjectScores[1],
        best: subjectScores[subjectScores.length - 1]
    };
}

function printMarksheet() {
    if (!selectedMarksheetStudentId) {
        showToast('Please select a student before printing.', 'error');
        return;
    }

    document.body.classList.add('printing-marksheet');
    window.print();
    setTimeout(() => document.body.classList.remove('printing-marksheet'), 300);
}

// ==========================================================================
// PERSONAL ROADMAP GENERATOR
// ==========================================================================
function renderRoadmap(studentId) {
    const student = students.find(item => item.id === studentId);
    if (!student) return;

    const subjectInsight = getSubjectInsights(student);
    const weakest = subjectInsight.weakest;
    const secondWeakest = subjectInsight.secondWeakest;
    const nextTarget = getNextTarget(student.average);
    const gap = Math.max(0, Math.ceil(nextTarget.score - student.average));
    const weeklyLift = Math.max(3, Math.ceil(gap / 3));
    const plan = buildRoadmapSteps(student, weakest, secondWeakest, nextTarget, weeklyLift);

    document.getElementById('roadmapStudentName').textContent = student.name;
    document.getElementById('roadmapSummary').textContent = `${student.name} is at ${student.average.toFixed(2)}%. Target ${nextTarget.label} by lifting the average ${gap || 2}+ marks with focused practice.`;
    document.getElementById('roadmapGrade').textContent = student.grade;
    document.getElementById('roadmapAverage').textContent = `${student.average.toFixed(2)}%`;
    document.getElementById('roadmapWeakSubject').textContent = `${weakest.name}: ${weakest.score}/100`;
    document.getElementById('roadmapWeakSubjectText').textContent = `First goal: move ${weakest.name} to at least ${Math.min(100, weakest.score + weeklyLift)} within 7 days, then repeat.`;
    document.getElementById('heroTargetText').textContent = `${student.name}: ${nextTarget.label}`;

    document.getElementById('roadmapSteps').innerHTML = plan.map((step, index) => `
        <div class="roadmap-step">
            <span>${index + 1}</span>
            <p>${escapeHtml(step)}</p>
        </div>
    `).join('');

    document.querySelectorAll('#studentTableBody tr').forEach(row => row.classList.remove('selected-row'));
    const clickedRow = [...document.querySelectorAll('#studentTableBody tr')]
        .find(row => row.firstElementChild && row.firstElementChild.textContent.includes(String(student.id)));
    if (clickedRow) clickedRow.classList.add('selected-row');
}

function getNextTarget(average) {
    if (average < 50) return { label: 'Pass recovery: 50%', score: 50 };
    if (average < 60) return { label: 'Grade C: 60%', score: 60 };
    if (average < 75) return { label: 'Grade B: 75%', score: 75 };
    if (average < 90) return { label: 'Grade A: 90%', score: 90 };
    return { label: 'Mastery: 95%', score: 95 };
}

function buildRoadmapSteps(student, weakest, secondWeakest, target, weeklyLift) {
    const intensity = student.average < 50 ? 'daily recovery' : student.average < 75 ? 'steady improvement' : 'mastery polish';

    return [
        `Week 1: ${intensity} block for ${weakest.name}. Spend 25 minutes daily on missed concepts, then solve 10 targeted questions.`,
        `Week 2: raise ${secondWeakest.name} with mixed practice. Make a mistake log with topic, reason, and corrected answer.`,
        `Every 3 days: take a 20-minute mini test covering ${weakest.name} and ${secondWeakest.name}; aim for a ${weeklyLift}-mark lift.`,
        `Before the next exam: revise one-page notes, redo wrong questions, and attempt one full timed paper to reach ${target.label}.`
    ];
}

// ==========================================================================
// SEARCH & FILTER FUNCTIONALITY (LIVE CLIENT-SIDE)
// ==========================================================================
function filterAndSearch() {
    const searchVal = document.getElementById('searchBar').value.toLowerCase().trim();
    const gradeVal = document.getElementById('gradeFilter').value;

    const filtered = students.filter(student => {
        const matchesSearch = student.name.toLowerCase().includes(searchVal) || 
                              String(student.id).includes(searchVal);
        const matchesGrade = gradeVal === '' || student.grade === gradeVal;
        return matchesSearch && matchesGrade;
    });

    renderStudentTable(filtered);
    renderLeaderboard(filtered);
}

// ==========================================================================
// ADD STUDENT FORM HANDLING (WITH CLIENT-SIDE VALIDATION)
// ==========================================================================
async function handleAddStudent(e) {
    e.preventDefault();

    // Reset error styling
    clearErrors();

    const nameInput = document.getElementById('studentName');
    const s1Input = document.getElementById('subject1');
    const s2Input = document.getElementById('subject2');
    const s3Input = document.getElementById('subject3');
    const s4Input = document.getElementById('subject4');

    const name = nameInput.value.trim();
    const s1Val = s1Input.value.trim();
    const s2Val = s2Input.value.trim();
    const s3Val = s3Input.value.trim();
    const s4Val = s4Input.value.trim();

    let hasError = false;

    // Validation Logic
    if (!name) {
        showFieldError('nameError', 'Student Name cannot be empty.');
        hasError = true;
    }

    const validateMark = (val, errorId) => {
        if (val === '') {
            showFieldError(errorId, 'Marks required.');
            return null;
        }
        const num = Number(val);
        if (isNaN(num)) {
            showFieldError(errorId, 'Must be numeric.');
            return null;
        }
        if (num < 0 || num > 100) {
            showFieldError(errorId, 'Range: 0-100.');
            return null;
        }
        return num;
    };

    const s1 = validateMark(s1Val, 's1Error');
    const s2 = validateMark(s2Val, 's2Error');
    const s3 = validateMark(s3Val, 's3Error');
    const s4 = validateMark(s4Val, 's4Error');

    if (s1 === null || s2 === null || s3 === null || s4 === null) {
        hasError = true;
    }

    if (hasError) return;

    // Lock Submit button
    const submitBtn = document.getElementById('submitStudentBtn');
    submitBtn.disabled = true;

    try {
        const response = await fetch('student', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: name,
                subject1: s1,
                subject2: s2,
                subject3: s3,
                subject4: s4
            })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast("Student grading record successfully computed and saved!", "success");
            document.getElementById('addStudentForm').reset();
            await refreshData();
        } else {
            showToast(result.message || "Failed to save student.", "error");
        }
    } catch (err) {
        showToast("Server error. Please check your connection.", "error");
        console.error("AddStudent error:", err);
    } finally {
        submitBtn.disabled = false;
    }
}

function showFieldError(elementId, msg) {
    const errorEl = document.getElementById(elementId);
    errorEl.textContent = msg;
    const input = errorEl.previousElementSibling;
    if (input) input.style.borderColor = 'var(--danger)';
}

function clearErrors() {
    const errors = document.querySelectorAll('.error-msg');
    errors.forEach(err => err.textContent = '');
    const inputs = document.querySelectorAll('.dashboard-form input');
    inputs.forEach(input => input.style.borderColor = 'var(--border-color)');
}

// ==========================================================================
// DELETE STUDENT CONFIRMATION MODAL & ACTIONS
// ==========================================================================
function openDeleteModal(id, name) {
    deleteTargetId = id;
    document.getElementById('deleteStudentName').textContent = name;
    document.getElementById('deleteModal').classList.add('active');
}

function closeDeleteModal() {
    deleteTargetId = null;
    document.getElementById('deleteModal').classList.remove('active');
}

async function confirmDeleteStudent() {
    if (!deleteTargetId) return;

    const confirmBtn = document.getElementById('confirmDeleteBtn');
    confirmBtn.disabled = true;

    try {
        const response = await fetch(`student?id=${deleteTargetId}`, {
            method: 'DELETE'
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            showToast("Student record successfully deleted from memory.", "success");
            closeDeleteModal();
            await refreshData();
        } else {
            showToast(result.message || "Failed to delete student.", "error");
        }
    } catch (err) {
        showToast("Error processing request.", "error");
        console.error("DeleteStudent error:", err);
    } finally {
        confirmBtn.disabled = false;
    }
}

// ==========================================================================
// AUTHENTICATION LOGOUT
// ==========================================================================
async function handleLogout() {
    try {
        const response = await fetch('login?action=logout');
        const data = await response.json();
        if (data.status === 'success') {
            showToast("Logged out successfully.", "success");
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 500);
        }
    } catch (err) {
        console.error("Logout failed:", err);
        window.location.href = 'login.html'; // Fail-safe fallback
    }
}

// ==========================================================================
// TOAST NOTIFICATIONS BANNER
// ==========================================================================
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    const icon = type === 'success' ? 
        `<svg class="toast-icon-success" viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
         </svg>` : 
        `<svg class="toast-icon-error" viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="12" y1="8" x2="12" y2="12"></line>
            <line x1="12" y1="16" x2="12.01" y2="16"></line>
         </svg>`;

    toast.innerHTML = `
        ${icon}
        <div class="toast-content">${message}</div>
    `;

    container.appendChild(toast);

    // Auto dismiss after 4 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => {
            if (toast.parentNode === container) {
                container.removeChild(toast);
            }
        }, 300);
    }, 4000);
}

// ==========================================================================
// SANITIZATION AND EVENT BINDINGS HELPERS
// ==========================================================================
function setupEventListeners() {
    // Search Bar input
    document.getElementById('searchBar').addEventListener('input', filterAndSearch);

    // Grade dropdown filter selector
    document.getElementById('gradeFilter').addEventListener('change', filterAndSearch);

    // Add Student Form Submission
    document.getElementById('addStudentForm').addEventListener('submit', handleAddStudent);

    document.getElementById('marksheetStudentSelect').addEventListener('change', (e) => {
        if (e.target.value) {
            selectStudentTools(Number(e.target.value));
        }
    });

    document.getElementById('printMarksheetBtn').addEventListener('click', printMarksheet);

    // Cancel modal deletion
    document.getElementById('cancelDeleteBtn').addEventListener('click', closeDeleteModal);

    // Confirm modal deletion
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDeleteStudent);

    // Close modal when clicking on overlay background
    document.getElementById('deleteModal').addEventListener('click', (e) => {
        if (e.target.id === 'deleteModal') {
            closeDeleteModal();
        }
    });

    // Logout trigger button
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;')
              .replace(/'/g, '&#039;');
}

function escapeQuote(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'");
}
