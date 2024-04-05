package com.service.indianfrog.domain.user.valid;

import com.service.indianfrog.domain.user.valid.UserValidationGroup.NotBlankGroup;
import jakarta.validation.GroupSequence;

import static com.service.indianfrog.domain.user.valid.UserValidationGroup.*;

@GroupSequence({
        NotBlankGroup.class,
        EmailBlankGroup.class,
        EmailGroup.class,
        PasswordBlankGroup.class,
        PasswordPatternGroup.class,
        NicknameBlankGroup.class,
        NicknamePatternGroup.class,
})
public interface UserValidationSequence {
}
