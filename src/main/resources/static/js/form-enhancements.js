/**
 * Form Enhancements - Shared functionality for form pages
 * Handles validation, character counting, auto-resize, and form interactions
 */

/**
 * Setup form enhancements
 * @param {Object} config - Configuration object
 */
function setupFormEnhancements(config = {}) {
    const {
        enableCharacterCount = true,
        enableAutoResize = true,
        enableRealTimeValidation = true,
        enablePreview = true,
        characterLimits = {}
    } = config;
    
    document.addEventListener('DOMContentLoaded', function() {
        if (enableCharacterCount) {
            setupCharacterCounters(characterLimits);
        }
        
        if (enableAutoResize) {
            setupAutoResizeTextareas();
        }
        
        if (enableRealTimeValidation) {
            setupRealTimeValidation();
        }
        
        if (enablePreview) {
            setupPreviewFunctionality();
        }
        
        setupFormSubmitHandling();
        setupFormResetHandling();
    });
}

/**
 * Setup character counters for text inputs and textareas
 * @param {Object} limits - Custom character limits
 */
function setupCharacterCounters(limits = {}) {
    const defaultLimits = {
        'title': 255,
        'description': 1000,
        'header': 100,
        'message': 500,
        'url': 500
    };
    
    const characterLimits = { ...defaultLimits, ...limits };
    
    // Find all text inputs and textareas with character limits
    Object.keys(characterLimits).forEach(fieldName => {
        const field = document.getElementById(fieldName);
        if (field) {
            const limit = characterLimits[fieldName];
            addCharacterCounter(field, limit);
        }
    });
    
    // Also handle any fields with data-char-limit attribute
    document.querySelectorAll('[data-char-limit]').forEach(field => {
        const limit = parseInt(field.getAttribute('data-char-limit'));
        if (limit > 0) {
            addCharacterCounter(field, limit);
        }
    });
}

/**
 * Add character counter to a specific field
 * @param {HTMLElement} field - The input/textarea element
 * @param {number} limit - Character limit
 */
function addCharacterCounter(field, limit) {
    const counterId = field.id + 'Counter';
    let counter = document.getElementById(counterId);
    
    if (!counter) {
        counter = document.createElement('div');
        counter.id = counterId;
        counter.className = 'character-counter form-text';
        field.parentNode.appendChild(counter);
    }
    
    function updateCounter() {
        const current = field.value.length;
        const remaining = limit - current;
        
        counter.textContent = `${current}/${limit} characters`;
        
        // Update styling based on remaining characters
        counter.className = 'character-counter form-text';
        
        if (remaining < 0) {
            counter.classList.add('text-danger');
            field.classList.add('is-invalid');
        } else if (remaining < 20) {
            counter.classList.add('text-warning');
            field.classList.remove('is-invalid');
        } else {
            counter.classList.add('text-muted');
            field.classList.remove('is-invalid');
        }
    }
    
    // Initial update
    updateCounter();
    
    // Update on input
    field.addEventListener('input', updateCounter);
    field.addEventListener('paste', () => setTimeout(updateCounter, 0));
}

/**
 * Setup auto-resize functionality for textareas
 */
function setupAutoResizeTextareas() {
    const textareas = document.querySelectorAll('textarea.auto-resize, textarea[data-auto-resize]');
    
    textareas.forEach(textarea => {
        makeTextareaAutoResize(textarea);
    });
}

/**
 * Make a textarea auto-resize
 * @param {HTMLTextAreaElement} textarea - The textarea element
 */
function makeTextareaAutoResize(textarea) {
    function resize() {
        textarea.style.height = 'auto';
        textarea.style.height = Math.max(textarea.scrollHeight, 100) + 'px';
    }
    
    textarea.addEventListener('input', resize);
    textarea.addEventListener('change', resize);
    
    // Initial resize
    setTimeout(resize, 0);
}

/**
 * Setup real-time validation
 */
function setupRealTimeValidation() {
    const form = document.querySelector('form');
    if (!form) return;
    
    const inputs = form.querySelectorAll('input[required], textarea[required], select[required]');
    
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            validateField(this);
        });
        
        input.addEventListener('input', function() {
            if (this.classList.contains('is-invalid')) {
                validateField(this);
            }
        });
    });
}

/**
 * Validate a specific field
 * @param {HTMLElement} field - The field to validate
 * @returns {boolean} - Whether the field is valid
 */
function validateField(field) {
    const isValid = field.checkValidity();
    
    if (isValid) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
        clearFieldError(field);
    } else {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
        showFieldError(field, field.validationMessage);
    }
    
    return isValid;
}

/**
 * Show field error message
 * @param {HTMLElement} field - The field with error
 * @param {string} message - Error message
 */
function showFieldError(field, message) {
    clearFieldError(field);
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'invalid-feedback';
    errorDiv.textContent = message;
    errorDiv.id = field.id + 'Error';
    
    field.parentNode.appendChild(errorDiv);
}

/**
 * Clear field error message
 * @param {HTMLElement} field - The field to clear error for
 */
function clearFieldError(field) {
    const errorDiv = document.getElementById(field.id + 'Error');
    if (errorDiv) {
        errorDiv.remove();
    }
}

/**
 * Setup preview functionality
 */
function setupPreviewFunctionality() {
    // Priority preview
    const prioritySelect = document.getElementById('priority');
    if (prioritySelect) {
        setupPriorityPreview(prioritySelect);
    }
    
    // Scrolling message preview
    const messageField = document.getElementById('message');
    if (messageField) {
        setupScrollingPreview(messageField);
    }
    
    // Date validity preview
    const validFromField = document.getElementById('validFrom');
    const validToField = document.getElementById('validTo');
    if (validFromField && validToField) {
        setupDateValidityPreview(validFromField, validToField);
    }
}

/**
 * Setup priority preview
 * @param {HTMLSelectElement} prioritySelect - Priority select element
 */
function setupPriorityPreview(prioritySelect) {
    const previewContainer = document.createElement('div');
    previewContainer.className = 'mt-2';
    previewContainer.innerHTML = '<small class="text-muted">Preview:</small><br><span id="priorityPreview" class="priority-preview"></span>';
    prioritySelect.parentNode.appendChild(previewContainer);
    
    const preview = document.getElementById('priorityPreview');
    
    function updatePriorityPreview() {
        const value = prioritySelect.value;
        const text = prioritySelect.options[prioritySelect.selectedIndex].text;
        
        preview.textContent = text;
        preview.className = 'priority-preview';
        
        if (value) {
            preview.classList.add(`priority-${value}`);
        }
    }
    
    prioritySelect.addEventListener('change', updatePriorityPreview);
    updatePriorityPreview(); // Initial update
}

/**
 * Setup scrolling message preview
 * @param {HTMLTextAreaElement} messageField - Message field
 */
function setupScrollingPreview(messageField) {
    let previewContainer = document.getElementById('scrollingPreview');
    
    if (!previewContainer) {
        previewContainer = document.createElement('div');
        previewContainer.id = 'scrollingPreview';
        previewContainer.className = 'scrolling-preview';
        previewContainer.innerHTML = `
            <h6><i class="bi bi-eye me-2"></i>Scrolling Message Preview</h6>
            <div class="scrolling-text" id="scrollingText">Enter your message above to see the preview</div>
        `;
        messageField.parentNode.appendChild(previewContainer);
    }
    
    const scrollingText = document.getElementById('scrollingText');
    
    function updateScrollingPreview() {
        const message = messageField.value.trim();
        scrollingText.textContent = message || 'Enter your message above to see the preview';
    }
    
    messageField.addEventListener('input', updateScrollingPreview);
    updateScrollingPreview(); // Initial update
}

/**
 * Setup date validity preview
 * @param {HTMLInputElement} validFromField - Valid from date field
 * @param {HTMLInputElement} validToField - Valid to date field
 */
function setupDateValidityPreview(validFromField, validToField) {
    const previewContainer = document.createElement('div');
    previewContainer.className = 'mt-3';
    previewContainer.innerHTML = `
        <div class="card">
            <div class="card-body py-2">
                <h6 class="card-title mb-2"><i class="bi bi-calendar-check me-2"></i>Validity Period</h6>
                <div id="dateValidityPreview" class="text-muted">Select dates to see validity period</div>
            </div>
        </div>
    `;
    
    // Insert after the second date field
    validToField.parentNode.insertBefore(previewContainer, validToField.nextSibling);
    
    const preview = document.getElementById('dateValidityPreview');
    
    function updateDateValidityPreview() {
        const fromDate = validFromField.value;
        const toDate = validToField.value;
        
        if (fromDate && toDate) {
            const from = new Date(fromDate);
            const to = new Date(toDate);
            const now = new Date();
            
            if (to < from) {
                preview.innerHTML = '<span class="text-danger"><i class="bi bi-exclamation-triangle me-1"></i>End date must be after start date</span>';
                return;
            }
            
            const diffTime = Math.abs(to - from);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            let status = '';
            if (now < from) {
                status = '<span class="text-info"><i class="bi bi-clock me-1"></i>Future</span>';
            } else if (now >= from && now <= to) {
                status = '<span class="text-success"><i class="bi bi-check-circle me-1"></i>Active</span>';
            } else {
                status = '<span class="text-secondary"><i class="bi bi-x-circle me-1"></i>Expired</span>';
            }
            
            preview.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <span>Duration: ${diffDays} day(s)</span>
                    <span>Status: ${status}</span>
                </div>
            `;
        } else {
            preview.textContent = 'Select dates to see validity period';
        }
    }
    
    validFromField.addEventListener('change', updateDateValidityPreview);
    validToField.addEventListener('change', updateDateValidityPreview);
    updateDateValidityPreview(); // Initial update
}

/**
 * Setup form submit handling
 */
function setupFormSubmitHandling() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const submitButton = form.querySelector('button[type="submit"], input[type="submit"]');
            
            if (submitButton) {
                // Prevent double submission
                if (submitButton.disabled) {
                    event.preventDefault();
                    return false;
                }
                
                // Show loading state
                const originalText = submitButton.innerHTML;
                submitButton.disabled = true;
                submitButton.innerHTML = '<i class="bi bi-arrow-repeat spin me-2"></i>Saving...';
                
                // Restore button if form validation fails
                setTimeout(() => {
                    if (!form.checkValidity()) {
                        submitButton.disabled = false;
                        submitButton.innerHTML = originalText;
                    }
                }, 100);
            }
        });
    });
}

/**
 * Setup form reset handling
 */
function setupFormResetHandling() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        const resetButton = form.querySelector('button[type="reset"], input[type="reset"]');
        if (resetButton) {
            resetButton.addEventListener('click', function(event) {
                if (!confirm('Are you sure you want to reset all fields? All unsaved changes will be lost.')) {
                    event.preventDefault();
                    return false;
                }
                
                // Clear validation states
                setTimeout(() => {
                    const fields = form.querySelectorAll('.is-valid, .is-invalid');
                    fields.forEach(field => {
                        field.classList.remove('is-valid', 'is-invalid');
                    });
                    
                    // Clear error messages
                    const errorMessages = form.querySelectorAll('.invalid-feedback');
                    errorMessages.forEach(msg => msg.remove());
                    
                    // Update character counters
                    const counters = form.querySelectorAll('.character-counter');
                    counters.forEach(counter => {
                        const fieldId = counter.id.replace('Counter', '');
                        const field = document.getElementById(fieldId);
                        if (field) {
                            const event = new Event('input');
                            field.dispatchEvent(event);
                        }
                    });
                    
                    // Update previews
                    const previews = form.querySelectorAll('[id*="Preview"]');
                    previews.forEach(preview => {
                        const event = new Event('change');
                        form.dispatchEvent(event);
                    });
                }, 0);
            });
        }
    });
}

/**
 * Show form success message
 * @param {string} message - Success message
 */
function showFormSuccess(message) {
    showFormAlert(message, 'success');
}

/**
 * Show form error message
 * @param {string} message - Error message
 */
function showFormError(message) {
    showFormAlert(message, 'danger');
}

/**
 * Show form alert
 * @param {string} message - Alert message
 * @param {string} type - Alert type
 */
function showFormAlert(message, type = 'info') {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll('.form-alert');
    existingAlerts.forEach(alert => alert.remove());
    
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show form-alert`;
    alert.innerHTML = `
        <i class="bi bi-${getFormAlertIcon(type)} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Insert at top of form or main content
    const form = document.querySelector('form');
    const container = form || document.querySelector('main .container');
    
    if (container) {
        container.insertBefore(alert, container.firstChild);
        
        // Scroll to alert
        alert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
    
    // Auto-hide after 5 seconds for success messages
    if (type === 'success') {
        setTimeout(() => {
            if (alert.parentNode) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    }
}

/**
 * Get form alert icon
 * @param {string} type - Alert type
 * @returns {string} - Icon class
 */
function getFormAlertIcon(type) {
    const icons = {
        success: 'check-circle',
        danger: 'exclamation-triangle',
        warning: 'exclamation-triangle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

/**
 * Validate entire form
 * @param {HTMLFormElement} form - Form to validate
 * @returns {boolean} - Whether form is valid
 */
function validateForm(form) {
    const fields = form.querySelectorAll('input[required], textarea[required], select[required]');
    let isValid = true;
    
    fields.forEach(field => {
        if (!validateField(field)) {
            isValid = false;
        }
    });
    
    return isValid;
}

/**
 * Clear all form validation
 * @param {HTMLFormElement} form - Form to clear
 */
function clearFormValidation(form) {
    const fields = form.querySelectorAll('.is-valid, .is-invalid');
    fields.forEach(field => {
        field.classList.remove('is-valid', 'is-invalid');
    });
    
    const errorMessages = form.querySelectorAll('.invalid-feedback');
    errorMessages.forEach(msg => msg.remove());
}

/**
 * Enable/disable form
 * @param {HTMLFormElement} form - Form to toggle
 * @param {boolean} enabled - Whether to enable the form
 */
function toggleFormEnabled(form, enabled = true) {
    const fields = form.querySelectorAll('input, textarea, select, button');
    
    fields.forEach(field => {
        field.disabled = !enabled;
    });
    
    if (enabled) {
        form.classList.remove('form-loading');
    } else {
        form.classList.add('form-loading');
    }
}

// CSS for form enhancements
const formEnhancementsCSS = `
.spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.character-counter {
    font-size: 0.875rem;
    margin-top: 0.25rem;
}

.form-alert {
    margin-bottom: 1rem;
}

.priority-preview {
    display: inline-block;
    padding: 0.375rem 0.75rem;
    border-radius: 0.375rem;
    font-size: 0.875rem;
    font-weight: 500;
    text-align: center;
    min-width: 80px;
}

.scrolling-preview {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border: 2px dashed #6c757d;
    border-radius: 8px;
    padding: 1rem;
    margin-top: 1rem;
    position: relative;
    overflow: hidden;
}

.scrolling-text {
    white-space: nowrap;
    overflow: hidden;
    animation: scroll-left 15s linear infinite;
    color: #212529;
    font-weight: 500;
}

@keyframes scroll-left {
    0% { transform: translateX(100%); }
    100% { transform: translateX(-100%); }
}

.form-loading {
    position: relative;
    opacity: 0.6;
    pointer-events: none;
}

.auto-resize {
    resize: vertical;
    min-height: 100px;
    max-height: 300px;
}
`;

// Inject CSS
if (!document.getElementById('form-enhancements-css')) {
    const style = document.createElement('style');
    style.id = 'form-enhancements-css';
    style.textContent = formEnhancementsCSS;
    document.head.appendChild(style);
}

// Export functions for global use
window.FormEnhancements = {
    setupFormEnhancements,
    setupCharacterCounters,
    addCharacterCounter,
    makeTextareaAutoResize,
    validateField,
    validateForm,
    clearFormValidation,
    showFormSuccess,
    showFormError,
    toggleFormEnabled
}; 