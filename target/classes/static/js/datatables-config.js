/**
 * DataTables Configuration - Shared functionality for list pages
 * Handles DataTables initialization, search, sorting, and pagination
 */

// Global variables
let dataTable;
let dataTableInitialized = false;
let currentPrioritySort = ''; // Track current priority sort state

/**
 * Initialize DataTable with configuration
 * @param {string} tableId - The ID of the table element
 * @param {Object} config - Configuration object
 */
function initializeDataTable(tableId, config = {}) {
    // Default configuration
    const defaultConfig = {
        responsive: true,
        paging: true,
        pageLength: 25, // Increased default page size
        lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
        searching: true,
        info: false,
        ordering: false, // Disable all column sorting by default
        deferRender: false, // Process all data immediately
        stateSave: false, // Don't save state to ensure fresh data processing
        processing: false, // Disable processing indicator since we're client-side
        serverSide: false, // Ensure client-side processing for full dataset operations
        
        // Enhanced language configuration
        language: {
            search: "Search:",
            searchPlaceholder: "Search records...",
            lengthMenu: "Show _MENU_ entries",
            info: "Showing _START_ to _END_ of _TOTAL_ records",
            infoEmpty: "Showing 0 to 0 of 0 records",
            infoFiltered: "(filtered from _MAX_ total records)",
            zeroRecords: "No matching records found",
            emptyTable: "No records available",
            paginate: {
                first: "First",
                last: "Last", 
                next: "Next",
                previous: "Previous"
            }
        },
        
        // Clean DOM layout - Remove info, keep only search/length and pagination
        dom: '<"row"<"col-sm-6"l><"col-sm-6"f>>' +
             '<"row"<"col-sm-12"tr>>' +
             '<"row"<"col-sm-12 text-center"p>>',
        
        // Configure pagination to show only numbers (no Previous/Next buttons)
        pagingType: "numbers",
        
        // Default column definitions
        columnDefs: [
            { 
                orderable: false, 
                targets: "_all" // Disable sorting for all columns by default
            },
            { 
                className: "text-center", 
                targets: [] // Will be set by specific page
            }
        ]
    };
    
    // Merge with custom configuration
    const finalConfig = Object.assign({}, defaultConfig, config);
    
    // Initialize DataTable with enhanced data processing
    dataTable = $(tableId).DataTable(finalConfig);
    dataTableInitialized = true;
    
    console.log(`DataTable initialized for ${tableId} with enhanced functionality for full dataset operations`);
    
    return dataTable;
}

/**
 * Configure DataTable for What's New page
 * @param {boolean} isAdmin - Whether user has admin privileges (deprecated - no longer uses bulk actions)
 */
function initializeWhatsNewDataTable(isAdmin = false) {
    // Fixed column indices since checkboxes are removed
    const priorityColumn = 2; // Priority column index
    const actionsColumn = 5; // Actions column index
    
    const config = {
        ordering: false, // Disable all sorting arrows
        columnDefs: [
            { 
                orderable: false, 
                targets: "_all" // Disable sorting for ALL columns
            },
            { 
                className: "text-center", 
                targets: [priorityColumn] // Center priority column
            }
        ],
        language: {
            search: "Search:",
            searchPlaceholder: "Search announcements...",
            lengthMenu: "Show _MENU_ entries",
            info: "Showing _START_ to _END_ of _TOTAL_ announcements",
            infoEmpty: "Showing 0 to 0 of 0 announcements",
            infoFiltered: "(filtered from _MAX_ total announcements)",
            zeroRecords: "No matching announcements found",
            emptyTable: "No announcements available",
            paginate: {
                first: "First",
                last: "Last", 
                next: "Next",
                previous: "Previous"
            }
        },
        drawCallback: function() {
            if (typeof updateBulkActions === 'function') {
                updateBulkActions();
            }
            // Maintain priority sort state across page navigation
            maintainPrioritySortState(this.api());
        },
        initComplete: function() {
            console.log('What\'s New DataTable initialized with enhanced search and sorting');
            dataTableInitialized = true;
        }
    };
    
    return initializeDataTable('#whatsNewTable', config);
}

/**
 * Configure DataTable for Message Board page
 * @param {boolean} isAdmin - Whether user has admin privileges (deprecated - no longer uses bulk actions)
 */
function initializeMessageBoardDataTable(isAdmin = false) {
    // Fixed column indices since checkboxes are removed
    const priorityColumn = 2; // Priority column index
    const actionsColumn = 5; // Actions column index
    
    const config = {
        ordering: false, // Disable all sorting arrows
        columnDefs: [
            { 
                orderable: false, 
                targets: "_all" // Disable sorting for ALL columns
            },
            { 
                className: "text-center", 
                targets: [priorityColumn] // Center priority column
            }
        ],
        language: {
            search: "Search:",
            searchPlaceholder: "Search messages...",
            lengthMenu: "Show _MENU_ entries",
            info: "Showing _START_ to _END_ of _TOTAL_ messages",
            infoEmpty: "Showing 0 to 0 of 0 messages",
            infoFiltered: "(filtered from _MAX_ total messages)",
            zeroRecords: "No matching messages found",
            emptyTable: "No messages available",
            paginate: {
                first: "First",
                last: "Last", 
                next: "Next",
                previous: "Previous"
            }
        },
        drawCallback: function() {
            if (typeof updateBulkActions === 'function') {
                updateBulkActions();
            }
            // Maintain priority sort state across page navigation
            maintainPrioritySortState(this.api());
        },
        initComplete: function() {
            console.log('Message Board DataTable initialized with enhanced search and sorting');
            dataTableInitialized = true;
        }
    };
    
    return initializeDataTable('#messagesTable', config);
}

/**
 * Load all data functionality
 * Shows notification when partial data is loaded
 */
function setupLoadAllDataNotification(totalRecords, currentRecords, loadAllUrl) {
    if (currentRecords < totalRecords) {
        const notification = `
            <div class="load-all-notification">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-info-circle notification-icon me-3"></i>
                            <div>
                                <h6 class="mb-1">Limited Data View</h6>
                                <p class="mb-0 text-muted">
                                    Showing ${currentRecords} of ${totalRecords} records. 
                                    Load all data for full search and sorting capabilities.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4 text-end">
                        <button class="btn btn-load-all" onclick="loadAllData('${loadAllUrl}')">
                            <i class="bi bi-download me-2"></i>Load All Data
                        </button>
                    </div>
                </div>
            </div>
        `;
        
        // Insert notification before the table card
        $('.card.border-0.shadow-sm').before(notification);
    }
}

/**
 * Load all data from server
 */
function loadAllData(url) {
    const button = document.querySelector('.btn-load-all');
    const originalText = button.innerHTML;
    
    // Show loading state
    button.innerHTML = '<i class="bi bi-arrow-repeat spin me-2"></i>Loading...';
    button.disabled = true;
    
    // Redirect to load all data
    window.location.href = url;
}

/**
 * Add custom search functionality
 */
function addCustomSearch(searchInputId, tableInstance = null) {
    const table = tableInstance || dataTable;
    const searchInput = document.getElementById(searchInputId);
    
    if (searchInput && table) {
        const debouncedSearch = (typeof debounce !== 'undefined' ? debounce : function(func, wait) {
            let timeout;
            return function(...args) {
                clearTimeout(timeout);
                timeout = setTimeout(() => func.apply(this, args), wait);
            };
        })(function(value) {
            table.search(value).draw();
        }, 300);
        
        searchInput.addEventListener('input', function() {
            debouncedSearch(this.value);
        });
    }
}

/**
 * Add column-specific filters
 */
function addColumnFilters(columnConfigs, tableInstance = null) {
    const table = tableInstance || dataTable;
    
    columnConfigs.forEach(config => {
        if (config.type === 'select') {
            addSelectFilter(config.column, config.options, table);
        } else if (config.type === 'dateRange') {
            addDateRangeFilter(config.column, table);
        }
    });
}

/**
 * Add select filter for a column
 */
function addSelectFilter(columnIndex, options, table) {
    const selectHtml = `
        <select class="form-select form-select-sm" onchange="filterColumn(${columnIndex}, this.value)">
            <option value="">All</option>
            ${options.map(opt => `<option value="${opt.value}">${opt.label}</option>`).join('')}
        </select>
    `;
    
    // Add to column header or create filter row
    // Implementation depends on specific layout needs
}

/**
 * Filter specific column
 */
function filterColumn(columnIndex, value) {
    if (dataTable) {
        dataTable.column(columnIndex).search(value).draw();
    }
}

/**
 * Export functionality
 */
function exportTableData(format = 'csv', filename = 'export') {
    if (!dataTable) {
        console.error('DataTable not initialized');
        return;
    }
    
    const data = dataTable.rows({ search: 'applied' }).data().toArray();
    
    if (format === 'csv') {
        exportToCSV(data, filename);
    } else if (format === 'excel') {
        exportToExcel(data, filename);
    }
}

/**
 * Export to CSV
 */
function exportToCSV(data, filename) {
    const headers = dataTable.columns().header().toArray().map(th => th.textContent);
    const csvContent = [
        headers.join(','),
        ...data.map(row => row.join(','))
    ].join('\n');
    
    downloadFile(csvContent, `${filename}.csv`, 'text/csv');
}

/**
 * Download file helper
 */
function downloadFile(content, filename, mimeType) {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

/**
 * Refresh table data
 */
function refreshTableData(ajaxUrl = null) {
    if (dataTable && ajaxUrl) {
        dataTable.ajax.url(ajaxUrl).load();
    } else {
        // Reload page for server-side rendering
        window.location.reload();
    }
}

/**
 * Get selected rows (for bulk operations)
 */
function getSelectedRows(checkboxSelector = '.form-check-input:checked') {
    const checkboxes = document.querySelectorAll(checkboxSelector);
    return Array.from(checkboxes)
        .filter(cb => cb.value && cb.value !== 'on')
        .map(cb => cb.value);
}

/**
 * Clear all filters
 */
function clearAllFilters() {
    if (dataTable) {
        dataTable.search('').columns().search('').draw();
        
        // Clear custom filter inputs
        document.querySelectorAll('.dataTables_wrapper input, .dataTables_wrapper select').forEach(input => {
            if (input.type === 'search' || input.type === 'text') {
                input.value = '';
            } else if (input.type === 'select-one') {
                input.selectedIndex = 0;
            }
        });
    }
}

/**
 * Show/hide table loading state
 */
function setTableLoading(isLoading = true) {
    const tableContainer = document.querySelector('.dataTables_wrapper');
    if (tableContainer) {
        if (isLoading) {
            tableContainer.classList.add('table-loading');
        } else {
            tableContainer.classList.remove('table-loading');
        }
    }
}

// CSS for loading state
const loadingCSS = `
.table-loading {
    position: relative;
    opacity: 0.6;
    pointer-events: none;
}

.table-loading::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    z-index: 10;
}

.spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
`;

// Inject loading CSS
if (!document.getElementById('datatables-config-css')) {
    const style = document.createElement('style');
    style.id = 'datatables-config-css';
    style.textContent = loadingCSS;
    document.head.appendChild(style);
}

/**
 * Add sort filters beside table headers (Priority column only)
 * @param {string} tableId - The ID of the table element
 * @param {number} priorityColumnIndex - Index of the priority column to add filter
 */
function addHeaderSortFilters(tableId, priorityColumnIndex = 2) {
    console.log('🔧 addHeaderSortFilters called with:', tableId, 'priority column:', priorityColumnIndex);
    
    const table = dataTable || $(tableId).DataTable();
    const tableWrapper = document.querySelector(tableId + '_wrapper');
    
    if (!tableWrapper) {
        console.error('❌ Table wrapper not found for:', tableId);
        return;
    }
    
    console.log('✅ Table wrapper found:', tableWrapper);

    // Create external filter container above the table
    let filterContainer = tableWrapper.querySelector('.priority-filter-container');
    if (!filterContainer) {
        console.log('📝 Creating external priority filter container...');
        filterContainer = document.createElement('div');
        filterContainer.className = 'priority-filter-container mb-3';
        filterContainer.innerHTML = `
            <div class="row align-items-center">
                <div class="col-auto">
                    <label class="form-label mb-0 fw-bold">Priority Filter:</label>
                </div>
                <div class="col-auto">
                    <div id="priority-sort-dropdown-container"></div>
                </div>
            </div>
        `;
        
        // Insert before the table (after the length and search controls)
        const tableContainer = tableWrapper.querySelector('.dataTables_wrapper > .row:nth-child(2)');
        if (tableContainer) {
            tableWrapper.insertBefore(filterContainer, tableContainer);
        } else {
            tableWrapper.insertBefore(filterContainer, tableWrapper.firstChild);
        }
    } else {
        console.log('♻️ Using existing priority filter container');
    }

    // Create priority dropdown in the external container
    const dropdownContainer = filterContainer.querySelector('#priority-sort-dropdown-container');
    if (dropdownContainer && !dropdownContainer.hasChildNodes()) {
        console.log('🎯 Adding priority filter dropdown');
        const sortSelect = createPrioritySortDropdown(priorityColumnIndex, table);
        dropdownContainer.appendChild(sortSelect);
    }
    
    console.log('✅ External priority filter setup completed');
}

/**
 * Create priority-specific sort dropdown
 * @param {number} columnIndex - The priority column index
 * @param {Object} table - DataTable instance
 */
function createPrioritySortDropdown(columnIndex, table) {
    console.log('🎨 Creating priority dropdown for column:', columnIndex);
    
    const select = document.createElement('select');
    select.className = 'form-select form-select-sm priority-sort-filter';
    select.style.fontSize = '0.75rem';
    select.style.padding = '2px 4px';
    
    // Add priority-specific options
    const options = [
        { value: '', text: 'Default Order' },
        { value: 'asc', text: 'Priority: Low to High' },
        { value: 'desc', text: 'Priority: High to Low' }
    ];
    
    console.log('📝 Adding options:', options);
    
    options.forEach(option => {
        const optionElement = document.createElement('option');
        optionElement.value = option.value;
        optionElement.textContent = option.text;
        select.appendChild(optionElement);
    });
    
    // Add event listener with global data filtering
    select.addEventListener('change', function() {
        const selectedValue = this.value;
        console.log('🔄 Priority filter changed to:', selectedValue);
        
        // Ensure we're working with the full dataset
        table.search('').columns().search('').draw();
        
        // Apply the priority sort filter
        applyPrioritySortFilter(columnIndex, selectedValue, table);
    });
    
    console.log('✅ Priority dropdown created and event listener attached');
    return select;
}

/**
 * Apply priority sort filter across ALL data (not just current page)
 * Enhanced to work with the entire dataset regardless of pagination
 * @param {number} columnIndex - The priority column index
 * @param {string} sortDirection - 'asc', 'desc', or '' for default
 * @param {Object} table - DataTable instance
 */
function applyPrioritySortFilter(columnIndex, sortDirection, table) {
    console.log('🎯 applyPrioritySortFilter called with:', {
        columnIndex: columnIndex,
        sortDirection: sortDirection,
        tableExists: !!table
    });
    
    if (!table) {
        console.error('❌ DataTable instance not provided');
        return;
    }
    
    // Store the current sort state globally
    currentPrioritySort = sortDirection;
    
    // Clear any existing search to ensure we work with all data
    table.search('').columns().search('');
    
    if (sortDirection === '') {
        console.log('↩️ Resetting to default order');
        // Reset to original DOM order by clearing all ordering
        table.order([]).draw();
    } else {
        console.log('📊 Applying persistent priority sort on ALL data:', sortDirection);
        
        // PERMANENTLY enable sorting for the priority column
        const settings = table.settings()[0];
        settings.oFeatures.bSort = true;
        settings.aoColumns[columnIndex].bSortable = true;
        
        // Apply sort to the priority column across ALL pages
        console.log(`🔄 Sorting column ${columnIndex} in ${sortDirection} direction`);
        table.order([[columnIndex, sortDirection]]).draw();
        
        // Keep sorting enabled to maintain state across page navigation
        // Don't disable it back - this is the key fix!
        
        if (sortDirection === 'asc') {
            console.log('✅ Sorted priority: Low to High (1, 2, 3, 4, 5, 6)');
        } else {
            console.log('✅ Sorted priority: High to Low (6, 5, 4, 3, 2, 1)');
        }
    }
    
    // Update dropdown visual state
    updatePrioritySortDropdownState(sortDirection);
    
    // Get accurate count of ALL data (not just current page)
    const totalRecords = table.data().length;
    const visibleRecords = table.page.info().recordsDisplay;
    console.log(`✅ Priority sort applied: ${sortDirection || 'default'} across all ${totalRecords} records (${visibleRecords} currently visible)`);
}

/**
 * Maintain priority sort state across page navigation and table redraws
 * @param {Object} table - DataTable API instance
 */
function maintainPrioritySortState(table) {
    if (currentPrioritySort && currentPrioritySort !== '') {
        console.log('🔄 Maintaining priority sort state:', currentPrioritySort);
        
        // Find priority column (usually column 2)
        const priorityColumnIndex = 2;
        
        // Ensure sorting remains enabled for priority column
        const settings = table.settings()[0];
        settings.oFeatures.bSort = true;
        settings.aoColumns[priorityColumnIndex].bSortable = true;
        
        // Reapply the sort order if it's not currently applied
        const currentOrder = table.order();
        if (!currentOrder.length || currentOrder[0][0] !== priorityColumnIndex || currentOrder[0][1] !== currentPrioritySort) {
            console.log('🔧 Reapplying priority sort:', currentPrioritySort);
            table.order([[priorityColumnIndex, currentPrioritySort]]);
        }
        
        // Update dropdown state
        updatePrioritySortDropdownState(currentPrioritySort);
    }
}

/**
 * Update priority sort dropdown visual state
 * @param {string} activeDirection - The current sort direction
 */
function updatePrioritySortDropdownState(activeDirection) {
    const priorityDropdown = document.querySelector('.priority-sort-filter');
    
    if (priorityDropdown) {
        priorityDropdown.value = activeDirection;
        if (activeDirection) {
            priorityDropdown.style.backgroundColor = '#e3f2fd';
            priorityDropdown.style.fontWeight = 'bold';
        } else {
            priorityDropdown.style.backgroundColor = '';
            priorityDropdown.style.fontWeight = 'normal';
        }
    }
}

/**
 * Initialize What's New DataTable with priority-only sort filter
 * @param {boolean} isAdmin - Whether user has admin privileges
 * @param {boolean} enablePriorityFilter - Whether to enable priority filter
 */
function initializeWhatsNewDataTableWithPriorityFilter(isAdmin = false, enablePriorityFilter = true) {
    // Initialize the standard DataTable first
    const table = initializeWhatsNewDataTable(isAdmin);
    
    // Add priority filter back as requested
    if (enablePriorityFilter) {
        console.log('Adding priority filter for What\'s New...');
        setTimeout(() => {
            addHeaderSortFilters('#whatsNewTable', 2);
        }, 200);
    }
    
    return table;
}

/**
 * Initialize Message Board DataTable with priority-only sort filter
 * @param {boolean} isAdmin - Whether user has admin privileges  
 * @param {boolean} enablePriorityFilter - Whether to enable priority filter
 */
function initializeMessageBoardDataTableWithPriorityFilter(isAdmin = false, enablePriorityFilter = true) {
    console.log('🚀 Starting Message Board DataTable initialization...');
    console.log('Priority filter enabled:', enablePriorityFilter);
    
    // Initialize the standard DataTable first
    const table = initializeMessageBoardDataTable(isAdmin);
    
    // Add priority filter back as requested
    if (enablePriorityFilter) {
        console.log('Adding priority filter for Message Board...');
        setTimeout(() => {
            console.log('🎯 Attempting to add header sort filters...');
            addHeaderSortFilters('#messagesTable', 2);
            console.log('✅ Priority filter setup completed');
        }, 200);
    }
    
    return table;
}

/**
 * Clear priority sort filter and reset to default
 */
function clearPrioritySortFilter() {
    // Clear global state
    currentPrioritySort = '';
    
    const priorityDropdown = document.querySelector('.priority-sort-filter');
    if (priorityDropdown) {
        priorityDropdown.value = '';
        priorityDropdown.style.backgroundColor = '';
        priorityDropdown.style.fontWeight = 'normal';
    }
    
    if (dataTable) {
        // Reset to default order by clearing all ordering
        dataTable.order([]).draw();
    }
}

// Export functions for global use
window.DataTablesConfig = {
    initializeDataTable,
    initializeWhatsNewDataTable,
    initializeMessageBoardDataTable,
    initializeWhatsNewDataTableWithPriorityFilter,
    initializeMessageBoardDataTableWithPriorityFilter,
    addHeaderSortFilters,
    createPrioritySortDropdown,
    applyPrioritySortFilter,
    updatePrioritySortDropdownState,
    clearPrioritySortFilter,
    setupLoadAllDataNotification,
    loadAllData,
    addCustomSearch,
    addColumnFilters,
    filterColumn,
    exportTableData,
    refreshTableData,
    getSelectedRows,
    clearAllFilters,
    setTableLoading
}; 