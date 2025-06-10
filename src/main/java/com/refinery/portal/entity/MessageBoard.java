package com.refinery.portal.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "vrp_scrollmsg")
public class MessageBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Message header is required")
    @Size(max = 200, message = "Header must be less than 200 characters")
    @Column(name = "MSG_HEADER", nullable = false, length = 200)
    private String header;

    @NotBlank(message = "Message content is required")
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    @Column(name = "MESSAGE", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "MESSAGE_HINDI", columnDefinition = "TEXT")
    private String messageHindi;

    @Column(name = "MSG_HEADER_HINDI", length = 200)
    private String headerHindi;

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

    @Column(name = "SPEED", length = 20)
    private String speed = "normal";

    @Column(name = "COLOR", length = 20)
    private String color = "#000000";

    @Column(name = "BGCOLOR", length = 20)
    private String backgroundColor = "#FFFFFF";

    @Column(name = "MSGBY", length = 50)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDate createdDate;

    @Column(name = "MODIFIED_DATE")
    private LocalDate modifiedDate;

    @Column(name = "DTSTAMP")
    private LocalDateTime dateTimeStamp;

    // Constructors
    public MessageBoard() {
        this.createdDate = LocalDate.now();
        this.modifiedDate = LocalDate.now();
        this.dateTimeStamp = LocalDateTime.now();
    }

    public MessageBoard(String header, String message, LocalDate validFrom) {
        this();
        this.header = header;
        this.message = message;
        this.validFrom = validFrom;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageHindi() {
        return messageHindi;
    }

    public void setMessageHindi(String messageHindi) {
        this.messageHindi = messageHindi;
    }

    public String getHeaderHindi() {
        return headerHindi;
    }

    public void setHeaderHindi(String headerHindi) {
        this.headerHindi = headerHindi;
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

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public LocalDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(LocalDateTime dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = LocalDate.now();
        this.dateTimeStamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "MessageBoard{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", validFrom=" + validFrom +
                ", enabled=" + enabled +
                ", priority=" + priority +
                '}';
    }
} 