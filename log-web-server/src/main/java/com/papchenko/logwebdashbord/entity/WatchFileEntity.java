package com.papchenko.logwebdashbord.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.papchenko.logwebdashbord.service.Severity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "WATCH_FILES")
public class WatchFileEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "KEY", nullable = false)
	private String key;

	@Column(name = "NAME")
	private String name;

	@Column(name = "path", nullable = false)
	private String path;

	@Column(name = "status")
	private Severity severity;

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
 	private LogSourceEntity logSourceEntity;
}
