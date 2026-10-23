package dev.recallforge.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@Embeddable
public class JobProfileCategoryId implements Serializable {

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "category_id")
    private Long categoryId;

    public JobProfileCategoryId(
            Long profileId,
            Long categoryId) {

        this.profileId = profileId;
        this.categoryId = categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof JobProfileCategoryId that))
            return false;

        return Objects.equals(profileId, that.profileId)
            && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, categoryId);
    }
}