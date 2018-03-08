package org.radarcns.management.domain.audit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.radarcns.management.config.audit.CustomRevisionListener;

@Entity
@RevisionEntity(CustomRevisionListener.class)
@Table(name="_revisions_info")
public class CustomRevisionEntity extends DefaultRevisionEntity {

	private String auditor;

	public String getAuditor() {
        return auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}
	
}