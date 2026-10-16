package com.gestionmicroempresas.backend.entity;

import com.gestionmicroempresas.backend.enums.TaskPriority;
import com.gestionmicroempresas.backend.enums.TaskStatus;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
public class Task
        extends TenantOwnedEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;


    @Column(
            name = "description",
            length = 1000
    )
    private String description;


    @Column(
            name = "due_at"
    )
    private OffsetDateTime dueAt;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "priority",
            nullable = false,
            length = 40
    )
    private TaskPriority priority;


    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private TaskStatus status;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "creator_user_id",
            nullable = false
    )
    private User creator;


    @ManyToMany(
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "task_assignees",
            joinColumns =
            @JoinColumn(
                    name = "task_id"
            ),
            inverseJoinColumns =
            @JoinColumn(
                    name = "user_id"
            )
    )
    private Set<User> assignees =
            new LinkedHashSet<>();


    @Column(
            name = "feedback",
            length = 1000
    )
    private String feedback;


    @Column(
            name = "reviewed_at"
    )
    private OffsetDateTime reviewedAt;


    @Column(
            name = "completed_at"
    )
    private OffsetDateTime completedAt;


    @Column(
            name = "created_at",
            nullable = false
    )
    private OffsetDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;


    protected Task() {
    }


    public Task(
            String title,
            String description,
            OffsetDateTime dueAt,
            TaskPriority priority,
            User creator
    ) {

        super(
                creator.getTenant()
        );

        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.priority = priority;
        this.creator = creator;

        this.status =
                TaskStatus.PENDING;
    }

    @PrePersist
    protected void beforeCreate() {

        normalize();

        OffsetDateTime now =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );

        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void beforeUpdate() {

        normalize();

        updatedAt =
                OffsetDateTime.now(
                        ZoneOffset.UTC
                );
    }


    private void normalize() {

        if (title != null) {
            title = title.trim();
        }


        if (description != null) {

            description =
                    description.trim();

            if (description.isBlank()) {
                description = null;
            }
        }


        if (feedback != null) {

            feedback =
                    feedback.trim();

            if (feedback.isBlank()) {
                feedback = null;
            }
        }
    }


    public void replaceAssignees(
            Collection<User> users
    ) {

        assignees.clear();

        if (users != null) {
            assignees.addAll(users);
        }
    }


    public Long getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(
            String title
    ) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(
            String description
    ) {
        this.description = description;
    }


    public OffsetDateTime getDueAt() {
        return dueAt;
    }


    public void setDueAt(
            OffsetDateTime dueAt
    ) {
        this.dueAt = dueAt;
    }


    public TaskPriority getPriority() {
        return priority;
    }


    public void setPriority(
            TaskPriority priority
    ) {
        this.priority = priority;
    }


    public TaskStatus getStatus() {
        return status;
    }


    public void setStatus(
            TaskStatus status
    ) {
        this.status = status;
    }


    public User getCreator() {
        return creator;
    }


    public Set<User> getAssignees() {
        return assignees;
    }


    public String getFeedback() {
        return feedback;
    }


    public void setFeedback(
            String feedback
    ) {
        this.feedback = feedback;
    }


    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }


    public void setReviewedAt(
            OffsetDateTime reviewedAt
    ) {
        this.reviewedAt = reviewedAt;
    }


    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }


    public void setCompletedAt(
            OffsetDateTime completedAt
    ) {
        this.completedAt = completedAt;
    }


    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}