package dev.recallforge.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@Table(name = "job_profile_categories")
public class JobProfileCategory {

    @EmbeddedId
    private JobProfileCategoryId id;

    public JobProfileCategory(
            Long profileId,
            Long categoryId) {

        this.id = new JobProfileCategoryId(
            profileId,
            categoryId
        );
    }
}