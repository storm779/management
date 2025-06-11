/**
 * Visakh Refinery Portal - JavaScript Functions
 * Enhanced functionality for better user experience
 */

// Wait for DOM to be ready
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

/**
 * Initialize the application
 */
function initializeApp() {
    // Initialize tooltips
    initializeTooltips();
    
    // Initialize auto-hide alerts
    initializeAlerts();
    
    // Initialize form enhancements
    initializeFormEnhancements();
    
    // Initialize table enhancements
    initializeTableEnhancements();
    
    // Initialize smooth scrolling
    initializeSmoothScrolling();
    
    console.log('Visakh Refinery Portal initialized successfully');
}

/**
 * Initialize Bootstrap tooltips
 */
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Auto-hide alerts after 5 seconds
 */
function initializeAlerts() {
    const alerts = document.querySelectorAll('.alert.alert-dismissible');
    alerts.forEach(alert => {
        // Auto-hide after 5 seconds
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
}

/**
 * Form enhancements
 */
function initializeFormEnhancements() {
    // Real-time validation
    const forms = document.querySelectorAll('form[novalidate]');
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                
                // Focus on first invalid field
                const firstInvalid = form.querySelector(':invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
            }
            form.classList.add('was-validated');
        });
    });
    
    // Character count and auto-resize handled by form-enhancements.js
}

/**
 * Character counter and auto-resize functions moved to form-enhancements.js
 */

/**
 * Table enhancements
 */
function initializeTableEnhancements() {
    // Highlight table rows on hover
    const tables = document.querySelectorAll('.table-hover');
    tables.forEach(table => {
        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(row => {
            row.addEventListener('mouseenter', function() {
                this.style.backgroundColor = 'rgba(25, 135, 84, 0.1)';
            });
            row.addEventListener('mouseleave', function() {
                this.style.backgroundColor = '';
            });
        });
    });
    
    // Sortable table headers
    const sortableHeaders = document.querySelectorAll('th[data-sortable]');
    sortableHeaders.forEach(header => {
        header.style.cursor = 'pointer';
        header.addEventListener('click', function() {
            // Add sorting logic here if needed
            console.log('Sorting by:', this.textContent);
        });
    });
}

/**
 * Smooth scrolling for anchor links
 */
function initializeSmoothScrolling() {
    const links = document.querySelectorAll('a[href^="#"]');
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

/**
 * Utility Functions
 */

/**
 * Show loading spinner
 */
function showLoading(element) {
    if (element) {
        element.innerHTML = '<div class="spinner-border spinner-border-sm me-2" role="status"></div>Loading...';
        element.disabled = true;
    }
}

/**
 * Hide loading spinner
 */
function hideLoading(element, originalText) {
    if (element) {
        element.innerHTML = originalText;
        element.disabled = false;
    }
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    
    toastContainer.appendChild(toast);
    
    // Show toast
    const bsToast = new bootstrap.Toast(toast, {
        autohide: true,
        delay: 3000
    });
    bsToast.show();
    
    // Remove toast from DOM after hiding
    toast.addEventListener('hidden.bs.toast', function() {
        this.remove();
    });
}

/**
 * Confirm dialog with custom styling
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * Format date for display
 */
function formatDate(date, format = 'dd-mm-yyyy') {
    if (!date) return '';
    
    const d = new Date(date);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    
    switch (format) {
        case 'dd-mm-yyyy':
            return `${day}-${month}-${year}`;
        case 'mm-dd-yyyy':
            return `${month}-${day}-${year}`;
        case 'yyyy-mm-dd':
            return `${year}-${month}-${day}`;
        default:
            return d.toLocaleDateString();
    }
}

/**
 * Debounce function for search inputs
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Local storage helpers
 */
const Storage = {
    set: function(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('Error saving to localStorage:', e);
        }
    },
    
    get: function(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (e) {
            console.error('Error reading from localStorage:', e);
            return defaultValue;
        }
    },
    
    remove: function(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.error('Error removing from localStorage:', e);
        }
    }
};

/**
 * AJAX helpers
 */
const Ajax = {
    get: function(url, callback, errorCallback) {
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => callback(data))
            .catch(error => {
                console.error('Ajax GET error:', error);
                if (errorCallback) errorCallback(error);
            });
    },
    
    post: function(url, data, callback, errorCallback) {
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => callback(data))
        .catch(error => {
            console.error('Ajax POST error:', error);
            if (errorCallback) errorCallback(error);
        });
    }
};

/**
 * Export functions for use in other scripts
 */
window.PortalApp = {
    showLoading,
    hideLoading,
    showToast,
    confirmAction,
    formatDate,
    debounce,
    Storage,
    Ajax
}; 