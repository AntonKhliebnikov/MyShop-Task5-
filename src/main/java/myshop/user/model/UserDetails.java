package myshop.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private Long userId;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
}
