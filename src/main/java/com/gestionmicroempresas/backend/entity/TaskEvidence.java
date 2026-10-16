package com.gestionmicroempresas.backend.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "task_evidence")
public class TaskEvidence
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "task_id",
            nullable = false
    )
    private Task task;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "author_user_id",
            nullable = false
    )
    private User author;


    @Column(
            name = "file_name",
            nullable = false,
            length = 255
    )
    private String fileName;


    @Column(
            name = "original_name",
            nullable = false,
            length = 255
    )
    private String originalName;


    @Column(
            name = "mime_type",
            nullable = false,
            length = 100
    )
    private String mimeType;


    @Column(
            name = "size_bytes",
            nullable = false
    )
    private long sizeBytes;


    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    protected TaskEvidence() {
    }


    public TaskEvidence(
            Task task,
            User author,
            String fileName,
            String originalName,
            String mimeType,
            long sizeBytes
    ) {

        super(
                task.getTenant()
        );

        this.task = task;
        this.author = author;
        this.fileName = fileName;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }


    @PrePersist
    protected void beforeCreate() {

        createdAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }


    public Long getId() {
        return id;
    }


    public Task getTask() {
        return task;
    }


    public User getAuthor() {
        return author;
    }


    public String getFileName() {
        return fileName;
    }


    public String getOriginalName() {
        return originalName;
    }


    public String getMimeType() {
        return mimeType;
    }


    public long getSizeBytes() {
        return sizeBytes;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}