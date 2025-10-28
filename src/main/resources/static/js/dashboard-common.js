/**
 * Common Dashboard Functions
 * Shared across User, Premium, and Admin dashboards
 */

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
});

/**
 * Check if user is authenticated
 */
function checkAuthentication() {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');

    if (!token || !userRole) {
        console.log('No authentication found, redirecting to login...');
        window.location.href = '/auth/login';
        return false;
    }

    // Verify the current page matches user role
    verifyRoleAccess(userRole);

    return true;
}

/**
 * Verify user has access to current page based on role
 */
function verifyRoleAccess(userRole) {
    const currentPath = window.location.pathname;

    const roleRoutes = {
        'USER': '/dashboard/user',
        'PREMIUM': '/dashboard/premium',
        'ADMIN': '/dashboard/admin'
    };

    const expectedRoute = roleRoutes[userRole];

    if (expectedRoute && !currentPath.includes(expectedRoute)) {
        console.log(`Redirecting ${userRole} to correct dashboard...`);
        window.location.href = expectedRoute;
    }
}

/**
 * Load user data from API
 */
async function loadUserData() {
    try {
        const token = localStorage.getItem('authToken');
        const username = localStorage.getItem('username');

        // Set username from localStorage first
        const usernameElements = document.querySelectorAll('#username');
        usernameElements.forEach(el => {
            el.textContent = username || 'User';
        });

        // Fetch full user data from API
        const response = await fetch('/auth/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const userData = await response.json();
            console.log('User data loaded:', userData);

            // Update UI with user data
            updateUserDisplay(userData);
        } else if (response.status === 401) {
            // Token expired or invalid
            console.log('Authentication expired, logging out...');
            logout();
        }
    } catch (error) {
        console.error('Error loading user data:', error);
    }
}

/**
 * Update dashboard with user data
 */
function updateUserDisplay(userData) {
    // Update username
    const usernameElements = document.querySelectorAll('#username');
    usernameElements.forEach(el => {
        el.textContent = userData.username || 'User';
    });

    // Update stats if elements exist
    if (document.getElementById('userLevel')) {
        document.getElementById('userLevel').textContent = `Level ${userData.level || 1}`;
    }

    if (document.getElementById('userPoints')) {
        document.getElementById('userPoints').textContent = userData.totalPoints || 0;
    }

    if (document.getElementById('userHearts')) {
        document.getElementById('userHearts').textContent =
            `${userData.hearts || 5}/${userData.maxHearts || 5}`;
    }

    if (document.getElementById('currentStreak')) {
        document.getElementById('currentStreak').textContent = userData.currentStreak || 0;
    }

    if (document.getElementById('completedLessons')) {
        // This would come from user progress data
        document.getElementById('completedLessons').textContent = '0';
    }

    if (document.getElementById('totalTime')) {
        // This would come from user activity data
        document.getElementById('totalTime').textContent = '0h';
    }

    if (document.getElementById('accuracy')) {
        // This would come from user performance data
        document.getElementById('accuracy').textContent = '0%';
    }
}

/**
 * Load user lessons
 */
async function loadUserLessons() {
    try {
        const token = localStorage.getItem('authToken');

        // Fetch categories first
        const categoriesResponse = await fetch('/v1/categories', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (categoriesResponse.ok) {
            const categories = await categoriesResponse.json();
            console.log('Categories loaded:', categories);

            // Load lessons for first category as example
            if (categories.length > 0) {
                await loadLessonsByCategory(categories[0].id);
            }
        }
    } catch (error) {
        console.error('Error loading lessons:', error);
    }
}

/**
 * Load lessons by category ID
 */
async function loadLessonsByCategory(categoryId) {
    try {
        const response = await fetch(`/v1/lessons?categoryId=${categoryId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const lessons = await response.json();
            console.log('Lessons loaded:', lessons);

            // Display lessons
            displayLessons(lessons);
        }
    } catch (error) {
        console.error('Error loading lessons for category:', error);
    }
}

/**
 * Display lessons in the grid
 */
function displayLessons(lessons) {
    const lessonsGrid = document.getElementById('lessonsGrid');

    if (!lessonsGrid || lessons.length === 0) {
        return;
    }

    // Clear existing lessons
    lessonsGrid.innerHTML = '';

    // Add each lesson
    lessons.forEach(lesson => {
        const lessonCard = createLessonCard(lesson);
        lessonsGrid.appendChild(lessonCard);
    });
}

/**
 * Create lesson card element
 */
function createLessonCard(lesson) {
    const card = document.createElement('div');
    card.className = 'lesson-card';

    const difficultyIcons = {
        'BEGINNER': '🌱',
        'INTERMEDIATE': '📚',
        'ADVANCED': '🚀'
    };

    card.innerHTML = `
        <div class="lesson-icon">${difficultyIcons[lesson.difficulty] || '📡'}</div>
        <h3>${lesson.title}</h3>
        <p>${lesson.description || 'Learn Morse code'}</p>
        <div class="lesson-progress">
            <div class="progress-bar">
                <div class="progress-fill" style="width: 0%"></div>
            </div>
            <span>Not Started</span>
        </div>
        <button class="lesson-btn" onclick="startLesson(${lesson.id})">Start Learning</button>
    `;

    return card;
}

/**
 * Load premium exclusive lessons
 */
async function loadPremiumLessons() {
    console.log('Loading premium lessons...');
    // This would fetch premium-only lessons
    // For now, it's a placeholder
}

/**
 * Start a lesson
 */
function startLesson(lessonId) {
    console.log('Starting lesson:', lessonId);
    alert(`Lesson ${lessonId} will start soon! (Feature coming soon)`);
}

/**
 * Logout function
 */
function logout() {
    // Clear all stored data
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('userRole');

    console.log('Logged out successfully');

    // Redirect to login page
    window.location.href = '/auth/login';
}

/**
 * Make authenticated API call
 */
async function apiCall(endpoint, options = {}) {
    const token = localStorage.getItem('authToken');

    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        }
    };

    const mergedOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers
        }
    };

    try {
        const response = await fetch(endpoint, mergedOptions);

        if (response.status === 401) {
            // Unauthorized - token expired
            logout();
            return null;
        }

        return response;
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
}
