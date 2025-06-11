/**
 * Bulk Actions - Shared functionality for list pages
 * Handles checkbox selection, bulk operations, and action confirmations
 */

/**
 * Toggle all checkboxes based on select all checkbox
 * @param {string} selectAllId - ID of the select all checkbox
 * @param {string} checkboxClass - Class of individual checkboxes
 */
function toggleSelectAll(selectAllId = 'selectAll', checkboxClass = '.form-check-input') {
    const selectAllCheckbox = document.getElementById(selectAllId);
    const checkboxes = document.querySelectorAll(checkboxClass);
    
    if (selectAllCheckbox) {
        checkboxes.forEach(checkbox => {
            // Skip the select all checkbox itself
            if (checkbox.id !== selectAllId) {
                checkbox.checked = selectAllCheckbox.checked;
            }
        });
        
        updateBulkActions();
    }
}

/**
 * Update bulk action buttons visibility and select all state
 * @param {string} checkboxClass - Class of individual checkboxes
 * @param {string} bulkButtonClass - Class of bulk action buttons
 * @param {string} selectAllId - ID of the select all checkbox
 */
function updateBulkActions(checkboxClass = '.form-check-input:not(#selectAll)', bulkButtonClass = '.bulk-action-btn', selectAllId = 'selectAll') {
    const checkedBoxes = document.querySelectorAll(`${checkboxClass}:checked`);
    const bulkButtons = document.querySelectorAll(bulkButtonClass);
    const selectAllCheckbox = document.getElementById(selectAllId);
    
    // Show/hide bulk action buttons
    bulkButtons.forEach(button => {
        if (checkedBoxes.length > 0) {
            button.style.display = 'inline-block';
            // Update button text with count
            const text = button.textContent.replace(/\d+/, checkedBoxes.length);
            if (!text.includes(checkedBoxes.length)) {
                button.textContent = `${button.textContent.split(' ')[0]} (${checkedBoxes.length}) ${button.textContent.split(' ').slice(1).join(' ')}`;
            }
        } else {
            button.style.display = 'none';
        }
    });
    
    // Update select all checkbox state
    if (selectAllCheckbox) {
        const allCheckboxes = document.querySelectorAll(checkboxClass);
        
        if (checkedBoxes.length === 0) {
            selectAllCheckbox.indeterminate = false;
            selectAllCheckbox.checked = false;
        } else if (checkedBoxes.length === allCheckboxes.length) {
            selectAllCheckbox.indeterminate = false;
            selectAllCheckbox.checked = true;
        } else {
            selectAllCheckbox.indeterminate = true;
            selectAllCheckbox.checked = false;
        }
    }
    
    // Update selection info
    updateSelectionInfo(checkedBoxes.length);
}

/**
 * Update selection information display
 * @param {number} count - Number of selected items
 */
function updateSelectionInfo(count) {
    const selectionInfo = document.querySelector('.selection-info');
    if (selectionInfo) {
        if (count > 0) {
            selectionInfo.textContent = `${count} item(s) selected`;
            selectionInfo.style.display = 'inline-block';
        } else {
            selectionInfo.style.display = 'none';
        }
    }
}

/**
 * Perform bulk action with confirmation
 * @param {string} action - The action to perform
 * @param {string} checkboxClass - Class of checkboxes to check
 * @param {Object} config - Configuration object
 */
function bulkAction(action, checkboxClass = '.form-check-input:not(#selectAll)', config = {}) {
    const checkedBoxes = document.querySelectorAll(`${checkboxClass}:checked`);
    const ids = Array.from(checkedBoxes).map(cb => cb.value).filter(id => id && id !== 'on');
    
    if (ids.length === 0) {
        showAlert('Please select at least one item.', 'warning');
        return;
    }
    
    let confirmMessage;
    let url;
    let method = 'POST';
    
    // Configure action based on type
    switch (action) {
        case 'delete':
            confirmMessage = `Are you sure you want to delete ${ids.length} selected item(s)? This action cannot be undone.`;
            url = config.deleteUrl || '/bulk-delete';
            break;
        case 'enable':
            confirmMessage = `Are you sure you want to enable ${ids.length} selected item(s)?`;
            url = config.enableUrl || '/bulk-enable';
            break;
        case 'disable':
            confirmMessage = `Are you sure you want to disable ${ids.length} selected item(s)?`;
            url = config.disableUrl || '/bulk-disable';
            break;
        case 'archive':
            confirmMessage = `Are you sure you want to archive ${ids.length} selected item(s)?`;
            url = config.archiveUrl || '/bulk-archive';
            break;
        default:
            console.error('Unknown bulk action:', action);
            return;
    }
    
    // Show confirmation dialog
    if (confirm(confirmMessage)) {
        performBulkOperation(url, ids, method, action);
    }
}

/**
 * Perform the actual bulk operation
 * @param {string} url - The URL to send the request to
 * @param {Array} ids - Array of IDs to process
 * @param {string} method - HTTP method
 * @param {string} action - Action type for loading message
 */
function performBulkOperation(url, ids, method = 'POST', action = 'processing') {
    // Show loading state
    const loadingMessage = showLoadingOverlay(`${action.charAt(0).toUpperCase() + action.slice(1)}ing ${ids.length} item(s)...`);
    
    // Create and submit form
    const form = document.createElement('form');
    form.method = method;
    form.action = url;
    form.style.display = 'none';
    
    // Add CSRF token if available
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    
    if (csrfToken && csrfHeader) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrfToken.getAttribute('content');
        csrfInput.value = csrfHeader.getAttribute('content');
        form.appendChild(csrfInput);
    }
    
    // Add IDs as form data
    ids.forEach(id => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'ids';
        input.value = id;
        form.appendChild(input);
    });
    
    document.body.appendChild(form);
    form.submit();
}

/**
 * Setup event listeners for bulk actions
 * @param {Object} config - Configuration object
 */
function setupBulkActions(config = {}) {
    const {
        selectAllId = 'selectAll',
        checkboxClass = '.form-check-input:not(#selectAll)',
        bulkButtonClass = '.bulk-action-btn'
    } = config;
    
    // Setup select all functionality
    const selectAllCheckbox = document.getElementById(selectAllId);
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', () => {
            toggleSelectAll(selectAllId, '.form-check-input');
        });
    }
    
    // Setup individual checkbox listeners using event delegation
    document.addEventListener('change', function(event) {
        if (event.target.matches(checkboxClass)) {
            updateBulkActions(checkboxClass, bulkButtonClass, selectAllId);
        }
    });
    
    // Setup bulk action button listeners
    document.addEventListener('click', function(event) {
        if (event.target.matches('[data-bulk-action]')) {
            event.preventDefault();
            const action = event.target.getAttribute('data-bulk-action');
            const actionConfig = {
                deleteUrl: event.target.getAttribute('data-delete-url'),
                enableUrl: event.target.getAttribute('data-enable-url'),
                disableUrl: event.target.getAttribute('data-disable-url'),
                archiveUrl: event.target.getAttribute('data-archive-url')
            };
            bulkAction(action, checkboxClass, actionConfig);
        }
    });
    
    // Initial update
    updateBulkActions(checkboxClass, bulkButtonClass, selectAllId);
}

/**
 * Show loading overlay
 * @param {string} message - Loading message
 * @returns {HTMLElement} - The overlay element
 */
function showLoadingOverlay(message = 'Loading...') {
    // Remove existing overlay
    const existingOverlay = document.querySelector('.bulk-loading-overlay');
    if (existingOverlay) {
        existingOverlay.remove();
    }
    
    const overlay = document.createElement('div');
    overlay.className = 'bulk-loading-overlay';
    overlay.innerHTML = `
        <div class="bulk-loading-content">
            <div class="spinner-border text-primary mb-3" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <div class="bulk-loading-text">${message}</div>
        </div>
    `;
    
    document.body.appendChild(overlay);
    return overlay;
}

/**
 * Hide loading overlay
 */
function hideLoadingOverlay() {
    const overlay = document.querySelector('.bulk-loading-overlay');
    if (overlay) {
        overlay.remove();
    }
}

/**
 * Show alert message
 * @param {string} message - Alert message
 * @param {string} type - Alert type (success, warning, danger, info)
 */
function showAlert(message, type = 'info') {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.bulk-alert');
    existingAlerts.forEach(alert => alert.remove());
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show bulk-alert`;
    alert.innerHTML = `
        <i class="bi bi-${getAlertIcon(type)} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Insert at top of main content
    const mainContent = document.querySelector('main .container, .container-fluid');
    if (mainContent) {
        mainContent.insertBefore(alert, mainContent.firstChild);
    } else {
        document.body.insertBefore(alert, document.body.firstChild);
    }
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
        if (alert.parentNode) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }
    }, 5000);
}

/**
 * Get alert icon based on type
 * @param {string} type - Alert type
 * @returns {string} - Bootstrap icon class
 */
function getAlertIcon(type) {
    const icons = {
        success: 'check-circle',
        warning: 'exclamation-triangle',
        danger: 'x-circle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

/**
 * Select items by criteria
 * @param {string} criteria - Selection criteria (all, none, visible, checked)
 * @param {string} checkboxClass - Class of checkboxes
 */
function selectByCriteria(criteria, checkboxClass = '.form-check-input:not(#selectAll)') {
    const checkboxes = document.querySelectorAll(checkboxClass);
    
    switch (criteria) {
        case 'all':
            checkboxes.forEach(cb => cb.checked = true);
            break;
        case 'none':
            checkboxes.forEach(cb => cb.checked = false);
            break;
        case 'visible':
            // Only select visible checkboxes (useful with DataTables)
            checkboxes.forEach(cb => {
                if (cb.offsetParent !== null) {
                    cb.checked = true;
                }
            });
            break;
        case 'invert':
            checkboxes.forEach(cb => cb.checked = !cb.checked);
            break;
    }
    
    updateBulkActions();
}

/**
 * Get bulk action statistics
 * @param {string} checkboxClass - Class of checkboxes
 * @returns {Object} - Statistics object
 */
function getBulkActionStats(checkboxClass = '.form-check-input:not(#selectAll)') {
    const allCheckboxes = document.querySelectorAll(checkboxClass);
    const checkedCheckboxes = document.querySelectorAll(`${checkboxClass}:checked`);
    
    return {
        total: allCheckboxes.length,
        selected: checkedCheckboxes.length,
        unselected: allCheckboxes.length - checkedCheckboxes.length,
        percentage: allCheckboxes.length > 0 ? Math.round((checkedCheckboxes.length / allCheckboxes.length) * 100) : 0
    };
}

/**
 * Add bulk action toolbar
 * @param {HTMLElement} container - Container to add toolbar to
 * @param {Object} actions - Available actions configuration
 */
function addBulkActionToolbar(container, actions = {}) {
    const toolbar = document.createElement('div');
    toolbar.className = 'bulk-action-toolbar d-none mb-3';
    toolbar.innerHTML = `
        <div class="card border-primary">
            <div class="card-body py-2">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <span class="selection-info text-primary fw-bold"></span>
                    </div>
                    <div class="col-md-6 text-end">
                        <div class="btn-group" role="group">
                            ${actions.delete ? `<button type="button" class="btn btn-outline-danger btn-sm bulk-action-btn" data-bulk-action="delete" data-delete-url="${actions.delete}">
                                <i class="bi bi-trash me-1"></i>Delete
                            </button>` : ''}
                            ${actions.enable ? `<button type="button" class="btn btn-outline-success btn-sm bulk-action-btn" data-bulk-action="enable" data-enable-url="${actions.enable}">
                                <i class="bi bi-check-circle me-1"></i>Enable
                            </button>` : ''}
                            ${actions.disable ? `<button type="button" class="btn btn-outline-warning btn-sm bulk-action-btn" data-bulk-action="disable" data-disable-url="${actions.disable}">
                                <i class="bi bi-x-circle me-1"></i>Disable
                            </button>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    container.insertBefore(toolbar, container.firstChild);
}

// CSS for bulk actions
const bulkActionsCSS = `
.bulk-loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
}

.bulk-loading-content {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.bulk-loading-text {
    font-weight: 500;
    color: #495057;
}

.bulk-action-toolbar {
    transition: all 0.3s ease;
}

.selection-info {
    font-size: 0.875rem;
}

.bulk-action-btn {
    transition: all 0.2s ease;
}

.bulk-action-btn:hover {
    transform: translateY(-1px);
}

.bulk-alert {
    position: relative;
    z-index: 1050;
}

/* Checkbox enhancements */
.form-check-input:indeterminate {
    background-color: #0d6efd;
    border-color: #0d6efd;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20'%3e%3cpath fill='none' stroke='%23fff' stroke-linecap='round' stroke-linejoin='round' stroke-width='3' d='M6 10h8'/%3e%3c/svg%3e");
}

.table tbody tr:hover .form-check-input {
    opacity: 1;
}

.table tbody .form-check-input {
    opacity: 0.7;
    transition: opacity 0.2s ease;
}
`;

// Inject CSS
if (!document.getElementById('bulk-actions-css')) {
    const style = document.createElement('style');
    style.id = 'bulk-actions-css';
    style.textContent = bulkActionsCSS;
    document.head.appendChild(style);
}

// Export functions for global use
window.BulkActions = {
    toggleSelectAll,
    updateBulkActions,
    bulkAction,
    setupBulkActions,
    showLoadingOverlay,
    hideLoadingOverlay,
    showAlert,
    selectByCriteria,
    getBulkActionStats,
    addBulkActionToolbar
}; 