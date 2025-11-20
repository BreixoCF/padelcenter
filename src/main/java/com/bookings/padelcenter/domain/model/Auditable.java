package com.bookings.padelcenter.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<U> {

	@CreatedBy
	protected U createdBy;

	@CreatedDate
	protected Instant createdAt;

	@LastModifiedBy
	protected U modifiedBy;

	@LastModifiedDate
	protected Instant modifiedAt;

	@Column(name = "deleted_by")
	protected U deletedBy;

	@Column(name = "deleted_at")
	protected Instant deletedAt;

}
