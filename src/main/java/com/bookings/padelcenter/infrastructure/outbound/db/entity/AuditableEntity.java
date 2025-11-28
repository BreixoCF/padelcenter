package com.bookings.padelcenter.infrastructure.outbound.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<U> {

	@CreatedBy
	private U createdBy;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedBy
	private U modifiedBy;

	@LastModifiedDate
	private Instant modifiedAt;

	@Column(name = "deleted_by")
	private U deletedBy;

	@Column(name = "deleted_at")
	private Instant deletedAt;

}
