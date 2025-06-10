package com.refinery.portal.entity;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "vrp_whatsnew")
public class WhatsNew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "URL", length = 500)
    private String url;

    @NotNull(message = "Valid from date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "VALIDFROM", nullable = false)
    private LocalDate validFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "VALIDTO")
    private LocalDate validTo;

    @NotNull(message = "Enabled status is required")
    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = true;

    @Column(name = "PRIORITY")
    private Integer priority = 2; // Default to Normal priority (2)

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder = 1; // Sequential number within priority level

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "MODIFIED_DATE")
    private LocalDate modifiedDate;

    // Constructors
    public WhatsNew() {
        this.createdDate = LocalDate.now();
        this.modifiedDate = LocalDate.now();
    }

    public WhatsNew(String title, String description, LocalDate validFrom) {
        this();
        this.title = title;
        this.description = description;
        this.validFrom = validFrom;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDate.now();
    }

    @Override
    public String toString() {
        return "WhatsNew{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", validFrom=" + validFrom +
                ", enabled=" + enabled +
                '}';
    }
} 