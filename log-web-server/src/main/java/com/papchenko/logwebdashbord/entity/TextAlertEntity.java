package com.papchenko.logwebdashbord.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import com.papchenko.logwebdashbord.service.Severity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TextAlertEntity {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column(name = "TEXT", nullable = false)
	private String text;

	@Column(name = "SEVERITY")
	private Severity severity;

	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private LogSourceEntity logSourceEntity;
}
