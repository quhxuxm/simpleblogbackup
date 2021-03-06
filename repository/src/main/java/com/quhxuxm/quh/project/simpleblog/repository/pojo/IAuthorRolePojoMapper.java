package com.quhxuxm.quh.project.simpleblog.repository.pojo;
import com.quhxuxm.quh.project.simpleblog.domain.pojo.AuthorRole;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthorRolePojoMapper {
    void create(AuthorRole authorRole);

    void update(AuthorRole authorRole);
}
