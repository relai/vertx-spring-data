package com.github.relai.vertx.springdata.example.rest.domain;

import java.util.Date;
import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

public class Contact {

	@Id String id;
    @Version Long version;
    @LastModifiedDate DateTime lastModified;
    @CreatedDate Date created;  
    
	String firstName;
	String lastName;

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public Long getVersion() {return version;}
    public void setVersion(Long version) {this.version = version;}

    public DateTime getLastModified() {return lastModified;}
    public void     setLastModified(DateTime lastModified) {this.lastModified = lastModified;}

    public Date getCreated() {return created;}
    public void setCreated(Date created) {this.created = created;}
    
    public String getFirstName() {return firstName;}
	public void   setFirstName(String firstName) {this.firstName = firstName;}

	public String getLastName() {return lastName;}
	public void   setLastName(String lastName) {this.lastName = lastName;}  
}
