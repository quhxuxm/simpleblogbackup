package com.quhxuxm.quh.project.simpleblog.domain.pojo;

import java.io.Serializable;
import java.util.Date;

public class AuthorFollower implements Serializable {
    private static final long serialVersionUID = -772343683934230934L;
    private Long authorId;
    private Long followerId;
    private Date followDate;

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Date getFollowDate() {
        return followDate;
    }

    public void setFollowDate(Date followDate) {
        this.followDate = followDate;
    }
}
