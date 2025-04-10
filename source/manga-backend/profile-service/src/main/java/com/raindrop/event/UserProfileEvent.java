package com.raindrop.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileEvent {
    String userId;
    String email;
    String displayName;
    String avatarUrl;
}
